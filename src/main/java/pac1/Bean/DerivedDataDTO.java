package pac1.Bean;

import java.io.Serializable;

/**
 * This Class is used by Derived Data Layer and other For Requested Data of Derived Graph To Server.
 */
public class DerivedDataDTO implements Serializable, Cloneable
{
  
  private static final long serialVersionUID = -188467353885652829L;

  /*Array of Derived Graph Name.*/
  private String arrDerivedGraphName[] = null;
  
  /*Array of Derived Graph Formula.*/
  private String arrDerivedGraphFormula[] = null;
  
  /*Array of Derived Graph Number.*/
  private int arrDerivedGraphNumber[] = null;
  
  /**
   * Gets the array of Derived Graph Name.
   * @return
   */
  public String[] getArrDerivedGraphName() 
  {
    return arrDerivedGraphName;
  }
  
  /**
   * Sets the array of Derived Graph Name.
   * @param arrDerivedGraphName
   */
  public void setArrDerivedGraphName(String[] arrDerivedGraphName) 
  {
    this.arrDerivedGraphName = arrDerivedGraphName;
  }
  
  /**
   * Gets the array of Derived Graph Formula.
   * @return
   */
  public String[] getArrDerivedGraphFormula() 
  {
    return arrDerivedGraphFormula;
  }
  
  /**
   * Sets the array of Derived Graph Formula.
   * @param arrDerivedGraphFormula
   */
  public void setArrDerivedGraphFormula(String[] arrDerivedGraphFormula) 
  {
    this.arrDerivedGraphFormula = arrDerivedGraphFormula;
  }
  
  /**
   * Gets the array of Derived Graph Number.
   * @return
   */
  public int[] getArrDerivedGraphNumber() 
  {
    return arrDerivedGraphNumber;
  }
  
  /**
   * Sets the array of Derived Graph Number.
   * @param arrDerivedGraphNumber
   */
  public void setArrDerivedGraphNumber(int[] arrDerivedGraphNumber) 
  {
    this.arrDerivedGraphNumber = arrDerivedGraphNumber;
  }
  
  /**
   * Logging available data on console.
   */
  public void logData()
  {
    if(arrDerivedGraphNumber == null)
    {
      System.out.println("Empty DTO.");
      return;
    }
    
    for(int k = 0; k < arrDerivedGraphNumber.length; k++)
    {
      System.out.println("Derived Graph Number = " + arrDerivedGraphNumber[k] + ", Derived Graph Name = "+ arrDerivedGraphName[k] + ", Derived Graph Formula = "+arrDerivedGraphFormula[k]);
    }
  }
  
  /**
   * Cloning Object through Clonable interface.
   */
  public DerivedDataDTO clone() 
  {
    try 
    {
       return (DerivedDataDTO) super.clone();
    } 
    catch (CloneNotSupportedException e) 
    {        
      Log.errorLog("DerivedDataDTO", "clone", "", "", "Error in Cloning DerivedDataDTO object"+e);
      return null;
    }
  }
}
