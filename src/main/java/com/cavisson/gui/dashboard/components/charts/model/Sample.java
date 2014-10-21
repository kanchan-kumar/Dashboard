/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cavisson.gui.dashboard.components.charts.model;

import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.ui.Component;

public interface Sample<T> {
    /**
     * Get a Component wrapped to a desired layout. Can be a composite of
     * multiple components. The Sampler doesn't handle wrapping itself.
     */
    Component getWrappedComponent();

    /**
     * Get the bean whose properties are manipulated trough propertyeditor.<br/>
     * <br/>
     * If returns null, property editor will not be enabled for the sample.
     */
    T getComponentUsedByPropertyEditor();

    /**
     * Get the same item of the Component used by property editor and possible
     * extra control components
     */
    Item getSampleItem();

    /** Get the property ids exluded in property editor. */
    List<?> getPropertyEditorExcludedList();

    /**
     * Get control Components/Fields for highlighted features for the property
     * editor.
     */
    List<Component> getExtraComponentsForPropertyEditor();

    /**
     * Invoked by Sampler when the sample is displayed.
     */
    void sampleOpened();

}