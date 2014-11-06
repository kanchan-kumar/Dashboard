
package pac1.Bean;

import java.util.ArrayList;

import pac1.Bean.GraphName.GraphNames;

/**
 * Class is used for operations on Group, Graphs of test run, like merge, search etc.
 * @version 1.0
 */
public class TestRunGraphUtils 
{

  private static String className = "TestRunGraphUtils";

  /**
   * Method is used to subtract two array. </br>
   * Method returns all the graphs of first array which are not in second array.</br> 
   * @param graphUniqueKeyDTO1
   * @param graphUniqueKeyDTO2
   * @return
   */
  public ArrayList<GraphUniqueKeyDTO> getSubstractedGraphList(GraphUniqueKeyDTO []graphUniqueKeyDTO1, GraphUniqueKeyDTO []graphUniqueKeyDTO2)
  {
    Log.debugLogAlways(className, "getSubstractedGraphList", "", "", "Method Called.");
    
    try
    {
      ArrayList<GraphUniqueKeyDTO> arrGraphDTOList = getGraphDTOArrayAsList(graphUniqueKeyDTO1);
      
      /*Iterating through second array.*/
      for(int i = 0; i < graphUniqueKeyDTO2.length; i++)
      {
	for(int k = 0; k < arrGraphDTOList.size(); k++)
	{
	  /*Check if graph exist in second graph DTO list.*/
	  if(graphUniqueKeyDTO2[i].equals(arrGraphDTOList.get(k)))
	  {
	    /*Remove from Graph DTO list if exist in another graph DTO array.*/
	    arrGraphDTOList.remove(k);
	    continue;
	  }
        }
      }
      
      return arrGraphDTOList;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Method is used to subtract two array by comparing two GraphNames object. </br>
   * Method returns all the graphs of first array which are not in second array.</br> 
   * @param newGraphNamesObj
   * @param oldGraphNamesObj
   * @return
   */
  public ArrayList<GraphUniqueKeyDTO> getSubstractedGraphListByGraphNames(GraphNames newGraphNamesObj, GraphNames oldGraphNamesObj)
  {
    Log.debugLogAlways(className, "getSubstractedGraphListByGraphNames", "", "", "Method Called.");
    
    try
    {
      /*ArrayList for Newly added Graphs*/
      ArrayList<GraphUniqueKeyDTO> arrNewGraphNamesDTOList = new ArrayList<GraphUniqueKeyDTO>();
      
      /*Iterating through array.*/
      for(int i = 0; i < newGraphNamesObj.getGraphUniqueKeyDTO().length; i++)
      {
	/*finds the existence of graph in old GraphNames object.*/
	if(oldGraphNamesObj.getGraphDataIndexByGraphUniqueKeyDTO(newGraphNamesObj.getGraphUniqueKeyDTO()[i]) == -1)
	  arrNewGraphNamesDTOList.add(newGraphNamesObj.getGraphUniqueKeyDTO()[i]);
      }
            
      return arrNewGraphNamesDTOList;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * The method returns array as list.
   * @param graphUniqueKeyDTO
   * @return
   */
  public ArrayList<GraphUniqueKeyDTO> getGraphDTOArrayAsList(GraphUniqueKeyDTO []graphUniqueKeyDTO)
  {
    try
    {
      ArrayList<GraphUniqueKeyDTO> arrGraphDTOList = new ArrayList<GraphUniqueKeyDTO>();
      
      for(int k = 0; k < graphUniqueKeyDTO.length; k++)
      {
	arrGraphDTOList.add(graphUniqueKeyDTO[k]);
      }
      
      return arrGraphDTOList;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Method is used to get the list of unique group id from Graph DTO List.
   * @param arrGraphDTOList
   * @return
   */
  public ArrayList<Integer> getUniqueGroupIdListFromGraphDTOList(ArrayList<GraphUniqueKeyDTO> arrGraphDTOList)
  {
    try
    {
      Log.debugLogAlways(className, "getUniqueGroupIdListFromGraphDTOList", "", "", "Method Called."); 
      
      ArrayList<Integer> arrGroupIdList = new ArrayList<Integer>();
      
      for(int i = 0; i < arrGraphDTOList.size(); i++)
      {
	int groupId = arrGraphDTOList.get(i).getGroupId();
	
	if(!arrGroupIdList.contains(groupId))
	  arrGroupIdList.add(groupId);
      }
      
      return arrGroupIdList;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
   
  public static void main(String[] args) 
  {   
    TestRunGraphUtils testRunGraphUtils = new TestRunGraphUtils();
    
    GraphNames graphNames = new GraphNames(10000, null, null, "-1", "", "20140521171000", false);
    GraphNames g2 = new GraphNames(10000, null, null, "-1", "", "20140521173000", false);
   
    System.out.println("length 1 = " + graphNames.getGraphUniqueKeyDTO().length);
    
    GraphUniqueKeyDTO gout[]  = graphNames.getGraphUniqueKeyDTO();
    GraphUniqueKeyDTO gout2[] = g2.getGraphUniqueKeyDTO();
    
    ArrayList<GraphUniqueKeyDTO> arrList = testRunGraphUtils.getSubstractedGraphList(gout2, gout);
         
    System.out.println("Unique Graph length = " + arrList.size());
    
    for(int k = 0; k < arrList.size(); k++)
    {
      System.out.println(arrList.get(k));
    }
    
    ArrayList<Integer> arrGroupIdList = testRunGraphUtils.getUniqueGroupIdListFromGraphDTOList(arrList);
    
    for(int k = 0; k < arrGroupIdList.size(); k++)
    {
      System.out.println("Added New Groups = " + arrGroupIdList.get(k));
    }
  }
}
