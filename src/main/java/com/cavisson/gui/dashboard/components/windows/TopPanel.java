/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cavisson.gui.dashboard.components.windows;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import java.nio.file.Files;

@SuppressWarnings("serial")
public class TopPanel extends HorizontalLayout {

    public TopPanel() {
        setSizeFull();
               Embedded e = new Embedded("",
                new ThemeResource("images/logo.png"));
               e.setSizeUndefined();
               addComponent(e);
               setComponentAlignment(e, Alignment.TOP_CENTER);
               //e.setSizeFull();
        //e.setAlternateText("Document icon from the Runo theme");

        
    }
}