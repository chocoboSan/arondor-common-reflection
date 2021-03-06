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
import java.util.List;
import java.util.Map;

import com.arondor.common.reflection.gwt.client.api.ObjectConfigurationMapPresenter;
import com.arondor.common.reflection.gwt.client.presenter.fields.KeyValuePresenterPair;
import com.arondor.common.reflection.gwt.client.presenter.fields.MapTreeNodePresenter;
import com.arondor.common.reflection.gwt.client.service.GWTReflectionServiceAsync;
import com.arondor.common.reflection.model.config.ElementConfiguration;
import com.arondor.common.reflection.model.config.ObjectConfiguration;
import com.arondor.common.reflection.model.config.ObjectConfigurationFactory;
import com.arondor.common.reflection.model.config.ObjectConfigurationMap;
import com.arondor.common.reflection.model.config.PrimitiveConfiguration;
import com.google.gwt.user.client.ui.IsWidget;

public class SimpleObjectConfigurationMapPresenter extends MapTreeNodePresenter implements
        ObjectConfigurationMapPresenter
{

    private static final List<String> GENERIC_TYPES = new ArrayList<String>();

    static
    {
        GENERIC_TYPES.add("java.lang.String");
        GENERIC_TYPES.add("java.lang.Object");
    }

    public interface ObjectConfigurationMapDisplay extends MapRootDisplay
    {
        IsWidget getDisplayWidget();
    }

    public SimpleObjectConfigurationMapPresenter(GWTReflectionServiceAsync rpcService, String fieldName,
            ObjectConfigurationMapDisplay mapDisplay)
    {
        super(rpcService, null, fieldName, GENERIC_TYPES, mapDisplay);
        mapDisplay.setNodeName("Shared Objects");
    }

    /**
     * For the sake of robustness, we have our very own bean model for the fake
     * PrimitiveConfiguration used for the Key mapping
     * 
     */
    private static class MyPrimitiveConfiguration implements PrimitiveConfiguration
    {
        /**
         * 
         */
        private static final long serialVersionUID = 9147786493404617175L;

        private String value;

        public MyPrimitiveConfiguration(String value)
        {
            this.value = value;
        }

        public ElementConfigurationType getFieldConfigurationType()
        {
            return ElementConfigurationType.Primitive;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }

    }

    public void setObjectConfigurationMap(ObjectConfigurationMap objectConfigurationMap)
    {
        if (objectConfigurationMap == null)
        {
            return;
        }
        super.setObjectConfigurationMap(objectConfigurationMap);
        for (Map.Entry<String, ObjectConfiguration> entry : objectConfigurationMap.entrySet())
        {
            addChild(new MyPrimitiveConfiguration(entry.getKey()), entry.getValue());
        }
    }

    public ObjectConfigurationMap getObjectConfigurationMap(ObjectConfigurationFactory objectConfigurationFactory)
    {
        ObjectConfigurationMap objectConfigurationMap = objectConfigurationFactory.createObjectConfigurationMap();
        for (KeyValuePresenterPair presenter : getKeyValuePresenters())
        {
            ElementConfiguration keyElementConfiguration = presenter.getKeyPresenter().getElementConfiguration(
                    objectConfigurationFactory);
            if (!(keyElementConfiguration instanceof PrimitiveConfiguration))
            {
                continue;
            }
            PrimitiveConfiguration primitiveKey = (PrimitiveConfiguration) keyElementConfiguration;
            String keyString = primitiveKey.getValue();
            ElementConfiguration valueConfiguration = presenter.getValuePresenter().getElementConfiguration(
                    objectConfigurationFactory);
            if (!(valueConfiguration instanceof ObjectConfiguration))
            {
                continue;
            }
            ObjectConfiguration objectConfiguration = (ObjectConfiguration) valueConfiguration;
            objectConfigurationMap.put(keyString, objectConfiguration);
        }
        return objectConfigurationMap;
    }

    public IsWidget getDisplayWidget()
    {
        return ((ObjectConfigurationMapDisplay) getDisplay()).getDisplayWidget();
    }

}
