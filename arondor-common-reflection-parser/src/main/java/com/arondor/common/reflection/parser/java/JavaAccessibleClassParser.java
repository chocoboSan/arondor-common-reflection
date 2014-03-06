/*
 *  Copyright 2013, Arondor
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.arondor.common.reflection.parser.java;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.arondor.common.management.mbean.annotation.Description;
import com.arondor.common.management.mbean.annotation.Mandatory;
import com.arondor.common.reflection.api.parser.AccessibleClassParser;
import com.arondor.common.reflection.bean.java.AccessibleClassBean;
import com.arondor.common.reflection.bean.java.AccessibleConstructorBean;
import com.arondor.common.reflection.bean.java.AccessibleFieldBean;
import com.arondor.common.reflection.bean.java.AccessibleMethodBean;
import com.arondor.common.reflection.model.java.AccessibleClass;
import com.arondor.common.reflection.model.java.AccessibleConstructor;
import com.arondor.common.reflection.model.java.AccessibleField;
import com.arondor.common.reflection.model.java.AccessibleMethod;
import com.arondor.common.reflection.util.PrimitiveTypeUtil;

public class JavaAccessibleClassParser implements AccessibleClassParser
{
    private static final Logger LOG = Logger.getLogger(JavaAccessibleClassParser.class);

    /**
     * Expensive log for this class
     */
    private static final boolean DEBUG = LOG.isDebugEnabled();

    private boolean replaceDollarByPointForEmbeddedClasses = false;

    /**
     * Convert a getter method name to an attribute name, in Java naming
     * conventions
     * 
     * @param getterName
     *            the getField() (or setField()) method
     * @return the field name, with first character in lowercase
     */
    public String getterToAttribute(String getterName)
    {
        int offset = -1;
        if (getterName.startsWith("get") || getterName.startsWith("set"))
        {
            offset = 3;
        }
        else if (getterName.startsWith("is"))
        {
            offset = 2;
        }
        else
        {
            throw new IllegalArgumentException("Invalid call to getterToAttribute('" + getterName + "')");
        }
        if (getterName.length() == offset)
        {
            return "none";
        }
        String rName = getterName.substring(offset, offset + 1);
        String rgName = getterName.substring(offset + 1);
        return rName.toLowerCase() + rgName;
    }

    /**
     * Generate an attribute getter name
     * 
     * @param name
     *            the field name
     * @return the getter method name, getField()
     */
    public String attributeToGetter(String name)
    {
        return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Generate an attribute getter name
     * 
     * @param name
     *            the field name
     * @return the getter method name, getField()
     */
    public String booleanAttributeToGetter(String name)
    {
        return "is" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Generate an attribute setter name
     * 
     * @param name
     *            the field name
     * @return the setter method name, setField()
     */
    public String attributeToSetter(String name)
    {
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Check if a class is part of classes considered to be directly exposable
     * to JConsole
     * 
     * @param clazz
     *            the class to check
     * @return true if this class is considered exposable, false otherwise
     */
    public boolean isExposableType(Class<?> clazz, boolean includeNonPrimitive)
    {
        if (PrimitiveTypeUtil.isPrimitiveType(clazz))
        {
            return true;
        }
        if (clazz.getPackage() != null)
        {
            if (clazz.getPackage().getName().startsWith("javax.management"))
            {
                return false;
            }
        }
        return includeNonPrimitive;
    }

    /**
     * Check if an array of classes is exposable to JConsole
     * 
     * @param parameterTypes
     *            array of classes to check
     * @return true if all classes are exposable, false if at least one is not
     *         exposable
     */
    public boolean isExposableSignature(Class<?>[] parameterTypes, boolean includeNonPrimitive)
    {
        if (parameterTypes == null || parameterTypes.length == 0)
        {
            return true;
        }
        for (int i = 0; i < parameterTypes.length; i++)
        {
            if (!isExposableType(parameterTypes[i], includeNonPrimitive))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a class is a 'void' type
     * 
     * @param clazz
     *            the class to check
     * @return true if the class is void, true otherwise
     */
    public boolean isVoid(Class<?> clazz)
    {
        return (clazz.isPrimitive() && clazz.getName().equals("void"));
    }

    /**
     * Parse method signatures and put it in exposed attributes or exposed
     * methods
     * 
     * @param clazz
     *            the class being parsed
     * @param methods
     *            the array of methods to parse
     * @param exposedAttributes
     *            map of exposed attributes
     * @param exposedMethods
     *            list of exposed methods
     */
    private void parseExposedMethodAndAttributes(Class<?> clazz, Method[] methods,
            Map<String, AccessibleField> exposedAttributes, List<Method> exposedMethods, boolean includeNonPrimitive)
    {
        for (int mth = 0; mth < methods.length; mth++)
        {
            Method method = methods[mth];
            if (DEBUG)
            {
                LOG.debug("Method : " + method.getName() + ", return=" + method.getReturnType().getName()
                        + " (exposable=" + isExposableType(method.getReturnType(), includeNonPrimitive) + ")"
                        + ", args=" + method.getParameterTypes().length + ", declaringClass="
                        + method.getDeclaringClass().getName());
            }
            if (isIgnoredMethod(method))
            {
                continue;
            }
            parseExposedMethod(clazz, exposedAttributes, exposedMethods, includeNonPrimitive, method);
        }
    }

    private void parseExposedMethod(Class<?> clazz, Map<String, AccessibleField> exposedAttributes,
            List<Method> exposedMethods, boolean includeNonPrimitive, Method method)
    {
        if (isGetterMethod(includeNonPrimitive, method))
        {
            String prop = getterToAttribute(method.getName());
            Class<?> fieldClass = method.getReturnType();
            AccessibleFieldBean field = getBeanFromMethod(clazz, exposedAttributes, fieldClass, prop);
            field.setReadable();
            setFieldDeclaredInClass(field, method.getDeclaringClass());
            if (method.getName().startsWith("is"))
            {
                field.setIs(true);
            }
        }
        else if (isSetterMethod(includeNonPrimitive, method))
        {
            String prop = getterToAttribute(method.getName());
            Class<?> fieldType = method.getParameterTypes()[0];
            AccessibleFieldBean field = getBeanFromMethod(clazz, exposedAttributes, fieldType, prop);
            field.setWritable();
            setFieldDeclaredInClass(field, method.getDeclaringClass());
            if (!field.getClassName().equals(fieldType.getName()))
            {
                LOG.warn("Incompatible setter type at class " + clazz.getName() + ", getter said "
                        + field.getClassName() + ", setter said " + fieldType.getName());
                // LOG.warn("Overriding getter type to the setter type " +
                // fieldType.getName());
                field.setClassName(fieldType.getName());
            }
            if (method.getGenericParameterTypes() != null && method.getGenericParameterTypes().length == 1)
            {
                addGenericParameter(field, method.getGenericParameterTypes()[0]);
            }
        }
        else if ((isVoid(method.getReturnType()) || (isExposableType(method.getReturnType(), includeNonPrimitive)))
                && (isExposableSignature(method.getParameterTypes(), includeNonPrimitive)))
        {
            exposedMethods.add(method);
        }
        else
        {
            if (DEBUG)
            {
                LOG.debug("Skipping method :" + method.getName() + ", modifiers="
                        + Modifier.toString(method.getModifiers()));
            }
        }
    }

    private void setFieldDeclaredInClass(AccessibleFieldBean field, Class<?> declaredClass)
    {
        if (field.getDeclaredInClass() != null)
        {
            if (field.getDeclaredInClass().equals(declaredClass.getName()))
            {
                return;
            }
            else
            {
                LOG.warn("Field " + field.getName() + " already declared in class " + field.getDeclaredInClass()
                        + ", now said to be declared (or overridden) in " + declaredClass.getName());
                return;
            }
        }
        field.setDeclaredInClass(declaredClass.getName());
    }

    private void addGenericParameter(AccessibleFieldBean attributeInfo, Type type)
    {
        LOG.debug("Setting field " + attributeInfo.getName() + " : type=" + type);
        List<String> genericTypes = new ArrayList<String>();
        if (type instanceof ParameterizedType)
        {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            for (Type subType : parameterizedType.getActualTypeArguments())
            {
                LOG.debug("subType : " + subType);
                if (subType instanceof Class<?>)
                {
                    Class<?> subTypeClass = (Class<?>) subType;
                    genericTypes.add(subTypeClass.getName());
                }
                else
                {
                    return;
                }
            }
        }
        LOG.debug("Setting field " + attributeInfo.getName() + " : genericTypes=" + genericTypes);
        attributeInfo.setGenericParameterClassList(genericTypes);
    }

    private static final String[] IGNORED_METHODS = { "wait", "notifyAll", "notify", "finalize", "getClass", "equals",
            "toString", "hashCode" };

    private boolean isIgnoredMethod(Method method)
    {
        if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers()))
        {
            return true;
        }
        for (String mth : IGNORED_METHODS)
        {
            if (method.getName().equals(mth))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether this mehod is a setter method
     * 
     * @param includeNonPrimitive
     * @param method
     * @return
     */
    private boolean isSetterMethod(boolean includeNonPrimitive, Method method)
    {
        return method.getName().startsWith("set") && method.getParameterTypes().length == 1
                && (isExposableSignature(method.getParameterTypes(), includeNonPrimitive))
                && isVoid(method.getReturnType());
    }

    /**
     * Check whether this mehod is a getter method
     * 
     * @param includeNonPrimitive
     * @param method
     * @return
     */
    private boolean isGetterMethod(boolean includeNonPrimitive, Method method)
    {
        return (method.getName().startsWith("get") || method.getName().startsWith("is"))
                && (isExposableType(method.getReturnType(), includeNonPrimitive))
                && method.getParameterTypes().length == 0;
    }

    private AccessibleFieldBean getBeanFromMethod(Class<?> clazz, Map<String, AccessibleField> exposedAttributes,
            Class<?> propertyClass, String propertyName)
    {
        AccessibleFieldBean attributeInfo = (AccessibleFieldBean) exposedAttributes.get(propertyName);
        if (attributeInfo == null)
        {
            attributeInfo = new AccessibleFieldBean(propertyName, propertyName, propertyClass, true, false);
            exposedAttributes.put(propertyName, attributeInfo);
            if (DEBUG)
            {
                LOG.debug("New exposed attribute (R) : " + attributeInfo.getName());
            }
        }
        else
        {
            if (!propertyClass.getName().equals(attributeInfo.getClassName()))
            {
                LOG.warn("Diverging classes at setter for class " + clazz.getName() + ", property " + propertyName
                        + " : was " + attributeInfo.getClassName() + ", now is " + propertyClass.getName());
            }
        }
        return attributeInfo;
    }

    private String getClassDescription(Class<?> clazz)
    {
        Description descAnnotation = clazz.getAnnotation(Description.class);
        if (descAnnotation != null)
        {
            return descAnnotation.value();
        }
        return null;
    }

    private String getFieldDescription(Field field)
    {
        Description descAnnotation = field.getAnnotation(Description.class);
        if (descAnnotation != null)
        {
            return descAnnotation.value();
        }
        return null;
    }

    private boolean getFieldMandatory(Field field)
    {
        Mandatory mandatoryAnnotation = field.getAnnotation(Mandatory.class);
        if (mandatoryAnnotation != null)
        {
            return mandatoryAnnotation.isMandatory();
        }
        return false;
    }

    public AccessibleClass parseAccessibleClass(Class<?> clazz)
    {
        if (DEBUG)
        {
            LOG.debug("Parsing accessible class : " + clazz.getName());
        }
        Method[] methods = null;
        try
        {
            methods = clazz.getMethods();
        }
        catch (NoClassDefFoundError e)
        {
            LOG.warn("Could not get methods :", e);
            return null;
        }
        AccessibleClassBean accessClass = createBaseAccessibleClass(clazz);

        if (Modifier.isAbstract(clazz.getModifiers()))
        {
            accessClass.setAbstract(true);
        }

        setAccessibleClassInheritance(clazz, accessClass);
        setAccessibleClassConstructors(clazz, accessClass);

        Map<String, AccessibleField> exposedAttributes = new HashMap<String, AccessibleField>();
        List<Method> exposedMethods = new ArrayList<Method>();

        parseExposedMethodAndAttributes(clazz, methods, exposedAttributes, exposedMethods, true);

        setAccessibleFieldsDescriptions(accessClass, clazz, exposedAttributes);

        setAccessibleMethods(accessClass, exposedMethods);
        return accessClass;
    }

    private void setAccessibleMethods(AccessibleClassBean accessClass, List<Method> exposedMethods)
    {
        List<AccessibleMethod> accessibleMethods = new ArrayList<AccessibleMethod>();
        for (Method method : exposedMethods)
        {
            if (isIgnoredMethod(method))
            {
                continue;
            }
            AccessibleMethodBean accessibleMethod = new AccessibleMethodBean();
            accessibleMethod.setName(method.getName());
            accessibleMethods.add(accessibleMethod);
        }

        accessClass.setAccessibleMethods(accessibleMethods);
    }

    private void setAccessibleFieldsDescriptions(AccessibleClassBean accessibleClass, Class<?> clazz,
            Map<String, AccessibleField> exposedAttributes)
    {
        for (AccessibleField accessibleField : exposedAttributes.values())
        {
            /**
             * We make an ugly cast because we do not want to expose setters in
             * the AccessibleField interface.
             */
            ((AccessibleFieldBean) accessibleField).setClassName(normalizeClassName(accessibleField.getClassName()));

            for (Class<?> superclass = clazz; superclass != null; superclass = superclass.getSuperclass())
            {
                try
                {
                    Field field = superclass.getDeclaredField(accessibleField.getName());
                    ((AccessibleFieldBean) accessibleField).setDescription(getFieldDescription(field));
                    ((AccessibleFieldBean) accessibleField).setMandatory(getFieldMandatory(field));
                    break;
                }
                catch (SecurityException e)
                {
                    LOG.debug("Could not fetch field '" + accessibleField.getName() + "'");
                }
                catch (NoSuchFieldException e)
                {
                    LOG.debug("Could not fetch field '" + accessibleField.getName() + "' from class "
                            + superclass.getName());
                }
                catch (NoClassDefFoundError e)
                {
                    LOG.debug("Could not fetch field '" + accessibleField.getName() + "' from class "
                            + superclass.getName());
                }
            }
        }
        accessibleClass.setAccessibleFields(exposedAttributes);
    }

    private void setAccessibleClassConstructors(Class<?> clazz, AccessibleClassBean accessClass)
    {
        for (Constructor<?> constructor : clazz.getConstructors())
        {
            AccessibleConstructorBean mConstructor = new AccessibleConstructorBean();
            mConstructor.setArgumentTypes(new ArrayList<String>());
            for (Class<?> arg : constructor.getParameterTypes())
            {
                mConstructor.getArgumentTypes().add(normalizeClassName(arg.getName()));
            }
            accessClass.getConstructors().add(mConstructor);
        }
    }

    private void setAccessibleClassInheritance(Class<?> clazz, AccessibleClassBean accessClass)
    {
        for (Class<?> itf : clazz.getInterfaces())
        {
            accessClass.getInterfaces().add(normalizeClassName(itf.getName()));
        }

        for (Class<?> superClass = clazz; superClass != null; superClass = superClass.getSuperclass())
        {
            for (Class<?> itf : superClass.getInterfaces())
            {
                accessClass.getAllInterfaces().add(normalizeClassName(itf.getName()));
            }
        }
    }

    private AccessibleClassBean createBaseAccessibleClass(Class<?> clazz)
    {
        AccessibleClassBean accessClass = new AccessibleClassBean();
        accessClass.setAllInterfaces(new ArrayList<String>());
        accessClass.setInterfaces(new ArrayList<String>());
        accessClass.setConstructors(new ArrayList<AccessibleConstructor>());

        accessClass.getInterfaces().add(java.lang.Object.class.getName());
        accessClass.getAllInterfaces().add(java.lang.Object.class.getName());
        accessClass.setName(normalizeClassName(clazz.getName()));
        accessClass.setDescription(getClassDescription(clazz));

        if (clazz.getSuperclass() == null)
        {
            LOG.debug("No superclass for class : '" + clazz.getName() + "'");
        }
        else
        {
            accessClass.setSuperclass(clazz.getSuperclass().getName());
        }
        return accessClass;
    }

    public boolean isReplaceDollarByPointForEmbeddedClasses()
    {
        return replaceDollarByPointForEmbeddedClasses;
    }

    public void setReplaceDollarByPointForEmbeddedClasses(boolean replaceDollarByPointForEmbeddedClasses)
    {
        this.replaceDollarByPointForEmbeddedClasses = replaceDollarByPointForEmbeddedClasses;
    }

    private String normalizeClassName(final String className)
    {
        if (isReplaceDollarByPointForEmbeddedClasses())
        {
            return className.replace('$', '.');
        }
        else
        {
            return className;
        }
    }

    public boolean isPrimitiveType(String className)
    {
        return PrimitiveTypeUtil.isPrimitiveType(className);
    }
}
