package com.cavisson.gui.dashboard.components.controls;

import com.vaadin.data.Container;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.VerticalLayout;

public class Trees extends VerticalLayout implements View
{

  public Trees()
  {
    //setMargin(true);

    /*Label h1 = new Label("Trees");
    h1.addStyleName("h1");
    addComponent(h1);*/

    HorizontalLayout row = new HorizontalLayout();
    //row.addStyleName("wrapping");
    row.setSizeFull();
    addComponent(row);

    Tree tree = new Tree();
    tree.setSelectable(true);
    Container generateContainer = ValoThemeUI.generateContainer(10, true);
    tree.setContainerDataSource(generateContainer);
    tree.setDragMode(TreeDragMode.NODE);
    row.addComponent(tree);
    tree.setItemCaptionPropertyId(ValoThemeUI.CAPTION_PROPERTY);
    tree.setItemIconPropertyId(ValoThemeUI.ICON_PROPERTY);
    tree.expandItem(generateContainer.getItemIds().iterator().next());
    tree.setMultiSelect(true);
    tree.setImmediate(true);
   
    tree.setDropHandler(new DropHandler()
    {
      @Override
      public AcceptCriterion getAcceptCriterion()
      {
        return AcceptAll.get();
      }

      @Override
      public void drop(DragAndDropEvent event)
      {
        Notification.show(event.getTransferable().toString());
      }
    });

    // Add actions (context menu)
    tree.addActionHandler(ValoThemeUI.getActionHandler());
  }

  @Override
  public void enter(ViewChangeEvent event)
  {
    // TODO Auto-generated method stub

  }

}
