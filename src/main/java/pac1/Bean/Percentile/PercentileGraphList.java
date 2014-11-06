/**
 * This class is for Storing separate list of Graphs for which percentile data generated 
 * from pctMessage.dat/rtgMessage.dat file.
 * 
 * @author Ravi Kant Sharma
 * @since Netsorm Version 4.0.0
 * @Modification_History Ravi Kant Sharma - Initial Version 4.0.0
 * @version 4.0.0
 * 
 */

package pac1.Bean.Percentile;

import java.util.ArrayList;
import java.util.HashMap;

import pac1.Bean.GeneratorUniqueKey;
import pac1.Bean.GraphUniqueKeyDTO;
import pac1.Bean.Log;

public class PercentileGraphList
{
  private String className = "PercentileGraphList";

  /**
   * Variable for debug level. Based on debug level, We need write logs
   */
  private int debugLevel = 0;

  /**
   * List of {@link GraphUniqueKeyDTO} for which percentile data generated from rtgMessage.dat file
   * 
   * This is used if Not NetCloud.
   */
  private ArrayList<GraphUniqueKeyDTO> rtgGraphsList = new ArrayList<GraphUniqueKeyDTO>();

  /**
   * List of {@link GraphUniqueKeyDTO} for which percentile data generated from pctMessage.dat file
   * 
   * This is used if Not NetCloud.
   */
  private ArrayList<GraphUniqueKeyDTO> pctGraphsList = new ArrayList<GraphUniqueKeyDTO>();

  /**
   * List of Derived Expression for which percentile data generated from rtgMessage.dat file
   * 
   * This is used if Not NetCloud as Derived Graphs not implemented for NetCloud
   */
  private ArrayList<String> derivedExpList = new ArrayList<String>();

  /**
   * This map is for generator name wise, used for graphs.
   * 
   * These graphs percentile data will be generating from pctMessage.dat
   */
  private HashMap<GeneratorUniqueKey, ArrayList<GraphUniqueKeyDTO>> generatorPctGraphsMap = new HashMap<GeneratorUniqueKey, ArrayList<GraphUniqueKeyDTO>>();

  /**
   * This map is for generator name wise, used for graphs.
   * 
   * These graphs percentile data will be generating from rtgMessage.dat
   */
  private HashMap<GeneratorUniqueKey, ArrayList<GraphUniqueKeyDTO>> generatorRtgGraphsMap = new HashMap<GeneratorUniqueKey, ArrayList<GraphUniqueKeyDTO>>();

  public HashMap<GeneratorUniqueKey, ArrayList<GraphUniqueKeyDTO>> getGeneratorPctGraphsMap()
  {
    return generatorPctGraphsMap;
  }

  public void setGeneratorPctGraphsMap(HashMap<GeneratorUniqueKey, ArrayList<GraphUniqueKeyDTO>> generatorPctGraphsMap)
  {
    this.generatorPctGraphsMap = generatorPctGraphsMap;
  }

  public HashMap<GeneratorUniqueKey, ArrayList<GraphUniqueKeyDTO>> getGeneratorRtgGraphsMap()
  {
    return generatorRtgGraphsMap;
  }

  public void setGeneratorRtgGraphsMap(HashMap<GeneratorUniqueKey, ArrayList<GraphUniqueKeyDTO>> generatorRtgGraphsMap)
  {
    this.generatorRtgGraphsMap = generatorRtgGraphsMap;
  }

  /**
   * Constructor for PercentileGraphList
   * 
   * @param debugLevel
   */
  public PercentileGraphList(int debugLevel)
  {
    this.debugLevel = debugLevel;
  }

  public ArrayList<GraphUniqueKeyDTO> getRtgGraphsList()
  {
    return rtgGraphsList;
  }

  public void setRtgGraphsList(ArrayList<GraphUniqueKeyDTO> rtgGraphsList)
  {
    this.rtgGraphsList = rtgGraphsList;
  }

  public ArrayList<GraphUniqueKeyDTO> getPctGraphsList()
  {
    return pctGraphsList;
  }

  public void setPctGraphsList(ArrayList<GraphUniqueKeyDTO> pctGraphsList)
  {
    this.pctGraphsList = pctGraphsList;
  }

  public ArrayList<String> getDerivedExpList()
  {
    return derivedExpList;
  }

  public void setDerivedExpList(ArrayList<String> derivedExpList)
  {
    this.derivedExpList = derivedExpList;
  }

  public int getDebugLevel()
  {
    return debugLevel;
  }

  public void setDebugLevel(int debugLevel)
  {
    this.debugLevel = debugLevel;
  }

  /**
   * This method is for adding {@link GraphUniqueKeyDTO} generator wise.
   * 
   * @param graphUniqueKeyDTO
   */
  public void addGraphInPctListByGenerator(String dataKey, GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (debugLevel > 1)
        Log.debugLogAlways(className, "addGraphInPctListByGenerator", "", "", "Method Called. graphUniqueKeyDTO = " + graphUniqueKeyDTO);

      // Getting Generator Name
      String generatorName = graphUniqueKeyDTO.getGeneratorName();

      // Getting Generator Test Run Number
      String generatorTestRunNum = graphUniqueKeyDTO.getGeneratorTestNum();

      GeneratorUniqueKey generatorUniqueKey = new GeneratorUniqueKey(dataKey, generatorName, generatorTestRunNum);

      // Getting List of Graphs
      ArrayList<GraphUniqueKeyDTO> pctGraphList = generatorPctGraphsMap.get(generatorUniqueKey);
      if (pctGraphList == null)
        pctGraphList = new ArrayList<GraphUniqueKeyDTO>();

      if (!pctGraphList.contains(graphUniqueKeyDTO))
        pctGraphList.add(graphUniqueKeyDTO);

      generatorPctGraphsMap.put(generatorUniqueKey, pctGraphList);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addGraphInPctListByGenerator", "", "", "Exception - ", e);
    }
  }

  /**
   * This method is for adding {@link GraphUniqueKeyDTO} generator wise.
   * 
   * @param graphUniqueKeyDTO
   */
  public void addGraphInRtgListByGenerator(String dataKey, GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (debugLevel > 1)
        Log.debugLogAlways(className, "addGraphInRtgListByGenerator", "", "", "Method Called. graphUniqueKeyDTO = " + graphUniqueKeyDTO);

      // Getting Generator Name
      String generatorName = graphUniqueKeyDTO.getGeneratorName();

      // Getting Generator Test Run Number
      String generatorTestRunNum = graphUniqueKeyDTO.getGeneratorTestNum();

      GeneratorUniqueKey generatorUniqueKey = new GeneratorUniqueKey(dataKey, generatorName, generatorTestRunNum);

      // Getting List of Graphs
      ArrayList<GraphUniqueKeyDTO> pctGraphList = generatorRtgGraphsMap.get(generatorUniqueKey);
      if (pctGraphList == null)
        pctGraphList = new ArrayList<GraphUniqueKeyDTO>();

      if (!pctGraphList.contains(graphUniqueKeyDTO))
        pctGraphList.add(graphUniqueKeyDTO);

      generatorRtgGraphsMap.put(generatorUniqueKey, pctGraphList);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addGraphInRtgListByGenerator", "", "", "Exception - ", e);
    }
  }

  /**
   * Method for adding Graph in pct graph list
   * 
   * It checks - if graph already added then ignore otherwise add.
   * 
   * @param graphUniqueKeyDTO
   */
  public void addGraphInPctList(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (debugLevel > 1)
        Log.debugLogAlways(className, "addGraphInPctList", "", "", "Going to add graph = " + graphUniqueKeyDTO + " in pctGraphList");

      if (pctGraphsList != null)
      {
        if (pctGraphsList.contains(graphUniqueKeyDTO))
        {
          if (debugLevel > 1)
            Log.debugLogAlways(className, "addGraphInPctList", "", "", "Graph = " + graphUniqueKeyDTO + " already available in pctGraphList");

          return;
        }
      }
      else
      {
        pctGraphsList = new ArrayList<GraphUniqueKeyDTO>();
      }

      pctGraphsList.add(graphUniqueKeyDTO);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addGraphInPctList", "", "", "Exception- ", e);
    }
  }

  /**
   * Method for adding Graph in RTG graph list
   * 
   * It checks - if graph already added then ignore otherwise add.
   * 
   * @param graphUniqueKeyDTO
   */
  public void addGraphInRtgList(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (debugLevel > 1)
        Log.debugLogAlways(className, "addGraphInRtgList", "", "", "Going to add graph = " + graphUniqueKeyDTO + " in rtgGraphsList");

      if (rtgGraphsList != null)
      {
        if (rtgGraphsList.contains(graphUniqueKeyDTO))
        {
          if (debugLevel > 1)
            Log.debugLogAlways(className, "addGraphInRtgList", "", "", "Graph = " + graphUniqueKeyDTO + " already available in rtgGraphsList");

          return;
        }
      }
      else
      {
        rtgGraphsList = new ArrayList<GraphUniqueKeyDTO>();
      }

      rtgGraphsList.add(graphUniqueKeyDTO);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addGraphInRtgList", "", "", "Exception- ", e);
    }
  }

  /**
   * This method check if derived expression is already available, then ignore.
   * 
   * Otherwise it adds derived expression in derived expression list
   * 
   * @param derivedExp
   */
  public void addDerivedExp(String derivedExp)
  {
    try
    {
      if (debugLevel > 1)
        Log.debugLogAlways(className, "addDerivedExp", "", "", "Going to add derivedExp = " + derivedExp);

      if (derivedExpList != null)
      {
        if (debugLevel > 2)
          Log.debugLogAlways(className, "addDerivedExp", "", "", "adding derived exp = " + derivedExp);

        if (!derivedExpList.contains(derivedExp))
          derivedExpList.add(derivedExp);
        else if (debugLevel > 1)
          Log.debugLogAlways(className, "addDerivedExp", "", "", "derivedExp = " + derivedExp + " already exist in derived exp list.");
      }
      else
      {
        derivedExpList = new ArrayList<String>();

        if (debugLevel > 2)
          Log.debugLogAlways(className, "addDerivedExp", "", "", "adding derived exp = " + derivedExp);

        derivedExpList.add(derivedExp);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addDerivedExp", "", "", "Exception - ", e);
    }
  }

  public String toString()
  {
    return "rtgGraphsList = " + rtgGraphsList + ", pctGraphsList = " + pctGraphsList + ", derivedExpList = " + derivedExpList;
  }
}
