/*--------------------------------------------------------------------
@Name    : RtgMsgViewer.java
@Author  : Prabhat
@Purpose : View the rtg msg data value from rtgMessage.dat file
@Modification History:

----------------------------------------------------------------------*/

package pac1.Bean;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RtgMsgViewer
{
  private ReportData rptData;
  private int numTestRun = -1;
  long startSeq = 0; // Seq number of first packet to be shown in the graph
  long endSeq = 0;   // Seq number of last packet to be shown in the graph

  public RtgMsgViewer(int numTestRun)
  {
    this.numTestRun = numTestRun;
    rptData = new ReportData(numTestRun);
  }

  private void init()
  {
    if(!rptData.openRTGMsgFile())
      return;

    startSeq = 1;  // First data packet seq is 1
    endSeq = rptData.maxSeq;

    rptData.setStartSeq(startSeq);
    rptData.setEndSeq(endSeq);

    System.out.println("Test Run Number = " + numTestRun);
    System.out.println("Target Completion Time = " + Scenario.getTargetCompTimeFromGlobalFile(numTestRun));
    System.out.println("Number of graphs = " + rptData.graphNames.getTotalNumOfGraphs());

    double[][] arrDataValuesAll = rptData.getAllGraphsDataFromRTGFile(rptData.graphNames.getGraphDataIndx(), "", "", "");

    /*for(int l = 0; l < (int )(endSeq - startSeq + 1); l++)
    {
      System.out.println("");
      System.out.println("Sequence Number = " + (startSeq + l));

      int k = 0;
      for(int i = 0; i < rptData.graphNames.getTotalNumOfGroups(); i++)
      {
        System.out.println("Group Name = " + rptData.graphNames.getGroupNameByGroupNum(i));

        for(int j = 0; j < rptData.graphNames.getNumOfGraphsByGroupNum(i); j++)
        {
          System.out.println("Graph Name = " + rptData.graphNames.getGraphNameByGraphNum(k) + ", Graph Data Value = " + arrDataValuesAll[k][l]);
          k++;
        }
        System.out.println("");
      }
    }*/
  }


  public static void main(String[] args)
  {
    System.out.print("Enter The Test Run Number : ");
    //  open up standard input
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    try
    {
      String testRun = br.readLine();
      int numTestRun = Integer.parseInt(testRun);

      RtgMsgViewer rtgMsgViewer = new RtgMsgViewer(numTestRun);
      rtgMsgViewer.init();
    }
    catch (IOException e)
    {
      System.out.println("Exception :" + e);
      System.exit(1);
    }
  }
}