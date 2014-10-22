/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cavisson.gui.dashboard.components.charts.model;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author kanchan
 */
public class CreateTree extends VerticalLayout implements Action.Handler
{

    private static final Action ACTION_ADD = new Action("Add child item");
    private static final Action ACTION_DELETE = new Action("Delete");
    private static final Action[] ACTIONS = new Action[]
    {
        ACTION_ADD, ACTION_DELETE
    };

    private Tree tree;
    private Button deleteButton;

    public CreateTree()
    {
        setSpacing(true);
        tree = new Tree("Hardware Inventory", new HierarchicalContainer());
        tree.setMultiSelect(true);
        tree.setImmediate(true);
        tree.addListener(new ValueChangeListener()
        {
            public void valueChange(ValueChangeEvent event)
            {
                Tree t = (Tree) event.getProperty();
                deleteButton.setEnabled(t.getValue() != null
                  && ((Set<?>) t.getValue()).size() > 0);
            }
        });

        // Add Actionhandler
        tree.addActionHandler(this);

        // Set tree to show the 'name' property as caption for items
        tree.setItemCaptionPropertyId("");
        tree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

        // Expand whole tree
        for (Iterator<?> it = tree.rootItemIds().iterator(); it.hasNext();)
        {
            tree.expandItemsRecursively(it.next());
        }

        // Create the 'delete button', inline click-listener
        deleteButton = new Button("Delete", new Button.ClickListener()
        {
            @SuppressWarnings("unchecked")
            public void buttonClick(ClickEvent event)
            {
                // Delete all the selected objects
                Object[] toDelete = ((Set<Object>) tree.getValue()).toArray();
                for (int i = 0; i < toDelete.length; i++)
                {
                    handleAction(ACTION_DELETE, tree, toDelete[i]);
                }
            }
        });
        deleteButton.setEnabled(false);

        addComponent(deleteButton);
        addComponent(tree);

    }

    /*
     * Returns the set of available actions
     */
    public Action[] getActions(Object target, Object sender)
    {
        return ACTIONS;
    }

    /*
     * Handle actions
     */
    public void handleAction(Action action, Object sender, Object target)
    {
        if (action == ACTION_ADD)
        {
            // Allow children for the target item
            tree.setChildrenAllowed(target, true);

            // Create new item, disallow children, add name, set parent
            Object itemId = tree.addItem();
            tree.setChildrenAllowed(itemId, false);
            String newItemName = "New Item # " + itemId;
            Item item = tree.getItem(itemId);
            item.getItemProperty("").setValue(
              newItemName);
            tree.setParent(itemId, target);
            tree.expandItem(target);
        }
        else if (action == ACTION_DELETE)
        {
            Object parent = tree.getParent(target);
            tree.removeItem(target);
            // If the deleted object's parent has no more children, set its
            // childrenallowed property to false
            if (parent != null && tree.getChildren(parent).size() == 0)
            {
                tree.setChildrenAllowed(parent, false);
            }
        }
    }
}
