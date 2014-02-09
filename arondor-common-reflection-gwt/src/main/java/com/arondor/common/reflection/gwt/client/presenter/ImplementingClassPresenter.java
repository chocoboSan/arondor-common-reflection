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
package com.arondor.common.reflection.gwt.client.presenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.arondor.common.reflection.gwt.client.service.GWTReflectionServiceAsync;
import com.arondor.common.reflection.gwt.client.view.MyValueChangeEvent;
import com.arondor.common.reflection.model.config.ObjectConfiguration;
import com.arondor.common.reflection.model.config.ObjectConfigurationMap;
import com.arondor.common.reflection.model.java.AccessibleClass;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class ImplementingClassPresenter
{
    private static final Logger LOG = Logger.getLogger(ImplementingClassPresenter.class.getName());

    public interface Display extends IsWidget
    {
        void setBaseClassName(String baseClassName);

        void setImplementingClasses(Collection<String> implementingClasses);

        void selectImplementingClass(String implementingClassName);

        HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> valueChangeHandler);
    }

    private final String baseClassName;

    private ImplementingClass currentImplementingClass = ImplementingClass.NULL_CLASS;

    private final Display display;

    private final GWTReflectionServiceAsync rpcService;

    private final List<ImplementingClass> implementingClasses = new ArrayList<ImplementingClass>();

    public ImplementingClassPresenter(GWTReflectionServiceAsync rpcService,
            ObjectConfigurationMap objectConfigurationMap, String baseClassName, Display display)
    {
        this.baseClassName = baseClassName;
        this.display = display;
        this.rpcService = rpcService;
        this.display.setBaseClassName(baseClassName);
        bind();

        addIImplementingClass(ImplementingClass.NULL_CLASS);

        fetchBaseClass();
        fetchImplementations();
        if (objectConfigurationMap != null)
        {
            fetchObjectConfigurations(objectConfigurationMap);
        }
    }

    private void fetchObjectConfigurations(ObjectConfigurationMap objectConfigurationMap)
    {
        for (Map.Entry<String, ObjectConfiguration> entry : objectConfigurationMap.entrySet())
        {
            final String referenceName = entry.getKey();
            final String referenceClassName = entry.getValue().getClassName();
            final ImplementingClass implementingClass = new ImplementingClass(true, referenceName);

            if (referenceClassName.equals(baseClassName))
            {
                addIImplementingClass(implementingClass);
            }
            else
            {

                rpcService.getAccessibleClass(referenceClassName, new AsyncCallback<AccessibleClass>()
                {
                    public void onFailure(Throwable caught)
                    {
                    }

                    public void onSuccess(AccessibleClass result)
                    {
                        for (String interfaceName : result.getAllInterfaces())
                        {
                            if (interfaceName.equals(baseClassName))
                            {
                                addIImplementingClass(implementingClass);
                            }
                        }
                    }
                });
            }
        }
    }

    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<ImplementingClass> valueChangeHandler)
    {
        return this.display.addValueChangeHandler(new ValueChangeHandler<String>()
        {
            public void onValueChange(ValueChangeEvent<String> event)
            {
                ImplementingClass implementingClass = ImplementingClass.parseImplementingClass(event.getValue());
                valueChangeHandler.onValueChange(new MyValueChangeEvent<ImplementingClass>(implementingClass));
            }
        });
    }

    private void bind()
    {
        display.addValueChangeHandler(new ValueChangeHandler<String>()
        {
            public void onValueChange(ValueChangeEvent<String> event)
            {
                currentImplementingClass = ImplementingClass.parseImplementingClass(event.getValue());
                LOG.finest("Changed implementClassName=" + currentImplementingClass);
            }
        });
    }

    private void addIImplementingClass(ImplementingClass implementingClass)
    {
        if (implementingClasses.contains(implementingClass))
        {
            return;
        }
        implementingClasses.add(implementingClass);

        updateDisplay();
    }

    private boolean updateDisplayScheduled = false;

    private void updateDisplay()
    {
        if (updateDisplayScheduled)
        {
            return;
        }
        updateDisplayScheduled = true;

        Collections.sort(implementingClasses);

        if (GWT.isClient())
        {
            Scheduler.get().scheduleFixedDelay(new RepeatingCommand()
            {

                public boolean execute()
                {
                    doUpdateDisplay();
                    return false;
                }
            }, 2000);
        }
        else
        {
            doUpdateDisplay();
        }
    }

    private void doUpdateDisplay()
    {
        List<String> names = new ArrayList<String>();
        for (ImplementingClass implementingClass : implementingClasses)
        {
            names.add(implementingClass.toString());
        }
        display.setImplementingClasses(names);

        LOG.finest("currentImplementingClass=" + currentImplementingClass);

        if (implementingClasses.contains(currentImplementingClass))
        {
            display.selectImplementingClass(currentImplementingClass.toString());
        }

        updateDisplayScheduled = false;
    }

    private void fetchBaseClass()
    {
        rpcService.getAccessibleClass(baseClassName, new AsyncCallback<AccessibleClass>()
        {
            public void onFailure(Throwable caught)
            {
            }

            public void onSuccess(AccessibleClass result)
            {
                if (isInstantiatable(result))
                {
                    addIImplementingClass(new ImplementingClass(false, result.getName()));
                }
            }
        });
    }

    protected boolean isInstantiatable(AccessibleClass result)
    {
        return result.getSuperclass() != null;
    }

    private void fetchImplementations()
    {
        rpcService.getImplementingAccessibleClasses(baseClassName, new AsyncCallback<Collection<AccessibleClass>>()
        {
            public void onSuccess(Collection<AccessibleClass> result)
            {
                for (AccessibleClass classes : result)
                {
                    addIImplementingClass(new ImplementingClass(false, classes.getName()));
                }
            }

            public void onFailure(Throwable caught)
            {
            }
        });
    }

    public String getBaseClassName()
    {
        return baseClassName;
    }

    public void setImplementingClass(ImplementingClass implementingClass)
    {
        this.currentImplementingClass = implementingClass;
        display.selectImplementingClass(currentImplementingClass.toString());
    }

    public ImplementingClass getImplementingClass()
    {
        return currentImplementingClass;
    }
}
