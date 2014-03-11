package com.arondor.common.reflection.parser.spring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jdom.Namespace;

public final class XMLBeanTagsConstant
{

    /**
     * List of tags use to object configuration parsing
     */
    public static final String ALL_BEANS_TAG = "beans";

    public static final String BEAN_TAG = "bean";

    public static final String BEAN_ID_TAG = "id";

    public static final String BEAN_CLASS_TAG = "class";
    
    public static final String BEAN_SCOPE_TAG = "scope";
    
    public static final String SINGLETON = "singleton";

    public static final String PROTOTYPE = "prototype";

    public static final String CONSTRUCTOR_ARG_TAG = "constructor-arg";

    public static final String PROPERTY_TAG = "property";

    public static final String PROPERTY_NAME_TAG = "name";

    public static final String PROPERTY_VALUE_TAG = "value";
    
    public static final String LIST_TAG = "list";
    
    public static final String MAP_TAG = "map";

    public static final String ENTRY_TAG = "entry";

    public static final String KEY_TAG = "key";
    
    public static final String REF_TAG = "ref";
    
    /**
     * Default namespace
     */
    public static final Namespace NAMESPACE = Namespace.getNamespace("http://www.springframework.org/schema/beans");
    
    public static final Namespace NAMESPACE_XSI = Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
    
    public static final Namespace NAMESPACE_SCHEME_LOCATION = Namespace.getNamespace("schemeLocation","http://www.springframework.org/schema/beans"
          + " http://www.springframework.org/schema/beans/spring-beans-3.0.xsd "
          + " http://www.springframework.org/schema/context "
          + " http://www.springframework.org/schema/context/spring-context-2.5.xsd");
    
    public static final Namespace NAMESPACE_CONTEXT = Namespace.getNamespace("context","http://www.springframework.org/schema/context");
    
    /**
     * All header attributes to set (ie : default-lazy-init)
     */
    public static final Map<String, String> HEADER_ATTRIBUTE_MAP;







    private XMLBeanTagsConstant()
    {

    }

    static
    {
        Map<String, String> headerMap = new HashMap<String, String>();
//        headerMap.put("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
//        headerMap.put("context", "http://www.springframework.org/schema/context");
//        headerMap.put("schemaLocation", "http://www.springframework.org/schema/beans"
//                + " http://www.springframework.org/schema/beans/spring-beans-3.0.xsd "
//                + " http://www.springframework.org/schema/context "
//                + " http://www.springframework.org/schema/context/spring-context-2.5.xsd");
        headerMap.put("default-lazy-init", "true");
        headerMap.put("default-autowire", "no");
        HEADER_ATTRIBUTE_MAP = Collections.unmodifiableMap(headerMap);
    }
}