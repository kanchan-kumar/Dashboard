/**
 * This class is for merging Time Based Test Run Data
 *
 * @author Ravi Sharma/Neeraj Jain
 * @since Netsorm Version 3.9.2
 * @Modification_History Ravi Kant Sharma - Initial Version 3.9.2 (dated 26/11/2013)
 * @version 3.9.2
 * @Purpose: This class is for merging two time based test run data (TBTRD) objects. Merge will add graph samples data of requested TBTRD to current TBTRD
 *  This is used when auto active is ON and graph(s) which are not active yet are used by user using Drag, Open All, Merge actions.
 *  In this case, GUI will get data for non active graphs using servlet communication. Data comes in TBTRD object.
 *  Then GUI will merge this in the current TBTRD and show data in the graphs.
 *  Merge will merge following:
 *    - Graphs samples
      - Array of active graphs
      - Graph Number to index
      - Last samples sum which is not yet averaged
      - MinData samples
      - Maxdata samples
      - CountData samples
      - SumSqrData samples
 *
 * In case of online mode, data pkts come and get added in current TBTRD. So if graphs are activated, it is possible that these graphs may have 
 * same or more or less data samples compared to current TBTRD. So merge need to handled these cases
 * For this, we need to compare following members of TBTRD
 *   - dataItemCount - Number of samples
 *   - avgCount - Number of pkts to be used for averaging (1, 2, 4, 8, ..)
 *   - avgSampleCount - Number of pkts received but not yet averaged (< avgCount)
 *   ( - avgCounter - Number of pkts received including lost pkts but not yet averaged (< avgCount) ) 
 *
 * If all three members of both TBTRD are same, then this is simple case and merging is simple
 *
 * If last samples are not same, then these need to be adjusted in requested TBTRD. There two cases:
 *    Case1 - Requested TBTRD has less (avgSampleCount is less)
 *    Case2 - Requested TBTRD has more (avgSampleCount is more)
 *       Last sample value is adjusted as follows:
 *              If avgSampleCount of requested TBTRD is NOT 0,
 *                adjusted value = (value / avgSampleCount of requested TBTRD) / (avgSampleCount of current TBTRD)
 *              else
 *                adjusted value = (last value from samples array) * (avgSampleCount of current TBTRD)
 *  Also lastCountData and lastSumSqrData are adjusted and lastMinData and lastMaxData are repeated.


 * If avgCount is not same, then we log warning and ignore for now

 * If dataItemCount is not same, then we need to handle case when requested have less by copying last value from samples array,lastMinData array,lastMaxData array,lastCountData array and lastSumSqrData array.
 * If requested have more, then extra samples are ignored.
 *
 * Who uses this class:
 *  This is used by Dashbaord client ONLY as of Nov 27, 2013
 *
 */

package pac1.Bean;

public class MergeTimeBasedTestRunData
{
  private static String className = "MergeTimeBasedTestRunData";

  /*
   * this method will check If all three members of both TBTRD are same, then in this is simple case it will simply merge If last samples are not same, then these need to be adjusted in requested
   * TBTRD.
   * 
   * @param debugLevel
   * 
   * @param reqestedTimeBased
   * 
   * @param currentTimeBased
   */

  public static void getCombinedTBTRD(int debugLevel, TimeBasedTestRunData reqTBTRD, TimeBasedTestRunData curTBTRD)
  {
    try
    {

      if (debugLevel > 0)
      {
        Log.debugLogAlways(className, "getCombinedTBTRD", "", "", "Method Called.");
      }
      if (reqTBTRD == null || curTBTRD == null)
      {
        Log.errorLog(className, "getCombinedTBTRD", "", "", "reqTBTRD is " + reqTBTRD + ", curTBTRD = " + curTBTRD + " so returning current timebased test run data.");
        return;
      }

      if (debugLevel > 0)
        logTBTRD(debugLevel, reqTBTRD, curTBTRD);

      int reqDataItemCount = reqTBTRD.getDataItemCount();
      int reqAvgCount = reqTBTRD.getAvgCount();
      int reqAvgSampleCount = reqTBTRD.getAvgSampleCount();

      int currDataItemCount = curTBTRD.getDataItemCount();
      int currAvgCount = curTBTRD.getAvgCount();
      int currAvgSampleCount = curTBTRD.getAvgSampleCount();

      // Case 1 - Most simple case when are are same
      if (reqDataItemCount == currDataItemCount && reqAvgCount == currAvgCount && reqAvgSampleCount == currAvgSampleCount)
      {
        merge(reqTBTRD, curTBTRD);
        return;
      }

      if (reqDataItemCount != currDataItemCount)
      {
        // Case 2 - Item less
        if (reqDataItemCount < currDataItemCount)
        {
          Log.debugLogAlways(className, "getCombinedTBTRD", "", "", "Warning: requestedTimeBasedTRD data item count is less than currentTimeBasedTRD. Last sample will be repeated");
          logTBTRD(debugLevel, reqTBTRD, curTBTRD); // Must be called before adjusting
          adjustSamplesForLess(debugLevel, reqTBTRD, curTBTRD);
        }
        else
        {
          // Case 3 - Item more
          // In this case, extra samples in requested are ignored
          Log.debugLogAlways(className, "getCombinedTBTRD", "", "", "Warning: requestedTimeBasedTRD data item count is greater than currentTimeBasedTRD. Extra samples will be ignored");
          logTBTRD(debugLevel, reqTBTRD, curTBTRD);
        }
      }

      chkAvgCount(debugLevel, reqTBTRD, curTBTRD);

      merge(reqTBTRD, curTBTRD);

    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getCombinedTBTRD", "", "", "Exception - ", ex);
    }
  }

  // Made it static because it is also called from static method getCombinedTBTRD
  private static void logTBTRD(int debugLevel, TimeBasedTestRunData reqTBTRD, TimeBasedTestRunData curTBTRD)
  {
    Log.debugLogAlways(className, "logTBTRD", "", "", "curTBTRD : ");
    curTBTRD.logInfo();

    Log.debugLogAlways(className, "logTBTRD", "", "", "reqTBTRD : ");
    reqTBTRD.logInfo();

    if (debugLevel > 3)
    {
      Log.debugLogAlways(className, "logTBTRD", "", "", "curTBTRD logGraphSamples : ");
      curTBTRD.logGraphSamples();
      Log.debugLogAlways(className, "logTBTRD", "", "", "reqTBTRD logGraphSamples : ");
      reqTBTRD.logGraphSamples();
    }
  }

  /**
   * This method checks reqTBTRD avgCount and curTBTRD avgCount and generates warning msgs accordingly
   * 
   * @param debugLevel
   * @param reqestedTimeBased
   * @param currentTimeBased
   */
  private static void chkAvgCount(int debugLevel, TimeBasedTestRunData reqTBTRD, TimeBasedTestRunData curTBTRD)
  {
    int reqAvgCount = reqTBTRD.getAvgCount();
    int curAvgCount = curTBTRD.getAvgCount();

    if (debugLevel > 0)
      Log.debugLogAlways(className, "chkAvgCount", "", "", "Method Called. reqAvgCount = " + reqAvgCount + ", curAvgCount = " + curAvgCount);

    if (reqAvgCount < curAvgCount)
    {
      // Case 2.1
      Log.debugLogAlways(className, "chkAvgCount", "", "", "Warning: reqestedTimeBased avgCount (" + reqAvgCount + ") is less than currentTimebased avgCount (" + curAvgCount + "). Ignored");
    }
    else if (reqAvgCount > curAvgCount)
    {
      // Case 3.1
      Log.debugLogAlways(className, "chkAvgCount", "", "", "Warning: reqestedTimeBased avgCount (" + reqAvgCount + ") is greater than currentTimebased avgCount (" + curAvgCount + "). Ignored");
    }

    chkAvgSampleCount(debugLevel, reqTBTRD, curTBTRD);
  }

  /**
   * This method checks the avgSampleCount for both reqTBTRD and curTBTRD
   * 
   * @param debugLevel
   * @param reqestedTimeBased
   * @param currentTimeBased
   */
  private static void chkAvgSampleCount(int debugLevel, TimeBasedTestRunData reqTBTRD, TimeBasedTestRunData curTBTRD)
  {
    int reqAvgSampleCount = reqTBTRD.getAvgSampleCount();
    int curAvgSampleCount = curTBTRD.getAvgSampleCount();

    if (debugLevel > 0)
      Log.debugLogAlways(className, "chkAvgSampleCount", "", "", "Method Called. reqAvgSampleCount = " + reqAvgSampleCount + ", curAvgSampleCount = " + curAvgSampleCount);

    if (reqAvgSampleCount < curAvgSampleCount)
    {
      // Case 2.1.1
      Log.debugLogAlways(className, "chkAvgSampleCount", "", "", "Warning: reqestedTimeBased avgSampleCount (" + reqAvgSampleCount + ") is less than currentTimebased avgSampleCount (" + curAvgSampleCount + ").");
      adjustLastSamples(debugLevel, reqTBTRD, curTBTRD);
    }
    else if (reqAvgSampleCount > curAvgSampleCount)
    {
      // Case 2.1.2
      Log.debugLogAlways(className, "chkAvgSampleCount", "", "", "Warning: reqestedTimeBased avgSampleCount (" + reqAvgSampleCount + ") is greater than currentTimebased avgSampleCount (" + curAvgSampleCount + ").");
      adjustLastSamples(debugLevel, reqTBTRD, curTBTRD);
    }
    else
    // same
    {
      // Case 2.1.3
      if (debugLevel > 0)
        Log.debugLog(className, "chkAvgSampleCount", "", "", "reqestedTimeBased avgSampleCount (" + reqAvgSampleCount + ") is same as currentTimebased avgSampleCount (" + curAvgSampleCount + ").");
    }
  }

  /**
   * This method will be called in both the cases when requested time based test run data has less or more samples than current time based
   * 
   * @param debugLevel
   * @param reqestedTimeBased
   * @param currentTimeBased
   */
  private static void adjustLastSamples(int debugLevel, TimeBasedTestRunData reqTBTRD, TimeBasedTestRunData curTBTRD)
  {
    try
    {
      int reqAvgSampleCount = reqTBTRD.getAvgSampleCount();
      int curAvgSampleCount = curTBTRD.getAvgSampleCount();
      double lastAdjustedSampleValue;
      int lastAdjustedCountValue;
      double lastAdjustedSumSqrvalue;

      if (debugLevel > 0)
      {
        Log.debugLogAlways(className, "adjustLastSamples", "", "", "Requested AvgSampleCount = " + reqAvgSampleCount + ", Current AvgSampleCount = " + curAvgSampleCount);
      }

      // Case 2.1.1.1 and 2.1.2.1
      // For all graphs in requested TBTRD
      for (int i = 0; i < reqTBTRD.getActiveGraphNum(); i++)
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO = reqTBTRD.getArrGraphUniqueKeyDTO()[i];
        TimeBasedDTO timeBasedDTO = reqTBTRD.getTimeBasedDTO(graphUniqueKeyDTO);

        // If reqAvgSampleCount is equal to zero than we take last from from samples array for adjustment
        if (reqAvgSampleCount == 0)
        {
          double lastSampleData = timeBasedDTO.getLastSampleData();
          int lastCountData = timeBasedDTO.getLastCountData();
          double lastSumSqrData = timeBasedDTO.getLastSumSqrData();

          // Case 2.1.1.1.1 or 2.1.2.1.1
          lastAdjustedSampleValue = lastSampleData * curAvgSampleCount;
          lastAdjustedCountValue = lastCountData * curAvgSampleCount;
          lastAdjustedSumSqrvalue = lastSumSqrData * curAvgSampleCount;
          if (debugLevel > 2)
            Log.debugLogAlways(className, "adjustLastSamples", "", "", "reqAvgSampleCount is 0. lastAdjustedSampleValue[" + i + "] = " + lastAdjustedSampleValue);
        }
        else
        {
          double lastAvgSampleData = timeBasedDTO.getLastAvgSampleData();
          int lastCountData = timeBasedDTO.getLastCountData();
          double lastSumSqrData = timeBasedDTO.getLastSumSqrData();

          // Case 2.1.1.1.2 or 2.1.2.1.2
          lastAdjustedSampleValue = (lastAvgSampleData * curAvgSampleCount) / reqAvgSampleCount;
          lastAdjustedCountValue = (lastCountData * curAvgSampleCount) / reqAvgSampleCount;
          lastAdjustedSumSqrvalue = (lastSumSqrData * curAvgSampleCount) / reqAvgSampleCount;
          if (debugLevel > 2)
            Log.debugLogAlways(className, "adjustLastSamples", "", "", "reqAvgSampleCount is not 0. lastAdjustedSampleValue[" + i + "] = " + lastAdjustedSampleValue);
        }

        timeBasedDTO.setLastAvgSampleData(lastAdjustedSampleValue);
        timeBasedDTO.setLastCountData(lastAdjustedCountValue);
        timeBasedDTO.setLastSumSqrData(lastAdjustedSumSqrvalue);

        reqTBTRD.setTimeBasedDTO(graphUniqueKeyDTO, timeBasedDTO);
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "adjustLastSamples", "", "", "Exception - ", ex);
    }
  }

  /**
   * This method will call when requested time based test run data has less samples than current time based
   * 
   * @param debugLevel
   * @param reqTBTRD
   * @param curTBTRD
   */

  // This is covered in Case2
  //
  public static void adjustSamplesForLess(int debugLevel, TimeBasedTestRunData reqTBTRD, TimeBasedTestRunData curTBTRD)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "adjustSamplesForLess", "", "", "Method Called.");

      // defined in TimeBasedTestRunData, it repeats the last sample.
      reqTBTRD.repeatSamples(curTBTRD.getDataItemCount() - reqTBTRD.getDataItemCount());
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "adjustSamplesForLess", "", "", "Exception - ", ex);
    }
  }

  /*
   * This method will merge data samples , Last Avg Samples , graph numbers If last samples are not same, then these need to be adjusted in requested TBTRD.
   * 
   * @param reqestedTimeBased
   * 
   * @param currentTimeBased
   */
  private static void merge(TimeBasedTestRunData reqTBTRD, TimeBasedTestRunData curTBTRD)
  {
    try
    {
      Log.debugLogAlways(className, "merge", "", "", "Method Called.");
      GraphUniqueKeyDTO[] reqArrGroupIdGraphIdVectorNameToActivate = reqTBTRD.getArrGraphUniqueKeyDTO();
      int n = reqArrGroupIdGraphIdVectorNameToActivate.length;
      for (int i = 0; i < n; i++)
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO = reqArrGroupIdGraphIdVectorNameToActivate[i];
        TimeBasedDTO reqTimeBasedDTO = reqTBTRD.getTimeBasedDTO(graphUniqueKeyDTO);
        curTBTRD.setTimeBasedDTO(graphUniqueKeyDTO, reqTimeBasedDTO);
      }

      /*Updating current Time based object.*/
      curTBTRD.activateGraphs(reqArrGroupIdGraphIdVectorNameToActivate);

      /*Here it combine Derived Graph Data if available.*/
      curTBTRD.getDerivedDataProcessor().mergeDerivedGraphs(reqTBTRD);

      Log.debugLogAlways(className, "merge", "", "", "Method End.");
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "merge", "", "", "Exception - ", ex);
    }
  }

  // For testing, use TestMergeTimeBasedTestRunData.java
}
