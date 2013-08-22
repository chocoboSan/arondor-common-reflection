package com.arondor.common.reflection.gwt.client;

import com.arondor.common.reflection.gwt.client.api.AccessibleClassPresenter;
import com.arondor.common.reflection.gwt.client.presenter.HierarchicAccessibleClassPresenter;
import com.arondor.common.reflection.gwt.client.service.GWTReflectionService;
import com.arondor.common.reflection.gwt.client.service.GWTReflectionServiceAsync;
import com.arondor.common.reflection.gwt.client.view.HierarchicAccessibleClassView;
import com.google.gwt.core.client.GWT;

public class AccessibleClassPresenterFactory
{
    private static final GWTReflectionServiceAsync ACCESSIBLE_CLASS_SERVICE = GWT.create(GWTReflectionService.class);

    public static AccessibleClassPresenter createAccessibleClassPresenter(String baseClassName)
    {
        return new HierarchicAccessibleClassPresenter(ACCESSIBLE_CLASS_SERVICE, baseClassName,
                new HierarchicAccessibleClassView());
    }
}
