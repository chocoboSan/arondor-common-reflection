package com.arondor.common.reflection.gwt.client.view;

import com.arondor.common.reflection.gwt.client.presenter.ClassTreeNodePresenter;
import com.arondor.common.reflection.gwt.client.presenter.ClassTreeNodePresenter.Display;
import com.arondor.common.reflection.gwt.client.presenter.ClassTreePresenter;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class ClassTreeView extends Composite implements ClassTreePresenter.Display
{
    private Tree tree = new Tree();

    public ClassTreeView()
    {
        AbsolutePanel content = new AbsolutePanel();
        initWidget(content);

        content.add(new Label("Tree"));

        content.add(tree);
    }

    public Tree getTree()
    {
        return tree;
    }

    public ClassTreeNodePresenter.Display createRootView()
    {
        return new ClassTreeNodeView(getTree());
    }

    public Widget asWidget()
    {
        return this;
    }

    private static class DisplaySelectionEvent extends SelectionEvent<Display>
    {
        protected DisplaySelectionEvent(Display selectedItem)
        {
            super(selectedItem);
        }
    }

    public HandlerRegistration addSelectionHandler(final SelectionHandler<Display> selectionHandler)
    {
        return tree.addSelectionHandler(new SelectionHandler<TreeItem>()
        {
            public void onSelection(SelectionEvent<TreeItem> event)
            {
                selectionHandler.onSelection(new DisplaySelectionEvent((Display) event.getSelectedItem()));
            }
        });
    }
}
