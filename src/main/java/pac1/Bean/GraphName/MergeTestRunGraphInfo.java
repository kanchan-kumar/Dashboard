package pac1.Bean.GraphName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import pac1.Bean.Log;
import pac1.Bean.rptUtilsBean;

public class MergeTestRunGraphInfo implements Serializable
{
  private static final long serialVersionUID = -3634203762773815629L;
  private ArrayList<Object> arrInfoAbtGraph = new ArrayList<Object>();
  private String className = "MergeTestRunGraphInfo";
  private Vector<String> gdfData = null;

  // Return array list of Groupid.GraphId.VectorName, graph data idx and data type of graph
  // 0th Index it will have GroupID.GraphID.VectorName eg. if group id is 1, graph id is 1
  // and vector name is not present then GroupID.GraphID.VectorName will be 1.1.NA ,
  // and group id is 7 graph id 3 and vector Name is 1xx then
  // GroupID.GraphID.VectorName will be 7.3.1xx
  // 1st Index it will have GraphDataIdx of given TestRun
  // 2nd Index it will have DataType of graph for given TestRun
  // 3rd index it will have group and graph lines of passed test run
  // 4th index it will have linked hash map with key group id and value will be number of graph for key (group id)
  public ArrayList generateInfoAboutGraph(Vector<String> gdfData, GraphNameProcessor graphNameProcessor)
  {
    try
    {
      Log.debugLogAlways(className, "generateInfoAboutGraph", "", "", "Method Called");

      this.gdfData = gdfData;
      ArrayList<String> arrGroupIdGraphIdVectorName = new ArrayList<String>(); // used for groupId-GraphId-VectorName
      ArrayList<Integer> arrGraphDataTypeNum = new ArrayList<Integer>(); // used for data type num
      ArrayList<Integer> arrDataIndex = new ArrayList<Integer>(); // used for data index
      ArrayList<Integer> arrGroupId = graphNameProcessor.getUniqueGroupIds(); // used for group id

      LinkedHashMap<String, String> hmGroupGraphLines = graphNameProcessor.getGroupGraphLines(); // used for group and graph lines
      // if any error comes while getting group and graph lines return error with description
      // for example - if group have duplicate graph id
      if (hmGroupGraphLines.containsKey("Error"))
      {
        arrInfoAbtGraph = new ArrayList<Object>();
        arrInfoAbtGraph.add("Error");
        arrInfoAbtGraph.add(hmGroupGraphLines.get("Error").toString());
        return arrInfoAbtGraph;
      }

      // used to calculate total number of graphs for every group id
      // key is group id and value is total number of graphs
      LinkedHashMap<Integer, Integer> hmTotalGraphsByGrpId = new LinkedHashMap<Integer, Integer>();
      // used for calculating total graphs for every group id
      int totalGraphsByGrpId = 0;
      for (int i = 0; i < arrGroupId.size(); i++)
      {
        int previousGroupId = -1;
        // getting group id one by one
        int groupId = (Integer) arrGroupId.get(i);
        ArrayList<Object> arrGroupTypeGraphIdGraphDataType = getGroupTypeAllGraphIdDataTypeByGroupId(groupId);
        if (arrGroupTypeGraphIdGraphDataType.contains("Error"))
        {
          ArrayList<Object> arrError = new ArrayList<Object>();
          arrError.add("Error");
          arrError.add(arrGroupTypeGraphIdGraphDataType.get(1).toString());
        }
        // getting group type
        String groupType = arrGroupTypeGraphIdGraphDataType.get(0).toString();
        // getting all graph ids and their data type for every group id
        LinkedHashMap arrGraphIdGraphDataType = (LinkedHashMap) arrGroupTypeGraphIdGraphDataType.get(1);
        // if group type is vector
        if (groupType.equals("vector"))
        {
          String[] arrGroupVectorNames = graphNameProcessor.getNameOfGroupIndicesByGroupId(groupId); // getting all vectors for group id
          for (int j = 0; j < arrGroupVectorNames.length; j++)
          {
            // getting each vector name
            String vectorName = arrGroupVectorNames[j];
            Iterator iterator = arrGraphIdGraphDataType.keySet().iterator();
            while (iterator.hasNext())
            {
              // used for graph id
              int graphId = (Integer) iterator.next();
              // used for graph data type
              String dataType = (String) arrGraphIdGraphDataType.get(graphId);
              int dataTypeNum = GraphNameUtils.getDataTypeNum(dataType);
              // adding each data type in array list of data types
              arrGraphDataTypeNum.add(dataTypeNum);
              String groupIdGraphIdVectorName = groupId + "-" + graphId + "-" + vectorName;
              // adding each GroupId-GraphId-VectorName in arraylist of groupId-GraphId-VectorName
              arrGroupIdGraphIdVectorName.add(groupIdGraphIdVectorName);
              // used for data index of each graph
              int dataIdx = graphNameProcessor.getGraphDataIdxByGrpIdGraphIdVecName(groupId, graphId, vectorName);
              // adding each data index in array list of DataIndex
              arrDataIndex.add(dataIdx);
              if (previousGroupId == groupId || previousGroupId == -1) // calculating total number of graphs for every group
                totalGraphsByGrpId = totalGraphsByGrpId + 1;
            }
          }
          // adding each entry of group id with their total number of graphs
          hmTotalGraphsByGrpId.put(groupId, totalGraphsByGrpId);
        }
        else
        {
          // in case when group type is scalar
          Iterator iterator = arrGraphIdGraphDataType.keySet().iterator();
          while (iterator.hasNext())
          {
            // used for graph id of graph
            int graphId = Integer.parseInt(iterator.next().toString().trim());
            // used for data type of graph
            String dataType = (String) arrGraphIdGraphDataType.get(graphId);
            // used for data type number -> DATA_TYPE_SAMPLE = 0 , DATA_TYPE_RATE = 1
            // DATA_TYPE_CUMULATIVE = 2 ,DATA_TYPE_TIMES = 3 , DATA_TYPE_TIMES_STD = 4;
            int dataTypeNum = GraphNameUtils.getDataTypeNum(dataType);
            // getting all vector name, if graph is scalar type then it resturns NA
            // otherwise it returns all graph vector names
            String[] arrVectNames = graphNameProcessor.getNameOfIndicesByGroupIdAndGraphId(groupId, graphId);
            for (int j = 0; j < arrVectNames.length; j++)
            {
              // getting each vector name of graph
              String vectName = arrVectNames[j];
              String groupIdGraphIdVectorName = groupId + "-" + graphId + "-" + vectName;
              // adding each groupId_GraphId-VectorName in array list of GroupId-GraphId-VectorName
              arrGroupIdGraphIdVectorName.add(groupIdGraphIdVectorName);
              // adding data type num in array list of graph data types
              arrGraphDataTypeNum.add(dataTypeNum);
              int dataIdx = graphNameProcessor.getGraphDataIdxByGrpIdGraphIdVecName(groupId, graphId, vectName); // used for data index
              arrDataIndex.add(dataIdx); // adding each data index in array list of graph data index
              if (previousGroupId == groupId || previousGroupId == -1) // calculating total graphs for every group id
                totalGraphsByGrpId = totalGraphsByGrpId + 1;
            }
          }
          // adding each entry of group id with their total graphs in linked hash map
          hmTotalGraphsByGrpId.put(groupId, totalGraphsByGrpId);
        }
        previousGroupId = groupId;
        totalGraphsByGrpId = 0;
      }

      Log.debugLog(className, "getInfoAboutGraph", "", "", "Total Graphs = " + arrGroupIdGraphIdVectorName.size());
      arrInfoAbtGraph.add(arrGroupIdGraphIdVectorName); // adding array list of GrpId_GraphId-VectorName at 0th index
      arrInfoAbtGraph.add(arrDataIndex); // adding array list of Graph Data Index at 1st index
      arrInfoAbtGraph.add(arrGraphDataTypeNum); // adding array list of Data Type Number at 2th index
      arrInfoAbtGraph.add(hmGroupGraphLines); // adding array list of Group and Graph Lines at 3th index
      arrInfoAbtGraph.add(hmTotalGraphsByGrpId); // adding linked hash map of key as Group Id and Value is total graphs at 4th index
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "generateInfoAboutGraph", "", "", "Exception - " + ex);
      arrInfoAbtGraph = new ArrayList<Object>();
      arrInfoAbtGraph.add("Error");
      arrInfoAbtGraph.add(ex.toString());
    }

    return arrInfoAbtGraph;
  }

  // returns an array list of group type and all graph id with their data type
  // 0th index group type
  // 1st index linked hash map of key as all graph id with their data type for given group id
  public ArrayList<Object> getGroupTypeAllGraphIdDataTypeByGroupId(int groupId)
  {
    try
    {
      Log.debugLogAlways(className, "getGroupTypeAllGraphIdDataTypeByGroupId", "", "", "Method Called. groupId = " + groupId);

      ArrayList<Object> arrResult = new ArrayList<Object>();
      int graphId = 0; // used for graph id
      String graphDataType = ""; // used for data type of graph
      String groupType = ""; // used for type of group (Scalar or vector)
      // used for graph id and their data type, key is graph id and value is data type of graph
      int tmpGroupId = 0; // used for group id
      LinkedHashMap<Integer, String> hmGraphIdGraphType = new LinkedHashMap<Integer, String>();
      for (int i = 1; i < gdfData.size(); i++)
      {
        String gdfLine = gdfData.get(i);
        if (gdfLine.toLowerCase().startsWith("group"))
        {
          String[] arrGroupLine = rptUtilsBean.strToArrayData(gdfLine, "|");
          tmpGroupId = Integer.parseInt(arrGroupLine[2].trim()); // used for group id
          if (tmpGroupId == groupId)
            groupType = arrGroupLine[3];
        }
        if (tmpGroupId == groupId)
        {
          if (gdfLine.toLowerCase().startsWith("graph"))
          {
            String[] arrGraphLine = rptUtilsBean.strToArrayData(gdfLine, "|");
            graphId = Integer.parseInt(arrGraphLine[2].trim()); // used for graph id
            graphDataType = arrGraphLine[4]; // used for graph data type
            // if graph id for same group id already exist in linked hash map
            // returns an error message of duplicate graph id
            if (hmGraphIdGraphType.containsKey(graphId))
            {
              arrResult.clear();
              arrResult.add("Error");
              arrResult.add("Group id " + groupId + " contains duplicate graph id.");
              return arrResult;
            }
            else
            {
              hmGraphIdGraphType.put(graphId, graphDataType);
            }
          }
        }
      }

      // adding group type in resulted array list at 0th index
      arrResult.add(groupType);
      // adding linked hash map in resulted array list at 1st index
      arrResult.add(hmGraphIdGraphType);
      return arrResult;
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getGroupTypeAllGraphIdDataTypeByGroupId", "", "", "Exception - " + ex);
      ArrayList<Object> arrError = new ArrayList<Object>();
      arrError.add("Error");
      arrError.add(ex.toString());
      return arrError;
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }

}
