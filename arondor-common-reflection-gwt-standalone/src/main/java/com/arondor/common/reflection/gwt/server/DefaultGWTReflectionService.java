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
package com.arondor.common.reflection.gwt.server;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.arondor.common.reflection.api.parser.AccessibleClassProvider;
import com.arondor.common.reflection.api.service.ReflectionService;
import com.arondor.common.reflection.service.DefaultReflectionService;
import com.arondor.common.reflection.service.ReflectionServiceFactory;

public class DefaultGWTReflectionService extends GWTReflectionServiceStub
{
    /**
     * 
     */
    private static final long serialVersionUID = -5917820772085494747L;

    public DefaultGWTReflectionService()
    {
        DefaultReflectionService defaultReflectionService = (DefaultReflectionService) getReflectionService();

        BeanFactory beanFactory = new ClassPathXmlApplicationContext("reflection-config.xml");

        AccessibleClassProvider accessibleClassProvider = beanFactory.getBean("accessibleClassProvider",
                AccessibleClassProvider.class);

        accessibleClassProvider.provideClasses(defaultReflectionService.getAccessibleClassCatalog());
    }

    @Override
    protected ReflectionService getReflectionService()
    {
        return ReflectionServiceFactory.getInstance().getReflectionService();
    }
}
