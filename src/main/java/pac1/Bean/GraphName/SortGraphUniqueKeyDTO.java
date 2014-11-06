package pac1.Bean.GraphName;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import pac1.Bean.GraphUniqueKeyDTO;

/**
 * Class is used to Sort Graph DTO with Graph Indexes array.
 * @author Pydi
 */
class SortGraphUniqueKeyDTO 
{

  /**
   * Method is used to partition array and change position of array elements.
   * @param arrGraphUniqueKeyDTO
   * @param left
   * @param right
   * @return
   */
  private int Partition(GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO, int[] arrMappingGraphIndex, int left, int right)
  {
    GraphUniqueKeyDTO pivot = arrGraphUniqueKeyDTO[left];
    while(true)
    {
      while(compare(arrGraphUniqueKeyDTO[left], pivot) < 0)
      {
	left++;
      }
      
      while(compare(arrGraphUniqueKeyDTO[right], pivot) > 0)
	right--;

      if(left < right)
      {
	GraphUniqueKeyDTO tempGraphUniqueKeyDTO = arrGraphUniqueKeyDTO[right];
	arrGraphUniqueKeyDTO[right] = arrGraphUniqueKeyDTO[left];
	arrGraphUniqueKeyDTO[left] = tempGraphUniqueKeyDTO;
	
	/*Mapping indexes.*/
	int temp = arrMappingGraphIndex[right];
	arrMappingGraphIndex[right] = arrMappingGraphIndex[left] ;
	arrMappingGraphIndex[left] = temp;
      }
      else
      {
	return right;
      }
    }
  }

  /**
   * Method is used to sort Graph DTO in ascending order.
   * @param arrGraphUniqueKeyDTO
   * @param left
   * @param right
   */
  public void QuickSort_Recursive(GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO, int[] arrMappingGraphIndex, int left, int right)
  {
    if(left < right)
    {
      int pivot = Partition(arrGraphUniqueKeyDTO, arrMappingGraphIndex, left, right);

      if(pivot > 1)
	QuickSort_Recursive(arrGraphUniqueKeyDTO, arrMappingGraphIndex, left, pivot - 1);

      if(pivot + 1 < right)
	QuickSort_Recursive(arrGraphUniqueKeyDTO, arrMappingGraphIndex, pivot + 1, right);
    }
  }

  /**
   * Compare Two Object and return status in Integer.
   * @param o1
   * @param o2
   * @return
   */
  private int compare(GraphUniqueKeyDTO o1, GraphUniqueKeyDTO o2)
  {
    if (o1.getGroupId() == o2.getGroupId())
    {
      if (o1.getGraphId() == o2.getGraphId())
      {
	int value = o1.getVectorName().compareTo(o2.getVectorName());
	if (value > 0)
	  return 1;
	else if(value < 0)
	  return -1;
	else return 0;
      }
      else if (o1.getGraphId() > o2.getGraphId())
	return 1;
      else
	return -1;
    }
    else if (o1.getGroupId() > o2.getGroupId())
      return 1;
    else if (o1.getGroupId() < o2.getGroupId())
      return -1;
    else
      return -1;
  }
  
  public static void main(String[] args)
  {
    System.out.println("Processing GraphNames......");
    long startTime = System.currentTimeMillis();
    GraphNames graphNames = new GraphNames(2346);
    System.out.println("Time taken to process GraphNames = " + (System.currentTimeMillis() - startTime)/1000);
    
    StringBuilder stBuild = new StringBuilder();
    
    //GraphUniqueKeyDTO[] graphUniqueKeyDTO = new GraphUniqueKeyDTO[5];
    GraphUniqueKeyDTO[] graphUniqueKeyDTO = graphNames.getGraphUniqueKeyDTO();
    
    graphUniqueKeyDTO[0] = new GraphUniqueKeyDTO(1, 1, "B");
    graphUniqueKeyDTO[1] = new GraphUniqueKeyDTO(3, 2, "A");
    graphUniqueKeyDTO[2] = new GraphUniqueKeyDTO(2, 2, "E");
    graphUniqueKeyDTO[3] = new GraphUniqueKeyDTO(1, 1, "A");
    graphUniqueKeyDTO[4] = new GraphUniqueKeyDTO(4, 2, "A");
    int len = graphUniqueKeyDTO.length;
    
    System.out.println("QuickSort Method Called");
    long sortStartTime = System.currentTimeMillis();
    new SortGraphUniqueKeyDTO().QuickSort_Recursive(graphUniqueKeyDTO, graphNames.getArrDataTypeIndx(), 0, len - 1);
    System.out.println("Total Time Taken to Sort = " + (System.currentTimeMillis() - sortStartTime) + " mill sec");
    
    for (int i = 0; i < len; i++)
      stBuild.append(graphUniqueKeyDTO[i] + "\n");
    
    File f = new File("C:\\B.txt");
    FileWriter fw;
    try 
    {
      fw = new FileWriter(f);
      fw.write(stBuild.toString());
    } 
    catch (IOException e) {
      e.printStackTrace();
    }
    
    //System.out.println(stBuild.toString());
    System.out.println();

  }
}