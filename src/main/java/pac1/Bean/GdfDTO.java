/**
 * @ Name: GdfDTO.java
 * @ Purpose: This is to setting and retrieving attribute for directory list.
 * @ Author: Neeraj Jain
 *
 * Modifications:
 */

package pac1.Bean;

import java.io.Serializable;
import java.util.ArrayList;
  
public class GdfDTO implements Serializable
{
  private static final long serialVersionUID = 2787697398409320179L;
  // Request parameters
  public String gdfPrefix = "";
  public String reqType = "";  // For future use. Currently pass getGDFData

  // Response parameters
  boolean status = true;
  String errorMsg = "";
  String detailErrorMsg = "";
  public ArrayList <GDFData> GdfData = new ArrayList <GDFData>();


  public GdfDTO()
  {}
}
