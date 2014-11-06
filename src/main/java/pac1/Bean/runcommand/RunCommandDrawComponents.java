/**----------------------------------------------------------------------------
 * Name:       RunCommandUI.java
 * Purpose:    To Run Commands on Server
 * @author:    B.Bala Sudheer
 * Modification History:
 * 
 *---------------------------------------------------------------------------**/

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.NumberFormatter;

import pac1.Bean.Log;
import pac1.Bean.NSColor;


public class RunCommandDrawComponents 
{
  String className = "RunCommandParser";
  Color leftPanelColor = NSColor.leftPanelcolor();
  
  /**
   * creates checkbox UI
   * @param label
   * @param value2
   * @param backGroundColor
   * @return
   */
  public JCheckBox createCheckBoxUI(String label, String value2 , Color backGroundColor) 
  {
    Log.debugLog(className, "createCheckBoxUI", "", "", "Method called.");

    JCheckBox jck = new JCheckBox(label);
    jck.setBackground(backGroundColor);
    return jck;  
  }
  
  public JTextField createTextBoxNumericUI(String defaultValue , String minValue , String maxValue) 
  {
    Log.debugLog(className, "createCheckBoxUI", "", "", "Method called.");
    int minIntValue, maxIntValue;
    
    NumberFormatter nf = new NumberFormatter();
 
    if(minValue != null && maxValue != null)
    {
      try
      {
        minIntValue = Integer.parseInt(minValue.trim());             
      }
      catch (NumberFormatException nfe) 
      {
        minIntValue = Integer.MIN_VALUE;
      }
      
      try
      {
        maxIntValue = Integer.parseInt(maxValue.trim());             
      }
      catch (NumberFormatException nfe) 
      {
        maxIntValue = Integer.MAX_VALUE;
      }
      
      nf.setMaximum(maxIntValue);
      nf.setMinimum(minIntValue);
    }
    
    JFormattedTextField fmtTxtFld = new JFormattedTextField(nf);   
    fmtTxtFld.setPreferredSize(new Dimension(100,22));
    fmtTxtFld.setText(defaultValue);
    return fmtTxtFld;  
    
  }
  
  
  public JSpinner createSpinnerUI(String defaultValue , String minValue , String maxValue) 
  {
    Log.debugLog(className, "createSpinnerUI", "", "", "Method called.");
    int minIntValue = 0, maxIntValue = 0;
 
    if(minValue != null && maxValue != null)
    {
      try
      {
        minIntValue = Integer.parseInt(minValue.trim());             
      }
      catch (NumberFormatException nfe) 
      {
        minIntValue = Integer.MIN_VALUE;
      }
      
      try
      {
        maxIntValue = Integer.parseInt(maxValue.trim());             
      }
      catch (NumberFormatException nfe) 
      {
        maxIntValue = Integer.MAX_VALUE;
      }
    }
    
    SpinnerNumberModel snm = new SpinnerNumberModel(Integer.parseInt(defaultValue), minIntValue, maxIntValue, 1);
    
    JSpinner spinner = new JSpinner(snm);   
    
    return spinner;  
    
  }
  
  /**
   * creates a TextField
   * @param defaultValue
   * @return created text field 
   */
  public JTextField createTextBoxUI(String defaultValue) 
  {
    Log.debugLog(className, "createCheckBoxUI", "", "", "Method called.");
   
    try
    {
      JTextField txtField = null;
      
      if(defaultValue != null && defaultValue.equals("NA"))
        txtField = new JTextField(defaultValue);
      else
        txtField = new JTextField();
      
      txtField.setPreferredSize(new Dimension(100,22));
      return txtField;
    }
    catch (Exception e) 
    {
      Log.errorLogClient(className, "createTextBoxUI", "", "", "Exception in creating text Box" );
      return null;
    }
  }
  
  
  /**
   * Creates a comboBox
   * @param defaultValue
   * @param arrItems
   * @return
   */
  public JComboBox createComboBoxUI(String defaultValue , String[] arrItems)
  {
    Log.debugLog(className, "createComboBox", "", "", "Method called.");
    try
    {
      JComboBox  comboBox = new JComboBox();
    
      if(defaultValue != null && !defaultValue.equals("NA"))
        comboBox.addItem(defaultValue);
      
      if(arrItems != null )
      {   
        for(int i =0 ; i < arrItems.length ; i++)
        {
          String item = arrItems[i];
          
          if(item != null && !item.equals("NA") && item.equals(defaultValue))
            comboBox.addItem(item);
        }
      }       
      comboBox.setSelectedIndex(0);    
    
      return comboBox;
    }
    
    catch(Exception ex)
    {
      return null;   
    }
  }
}

  
