package pac1.Bean;

import java.util.ArrayList;

import pac1.Bean.GraphName.GraphNameUtils;

/**
 * This Class is used For doing utilities method of Reporting.
 */
public class ReportDataUtils 
{
  
  private static String className = "ReportDataUtils";
  
  /*Instance of Report Data.*/
  private ReportData rptData = null;
  
  /*double array containing averaged data.*/
  private ArrayList<Double> avgSampleDataList = new ArrayList<Double>();
  
  /*Time Stamp Array of averaged data.*/
  private ArrayList<Long> avgTimeStampList = new ArrayList<Long>();
  
  /*Count Data Array For Calculation.*/
  private ArrayList<Integer> avgCountData = new ArrayList<Integer>();
  
  /*Sum Square Data Array for Standard deviation calculation.*/
  private ArrayList<Double> avgSumSqrDataList = new ArrayList<Double>();
  
  /*Keeping the Max Data of Graph.*/
  private ArrayList<Double> avgMaxSampleData = new ArrayList<Double>();
  
  /*Keeping the Min Data of Graph.*/
  private ArrayList<Double> avgMinSampleData = new ArrayList<Double>();
  
  /*Keeping Time Based Object*/
  TimeBasedTestRunData timeBasedTestRunDataObj = null;
  
  /*Available Data Item.*/
  int sampleCount = 0;
  
  /*Report Chart Width*/
  int reportWidth = 900;
  
  /*Last Graph Sample Data Initially taken 0.*/
  double graphData = 0.0;
  
  /*Last Sum Square Data.*/
  double sumSqrData = 0.0;
  
  /*Last Time Stamp Data.*/
  long timeStamp = 0L;
  
  /*Last Count Data.*/
  int countData = 0;
  
  /*Last Max Data.*/
  double maxData = 0;
  
  /*Last Min Data.*/
  double minData = Double.MAX_VALUE;
  
  /*Counter for averaging sample count.*/
  int counter = 0;
  
  /*Average Count.*/
  int avgCount = 1;
  
  /*Debug Level*/
  int debugLevel = 0;
 
  public ReportDataUtils(ReportData rptData)
  {
    this.rptData = rptData;
  }
  
  /**
   * Method is used to calculate the parameters for averaging of sample data to show in reports and generate charts.
   * @param timeBasedDTOObj
   * @param timeBasedTestRunDataObj
   * @param graphType
   * @param granularity
   * @param xAxisTimeFormat
   * @param overrideRptOptions
   * @param timeOption
   * @param elapsedStartTime
   * @param elapsedEndTime
   * @param avgCount
   * @return
   */
  public boolean calculateParametersForAveraging(double []arrGraphData, double[]arrSumSqrData, int []arrCountData, double []arrMaxData, double []arrMinData, TimeBasedTestRunData timeBasedTestRunDataObj, String graphType, int graphDataType, String granularity, String xAxisTimeFormat, boolean overrideRptOptions, String timeOption, String elapsedStartTime, String elapsedEndTime, int avgCount)
  {
    try
    {
      Log.reportDebugLog(className, "calculateParametersForAveraging", "", "", "Method Called. graphType = " + graphType + ", granularity = " + granularity + ", xAxisTimeFormat = " + xAxisTimeFormat + ", overrideRptOptions = " + overrideRptOptions + ", timeOption = " + timeOption + ", elapsedStartTime = " + elapsedStartTime + ", elapsedEndTime = " + elapsedEndTime + ", avgCount = " + avgCount + ", graphDataType = " + graphDataType);
         
      if(arrGraphData == null || timeBasedTestRunDataObj == null)
      {
	Log.errorLog(className, "calculateParametersForAveraging", "", "", "Time Based Data must not be null.");
	return false;
      }
      
      /*Keeping Time Based Object.*/
      this.timeBasedTestRunDataObj = timeBasedTestRunDataObj;
      
      /*Time Option To Filter and averaging data.*/
      long timeRange[] = null;
      
      /*Test Run Start Time in Millisecond.*/
      long testStartTime = timeBasedTestRunDataObj.getTestRunDataObj().trStartTimeStamp;
      
      /*Getting Test Run Duration.*/
      long testRunDuration = ExecutionDateTime.convertFormattedTimeToMillisecond(timeBasedTestRunDataObj.getTestRunDataObj().testDuration, ":");
      
      /*calculate Average Count for averaging.*/
      int averageCount = -1;
         
      /*Taking Time Stamp Array From Time Based object.*/
      long []arrTimeStamp = timeBasedTestRunDataObj.getTimeStampArray();
            
      timeRange = new long[2];
      timeRange[0] = testStartTime;
      timeRange[1] = testStartTime + testRunDuration;
            
      /*Check override option.*/
      if(!overrideRptOptions)
      {
	if(!xAxisTimeFormat.equals("ElapsedCompare"))
	{
	  timeRange = calculateTimeRange(timeOption, elapsedStartTime, elapsedEndTime, testStartTime, testRunDuration);
	  
	  /*Filtering Graph Data according to user selected Time Range.*/
	  Object []arrFiltereGraphdData = getTimeRangeGraphData(arrGraphData, arrSumSqrData, arrCountData, arrMaxData, arrMinData, arrTimeStamp, timeRange[0], timeRange[1]);
	  
	  if(arrFiltereGraphdData == null)
	  {
	    Log.errorLog(className, "calculateParametersForAveraging", "", "", "Found Empty Filtered Data For Graph. Please check error logs for detail.");
	    return false;
	  }
	  
	  /*Copy Filtered Data back to original array.*/
	  arrGraphData = (double [])arrFiltereGraphdData[0];
	  arrSumSqrData = (double [])arrFiltereGraphdData[1];
	  arrCountData = (int [])arrFiltereGraphdData[2];
	  arrTimeStamp = (long [])arrFiltereGraphdData[3];
	  arrMaxData = (double [])arrFiltereGraphdData[4];
	  arrMinData = (double [])arrFiltereGraphdData[5];
	}	
      }

      /*Calculate Average Count for averaging.*/
      if(avgCount == -1)
        averageCount = calculateAverageCount(granularity, graphType, rptData.graphNames.getInterval());
      else
        averageCount = avgCount;
            
      /*Doing Averaging of Data Samples.*/
      genAverageSampleData(arrGraphData, arrSumSqrData, arrCountData, arrMaxData, arrMinData, arrTimeStamp, timeBasedTestRunDataObj.getDataItemCount(), averageCount, graphDataType);
     
      /*Calculating Report Chart Width.*/
      calculateReportWidth(graphType, (int)(timeRange[1] - timeRange[0]));
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
  /**
   * This will get array of data with selected start and end time stamp.
   * @param arrValues
   * @param rangeDateForArr
   * @return
   */
  public Object[] getTimeRangeGraphData(double arrSampleData[], double arrSumSqrData[], int []arrCountData, double []arrMaxData, double []arrMinData, long arrTimeStamp[], long startTimeStamp, long endTimeStamp)
  {
    try
    {    
      Log.reportDebugLog(className, "getTimeRangeGraphData", "", "", "Method called. startTimeStamp = " + startTimeStamp + ", endTimeStamp = " + endTimeStamp);
      
      ArrayList<Double> arrSampleDataList = new ArrayList<Double>();
      ArrayList<Integer> arrCountDataList = new ArrayList<Integer>();
      ArrayList<Long> arrTimeStampList = new ArrayList<Long>();
      ArrayList<Double> arrStdDevList = new ArrayList<Double>();
      ArrayList<Double> arrMaxDataList = new ArrayList<Double>();
      ArrayList<Double> arrMinDataList = new ArrayList<Double>();

      for(int i = 0; i < arrTimeStamp.length; i++)
      {
	/*Search for Time stamp of selected range.*/
	if(arrTimeStamp[i] >= startTimeStamp && arrTimeStamp[i] <= endTimeStamp)
	{
	  arrSampleDataList.add(arrSampleData[i]);
	  arrTimeStampList.add(arrTimeStamp[i]);
	  arrCountDataList.add(arrCountData[i]);
	  arrStdDevList.add(arrSumSqrData[i]);
	  arrMaxDataList.add(arrMaxData[i]);
	  arrMinDataList.add(arrMinData[i]);
	}
      }
      
      /*Creating object array to return the filtered arrays of graph.*/
      Object []arrGraphFilterData = new Object[6];
      
      arrSampleData = new double[arrSampleDataList.size()];
      arrTimeStamp = new long[arrSampleDataList.size()];
      arrCountData = new int[arrSampleDataList.size()];
      arrSumSqrData = new double[arrSampleDataList.size()];
      arrMaxData = new double[arrMaxDataList.size()];
      arrMinData = new double[arrMinDataList.size()];

      /*Copy From ArrayList to array.*/
      for(int i = 0; i < arrSampleDataList.size(); i++)
      {
	arrSampleData[i] = arrSampleDataList.get(i);
	arrTimeStamp[i] = arrTimeStampList.get(i);
	arrCountData[i] = arrCountDataList.get(i);
	arrSumSqrData[i] = arrSampleDataList.get(i);
	arrMaxData[i] = arrMaxDataList.get(i);
	arrMinData[i] = arrMinDataList.get(i);
      }

      /*Putting arrays to object array to return. Make sure the order of converting back remain same to prevent Run Time Exception.*/
      arrGraphFilterData[0] = arrSampleData;
      arrGraphFilterData[1] = arrSumSqrData;
      arrGraphFilterData[2] = arrCountData;
      arrGraphFilterData[3] = arrTimeStamp;
      arrGraphFilterData[4] = arrMaxData;
      arrGraphFilterData[5] = arrMinData;
      
      return arrGraphFilterData;

    }
    catch (Exception e)
    {
      //If Any Exception Occurs due to data
      Log.stackTraceLog(className, "getRangeDataWithSequenceOnly", "", "", "Exception in getting data - ", e);
      return null;
    }
  }
  
  /**
   * Method is used to average samples.
   * @param timeBasedDTOObj
   * @param timeBasedTestRunDataObj
   * @param averageCount
   */
  private void genAverageSampleData(double[] arrGraphData, double[] arrSumSqrData, int[] arrCountData, double[] arrMaxData, double []arrMinData, long[] arrTimeStamp, int dataItemCount, int averageCount, int graphDataType)
  {
    try
    {
      Log.reportDebugLog(className, "genAverageSampleData", "", "", "Method Called. Average Count = " + averageCount + ", Total Available Sample = " + dataItemCount + ", graphDataType = " + graphDataType);
      
      boolean isAutoAveraging = true;
                  
      /*Check if Granularity Applied.*/
      if(averageCount >= 1)
      {
	Log.reportDebugLog(className, "genAverageSampleData", "", "", "Granularity enabled on data. Apply sample averaging with value = " + averageCount);
	
	avgCount = averageCount;
	isAutoAveraging = false;
      }
              
      for(int i = 0; (i < dataItemCount && i < arrGraphData.length); i++)
      {
	graphData += (arrGraphData[i] * arrCountData[i]);	
	timeStamp += arrTimeStamp[i];
	countData += arrCountData[i];
	sumSqrData += arrSumSqrData[i];
	
	if(maxData < arrMaxData[i])
	  maxData = arrMaxData[i];
	
	if(minData > arrMinData[i])
	  minData = arrMinData[i];
	
	/*Increase the counter to track total number of samples.*/
	counter++;
	
	if(counter >= avgCount)
	{
	  if(isAutoAveraging && sampleCount >= rptData.maxPktsForAutoGranularity)
	  {
	    samplesAveraging(graphDataType);
	    if(counter >= avgCount)
	      genAvgSamples();
	  }
	  else
	  {
	    genAvgSamples();
	  }
	}	
      }     
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * Averaging of samples.
   * @param graphDataType
   */
  private void samplesAveraging(int graphDataType)
  {
    try
    {
      if(debugLevel > 1)
        Log.reportDebugLog(className, "samplesAveraging", "", "", "Average Count = " + avgCount + ", graphDataType = " + graphDataType);
      
      int k = 0;

      for(int j = 0; (j + 1) <= sampleCount; j = j + 2)
      {
	int totalCount = avgCountData.get(j) + avgCountData.get(j + 1);
	double avgSampleValue = 0;

	/*Here we check if Graph is of Time/TimesSTD type then it must calculated by this way.*/
	if(graphDataType == GraphNameUtils.DATA_TYPE_TIMES || graphDataType == GraphNameUtils.DATA_TYPE_TIMES_STD)
	{
	  if(totalCount > 0)
	    avgSampleValue = ((avgSampleDataList.get(j) * avgCountData.get(j)) + (avgSampleDataList.get(j+1) * avgCountData.get(j+1))) / totalCount;
	  else
	    avgSampleValue = 0;
	}
	else
	{
	  avgSampleValue = (avgSampleDataList.get(j) + avgSampleDataList.get(j+1))/2;
	}

	avgSampleDataList.set(k, avgSampleValue);
	avgCountData.set(k, totalCount);

	long avgTimeStamp = (avgTimeStampList.get(j) + avgTimeStampList.get(j+1))/2;
	avgTimeStampList.set(k, avgTimeStamp);

	double avgSumSqrData = avgSumSqrDataList.get(j) + avgSumSqrDataList.get(j+1);
	avgSumSqrDataList.set(k, avgSumSqrData);
	
	if(avgMaxSampleData.get(j) > avgMaxSampleData.get(j+1))
	  avgMaxSampleData.set(k, avgMaxSampleData.get(j));
	else
	  avgMaxSampleData.set(k, avgMaxSampleData.get(j+1));
	
	if(avgMinSampleData.get(j) < avgMinSampleData.get(j+1))
	  avgMinSampleData.set(k, avgMinSampleData.get(j));
	else
	  avgMinSampleData.set(k, avgMinSampleData.get(j+1));

	k++;
      }

      /*Post Processing after averaging samples.*/
      sampleCount = sampleCount/2;
      avgCount = avgCount * 2;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * Generate Samples.
   */
  private void genAvgSamples()
  {
    try
    {
      if(debugLevel > 1)
        Log.reportDebugLog(className, "genAvgSamples", "", "", "Method Called. sampleCount = " + sampleCount);
      
      /*Putting Data into ArrayList.*/
      if(avgSampleDataList.size() > sampleCount)
      {
	if(countData <= 0)
	  avgSampleDataList.set(sampleCount, graphData);
	else
	  avgSampleDataList.set(sampleCount, (graphData / countData));
	
	avgCountData.set(sampleCount, countData);
	avgSumSqrDataList.set(sampleCount, sumSqrData);
	avgMaxSampleData.set(sampleCount, maxData);
	avgMinSampleData.set(sampleCount, minData);
	avgTimeStampList.set(sampleCount, (timeStamp/counter));
      }
      else
      {
	if(countData <= 0)
	  avgSampleDataList.add(sampleCount, graphData);
	else
	  avgSampleDataList.add(sampleCount, (graphData / countData));
	
	avgCountData.add(sampleCount, countData);
	avgSumSqrDataList.add(sampleCount, sumSqrData);
	avgMaxSampleData.add(sampleCount, maxData);
	avgMinSampleData.add(sampleCount, minData);
	avgTimeStampList.add(sampleCount, (timeStamp/counter));
      }

      sampleCount++;

      /*Resetting the variables.*/
      counter = 0;
      graphData = 0.0;
      timeStamp = 0L;
      countData = 0;
      sumSqrData = 0.0;
      maxData = 0.0;
      minData = Double.MAX_VALUE;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
 
  /**
   * Method is used to calculate Average Count for averaging and applying Granularity.
   * @param granularity
   * @param graphType
   * @param interval
   * @return
   */
  private int calculateAverageCount(String granularity, String graphType, int interval)
  {
    try
    {
      Log.reportDebugLog(className, "calculateAverageCount", "", "", "Method called. Granularity is  " + granularity + ", Graph Type is " + graphType + ", interval = " + interval);
     
      /*0  means auto averaging*/
      int averageCount = 0;

      /*Granularity is only for Normal graphs*/
      if(graphType.equals("Normal"))
      {
	/*This is Auto granularity*/
        if(Integer.parseInt(granularity) <= 0)
          averageCount = 0;
        else
        {
          int tmpGranularity = Integer.parseInt(granularity.trim());          
          averageCount = tmpGranularity / (int) interval;
        }
      }
      
      return averageCount;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "calculateAverageCount", "", "", "Error while calculating average count - " + e);
      return 0;
    }
  }
  
  /**
   * Method is used to calculate the time Range in Millisecond for User Specified Time/Phase.
   * @param time
   * @param strStartTime
   * @param strEndTime
   * @return
   */
  private long[] calculateTimeRange(String time, String strStartTime, String strEndTime, long testRunDateInMillies, long testRunDuartion)
  {
    try
    {
      Log.reportDebugLog(className, "calculateTimeRange", "", "", "Method Called. time = " + time + ", strStartTime = " + strStartTime + ", strEndTime = " + strEndTime + ", testRunDateInMillies = " + testRunDateInMillies + ", testRunDuration = " + testRunDuartion);
      
      /*Initialize long array to store Time.*/
      long arrTimeOption[] = new long[2];
      
      /*In Case of Continuous Mode it is not possible to deal with elapsed time, so in this case time in Absolute Millisecond.*/
      if(rptData.isnDE_Continuous_Mode())
      {
	if(ExecutionDateTime.isValidTimeFormatInHHMMSS(strStartTime))
	{
	  arrTimeOption[0] = testRunDateInMillies + ExecutionDateTime.convertFormattedTimeToMillisecond(strStartTime, ":");
	  arrTimeOption[1] = testRunDateInMillies + ExecutionDateTime.convertFormattedTimeToMillisecond(strEndTime, ":");
	}
	else
	{
	  arrTimeOption[0] = Long.parseLong(strStartTime);
	  arrTimeOption[1] = Long.parseLong(strEndTime);
	}
      }
      else
      {
	/*It not Continuous Case it is must that time should be in HH:mm:ss format.*/
	if(time.equals("Total Run") || (time.equals("Run Phase Only") && rptData.phaseTimes == null))
	{	  
	  Log.reportDebugLog(className, "calculateTimeRange", "", "", "Taking Whole Scenario Data. testRunDuartion = " + ExecutionDateTime.convertTimeToFormattedString(testRunDuartion, ":"));
	  
	  arrTimeOption[0] = testRunDateInMillies; /*Start Time Stamp.*/
	  arrTimeOption[1] = testRunDateInMillies + testRunDuartion;
	}
	/*Exclude RampUp, WarmUp and RampDown packets.*/
	else if(time.equals("Run Phase Only"))
        {
	  Log.reportDebugLog(className, "calculateTimeRange", "", "", "Taking Run Phase Data. Run Phase Start Time = " + ExecutionDateTime.convertTimeToFormattedString((long)rptData.phaseTimes[1], ":") + ", End Time = " + ExecutionDateTime.convertTimeToFormattedString((long)rptData.phaseTimes[2], ":"));
	  
          /*phaseTimes[1] includes RampUp and WarmUp time*/
          arrTimeOption[0] = (long) rptData.phaseTimes[1] + testRunDateInMillies;
          
          /*phaseTimes[2] includes RampUp, WarmUp and Run time*/
          arrTimeOption[1] = (long) rptData.phaseTimes[2] + testRunDateInMillies;
        }
	else
	{
	  Log.reportDebugLog(className, "calculateTimeRange", "", "", "Taking User Specified Time. Start Time = " + strStartTime + ", End Time = " + strEndTime);
	  
	  if(strStartTime.trim().equals("NA"))
	  {
	    Log.reportDebugLog(className, "calculateTimeRange", "", "", "Start Time Found NA. Taking Test Start Time.");
	    arrTimeOption[0] = testRunDateInMillies;
	  }
	  else
	  {
	    arrTimeOption[0] = ExecutionDateTime.convertFormattedTimeToMillisecond(strStartTime, ":") + testRunDateInMillies;
	  }
	  
	  if(strEndTime.trim().equals("NA"))
	  {	    
	    Log.reportDebugLog(className, "calculateTimeRange", "", "", "End Time Found NA. Taking Test End Time. testRunDuartion = "+ testRunDuartion);
	    arrTimeOption[1] = testRunDateInMillies + testRunDuartion;
	  }
	  else
	  {
	    arrTimeOption[1] = ExecutionDateTime.convertFormattedTimeToMillisecond(strEndTime, ":") + testRunDateInMillies;
	  }
	}
      }
      
      return arrTimeOption;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Method used to calculate Report Chart Width.
   * @param graphType
   */
  public void calculateReportWidth(String graphType, int timeStampDiff)
  {
    if(graphType.equals("Normal"))
      reportWidth = rptData.normalGraphWidth;
    else
    {
      reportWidth = 300 + (timeStampDiff/rptData.graphNames.getInterval());
      
      /*Check For Maximum width to prevent memory errors.*/
      if(reportWidth > rptData.maxLongGraphWidth)
	reportWidth = rptData.maxLongGraphWidth;
    }
  }
  
  /**
   * Calculate Average Count for Correlated Graph.
   * @param graphType
   * @param granularity
   * @return
   */
  public int calculateAverageCountForCorrelatedGraph(String graphType, String granularity)
  {
    Log.reportDebugLog(className, "calculateAverageCountForCorrelatedGraph", "", "", "Method Called. graphType = " + graphType + ", granularity = " + granularity);
    
    try
    {
      calculateReportWidth(graphType, (int)(avgTimeStampList.get(sampleCount - 1) - avgTimeStampList.get(0)));
      return calculateAverageCount(granularity, graphType, rptData.graphNames.getInterval());
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0;
    }
  }
  
  /**
   * Method Returns the Average Value of Graph Data.
   * @return
   */
  public double getGraphAveragedValue()
  {
    try
    {
      Log.reportDebugLog(className, "getGraphAveragedValue", "", "", "Method Called.");
         
      /*This object is used to perform some math operation using utilities method.*/
      TimeBasedDataUtils timeBasedDataUtils = new TimeBasedDataUtils();

      double sum = timeBasedDataUtils.getAvgSum(getAverageSampleArray(), getAverageCountArray(), sampleCount);
      int totalCount = timeBasedDataUtils.getSumOfArrayElements(getAverageCountArray(), sampleCount);
            
      totalCount += countData;
      sum += graphData;
      
      return sum/totalCount;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0.0;
    }
  }
  
  /**
   * Method Returns the Average Maximum value of Graph Data.
   * @return
   */
  public double getGraphMaxValue()
  {
    try
    {
      Log.reportDebugLog(className, "getGraphMaxValue", "", "", "Method Called.");
         
      /*This object is used to perform some math operation using utilities method.*/
      TimeBasedDataUtils timeBasedDataUtils = new TimeBasedDataUtils();

      /*Getting the max sample.*/
      double max = timeBasedDataUtils.getMaxValueFromArray(getAverageMaxSampleArray(), sampleCount);

      /*check for last sample.*/
      if(max < maxData)
	max = maxData;
      
      return max;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0.0;
    }
  }
  
  /**
   * Method Returns the Average Value of Derived Graph Data.
   * @return
   */
  public double getDerivedGraphAverageValue(double []arrDerivedData, int []arrDerivedCountData, int sampleCount)
  {
    try
    {
      Log.reportDebugLog(className, "getDerivedGraphAverageValue", "", "", "Method Called. sampleCount = " + sampleCount);
      
      /*This object is used to perform some math operation using utilities method.*/
      TimeBasedDataUtils timeBasedDataUtils = new TimeBasedDataUtils();
      int count = timeBasedDataUtils.getSumOfArrayElements(arrDerivedCountData, sampleCount);
      double sum = timeBasedDataUtils.getAvgSum(arrDerivedData, arrDerivedCountData, sampleCount);                
      return sum/count;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0.0;
    }
  }
  
  /**
   * Method Returns the Average Maximum value of Derived Graph Data.
   * @return
   */
  public double getDerivedGraphMinValue(double []arrDerivedData, int sampleCount)
  {
    try
    {
      Log.reportDebugLog(className, "getDerivedGraphMinValue", "", "", "Method Called. sampleCount = " + sampleCount);
         
      /*This object is used to perform some math operation using utilities method.*/
      TimeBasedDataUtils timeBasedDataUtils = new TimeBasedDataUtils();

      /*Returning the max sample.*/
      return timeBasedDataUtils.getMinValueFromArray(arrDerivedData, sampleCount);
      
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0.0;
    }
  }
  
  /**
   * Method Returns the Average Maximum value of Derived Graph Data.
   * @return
   */
  public double getDerivedGraphCountValue(double []arrDerivedData, int sampleCount)
  {
    try
    {
      Log.reportDebugLog(className, "getDerivedGraphCountValue", "", "", "Method Called. sampleCount = " + sampleCount);
         
      /*This object is used to perform some math operation using utilities method.*/
      TimeBasedDataUtils timeBasedDataUtils = new TimeBasedDataUtils();

      /*Returning the max sample.*/
      return timeBasedDataUtils.getSumOfDoubleArrayElements(arrDerivedData, sampleCount);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0.0;
    }
  }
  
  /**
   * Method Returns the Average Maximum value of Derived Graph Data.
   * @return
   */
  public double getDerivedGraphSumCountValue(double []arrDerivedData, int sampleCount)
  {
    try
    {
      Log.reportDebugLog(className, "getDerivedGraphSumCountValue", "", "", "Method Called. sampleCount = " + sampleCount);
         
      /*Returning the max sample.*/
      return arrDerivedData[sampleCount - 1];
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0.0;
    }
  }
  
  /**
   * Method Returns the Average Value of Derived Graph Data.
   * @return
   */
  public double getDerivedGraphAverageValue()
  {
    try
    {
      Log.reportDebugLog(className, "getDerivedGraphAverageValue", "", "", "Method Called.");
      
      /*This object is used to perform some math operation using utilities method.*/
      TimeBasedDataUtils timeBasedDataUtils = new TimeBasedDataUtils();
      double sum = timeBasedDataUtils.getSumOfDoubleArrayElements(getAverageSampleArray(), sampleCount);                
      return sum/sampleCount;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0.0;
    }
  }
  
  /**
   * Method Returns the Average Maximum value of Derived Graph Data.
   * @return
   */
  public double getDerivedGraphMaxValue(double []arrDerivedData, int sampleCount)
  {
    try
    {
      Log.reportDebugLog(className, "getDerivedGraphMaxValue", "", "", "Method Called. sampleCount = " + sampleCount);
         
      /*This object is used to perform some math operation using utilities method.*/
      TimeBasedDataUtils timeBasedDataUtils = new TimeBasedDataUtils();

      /*Returning the max sample.*/
      return timeBasedDataUtils.getMaxValueFromArray(arrDerivedData, sampleCount);
      
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0.0;
    }
  }
  
  /**
   * Method Returns the Standard Deviation Value of Graph Data.
   * @return
   */
  public double getGraphStandardDeviationValue(int graphDataType)
  {
    try
    {
      Log.reportDebugLog(className, "getGraphStandardDeviationValue", "", "", "Method Called. graphDataType = " + graphDataType);
      
      /*This object is used to perform some math operation using utilities method.*/
      TimeBasedDataUtils timeBasedDataUtils = new TimeBasedDataUtils();

      double sum = timeBasedDataUtils.getAvgSum(getAverageSampleArray(), getAverageCountArray(), sampleCount);
      int totalCount = timeBasedDataUtils.getSumOfArrayElements(getAverageCountArray(), sampleCount);
      double totalSumSqr = timeBasedDataUtils.getSumOfDoubleArrayElements(getAverageSumSqrArray(), sampleCount);
      
      totalCount += countData;
      sum += graphData;
      totalSumSqr += sumSqrData;
      
      if(graphDataType == GraphNameUtils.DATA_TYPE_TIMES_STD)
        totalSumSqr = totalSumSqr / 1000;

      double average = sum/totalCount;
      
      return  Math.sqrt((totalSumSqr - (sum * average)) / totalCount - 1);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0.0;
    }
  }
  
  /**
   * Return the Sample Count on Averaged Array.
   * @return
   */
  public int getSampleCount() 
  {
    return sampleCount;
  }

  /**
   * Return the Report Width of Graph.
   * @return
   */
  public int getReportWidth() 
  {
    return reportWidth;
  }
  
  /**
   * Return Averaged Sample Data Array.
   * @return
   */
  public double[] getAverageSampleArray()
  {
    double [] arrSampleData = new double[avgSampleDataList.size()];
    
    for(int i = 0; i < arrSampleData.length; i++)
      arrSampleData[i] = avgSampleDataList.get(i);
    
    return arrSampleData;
  }
  
  /**
   * Return Averaged Max Sample Data Array.
   * @return
   */
  public double[] getAverageMaxSampleArray()
  {
    double [] arrMaxSampleData = new double[avgMaxSampleData.size()];
    
    for(int i = 0; i < arrMaxSampleData.length; i++)
      arrMaxSampleData[i] = avgMaxSampleData.get(i);
    
    return arrMaxSampleData;
  }
  
  /**
   * Return Averaged Max Sample Data Array.
   * @return
   */
  public double[] getAverageMinSampleArray()
  {
    double [] arrMinSampleData = new double[avgMinSampleData.size()];
    
    for(int i = 0; i < arrMinSampleData.length; i++)
      arrMinSampleData[i] = avgMinSampleData.get(i);
    
    return arrMinSampleData;
  }
  
  /**
   * Return Averaged Sum Square Data Array.
   * @return
   */
  public double[] getAverageSumSqrArray()
  {
    double [] arrSumSqrSampleData = new double[avgSumSqrDataList.size()];
    
    for(int i = 0; i < arrSumSqrSampleData.length; i++)
      arrSumSqrSampleData[i] = avgSumSqrDataList.get(i);
    
    return arrSumSqrSampleData;
  }
  
  /**
   * Return Averaged Count Data Array.
   * @return
   */
  public int[] getAverageCountArray()
  {
    int [] arrCountData = new int[avgCountData.size()];
    
    for(int i = 0; i < arrCountData.length; i++)
      arrCountData[i] = avgCountData.get(i);
    
    return arrCountData;
  }
  
  public void setSampleDataArray(ArrayList<Double> avgSampleDataList)
  {
    this.avgSampleDataList = avgSampleDataList;
  }
  
  /**
   * Getting Time Stamp ArrayList.
   * @return
   */
  public ArrayList<Long> getAvgTimeStampList() 
  {
    return avgTimeStampList;
  }

  /**
   * Setting Time Stamp ArrayList.
   * @param avgTimeStampList
   */
  public void setAvgTimeStampList(ArrayList<Long> avgTimeStampList) 
  {
    this.avgTimeStampList = avgTimeStampList;
  }

  /**
   * Return Averaged Time Stamp Data Array.
   * @return
   */
  public long[] getAveragTimeStampArray()
  {
    long [] arrTimeStampData = new long[avgTimeStampList.size()];
    
    for(int i = 0; i < arrTimeStampData.length; i++)
      arrTimeStampData[i] = avgTimeStampList.get(i);
    
    return arrTimeStampData;
  }
 
  /**
   * This method return packet count for last sample(not averaged).
   * @return
   */
  public int getLastCountData()
  {
    return countData;
  }
  
  /**
   * This method return last min sample(not averaged).
   * @return
   */
  public double getMinLastData() 
  {
    return minData;
  }

  /**
   * This method return last max sample(not averaged).
   * @return
   */
  public double getMaxLastData() 
  {
    return maxData;
  }

  public static void main(String[] args) 
  {
    //TODO Auto-generated method stub
    ReportDataUtils reportDataUtils = new ReportDataUtils(new ReportData(3661));
    
    int sampleSize = 239;
    double []arrGraphData = new double[sampleSize];
    long []arrTimeStamp = new long[sampleSize];
    int []arrCountData = new int[sampleSize];
    double []sumSqrData = new double[sampleSize];
    
    for(int i = 0; i < arrGraphData.length; i++)
    {
      arrGraphData[i] = 0.23451 * i - 0.11 * i;
      arrTimeStamp[i] = System.currentTimeMillis() + i*10;
      arrCountData[i] = 1;
      sumSqrData[i] = arrGraphData[i] * arrGraphData[i];
      
      System.out.println("Sample = " + arrGraphData[i] + ", Time = " + arrTimeStamp[i] + ", Count = " + arrCountData[i]);
    }
    
    reportDataUtils.genAverageSampleData(arrGraphData, sumSqrData, arrCountData, arrGraphData, arrGraphData,arrTimeStamp, sampleSize, 1, 0);
    
    System.out.println("------------------------------------ After -------------------------------------------------------------------------\n\n");
    System.out.println("total sample = " + reportDataUtils.sampleCount);
    
    for(int k = 0; k < reportDataUtils.sampleCount; k++)
    {
      System.out.println("Sample = " + reportDataUtils.avgSampleDataList.get(k) + ", Time = " + reportDataUtils.avgTimeStampList.get(k) + ", Count = " + reportDataUtils.avgCountData.get(k));
    }    
  }

}
