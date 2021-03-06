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
package com.arondor.common.reflection.gwt.client.presenter.fields;

import java.util.ArrayList;
import java.util.List;

import com.arondor.common.reflection.gwt.client.presenter.TreeNodePresenter;
import com.arondor.common.reflection.model.config.ElementConfiguration;
import com.arondor.common.reflection.model.config.ListConfiguration;
import com.arondor.common.reflection.model.config.ObjectConfigurationFactory;
import com.arondor.common.reflection.model.config.PrimitiveConfiguration;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

public class StringListTreeNodePresenter implements TreeNodePresenter
{
    private final String fieldName;

    public interface StringListDisplay extends ValueDisplay<List<String>>
    {
    }

    private final StringListDisplay primitiveDisplay;

    private List<String> values;

    public StringListTreeNodePresenter(String fieldName, StringListDisplay primitiveDisplay)
    {
        this.fieldName = fieldName;
        this.primitiveDisplay = primitiveDisplay;
        bind();
    }

    private void bind()
    {
        primitiveDisplay.addValueChangeHandler(new ValueChangeHandler<List<String>>()
        {
            public void onValueChange(ValueChangeEvent<List<String>> event)
            {
                values = event.getValue();
            }
        });

    }

    public String getFieldName()
    {
        return fieldName;
    }

    public ElementConfiguration getElementConfiguration(ObjectConfigurationFactory objectConfigurationFactory)
    {
        if (values != null)
        {
            ListConfiguration listConfiguration = objectConfigurationFactory.createListConfiguration();
            listConfiguration.setListConfiguration(new ArrayList<ElementConfiguration>());
            for (String value : values)
            {
                listConfiguration.getListConfiguration().add(
                        objectConfigurationFactory.createPrimitiveConfiguration(value));
            }
            return listConfiguration;
        }
        return null;
    }

    public void setElementConfiguration(ElementConfiguration elementConfiguration)
    {
        if (elementConfiguration instanceof ListConfiguration)
        {
            ListConfiguration listConfiguration = (ListConfiguration) elementConfiguration;
            List<String> stringList = new ArrayList<String>();
            for (ElementConfiguration childConfiguration : listConfiguration.getListConfiguration())
            {
                if (childConfiguration instanceof PrimitiveConfiguration)
                {
                    PrimitiveConfiguration primitiveConfiguration = (PrimitiveConfiguration) childConfiguration;
                    stringList.add(primitiveConfiguration.getValue());
                }
            }
            primitiveDisplay.setValue(stringList);
            this.values = stringList;
        }
    }

    public Display getDisplay()
    {
        return primitiveDisplay;
    }

}
