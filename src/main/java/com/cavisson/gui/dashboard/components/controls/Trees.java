package com.cavisson.gui.dashboard.components.controls;

import com.cavisson.gui.dashboard.components.charts.model.GenerateTreeContainer;
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
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.VerticalLayout;
import java.io.IOException;

public class Trees extends VerticalLayout implements View
{
  Container generateContainer = null;
  Tree tree = null;
  public Trees() throws IOException
  {
    //setMargin(true);

    /*Label h1 = new Label("Trees");
    h1.addStyleName("h1");
    addComponent(h1);*/

    HorizontalLayout row = new HorizontalLayout();
    //row.addStyleName("wrapping");
    row.setSizeFull();
    addComponent(row);

    tree = new Tree();
    tree.setSelectable(true);
    generateContainer = GenerateTreeContainer.generateTreeContainer(10, true);
    tree.setContainerDataSource(generateContainer);
    tree.setDragMode(TreeDragMode.NODE);
    
    row.addComponent(tree);
    tree.setItemCaptionPropertyId(GenerateTreeContainer.CAPTION_PROPERTY);
    tree.setItemIconPropertyId(GenerateTreeContainer.ICON_PROPERTY);
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
    
    tree.addActionHandler(GenerateTreeContainer.getActionHandler());
  }
  
  @Override
  public void enter(ViewChangeEvent event)
  {
    // TODO Auto-generated method stub
  }  
}
