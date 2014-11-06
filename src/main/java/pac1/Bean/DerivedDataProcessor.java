package pac1.Bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import pac1.Bean.GraphName.*;
/**
 * This class is used to process data for Derived Graph.
 * 
 */
public class DerivedDataProcessor implements java.io.Serializable
{

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 5238660975600340277L;

  /* This is used for Logging */
  private final String className = "DerivedDataProcessor";

  /* Time Based Data object for generating time based derived data. */
  private transient TimeBasedTestRunData timeBasedTestRunData = null;

  /* The array of DerivedGraphInfo mapped with each derived Graph Number */
  private ArrayList<DerivedGraphInfo> arrDerivedGraphInfo = new ArrayList<DerivedGraphInfo>();

  /* This array is used to store active derived graph Name */
  private ArrayList<String> arrDerivedGraphName = new ArrayList<String>();

  /* This array is used to store active derived graph numbers */
  private ArrayList<Integer> arrDerivedGraphNumber = new ArrayList<Integer>();

  /* This array is used to store active derived graph formula */
  private ArrayList<String> arrDerivedGraphFormula = new ArrayList<String>();

  /* This variable contains default sample array size for Derived Graphs */
  private int sampleArraySize;

  /* This variable keep the total number of sample used to create Derived Graph Data Array. */
  private int totalSamples = 0;

  private int debugLevel = 0;

  /* This object is used to perform some action using utilities method. */
  private TimeBasedDataUtils timeBasedDataUtils = new TimeBasedDataUtils();

  /**
   * Default Constructor.
   */
  public DerivedDataProcessor()
  {

  }

  public void setDebugLevel(int debugLevel)
  {
    this.debugLevel = debugLevel;
  }

  /**
   * To Initialize Requested Derived Graph.
   * 
   * @param derivedDTOObj
   * @param timeBasedTestRunData
   */
  public boolean init(DerivedDataDTO derivedDTOObj, TimeBasedTestRunData timeBasedTestRunData)
  {
    if (debugLevel > 0)
      Log.debugLogAlways(className, "init", "", "", "Method Called.");

    this.timeBasedTestRunData = timeBasedTestRunData;
    this.sampleArraySize = timeBasedTestRunData.getMaxSampleInGraph();

    if (derivedDTOObj == null || derivedDTOObj.getArrDerivedGraphNumber() == null)
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "init", "", "", "Initialization Failed Due to Unavailability of Derived Graph Number.");
      return false;
    }

    for (int i = 0; i < derivedDTOObj.getArrDerivedGraphNumber().length; i++)
    {
      String derivedGraphName = derivedDTOObj.getArrDerivedGraphName()[i];
      String derivedGraphFormula = derivedDTOObj.getArrDerivedGraphFormula()[i];
      int derivedGraphNumber = derivedDTOObj.getArrDerivedGraphNumber()[i];

      this.arrDerivedGraphNumber.add(derivedGraphNumber);
      this.arrDerivedGraphName.add(derivedGraphName);
      this.arrDerivedGraphFormula.add(derivedGraphFormula);

      // Now Creating Derived Info Object.
      arrDerivedGraphInfo.add(createDerivedGraphInfoObj(derivedGraphName, derivedGraphNumber, derivedGraphFormula));
    }

    return true;
  }

  /**
   * Method Initialize Derived Data processor from client.
   * 
   * @param timeBasedTestRunData
   */
  public void init(TimeBasedTestRunData timeBasedTestRunData)
  {
    this.timeBasedTestRunData = timeBasedTestRunData;  
    this.sampleArraySize = timeBasedTestRunData.getMaxSampleInGraph();
  }
  
  /**
   * Method Initialize Derived Data processor from Reporting Classes.
   * @param timeBasedTestRunData
   */
  public void init(TimeBasedTestRunData timeBasedTestRunData, int sampleCount)
  {
    this.timeBasedTestRunData = timeBasedTestRunData;
    this.sampleArraySize = sampleCount;
  }

  /**
   * This Method returns the object of derived Graph Info containing derived data and other info.
   * 
   * @return
   */
  public ArrayList<DerivedGraphInfo> getArrDerivedGraphInfo()
  {
    return arrDerivedGraphInfo;
  }

  /**
   * This method sets the object of DerviedGraphInfo in DDP.
   * 
   * @param arrDerivedGraphInfo
   */
  public void setArrDerivedGraphInfo(ArrayList<DerivedGraphInfo> arrDerivedGraphInfo)
  {
    this.arrDerivedGraphInfo = arrDerivedGraphInfo;
  }

  /**
   * Method returns available Derived Graph Number in DDP.
   * 
   * @return
   */
  public ArrayList<Integer> getDerivedGraphNumbers()
  {
    return arrDerivedGraphNumber;
  }

  /**
   * Method sets the new Derived Graph Numbers in DDP.
   * 
   * @param derivedGraphNumbers
   */
  public void setDerivedGraphNumbers(ArrayList<Integer> derivedGraphNumbers)
  {
    this.arrDerivedGraphNumber = derivedGraphNumbers;
  }

  /**
   * Method returns formula of available derived Graph.
   * 
   * @return
   */
  public ArrayList<String> getDerivedGraphFormula()
  {
    return arrDerivedGraphFormula;
  }

  /**
   * Method sets new Derived Graph Formula in DDP.
   * 
   * @param derivedGraphFormula
   */
  public void setDerivedGraphFormula(ArrayList<String> derivedGraphFormula)
  {
    this.arrDerivedGraphFormula = derivedGraphFormula;
  }

  /**
   * Method is used to get Array of Derived Graph Name.
   * 
   * @return
   */
  public ArrayList<String> getArrDerivedGraphName()
  {
    return arrDerivedGraphName;
  }

  /**
   * Method is used to set Array of Derived Graph Name.
   * 
   * @param arrDerivedGraphName
   */
  public void setArrDerivedGraphName(ArrayList<String> arrDerivedGraphName)
  {
    this.arrDerivedGraphName = arrDerivedGraphName;
  }

  /**
   * Inject dependency of Time Based data object.
   * 
   * @param timeBasedTestRunData
   */
  public void setTimeBasedTestRunData(TimeBasedTestRunData timeBasedTestRunData)
  {
    this.timeBasedTestRunData = timeBasedTestRunData;
  }

  /**
   * Constructor with Injecting time based object.
   * 
   * @param timeBasedTestRunData
   */
  public DerivedDataProcessor(TimeBasedTestRunData timeBasedTestRunData, int[] derivedGraphNumbers, String[] derivedGraphFormula, int sampleArraySize)
  {
    this.sampleArraySize = sampleArraySize;

    // Copy the derived Graph Number and Formula to process and persist it.
    for (int i = 0; i < derivedGraphNumbers.length; i++)
    {
      this.arrDerivedGraphNumber.add(derivedGraphNumbers[i]);
      this.arrDerivedGraphFormula.add(derivedGraphFormula[i]);
    }
  }

  /**
   * This method initialize and create object of DerivedGraphInfo to persist information of derived Graph.
   */
  public void initDerivedInfo()
  {
    try
    {
      // Iterating through each derived graph.
      for (int i = 0; i < arrDerivedGraphNumber.size(); i++)
      {
        // Create DerivedGraphInfo Instance for each derived Graph.
        arrDerivedGraphInfo.add(createDerivedGraphInfoObj(arrDerivedGraphName.get(i), arrDerivedGraphNumber.get(i), arrDerivedGraphFormula.get(i)));
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "initDerivedInfo", "", "", "Exception - ", e);
    }
  }

  /**
   * This method is used to process Derived formula and generate Derived Data By using Time Based Object.
   * 
   * @param derivedGraphName
   * @param derivedGraphNumber
   * @param derivedFormula
   * @return
   */
  public DerivedGraphInfo createDerivedGraphInfoObj(String derivedGraphName, int derivedGraphNumber, String derivedFormula)
  {
    try
    {
      // Create new Instance and Initialization
      DerivedGraphInfo derivedGraphInfo = new DerivedGraphInfo(sampleArraySize);

      // Setting Information on it.
      derivedGraphInfo.setDerivedGraphNumber(derivedGraphNumber);
      derivedGraphInfo.setDerivedGraphFormula(derivedFormula);

      // Here it generates the math expression.
      String derivedMathExpression = genDerivedMathExpression(derivedFormula, timeBasedTestRunData.getGraphNamesObj(), derivedGraphInfo, timeBasedTestRunData.testRunData.getVectorMappingList());

      derivedGraphInfo.setMathExpression(derivedMathExpression);
      derivedGraphInfo.setMathEvaluator(new MathEvaluator(derivedMathExpression));

      // TODO - Set More Info here like Derived Graph Name etc.
      derivedGraphInfo.setDerivedGraphName(derivedGraphName);

      return derivedGraphInfo;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "createDerivedGraphInfoObj", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   * Method returns the object of DerivedGraphInfo by Derived Graph Number.
   * 
   * @param derivedGraphNumber
   * @return
   */
  public DerivedGraphInfo getDerivedGraphInfoByDerivedGraphNumber(int derivedGraphNumber)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getDerivedGraphInfoByDerivedGraphNumber", "", "", "derivedGraphNumber = " + derivedGraphNumber);

      // Searching For Derived Graph Number Availability.
      // The index Mapping of Object to Derived Graph Number array help to find out object for Derived Graph Number.
      for (int k = 0; k < arrDerivedGraphNumber.size(); k++)
      {
        if (arrDerivedGraphNumber.get(k) == derivedGraphNumber)
          return arrDerivedGraphInfo.get(k);
      }

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getDerivedGraphInfoByDerivedGraphNumber", "", "", "Derived Data is Not Available For Derived Graph Number = " + derivedGraphNumber);
      return null;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "getDerivedGraphInfoByDerivedGraphNumber", "", "", "Exception - ", e);
      return null;
    }
  }

  
  /**
   * Method is used to update Derived Data Sample on Overall Averaging.
   */
  public void averageAllDerivedDataSample()
  {
    try
    {
      
      if(timeBasedTestRunData == null)
	return;
      
      for(int i = 0; i < timeBasedTestRunData.getDataItemCount(); i++)
	updateDerivedGraphsData(i);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * This Method updates Derived Graph Data in graph activation and Online Mode.
   * 
   * @return
   */
  public boolean updateDerivedGraphsData(int dataItemCount)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "updateDerivedGraphsData", "", "", "Method Called. dataItemCount = " + dataItemCount);

      DerivedGraphInfo derivedGraphInfo = null;
      
      for (int i = 0; i < arrDerivedGraphInfo.size(); i++)
      {
        // Getting the Instance
        derivedGraphInfo = arrDerivedGraphInfo.get(i);

        if (debugLevel > 0)
          Log.debugLogAlways(className, "updateDerivedGraphsData", "", "", "updating derived graph with formula = " + derivedGraphInfo.getDerivedGraphFormula() + " and Derived Graph Number = " + derivedGraphInfo.getDerivedGraphNumber());

        // The Following method generates sample data for current derived graph.
        genSampleDataForDerived(dataItemCount, derivedGraphInfo);
      }
      
      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "updateDerivedGraphsData", "", "", "Exception - ", e);
      return false;
    }
  }

  /**
   * This method generate sample data for derived graph.
   * 
   * @param dataItemCount
   */
  private void genSampleDataForDerived(int dataItemCount, DerivedGraphInfo derivedGraphInfo)
  {
    try
    {
      if(derivedGraphInfo == null || derivedGraphInfo.getArrGraphsList() == null || derivedGraphInfo.getArrGraphsList().size() == 0)
      {
	Log.errorLog(className, "genSampleDataForDerived", "", "", "derivedGraphInfo ArrGraphsList size = 0");
	return;
      }
      
      int avgCountData = 0;
      int maxSampleCount = 0;
      double minDataValue = Double.MAX_VALUE;
      double maxDataValue = 0.0;
      double lastMaxDataValue = 0.0;
      double lastMinDataValue = 0.0;
      
      // Generating and updating data sample in derived sample array.
      for (int k = 0; k < derivedGraphInfo.getArrGraphsList().size(); k++)
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO = derivedGraphInfo.getArrGraphsList().get(k);
        if (graphUniqueKeyDTO == null)
          continue;

        String expVar = derivedGraphInfo.getArrExpressionVar().get(k);
        TimeBasedDTO timeBasedDTO = timeBasedTestRunData.getTimeBasedDTO(graphUniqueKeyDTO);
        
        if (timeBasedDTO == null)
          continue;

        // variable for derived sample data.
        double derivedSampleData = 0.0;
        int derivedCountSampleData = timeBasedDTO.getArrCountData()[dataItemCount]; 
        
        /*Getting Max Sample From all Derived Associated Graphs.*/
	  if(maxSampleCount < derivedCountSampleData)
	    maxSampleCount = derivedCountSampleData;
	
	  /*Here we are keeping the last un averaged max sample data*/
	  if(lastMaxDataValue < timeBasedDTO.getLastMaxData())
	    lastMaxDataValue = timeBasedDTO.getLastMaxData();
	  
	  /*Here we are keeping the last un averaged max sample data*/
	  if(lastMinDataValue < timeBasedDTO.getLastMinData())
	    lastMinDataValue = timeBasedDTO.getLastMinData();
	  
        // Checking for Derived Graph Type.
	if(derivedGraphInfo.getDerivedGraphType().get(k) == DerivedGraphInfo.DERIVED_MIN_TYPE_GRAPH) // Case of Derived Type Min Graph
	{
	  if(minDataValue > timeBasedDTO.getArrMinData()[dataItemCount])
	    minDataValue = timeBasedDTO.getArrMinData()[dataItemCount];

	  if(k == derivedGraphInfo.getArrGraphsList().size() - 1)
	    derivedSampleData = minDataValue;
	}
	else if(derivedGraphInfo.getDerivedGraphType().get(k) == DerivedGraphInfo.DERIVED_MAX_TYPE_GRAPH) // Case of Derived Type Max Graph.
	{
	  if(maxDataValue < timeBasedDTO.getArrMaxData()[dataItemCount])
	    maxDataValue = timeBasedDTO.getArrMaxData()[dataItemCount];

	  if(k == derivedGraphInfo.getArrGraphsList().size() - 1)
	    derivedSampleData = maxDataValue;
	}
        else if (derivedGraphInfo.getDerivedGraphType().get(k) == DerivedGraphInfo.DERIVED_NORMAL_AVG_TYPE_GRAPH) // Case of Derived Type Avg Graph.
        {
          derivedSampleData = timeBasedDTO.getArrGraphSamplesData()[dataItemCount];
          derivedSampleData = derivedSampleData * derivedCountSampleData;
          avgCountData = avgCountData + derivedCountSampleData;
        }
        else if(derivedGraphInfo.getDerivedGraphType().get(k) == DerivedGraphInfo.DERIVED_SUM_COUNT_TYPE_GRAPH) // Case of Derived Type Avg Graph.
          derivedSampleData = timeBasedDTO.getArrCountData()[dataItemCount];
        else if(derivedGraphInfo.getDerivedGraphType().get(k) == DerivedGraphInfo.DERIVED_COUNT_TYPE_GRAPH) // Case of Derived Type Avg Graph.
          derivedSampleData = timeBasedDTO.getArrCountData()[dataItemCount];
        else
          derivedSampleData = timeBasedDTO.getArrGraphSamplesData()[dataItemCount];

        if (derivedGraphInfo.getMathEvaluator() == null)
        {
          // Math Evaluator class is not serialized. It may be null at client side sample data update. create it, if it null only first time.
          derivedGraphInfo.setMathEvaluator(new MathEvaluator(derivedGraphInfo.getMathExpression()));
        }

        // Now adding it in MathEvaluator for applying formula.
        derivedGraphInfo.getMathEvaluator().addVariable(expVar, derivedSampleData);
      }
      
      /*create samples for derived graph based on derived graph functions*/
      updateDerivedSampleData(dataItemCount, derivedGraphInfo, avgCountData, maxSampleCount, lastMaxDataValue, lastMinDataValue);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "genSampleDataForDerived", "", "", "Exception - ", e);
    }
  }
   
  
  /**
   * This method will calculate and update value in sample data array for the given data item count
   * @param dataItemCount
   * @param derivedGraphInfo
   */
  private void updateDerivedSampleData(int dataItemCount, DerivedGraphInfo derivedGraphInfo, int avgCountData, int maxSampleCount, double lastMaxDataValue, double lastMinDataValue)
  {
   try
   {
     MathEvaluator localMathEvaluator = derivedGraphInfo.getMathEvaluator();
     if (localMathEvaluator != null)
     {
	/*This is the case for cumulative data*/
	if(derivedGraphInfo.getDerivedGraphType().get(0) == DerivedGraphInfo.DERIVED_SUM_COUNT_TYPE_GRAPH)
	{
	  if(dataItemCount != 0)
	    derivedGraphInfo.getArrDerivedSampleData()[dataItemCount] = localMathEvaluator.getValue() + derivedGraphInfo.getArrDerivedSampleData()[dataItemCount - 1];
	  else
	    derivedGraphInfo.getArrDerivedSampleData()[dataItemCount] = localMathEvaluator.getValue();
	}
	else if(avgCountData == 0)
         derivedGraphInfo.getArrDerivedSampleData()[dataItemCount] = localMathEvaluator.getValue();
	else
	  derivedGraphInfo.getArrDerivedSampleData()[dataItemCount] = localMathEvaluator.getValue()/avgCountData;
	
       derivedGraphInfo.getArrDerivedCountData()[dataItemCount] = maxSampleCount;
       derivedGraphInfo.setLastDerivedMaxData(lastMaxDataValue);
       derivedGraphInfo.setLastDerivedMinData(lastMinDataValue);
     }
   }
   catch(Exception e)
   {
     Log.stackTraceLog(className, "genSampleDataForDerived", "", "", "Exception - ", e);
   }
  }
  
  /**
   * This method generate sample data for derived graph by using Graph data.
   * @param dataItemCount
   */
  public void genDerivedSampleByGraphData(ArrayList<double []> arrGraphData, ArrayList<int []>arrGraphCountData, ArrayList<double []>arrMaxGraphData, ArrayList<double []>arrMinGraphData, double maxLastDataValue, double minLastDataValue, DerivedGraphInfo derivedGraphInfo, int sampleCount)
  {
    try
    {
      if(derivedGraphInfo == null || derivedGraphInfo.getArrGraphsList() == null || derivedGraphInfo.getArrGraphsList().size() == 0)
      {
	Log.errorLog(className, "genDerivedSampleByGraphData", "", "", "derivedGraphInfo ArrGraphsList size = 0");
	return;
      }
      
      Log.debugLogAlways(className, "genDerivedSampleByGraphData", "", "", "Graph Data Length = " + arrGraphData.size() + ", sampleCount = " + sampleCount);
      
      int dataItemCount = 0;
      
      /*Iterating through sample count.*/
      for(int i = 0; i < sampleCount; i++)
      {
	int avgCountData = 0;
	int maxSample = 0;
	double minDataValue = Double.MAX_VALUE;
	double maxDataValue = 0.0;
	
	/*Generating and updating data sample in derived sample array.*/
	for(int k = 0; k < derivedGraphInfo.getArrGraphsList().size(); k++)
	{
	  GraphUniqueKeyDTO graphUniqueKeyDTO = derivedGraphInfo.getArrGraphsList().get(k);
	  if(graphUniqueKeyDTO == null)
	    continue;

	  /*Getting Expression variable.*/
	  String expVar = derivedGraphInfo.getArrExpressionVar().get(k);

	  /*variable for derived sample data.*/
	  double derivedSampleData = arrGraphData.get(k)[i];
	  int countData = arrGraphCountData.get(k)[i];
	  
	  /*Getting Max Sample From all Derived Associated Graphs.*/
	  if(maxSample < countData)
	    maxSample = countData;
	  
	  if(derivedGraphInfo.getDerivedGraphType().get(k) == DerivedGraphInfo.DERIVED_NORMAL_AVG_TYPE_GRAPH)
	  {
	    derivedSampleData = derivedSampleData * countData;
	    avgCountData = avgCountData + countData;
	  }
	  else if(derivedGraphInfo.getDerivedGraphType().get(k) == DerivedGraphInfo.DERIVED_SUM_COUNT_TYPE_GRAPH || derivedGraphInfo.getDerivedGraphType().get(k) == DerivedGraphInfo.DERIVED_COUNT_TYPE_GRAPH) // Case of Derived Type Avg Graph.
	    derivedSampleData = countData;
	  else if(derivedGraphInfo.getDerivedGraphType().get(k) == DerivedGraphInfo.DERIVED_MIN_TYPE_GRAPH) // Case of Derived Type Min Graph.
	  {
	    if(minDataValue > arrMinGraphData.get(k)[i])
	      minDataValue = arrMinGraphData.get(k)[i];
	    
	    derivedSampleData = 0.0;
	    if(k == derivedGraphInfo.getArrGraphsList().size() - 1)
	      derivedSampleData = minDataValue;
	  }
	  else if(derivedGraphInfo.getDerivedGraphType().get(k) == DerivedGraphInfo.DERIVED_MAX_TYPE_GRAPH) // Case of Derived Type Max Graph.
	  {
	    if(maxDataValue < arrMaxGraphData.get(k)[i])
	      maxDataValue = arrMaxGraphData.get(k)[i];
	    
	    derivedSampleData = 0.0;
	    if(k == derivedGraphInfo.getArrGraphsList().size() - 1)
	      derivedSampleData = maxDataValue;
	  }

	  /*Now adding it in MathEvaluator for applying formula.*/
	  derivedGraphInfo.getMathEvaluator().addVariable(expVar, derivedSampleData);
	}

	/*Putting the sample in array*/
	updateDerivedSampleData(dataItemCount, derivedGraphInfo, avgCountData, maxSample, maxLastDataValue, minLastDataValue);
	dataItemCount++;
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "genDerivedSampleByGraphData", "", "", "Exception - ", e);
    }
  }
 

  /**
   * Method is used to increase size of array when granularity/Resolution is applied on Derived Graph.
   * 
   * @param sizeToIncrease
   */
  public void increaseDerivedArraySize(int sizeToIncrease)
  {
    try
    {
      DerivedGraphInfo derivedGraphInfo = null;

      for (int i = 0; i < arrDerivedGraphInfo.size(); i++)
      {
        derivedGraphInfo = arrDerivedGraphInfo.get(i);

        // Increasing the size of Derived Graph Sample Data Array if resolution is applied.
        derivedGraphInfo.setArrDerivedSampleData(timeBasedDataUtils.increaseSizeOfDoubleArray(derivedGraphInfo.getArrDerivedSampleData(), sizeToIncrease));
        derivedGraphInfo.setArrDerivedCountData(TimeBasedDataUtils.increase1DIntArraySize(derivedGraphInfo.getArrDerivedCountData(), sizeToIncrease));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "increaseDerivedArraySize", "", "", "Exception - ", e);
    }
  }

  /**
   * Updating Derived Graph Statistics For Lower Pane.
   */
  public void updateDerivedGraphStats()
  {
    try
    {
      totalSamples++;
            
      /*Iterating through each Derived Graph and update Derived Last Sample Count.*/
      for(int i = 0; i < arrDerivedGraphInfo.size(); i++)
      {
	DerivedGraphInfo derivedGraphInfoObj = arrDerivedGraphInfo.get(i);
        int maxSampleGraph = 0;

	for(int k = 0; k < derivedGraphInfoObj.getArrGraphsList().size(); k++)
	{
	  GraphUniqueKeyDTO graphUniqueKeyDTO = derivedGraphInfoObj.getArrGraphsList().get(k);
	  if(graphUniqueKeyDTO == null)
	    continue;
	  
	  TimeBasedDTO timeBasedDTO = timeBasedTestRunData.getTimeBasedDTO(graphUniqueKeyDTO);
	  if (timeBasedDTO == null)
	    continue;
	  
	  /*Updating the Max Sample Count.*/
	  if(derivedGraphInfoObj.getDerivedGraphType().get(0) == DerivedGraphInfo.DERIVED_SUM_COUNT_TYPE_GRAPH || derivedGraphInfoObj.getDerivedGraphType().get(0) == DerivedGraphInfo.DERIVED_COUNT_TYPE_GRAPH )
	  {
	    maxSampleGraph = maxSampleGraph + timeBasedDTO.getLastCountData();
	  }
	  else
	  {
	    if(maxSampleGraph < timeBasedDTO.getLastCountData())
	      maxSampleGraph = timeBasedDTO.getLastCountData();
	  }
	}
	
	/*Updating the Last Sample Value in Derived Graph.*/
	derivedGraphInfoObj.setDerivedSampleCount(maxSampleGraph);
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * Method is used to update total samples in Last N view mode.
   */
  public void updateRecurringSamplesInLastNMinutes(int samples)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "updateRecurringSamplesInLastNMinutes", "", "", "Method Called. samples = " + samples);
      
      /*Iterating through each Derived Graph and update Derived Data Sample.*/
      for(int i = 0; i < arrDerivedGraphInfo.size(); i++)
      {
	DerivedGraphInfo derivedGraphInfoObj = arrDerivedGraphInfo.get(i);
	
	for(int k = 0; k < timeBasedTestRunData.getDataItemCount() - 1; k++)
	{
	  derivedGraphInfoObj.getArrDerivedSampleData()[k] = derivedGraphInfoObj.getArrDerivedSampleData()[k+1];
	  derivedGraphInfoObj.getArrDerivedCountData()[k] = derivedGraphInfoObj.getArrDerivedCountData()[k+1];
	}
      }
      
      totalSamples -= samples;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Method returns Graph Statistics For Derived Graph Number.
   * 
   * @param derivedGraphNumber
   * @param graphStatsObj
   * @return
   */
  public void getDerivedGraphStats(int derivedGraphNumber, GraphStats graphStatsObj)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getDerivedGraphStats", "", "", "derivedGraphNumber = " + derivedGraphNumber);

      DerivedGraphInfo derivedGraphInfo = getDerivedGraphInfoByDerivedGraphNumber(derivedGraphNumber);
      int dataItemCount = timeBasedTestRunData.getDataItemCount();

      if (derivedGraphInfo == null || dataItemCount <= 0)
      {
        // if Derived Graph Data is not available then reset object before returning it.
        graphStatsObj.setMaxData(0.0);
        graphStatsObj.setMinData(0.0);
        graphStatsObj.setAvgData(0.0);
        graphStatsObj.setCount(0);
        graphStatsObj.setGraphDataType(-1);
        graphStatsObj.setLastSample(0.0);
        graphStatsObj.setStdDev(0.0);

        return;
      }

      // TODO - Creating Graph Stats From Sample Array. Is it Right?
      // All The Calculation are done only using Derived Sample Data, not included last sample data.
      // Getting Sample Array of Derived Graph.
      double[] arrDerivedSampleData = derivedGraphInfo.getArrDerivedSampleData();

      double minData = timeBasedDataUtils.getMinValueFromArray(arrDerivedSampleData, dataItemCount);
      double maxData = timeBasedDataUtils.getMaxValueFromArray(arrDerivedSampleData, dataItemCount);
      
      if(maxData < derivedGraphInfo.getLastDerivedMaxData())
	maxData = derivedGraphInfo.getLastDerivedMaxData();
      
      if(minData > derivedGraphInfo.getLastDerivedMinData())
	minData = derivedGraphInfo.getLastDerivedMinData();
      
      if(minData >= Double.MAX_VALUE)
	minData = 0.0;
      
      if(maxData >= Double.MAX_VALUE)
	maxData = minData;
      
      int totalSampleCount = timeBasedDataUtils.getSumOfArrayElements(derivedGraphInfo.getArrDerivedCountData(), dataItemCount);
      double sum = timeBasedDataUtils.getAvgSum(arrDerivedSampleData, derivedGraphInfo.getArrDerivedCountData(), dataItemCount);
      
      // Calculate Average.
      double avg = sum / totalSampleCount;

      // Calculate Std - dev
      double sumSqr = timeBasedDataUtils.getAvgSumSqrForDerived(arrDerivedSampleData, avg, dataItemCount);
      double std_dev = Math.sqrt(sumSqr) / dataItemCount;
      double lastDerivedSample = arrDerivedSampleData[dataItemCount - 1];

      // Persisting values in object.
      graphStatsObj.setMinData(minData);
      graphStatsObj.setMaxData(maxData);
      graphStatsObj.setAvgData(avg);
      
      /*Count Include Last Sample Count to Show in Lower Pane. Not Including with Graph Average Calculation as Last Graph Sample Count Not Included. */
      int derivedLasCount = derivedGraphInfo.getDerivedSampleCount();

      if(timeBasedTestRunData.getTestRunDataTypeObj().getGranularity() != -1 && timeBasedTestRunData.getGraphNamesObj() != null && timeBasedTestRunData.getTestRunDataTypeObj().getGranularity() <= (timeBasedTestRunData.getGraphNamesObj().getInterval())/1000)
	derivedLasCount = 0;

      if(derivedGraphInfo.getDerivedGraphType().get(0) == DerivedGraphInfo.DERIVED_SUM_COUNT_TYPE_GRAPH)
	graphStatsObj.setCount((int)lastDerivedSample + derivedLasCount);
      else if(derivedGraphInfo.getDerivedGraphType().get(0) == DerivedGraphInfo.DERIVED_COUNT_TYPE_GRAPH)
      {
	double totalCount = timeBasedDataUtils.getSumOfDoubleArrayElements(arrDerivedSampleData, dataItemCount);
	graphStatsObj.setCount((int)totalCount + derivedLasCount);
      }
      else
      graphStatsObj.setCount(totalSampleCount + derivedLasCount);
      
      graphStatsObj.setStdDev(std_dev);
      graphStatsObj.setGraphDataType(-1);
      graphStatsObj.setLastSample(lastDerivedSample);
      graphStatsObj.setGraphName(derivedGraphInfo.getDerivedGraphName());
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "getDerivedGraphStats", "", "", "Exception - ", e);
    }
  }

  /**
   * Generating Derived Graph Data By using Time Based Test Run Data.
   * 
   * @param derivedGraphName
   * @param derivedGraphNumber
   * @param derivedFormula
   */
  public void genDerivedDataByTBTRD(String derivedGraphName, int derivedGraphNumber, String derivedFormula)
  {
    try
    {
      // Create DerivedGraphInfo Instance for each derived Graph.
      DerivedGraphInfo derivedGraphInfo = createDerivedGraphInfoObj(derivedGraphName, derivedGraphNumber, derivedFormula);

      // Generating Data for Derived Graph.
      for (int i = 0; i < timeBasedTestRunData.getDataItemCount(); i++)
      {
        // Generate Derived Graph samples.
        genSampleDataForDerived(i, derivedGraphInfo);
      }

      // Persisting Derived Graph Info.
      arrDerivedGraphInfo.add(derivedGraphInfo);
      arrDerivedGraphNumber.add(derivedGraphNumber);
      arrDerivedGraphFormula.add(derivedFormula);
      arrDerivedGraphName.add(derivedGraphName);
      
      /*Updating Last Sample Data.*/
      updateDerivedGraphStats();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "genDerivedDataByTBTRD", "", "", "Exception - ", e);
    }
  }
  
  /**
   * Generating Derived Graph Data By using Averaged Graph Data.
   * 
   * @param derivedGraphName
   * @param derivedGraphNumber
   * @param derivedFormula
   */
  public DerivedGraphInfo genDerivedDataByGraphData(String derivedGraphName, int derivedGraphNumber, String derivedFormula)
  {
    try
    {
      Log.debugLogAlways(className, "genDerivedDataByGraphData", "", "", "Method Called. derivedGraphName = " + derivedGraphName + ", derivedGraphNumber = " + derivedGraphNumber + ", derivedFormula = " + derivedFormula);
      
      //Create DerivedGraphInfo Instance for each derived Graph.
      DerivedGraphInfo derivedGraphInfo = createDerivedGraphInfoObj(derivedGraphName, derivedGraphNumber, derivedFormula);
      return derivedGraphInfo;     
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "genDerivedDataByGraphData", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   * This method merge/add the Derived Graphs of Two Time Based object, Merge Derived Graphs which are not in current Time Based Data.
   * 
   * @param reqTBTRD
   */
  public void mergeDerivedGraphs(TimeBasedTestRunData reqTBTRD)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "mergeDerivedGraphs", "", "", "Method Called.");

      // Getting the DerivedDataProcessor object of requested TBTRD.
      DerivedDataProcessor reqTimeBasedDDP = reqTBTRD.getDerivedDataProcessor();

      // Checking Existence of Derived Graph in Requested TBTRD.
      if (reqTimeBasedDDP.getDerivedGraphNumbers().size() == 0)
      {
        if (debugLevel > 0)
          Log.debugLogAlways(className, "mergeDerivedGraphs", "", "", "Requested Time Based object not containing Derived Graphs. No Need to Merge with Existing.");
        return;
      }

      // TODO - This condition Only for Logging purpose.
      if (arrDerivedGraphNumber.size() == 0)
        Log.debugLogAlways(className, "mergeDerivedGraphs", "", "", "Existing Time Based object doesn't have Derived Graphs.");

      // Merging Derived Graphs with Existing one.
      arrDerivedGraphNumber.addAll(reqTimeBasedDDP.getDerivedGraphNumbers());
      arrDerivedGraphFormula.addAll(reqTimeBasedDDP.getDerivedGraphFormula());
      arrDerivedGraphName.addAll(reqTimeBasedDDP.getArrDerivedGraphName());
      arrDerivedGraphInfo.addAll(reqTimeBasedDDP.getArrDerivedGraphInfo());
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "mergeDerivedGraphs", "", "", "Exception - ", e);
    }
  }

  /**
   * Updating Derived Associated Graphs in Existing TBTRD, if may be possible that some graphs which are not in active state, are used to create Derived Graph.
   * 
   * @param newReqTBTRD
   */
  public void updateTimeBasedWithDerivedAssociatedGraphs(TimeBasedTestRunData newReqTBTRD)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "updateTimeBasedWithDerivedAssociatedGraphs", "", "", "Method Called.");

      ArrayList<GraphUniqueKeyDTO> groupIdGraphIdVectorNameList = new ArrayList<GraphUniqueKeyDTO>();

      GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = newReqTBTRD.getArrGraphUniqueKeyDTO();
      for (int k = 0; k < arrGraphUniqueKeyDTO.length; k++)
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO = arrGraphUniqueKeyDTO[k];

        if (!timeBasedTestRunData.isGraphExistInMemory(graphUniqueKeyDTO) && !groupIdGraphIdVectorNameList.contains(graphUniqueKeyDTO))
          groupIdGraphIdVectorNameList.add(graphUniqueKeyDTO);

      }

      if (debugLevel > 0)
        Log.debugLogAlways(className, "updateTimeBasedWithDerivedAssociatedGraphs", "", "", groupIdGraphIdVectorNameList.size() + " Number of Derived Associated Graphs not in active state.");

      if (groupIdGraphIdVectorNameList.size() == 0) // No Need to update Time based Arrays.
        return;

      GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO = newReqTBTRD.getArrGraphUniqueKeyDTO();
      // copy only active Graph Content.
      for (int i = 0; i < arrActiveGraphUniqueKeyDTO.length; i++)
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO = arrActiveGraphUniqueKeyDTO[i];
        TimeBasedDTO timeBasedDTO = newReqTBTRD.getTimeBasedDTO(graphUniqueKeyDTO);
        // Bug: If array size is different so will throw exception
        timeBasedTestRunData.setTimeBasedDTO(graphUniqueKeyDTO, timeBasedDTO);
      }

      GraphUniqueKeyDTO[] mergedGraphUniqueKeyDTO = TimeBasedDataUtils.appendGraphUniqueKeyDTOArray(timeBasedTestRunData.getArrGraphUniqueKeyDTO(), newReqTBTRD.getArrGraphUniqueKeyDTO());
      timeBasedTestRunData.activateGraphs(mergedGraphUniqueKeyDTO);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "updateTimeBasedWithDerivedAssociatedGraphs", "", "", "Exception - ", e);
    }
  }

  /**
   * Remove brackets from input string.
   * 
   * @param realToken
   * @return
   */
  private String removeBrackets(String realToken)
  {
    realToken = realToken.replace("{", "");
    realToken = realToken.replace("}", "");
    realToken = realToken.replace("(", "");
    realToken = realToken.replace(")", "");
    realToken = realToken.replace("[", "");
    realToken = realToken.replace("]", "");

    return realToken;
  }

  /**
   * Method Convert Any Type of array to related type ArrayList.
   * 
   * @param array
   * @return
   */
  public static ArrayList<Integer> arrayToList(int[] array)
  {
    final ArrayList<Integer> list = new ArrayList<Integer>(array.length);

    for (int num : array)
      list.add(num);

    return list;
  }

  /**
   * This function is to convert derived graph expression to math expression.
   * 
   * @param derivedGraphExpression
   * @param graphNames
   * @param derivedGraphInfo
   * @return
   */
  public String genDerivedMathExpression(String derivedGraphExpression, GraphNames graphNames, DerivedGraphInfo derivedGraphInfo, ArrayList<VectorMappingDTO> arrVectorMappingList)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "genDerivedMathExpression", "", "", "Method called. derivedGraphExpression = " + derivedGraphExpression);

      // Split the derived expression.
      String[] stTemp = getTokens(derivedGraphExpression);

      int i = 1;
      int type = 2;// default it is none

      for (int j = 0; j < stTemp.length; j++)
      {
        String strToken = stTemp[j].trim();
        String realToken = removeBrackets(strToken);

        try
        {
          Integer.parseInt(realToken);
        }
        catch (Exception ex)
        {
          // Getting String Array.
          String[] arrTemp = getStringArray(realToken);

          if (arrTemp.length == 3)
          {
            // Getting Report Id from token array.
            String rptId = arrTemp[0].trim();

            // Checking for Derived type.
            type = ParseDerivedExp.getDerivedFunTypeFromFormula(rptId, "_");
            rptId = ParseDerivedExp.replaceDerivedFunName(rptId);

            // Getting Group Id.
            String rptGrpId = arrTemp[1].trim();

            // Getting vector name.
            String vectName = arrTemp[2].trim();

            if (debugLevel > 0)
              Log.debugLogAlways(className, "genDerivedMathExpression", "", "", "vectName = " + vectName);

            // Replace brackets coming in vector name.
            vectName = rptUtilsBean.replace(vectName, "[", "");
            vectName = rptUtilsBean.replace(vectName, "]", "");

            if (debugLevel > 0)
              Log.debugLogAlways(className, "genDerivedMathExpression", "", "", "Group Id = " + rptId + ", Graph Id = " + rptGrpId + ", Vector Name = " + vectName);

            // Check for error case.
            if (rptId.trim().equals("") || rptGrpId.trim().equals("") || vectName.trim().equals(""))
              continue;

            /* check for greater than 2 because server name can also have ".", like 192.168.18.106 */
            if (arrTemp.length > 2)
            {
              derivedGraphInfo.getArrExpressionVar().add("a" + i);
              derivedGraphInfo.getArrExpressionVarValue().add(strToken);
              derivedGraphExpression = derivedGraphExpression.replace(strToken, "a" + i);

              GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(Integer.parseInt(rptId), Integer.parseInt(rptGrpId), vectName);
              int graphDataIndex = graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);

              if (graphDataIndex == -1)
                continue;

              graphUniqueKeyDTO.setGraphDataIndex(graphDataIndex);
              if (debugLevel > 0)
                Log.debugLogAlways(className, "genDerivedMathExpression", "", "", "derivedGraphExpression = " + derivedGraphExpression + ", Group Id = " + rptId + ", Graph Id = " + rptGrpId + ", Vector Name = " + vectName + ", graphUniqueKeyDTO = " + graphUniqueKeyDTO.toString());

              // Setting Array of Graph Numbers available in current formula.
              derivedGraphInfo.getArrGraphsList().add(graphUniqueKeyDTO);

              // Setting the type of graph in array.
              derivedGraphInfo.getDerivedGraphType().add(type);
              i++;
            }
          }
        }
      }

      if (debugLevel > 0)
        Log.debugLogAlways(className, "genDerivedMathExpression", "", "", "derivedGraphExpression = " + derivedGraphExpression);

      return derivedGraphExpression;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      Log.stackTraceLog(className, "genDerivedMathExpression", "", "", "Exception - ", ex);
      return "";
    }
  }

  /**
   * Input: 1.2.[v1,v2] + 2.3.[v3] Output: str[] = {1.2.[v1,v2], 2.3.[v3]} Process: reads character by character and parse the entire string and returns a final String as 1.2.[v1,v2]#2.3.[v3]
   * 
   * @return An array by splitting this with #
   * @author Pydi
   * 
   */
  public String[] getTokens(String expression)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getTokens", "", "", "Method Called for expression = " + expression);
      char ch;
      int dotCount = 0;
      boolean startBracketFound = false;
      boolean isDigitFound = false;
      StringBuffer strBuff = new StringBuffer();

      if (expression == null)
        return null;

      for (int i = 0; i < expression.length(); i++)
      {
        ch = expression.charAt(i);

        if ((ch == '(' || ch == ')' || ch == '{' || ch == '}' || ch == ' ') && !startBracketFound)
          continue;

        if ((ch == '+' || ch == '-' || ch == '*' || ch == '/') && !startBracketFound)
          continue;

        if (dotCount == 2 && ch != '[')
        {
          strBuff.append(ch);
          strBuff.append(expression.charAt(i + 1) + "#$");
          i++;
          dotCount = 0;
          continue;
        }

        if (dotCount == 2 && ch == '[')
          dotCount = 0;

        if (ch == '.' && !startBracketFound)
          dotCount++;

        if (ch == '[')
          startBracketFound = true;

        if (ch != ']')
        {
          int j = i;
          while (Character.isDigit(ch))
          {
            isDigitFound = true;
            strBuff.append(ch);
            j++;
            if (j >= expression.length())
              break;
            ch = expression.charAt(j);
          }

          if (ch != '.' && !startBracketFound && isDigitFound)
            strBuff.append("#$");
          else if (ch == '.' && !startBracketFound)
            dotCount++;

          isDigitFound = false;
          i = j;

          if (ch == ']')
          {
            strBuff.append("]#$");
            dotCount = 0;
            startBracketFound = false;
          }
          else
          {
            if ((ch == ')' || ch == '(' || ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == ' ') && !startBracketFound)
              continue;
            strBuff.append(ch);
          }
        }
        else
        {
          strBuff.append("]#$");
          dotCount = 0;
          startBracketFound = false;
        }
      }

      if (strBuff.toString().endsWith("#$"))
        strBuff.delete(strBuff.length() - 2, strBuff.length());

      return strBuff.toString().split("\\#\\$");
    }
    catch (Exception ex)
    {
      Log.errorLog("DerivedData", "getTokens", "", "", "Exception : " + expression);
      return null;
    }
  }

  /**
   * This function is used when VectorName have dot(.) then getting problem to convert string to string array(Like 7.2.T.O).
   * 
   * @param inputString
   * @return
   */
  public String[] getStringArray(String inputString)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getStringArray", "", "", "Method Called. inputString = " + inputString);

      int count = 0;
      String grpId = "";
      String graphId = "";
      String vectName = "";

      for (int i = 0; i < inputString.length(); i++)
      {
        if (("" + inputString.charAt(i)).equals("."))
        {
          if (count == 0)
          {
            grpId = inputString.substring(0, i);
            inputString = inputString.replaceFirst(grpId + ".", "");
            count++;
            i = 0;
          }
          else if (count == 1)
          {
            graphId = inputString.substring(0, i);
            inputString = inputString.replaceFirst(graphId + ".", "");
            count++;
            i = 0;
          }
        }

        if (count == 2)
        {
          vectName = inputString;
        }
      }

      String[] arrGrpIdGraphIdVectName = new String[3];
      arrGrpIdGraphIdVectName[0] = grpId;
      arrGrpIdGraphIdVectName[1] = graphId;
      arrGrpIdGraphIdVectName[2] = vectName;
      return arrGrpIdGraphIdVectName;
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getStringArray", "", "", "Exception - " + ex);
      return null;
    }
  }

  public static void main(String[] args)
  {

  }

}
