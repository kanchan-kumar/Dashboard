package pac1.Bean;

/**----------------------------------------------------------------------------
 * Name       MsgDataForDerived.java
 * Purpose    processing of message data for Derived Graphs. It generate
 *             - minData
 *             - maxData
 *             - avgData
 *             - std-Dev
 *             - Last
 *             - Samples
 *Modification History: 
 *                     24/12/2012 Add Report Name attribute
 * @author    Ravi Kant Sharma
 *******************************************************************************/
public class MsgDataForDerived
{
  // Static members
  final static private String className = "MsgDataForDerived";
  
   public double maxData = 0; // used for max value of graph
   public double minData = 0; // used for min value of graph
   public double avgData = 0; // used for average data value of graph 
   public double stdDev = 0; // used to calculate Std-Dev in graph data
   public double graphDataInLastSample = 0; // used data in last sample
   public long totalSample = 0;  // used for total samples in graph
   double[] arrDerivedGraphData = null;
   String reportName = "";
   public MsgDataForDerived()
   {
     
   }
   public MsgDataForDerived(String reportName, double[] arrDerivedGraphData , TestRunData testRunData)
   {
     try
     {
       Log.debugLog(className, "MsgDataForDerived", "", "", "Method Called.");
       if(arrDerivedGraphData == null || arrDerivedGraphData.length == 0)
       {
         Log.errorLog(className, "MsgDataForDerived", "", "", "arrDerivedGraphData =  " + arrDerivedGraphData);
         return;
       }
       
       setReportName(reportName);
       this.arrDerivedGraphData = arrDerivedGraphData;
       minData = getMinData();
       maxData = getMaxData();
       avgData = getAvgData();
       stdDev = getStdDev();
       totalSample = getTotalSample(testRunData);
       graphDataInLastSample = getGraphDataInLastSample();
       
       // setting all req fields
       setMinData(minData);
       setMaxData(maxData);
       setAvgData(avgData);
       setStdDev(stdDev);
       setTotalSample(totalSample);
       setGraphDataInLastSample(graphDataInLastSample);
     }
     catch(Exception ex)
     {
       Log.stackTraceLog(className, "MsgDataForDerived", "", "", "Exception - " , ex);
     }
   }

  public double getMaxData()
  {
    if(arrDerivedGraphData != null)
    {
      maxData = arrDerivedGraphData[0];   
      for (int i=1; i<arrDerivedGraphData.length; i++) 
      {
        if (arrDerivedGraphData[i] > maxData) 
        {
          maxData = arrDerivedGraphData[i];
        }
      }
      
      maxData = convTo3DigitDecimal(maxData);
    }
    return maxData;

  }

  public void setMaxData(double maxData)
  {
    this.maxData = maxData;
  }

  public double getMinData()
  {
    if(arrDerivedGraphData != null)
    {
      minData = arrDerivedGraphData[0];
      for(int i=1; i<arrDerivedGraphData.length; i++)
      {
        if(arrDerivedGraphData[i] < minData)
        {
          minData = arrDerivedGraphData[i];
        }
      }
    
      minData = convTo3DigitDecimal(minData);
    }
    
    return minData;
  }

  public void setMinData(double minData)
  {
    this.minData = minData;
  }

  public double getAvgData()
  {
    if(arrDerivedGraphData != null)
    {
      double sum = 0;
      for(int i=0; i < arrDerivedGraphData.length ; i++)
      {
        sum = sum + arrDerivedGraphData[i];
      }
      
      avgData = sum / arrDerivedGraphData.length;
      avgData = convTo3DigitDecimal(avgData);
    }
    return avgData;
  }

  public void setAvgData(double avgData)
  {
    this.avgData = avgData;
  }

  public double getStdDev()
  {
    if(arrDerivedGraphData != null)
    {
      double d1 = 0, d2 = 0;
      for(int i = 0; i < arrDerivedGraphData.length; i++)
      {
        d2 = (avgData - arrDerivedGraphData[i])*(avgData - arrDerivedGraphData[i]);
        d1 = d2 + d1;
      }
    
      stdDev = Math.sqrt((d1/(arrDerivedGraphData.length-1)));
      stdDev = convTo3DigitDecimal(stdDev);
    }
    return stdDev;
  }


  public void setStdDev(double stdDev)
  {
    this.stdDev = stdDev;
  }

  public double getGraphDataInLastSample()
  {
    if(arrDerivedGraphData != null)
      graphDataInLastSample = convTo3DigitDecimal(arrDerivedGraphData[arrDerivedGraphData.length - 1]);
    
    return graphDataInLastSample;
  }

  public void setGraphDataInLastSample(double graphDataInLastSample)
  {
    this.graphDataInLastSample = graphDataInLastSample;
  }

  public long getTotalSample(TestRunData testRunData)
  {
    totalSample = testRunData.getDefaultTimeBasedTestRunData().seqNum;
    return totalSample;
  }

  public void setTotalSample(long totalSample)
  {
    this.totalSample = totalSample;
  }

  public static double convTo3DigitDecimal(double val)
  {
    double dblVal;
    dblVal = (double )Math.round(val * 1000);
    dblVal = dblVal/1000;
    return (dblVal);
  }
  public String getReportName()
  {
    return reportName;
  }
  public void setReportName(String reportName)
  {
    this.reportName = reportName;
  }
}
