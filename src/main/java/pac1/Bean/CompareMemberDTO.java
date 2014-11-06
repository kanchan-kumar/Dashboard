package pac1.Bean;

import java.util.ArrayList;

public class CompareMemberDTO
{
  private String[] operatorName;
  private String[] operatorValue;

  private ArrayList<ArrayList<GraphUniqueKeyDTO>> listOfGraphPanels = null;

  public ArrayList<ArrayList<GraphUniqueKeyDTO>> getListOfGraphPanels()
  {

    return listOfGraphPanels;
  }

  public void setListOfGraphPanels(ArrayList<ArrayList<GraphUniqueKeyDTO>> listOfGraphPanels)
  {
    this.listOfGraphPanels = listOfGraphPanels;
  }

  public String[] getOperatorName()
  {
    return operatorName;
  }

  /*public void setOperatorName(String[] operatorName)
  {
    this.operatorName = operatorName;
  }
  */

  public String[] getOperatorValue()
  {
    return operatorValue;
  }

  /* public void setOperatorValue(String[] operatorValue)
   {
     this.operatorValue = operatorValue;
   }
   */
  public void setOperators(String selectedMenu)
  {
    try
    {
      String[] arr = selectedMenu.split(">");
      /*for(int i = 0; i < arr.length; i++)
      {
        System.out.println("splited values as separator >|" + arr[i]);
      }*/

      ArrayList<String> operateNameList = new ArrayList<String>();
      ArrayList<String> operateValuesList = new ArrayList<String>();

      for(int i = 0; i < arr.length; i++)
      {
        String[] operator = arr[i].split("\\#\\$");
        if(operator.length != 2)
          return;
        //System.out.println("splited values as $#|" + operator[0]);
        //System.out.println("splited values as $#|" + operator[1]);
        operateNameList.add(operator[0]);
        operateValuesList.add(operator[1]);

      }

      operatorName = operateNameList.toArray(new String[operateNameList.size()]);
      operatorValue = operateValuesList.toArray(new String[operateValuesList.size()]);

    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

}
