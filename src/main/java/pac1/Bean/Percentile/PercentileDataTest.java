package pac1.Bean.Percentile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import pac1.Bean.ExecutionDateTime;
import pac1.Bean.GraphUniqueKeyDTO;
import pac1.Bean.Scenario;
import pac1.Bean.TestRunDataType;

public class PercentileDataTest
{
  private int groupId, graphId, testRunNumber;
  private String vectorName, startTime, endTime, phaseName, dataKey;
  private boolean isPartitionModeEnabled = true;
  private int debugLevel = 4;
  private ArrayList<String> derivedExpList = null;
  private boolean testViewMode = false;
  private String uniqueKey = "CONTROLLER";

  public int getGroupId()
  {
    return groupId;
  }

  public void setGroupId(int groupId)
  {
    this.groupId = groupId;
  }

  public int getGraphId()
  {
    return graphId;
  }

  public void setGraphId(int graphId)
  {
    this.graphId = graphId;
  }

  public String getVectorName()
  {
    return vectorName;
  }

  public void setVectorName(String vectorName)
  {
    this.vectorName = vectorName;
  }

  public int getTestRunNumber()
  {
    return testRunNumber;
  }

  public void setTestRunNumber(int testRunNumber)
  {
    this.testRunNumber = testRunNumber;
  }

  public boolean isPartitionModeEnabled()
  {
    return isPartitionModeEnabled;
  }

  public void setPartitionModeEnabled(boolean isPartitionModeEnabled)
  {
    this.isPartitionModeEnabled = isPartitionModeEnabled;
  }

  public int getDebugLevel()
  {
    return debugLevel;
  }

  public void setDebugLevel(int debugLevel)
  {
    this.debugLevel = debugLevel;
  }

  public ArrayList<String> getDerivedExpList()
  {
    return derivedExpList;
  }

  public void setDerivedExpList(ArrayList<String> derivedExpList)
  {
    this.derivedExpList = derivedExpList;
  }

  public String getStartTime()
  {
    return startTime;
  }

  public void setStartTime(String startTime)
  {
    this.startTime = startTime;
  }

  public String getEndTime()
  {
    return endTime;
  }

  public void setEndTime(String endTime)
  {
    this.endTime = endTime;
  }

  public String getPhaseName()
  {
    return phaseName;
  }

  public void setPhaseName(String phaseName)
  {
    this.phaseName = phaseName;
  }

  public boolean isTestViewMode()
  {
    return testViewMode;
  }

  public void setTestViewMode(boolean testViewMode)
  {
    this.testViewMode = testViewMode;
  }

  public String getDataKey()
  {
    return dataKey;
  }

  public void setDataKey(String dataKey)
  {
    this.dataKey = dataKey;
  }

  public String getUniqueKey()
  {
    return uniqueKey;
  }

  public void setUniqueKey(String uniqueKey)
  {
    this.uniqueKey = uniqueKey;
  }

  private ArrayList<GraphUniqueKeyDTO> getGraphUniqueKeyDTOList()
  {
    GraphUniqueKeyDTO graphUniqueKey = new GraphUniqueKeyDTO(groupId, graphId, vectorName);
    ArrayList<GraphUniqueKeyDTO> graphUniqueKeyList = new ArrayList<GraphUniqueKeyDTO>();
    graphUniqueKeyList.add(graphUniqueKey);

    return graphUniqueKeyList;
  }

  private ArrayList<PanelDataInfo> getPanelDataInfoList()
  {
    PanelDataInfo panelDataInfo = new PanelDataInfo(debugLevel);

    try
    {
      TimeZone trTimeZone = ExecutionDateTime.getSystemTimeZoneGMTOffset();
      SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
      format.setTimeZone(trTimeZone);
      String trStartTime = Scenario.getTestRunStartTime(testRunNumber);
      long trStartTimeStamp = format.parse(trStartTime).getTime();
      TestRunDataType testRunDataTypeObj = new TestRunDataType(testViewMode);
      panelDataInfo.setAllParams(getGraphUniqueKeyDTOList(), derivedExpList, startTime, endTime, phaseName, testRunDataTypeObj, dataKey, uniqueKey, trStartTimeStamp);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }

    ArrayList<PanelDataInfo> arrPanelDataInfoList = new ArrayList<PanelDataInfo>();
    arrPanelDataInfoList.add(panelDataInfo);

    return arrPanelDataInfoList;
  }

  private PercentileDataDTO[] getPercentileDataDTO()
  {
    PercentileDataDTO percentileDataDTO = new PercentileDataDTO(testRunNumber, getPanelDataInfoList(), debugLevel);
    percentileDataDTO.setGraphType(PercentileDataUtils.GRAPH_TYPE_PERCENTILE);
    percentileDataDTO.setPartitionModeEnabled(isPartitionModeEnabled);
    PercentileDataDTO[] percentileDataDTOArray = new PercentileDataDTO[] { percentileDataDTO };
    return percentileDataDTOArray;
  }

  public static void main(String[] args)
  {
    PercentileDataTest percentileDataTest = new PercentileDataTest();

    System.out.print("Enter The Test Run Number : ");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    int testRunNumber = -1;
    try
    {
      String testRun = br.readLine();
      testRunNumber = Integer.parseInt(testRun);
    }
    catch (Exception ex)
    {
      System.out.println("Please enter correct test run.");
      return;
    }

    percentileDataTest.setTestRunNumber(testRunNumber);

    System.out.print("Enter Group Id : ");
    int groupId = -1;
    try
    {
      String line = br.readLine();
      groupId = Integer.parseInt(line);
    }
    catch (Exception ex)
    {
      System.out.println("Please enter correct group id.");
      return;
    }

    percentileDataTest.setGroupId(groupId);
    System.out.print("Enter Graph Id : ");
    int graphId = -1;
    try
    {
      String line = br.readLine();
      graphId = Integer.parseInt(line);
    }
    catch (Exception ex)
    {
      System.out.println("Please enter correct graph id.");
      return;
    }

    percentileDataTest.setGraphId(graphId);

    System.out.print("Enter Vector Name : ");
    String vectorName = "NA";
    try
    {
      vectorName = br.readLine();
    }
    catch (Exception e)
    {
      System.out.println("Please enter correct vector name.");
    }

    percentileDataTest.setVectorName(vectorName);

    System.out.print("Enter Partition Mode (true/false) : ");
    boolean isPartitionModeEnabled = false;
    try
    {
      isPartitionModeEnabled = Boolean.parseBoolean(br.readLine());
    }
    catch (Exception e)
    {
      System.out.println("Please enter correct Partition Mode (true/false).");
      return;
    }

    percentileDataTest.setPartitionModeEnabled(isPartitionModeEnabled);

    String startTime = "NA";
    System.out.print("Enter Start Time (absolute Time Stamp/NA) : ");
    try
    {
      startTime = br.readLine();
    }
    catch (Exception e)
    {
      System.out.println("Please enter correct Start Time (absolute Time Stamp/NA).");
      return;
    }
    percentileDataTest.setStartTime(startTime);

    String endTime = "NA";
    System.out.print("Enter End Time (absolute Time Stamp/NA) : ");
    try
    {
      endTime = br.readLine();
    }
    catch (Exception e)
    {
      System.out.println("Please enter correct End Time (absolute Time Stamp/NA).");
      return;
    }
    percentileDataTest.setEndTime(endTime);

    String phaseName = "NA";
    System.out.print("Enter Phase Name (Duration/SPECIFIED/NA/...) : ");
    try
    {
      phaseName = br.readLine();
    }
    catch (Exception e)
    {
      System.out.println("Please enter Phase Name (Duration/SPECIFIED/NA/...).");
      return;
    }

    percentileDataTest.setPhaseName(phaseName);
    String dataKey = "NA";
    System.out.print("Enter Data Key (WholeScenario/SPECIFIED/...) : ");
    try
    {
      dataKey = br.readLine();
    }
    catch (Exception e)
    {
      System.out.println("Please enter Data Key (WholeScenario/SPECIFIED/...).");
      return;
    }
    percentileDataTest.setDataKey(dataKey);

    System.out.println("*********** Summary of Arguments ***************");
    System.out.println("testRunNumber = " + testRunNumber + ", groupId = " + groupId + ", graphId = " + graphId + ", vectorName = " + vectorName);
    System.out.println("Start Time = " + startTime + ", End Time = " + endTime + ", Phase Name = " + phaseName);
    System.out.println("Data Key = " + dataKey + ", Partition Mode = " + isPartitionModeEnabled);
    System.out.println("**************************************");

    PercentileDataDTO[] percentileDataDTOArray = percentileDataTest.getPercentileDataDTO();

    new PercentileDataProcessor(percentileDataDTOArray);

    for (int i = 0; i < percentileDataDTOArray.length; i++)
    {
      HashMap<PercentileDataKey, PercentileInfo> percentileDataMap = percentileDataDTOArray[i].getPercentileDataMap();
      Iterator<PercentileDataKey> itr = percentileDataMap.keySet().iterator();
      while (itr.hasNext())
      {
        PercentileDataKey percentileDataKey = itr.next();
        System.out.println("PercentileDataKey = " + percentileDataKey);
        System.out.println(percentileDataMap.get(percentileDataKey));
      }
    }
  }
}
