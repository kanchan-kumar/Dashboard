package pac1.Bean;

import java.util.ArrayList;

/**
 * This Class contains properties, formula and sample data of each derived graph.
 * 
 */
public class DerivedGraphInfo implements java.io.Serializable
{

  private static final long serialVersionUID = 5968986933975058271L;

  /*Identifier for Derived Min Type Graphs.*/
  public static int DERIVED_MIN_TYPE_GRAPH = 0;
  
  /*Identifier for Derived Max Type Graphs.*/
  public static int DERIVED_MAX_TYPE_GRAPH = 1;
  
  /*Identifier for Derived Normal with AVG Type Graphs.*/
  public static int DERIVED_NORMAL_AVG_TYPE_GRAPH = 2;
  
  /*Identifier for Derived COUNT Type Graphs.*/
  public static int DERIVED_SUM_TYPE_GRAPH = 3;
  
  /*Identifier for Derived COUNT Type Graphs.*/
  public static int DERIVED_SUM_COUNT_TYPE_GRAPH = 4;
  
  /*Identifier for Derived COUNT Type Graphs.*/
  public static int DERIVED_COUNT_TYPE_GRAPH = 5;
  
  /*Identifier for Derived Normal Type Graphs.*/
  public static int DERIVED_NORMAL_TYPE_GRAPH = 7;
  
  /*Identifier for Derived SUM COUNT Type Graphs.*/
  public static String DERIVED_SUM_COUNT_NAME = "SUMCOUNT";
  
  /*Identification of name of derived graph function*/
  public static String DERIVED_COUNT_NAME = "COUNT";
  
  /*Identification of name of derived graph function*/
  public static String DERIVED_MIN_NAME = "MIN";
  
  /*Identification of name of derived graph function*/
  public static String DERIVED_MAX_NAME = "MAX";
  
  /*Identification of name of derived graph function*/
  public static String DERIVED_AVG_NAME = "AVG";
  
  public static String DERIVED_SUM_NAME = "SUM";
  
  /*This variable is using in special case (merge all instances of min/max or both)*/
  public static String DERIVED_ALLINSTANCES = "ALLINSTANCES_";
  
  /* Derived Graph Name */
  private String derivedGraphName = "";

  /* Derived Graph Number */
  private int derivedGraphNumber;

  /* Derived Graph Formula */
  private String derivedGraphFormula = "";

  /* Derived Math Expression */
  private String mathExpression = "";

  /* Derived Graph Numbers ArrayList contains all the graph number included in derived formula. */
  private ArrayList<GraphUniqueKeyDTO> arrGraphsList = new ArrayList<GraphUniqueKeyDTO>();

  /* Derived Graph Type , default is 2 (Normal) */
  private ArrayList<Integer> derivedGraphType = new ArrayList<Integer>();

  /* Setting the mapped variable of derived expression */
  private ArrayList<String> arrExpressionVar = new ArrayList<String>();

  /* Setting the values of mapped variable of derived expression */
  private ArrayList<String> arrExpressionVarValue = new ArrayList<String>();

  /* This array contains sample array for derived graph for generating data set for chart */
  private double[] arrDerivedSampleData = null;

  /* This array contains count array for derived graph for generating data set for chart */
  private int[] arrDerivedCountData = null;
  
  /* Variable Store Last Sample of Derived Graph. */
  private double lastDerivedSampleData = 0.0;
  
  /* Variable Store Last Sample of Derived Graph. */
  private double lastDerivedMaxData = 0.0;
  
  /* Variable Store Last Sample of Derived Graph. */
  private double lastDerivedMinData = 0.0;

  /* Variable Store last Sample Count. */
  private int derivedSampleCount = 0;

  /* The object of MathEvaluator is specific for each derived formula */
  private transient MathEvaluator mathEvaluator = null;

  /**
   * Constructor Initializing Data Members.
   * 
   * @param sampleArraySize
   */
  public DerivedGraphInfo(int sampleArraySize)
  {
    arrDerivedSampleData = new double[sampleArraySize];
    arrDerivedCountData = new int[sampleArraySize];
  }

  /**
   * Getting Derived Graph Name.
   * 
   * @return
   */
  public String getDerivedGraphName()
  {
    return derivedGraphName;
  }

  /**
   * Setting Derived Graph Name.
   * 
   * @param derivedGraphName
   */
  public void setDerivedGraphName(String derivedGraphName)
  {
    this.derivedGraphName = derivedGraphName;
  }

  /**
   * Getting Derived Graph Number.
   * 
   * @return
   */
  public int getDerivedGraphNumber()
  {
    return derivedGraphNumber;
  }

  /**
   * Setting Derived Graph Number.
   * 
   * @param derivedGraphNumber
   */
  public void setDerivedGraphNumber(int derivedGraphNumber)
  {
    this.derivedGraphNumber = derivedGraphNumber;
  }

  /**
   * Getting Derived Graph Formula.
   * 
   * @return
   */
  public String getDerivedGraphFormula()
  {
    return derivedGraphFormula;
  }

  /**
   * Setting Derived Graph formula.
   * 
   * @param derivedGraphFormula
   */
  public void setDerivedGraphFormula(String derivedGraphFormula)
  {
    this.derivedGraphFormula = derivedGraphFormula;
  }

  /**
   * Getting the Math Expression of Derived Graph.
   * 
   * @return
   */
  public String getMathExpression()
  {
    return mathExpression;
  }

  /**
   * Setting Math Expression of Derived Graph.
   * 
   * @param mathExpression
   */
  public void setMathExpression(String mathExpression)
  {
    this.mathExpression = mathExpression;
  }

  /**
   * Getting ArrayList containing the type of Derived (Normal/Min/Max).
   * 
   * @return
   */
  public ArrayList<Integer> getDerivedGraphType()
  {
    return derivedGraphType;
  }

  /**
   * Setting ArrayList containing the type of Derived (Normal/Min/Max).
   * 
   * @param derivedGraphType
   */
  public void setDerivedGraphType(ArrayList<Integer> derivedGraphType)
  {
    this.derivedGraphType = derivedGraphType;
  }

  /**
   * Getting ArrayList of expression Mapped variable.
   * 
   * @return
   */
  public ArrayList<String> getArrExpressionVar()
  {
    return arrExpressionVar;
  }

  /**
   * Setting ArrayList of expression mapped variable.
   * 
   * @param arrExpressionVar
   */
  public void setArrExpressionVar(ArrayList<String> arrExpressionVar)
  {
    this.arrExpressionVar = arrExpressionVar;
  }

  /**
   * Getting ArrayList of expression mapped value.
   * 
   * @return
   */
  public ArrayList<String> getArrExpressionVarValue()
  {
    return arrExpressionVarValue;
  }

  /**
   * Setting ArrayList of expression mapped value.
   * 
   * @param arrExpressionVarValue
   */
  public void setArrExpressionVarValue(ArrayList<String> arrExpressionVarValue)
  {
    this.arrExpressionVarValue = arrExpressionVarValue;
  }

  /**
   * Getting List of Graph Number of Graphs which are used to create Derived Graphs.
   * 
   * @return
   */
  public ArrayList<GraphUniqueKeyDTO> getArrGraphsList()
  {
    return arrGraphsList;
  }

  /**
   * Setting List of Graph Number of Graphs which are used to create Derived Graphs.
   * 
   * @param arrGraphNumberList
   */
  public void setArrGraphsList(ArrayList<GraphUniqueKeyDTO> arrGraphsList)
  {
    this.arrGraphsList = arrGraphsList;
  }

  /**
   * Gets Derived Graph Sample Array.
   * 
   * @return
   */
  public double[] getArrDerivedSampleData()
  {
    return arrDerivedSampleData;
  }

  /**
   * Sets Derived Graph Sample Array.
   * 
   * @param arrDerivedSampleData
   */
  public void setArrDerivedSampleData(double[] arrDerivedSampleData)
  {
    this.arrDerivedSampleData = arrDerivedSampleData;
  }

  /**
   * Get the Math Evaluator object associated with this Derived Graph.
   * 
   * @return
   */
  public MathEvaluator getMathEvaluator()
  {
    return mathEvaluator;
  }

  /**
   * Set the Math Evaluator object associated with this Derived Graph.
   * 
   * @param mathEvaluator
   */
  public void setMathEvaluator(MathEvaluator mathEvaluator)
  {
    this.mathEvaluator = mathEvaluator;
  }

  /**
   * Method returns last sample data of Derived Graph.
   * 
   * @return
   */
  public double getLastDerivedSampleData()
  {
    return lastDerivedSampleData;
  }

  /**
   * Method sets last sample data of Derived Graph.
   * 
   * @param lastDerivedSampleData
   */
  public void setLastDerivedSampleData(double lastDerivedSampleData)
  {
    this.lastDerivedSampleData = lastDerivedSampleData;
  }

  /**
   * Method returns last sample count data of Derived Graph.
   * 
   * @return
   */
  public int getDerivedSampleCount()
  {
    return derivedSampleCount;
  }

  /**
   * Method sets last sample count data of Derived Graph.
   * 
   * @param lastDerivedSampleCount
   */
  public void setDerivedSampleCount(int lastDerivedSampleCount)
  {
    this.derivedSampleCount = lastDerivedSampleCount;
  }

  /**
   * Method to get Count Data Array For Derived Graph.
   * @return
   */
  public int[] getArrDerivedCountData() 
  {
    return arrDerivedCountData;
  }

  /**
   * Method to set Count Data Array.
   * @param arrDerivedCountData
   */
  public void setArrDerivedCountData(int[] arrDerivedCountData) 
  {
    this.arrDerivedCountData = arrDerivedCountData;
  }
  
  public double getLastDerivedMaxData() 
  {
    return lastDerivedMaxData;
  }

  public void setLastDerivedMaxData(double lastDerivedMaxData) 
  {
    this.lastDerivedMaxData = lastDerivedMaxData;
  }
  
  public double getLastDerivedMinData() 
  {
    return lastDerivedMinData;
  }

  public void setLastDerivedMinData(double lastDerivedMinData) 
  {
    this.lastDerivedMinData = lastDerivedMinData;
  }
  
  /**
   * @param args
   */
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }

}
