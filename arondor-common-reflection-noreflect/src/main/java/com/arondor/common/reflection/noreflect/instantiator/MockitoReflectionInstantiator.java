package com.arondor.common.reflection.noreflect.instantiator;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;

import com.arondor.common.reflection.api.instantiator.InstantiationContext;
import com.arondor.common.reflection.api.instantiator.ReflectionInstantiator;
import com.arondor.common.reflection.api.parser.ObjectConfigurationMapParser;
import com.arondor.common.reflection.model.config.ObjectConfiguration;
import com.arondor.common.reflection.model.config.ObjectConfigurationMap;
import com.arondor.common.reflection.parser.spring.XMLBeanDefinitionParser;

/**
 * A {@link ReflectionInstantiator} which should be only used for test purposes.
 * This implementation is based on Mockito framework which should be declared as
 * project dependency. This reflection instanciator supports singleton
 * management
 * 
 * @see Mockito#mock(Class)
 * @author Christopher Laszczuk
 * 
 */
public class MockitoReflectionInstantiator implements ReflectionInstantiator
{
    private final Map<String, Object> singletonObjectConfigurationMap = new HashMap<String, Object>();

    private final ObjectConfigurationMap objectConfigurations;

    /**
     * This constructor allows to provide a specific XML configuration file
     * 
     * @param configurationLocation
     *            The context to use for reflection instantiation
     */
    public MockitoReflectionInstantiator(String configurationLocation)
    {
        ObjectConfigurationMapParser beanDefinitionParser = new XMLBeanDefinitionParser(configurationLocation);
        objectConfigurations = beanDefinitionParser.parse();
    }

    public InstantiationContext createDefaultInstantiationContext()
    {
        throw new RuntimeException("This method should not be used. This class is only for test purposes.");
    }

    public <T> T instanciateObject(ObjectConfiguration objectConfiguration, Class<T> desiredClass,
            InstantiationContext context)
    {
        throw new RuntimeException("This method should not be used. This class is only for test purposes.");
    }

    /**
     * This method uses the Mockito framework to generate objects. Those
     * generated objects can be singleton according to configuration context
     * 
     * @see Mockito#mock(Class)
     */
    public <T> T instanciateObject(String beanName, Class<T> desiredClass, InstantiationContext context)
    {
        ObjectConfiguration objectConfiguration = objectConfigurations.get(beanName);
        if (objectConfiguration.isSingleton())
        {
            @SuppressWarnings("unchecked")
            T singleton = (T) singletonObjectConfigurationMap.get(beanName);
            if (singleton != null)
            {
                return singleton;
            }
            else
            {
                singleton = mock(desiredClass);
                singletonObjectConfigurationMap.put(beanName, singleton);
                return singleton;
            }
        }
        return mock(desiredClass);
    }

}