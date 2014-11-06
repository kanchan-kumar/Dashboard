/**File Name: ExecutionDrillDown.java
 * Purpose: Finds Object Type for graph based on Group Id and Graph Id of Graph
 **/

package pac1.Bean;


/*
 * 
Description	Group Id	GraphId	ObjectType	Status
Transaction
Transactions Started/Minute	6	4	2	-2
Transactions Completed/Minute	6	1	2	-2
Transactions Successful/Minute	6	2	2	-2
Average Transaction Response Time (Secs)	6	3	2	-2
Transactions Completed	6	5	2	-2
Transactions Success	6	6	2	0

Transactions Failures(All)/Minute	2	1	2	-1
Transactions Failures/Minute	2	2	2	NA (get vec name as particular status. Eg: 1xx)

Transaction Time (Sec)	102	1	2	-2
Transaction Completed/Sec	102	2	2	-2
Transaction Successful/Sec	102	3	2	0
Transaction Failures/Sec	102	4	2	-1

Transaction Completed	103	1	2	-2
Transaction Successful	103	2	2	0
Transaction Failures	103	3	2	-1

URL
Requests Sent/Sec	3	6	0	-2
Requests Completed/Sec	3	1	0	-2
Requests Successful/Sec	3	2	0	-2
Average Response Time (Secs)	3	3	0	-2
Requests Completed	3	4	0	-2
Requests Successful	3	5	0	0

Page
Page Download Started/Minute	4	4	1	-2
Page Download Completed/Minute	4	1	1	-2
Success Page Responses/Minute	4	2	1	0
Average Page Response Time (Secs)	4	3	1	-2
Total Pages Completed	4	5	1	-2
Total Pages Success	4	6	1	0
Average Java Script processing Time (Secs)	4	7	1	-2
Average Page processing Time (Secs)	4	8	1	-2

Session
Sessions Started/Minute	5	4	3	-2
Sessions Completed/Minute	5	1	3	-2
Successful Sessions/Minute	5	2	3	-2
Average Session Response Time (Secs)	5	3	3	-2
Total Session Completed	5	5	3	-2
Total Session Success	5	6	3	0

HTTP Failures
HTTP Failures(All)/Sec	7	1	0	-1
HTTP Failures/Sec	7	2	0	-1

Page Failures
Page Failures(All)/Minute	8	1	1	-1
Page Failures/Minute	8	2	1	-1

Session Failures	9
Sessions Failures(All)/Minute	9	1	3	-1
Sessions Failures/Minute	9	2	3	-1

Transactions Failures	10
Transactions Failures(All)/Minute	10	1	2	-1
Transactions Failures/Minute	10	2	2	-1


 */




//Used for analyze object type based on group_id and graph_id of particular graph.
public class ExecutionDrillDown implements java.io.Serializable
{

	String className = "ExecutionDrillDown";
	/* String Array to Store the different values of graphs to identify object type, The Array Store the following information.
	 * Description - Description of graph
	 * group_Id - Specify the Group, the graph relates the group.
	 * graph_Id - Specify and Identify the Individual graph.
	 * object_Type - Object type like 0 - URL, 1 - Page, 2 - Transaction, 3 - Session.
	 * status - Object Status like success, 1xx, 2xx etc.
	 */
	String [][]arrDataValues = null;

	public ExecutionDrillDown()
	{

		//Initialize array.
		arrDataValues = new String[41][5];

		/*
		 * Description	Group Id	GraphId	ObjectType	Status
		 * Transaction
		 * Transactions Started/Minute	6	4	2	-2
		 * Transactions Completed/Minute	6	1	2	-2
		 * Transactions Successful/Minute	6	2	2	-2
		 * Average Transaction Response Time (Secs)	6	3	2	-2
		 * Transactions Completed	6	5	2	-2
		 * Transactions Success	6	6	2	0
		 */

		arrDataValues[0][0] = "Transactions Started/Minute";
		arrDataValues[0][1] = "6";
		arrDataValues[0][2] = "4";
		arrDataValues[0][3] = "2";
		arrDataValues[0][4] = "-2";

		arrDataValues[1][0] = "Transactions Completed/Minute";
		arrDataValues[1][1] = "6";
		arrDataValues[1][2] = "1";
		arrDataValues[1][3] = "2";
		arrDataValues[1][4] = "-2";

		arrDataValues[2][0] = "Transactions Successful/Minute";
		arrDataValues[2][1] = "6";
		arrDataValues[2][2] = "2";
		arrDataValues[2][3] = "2";
		arrDataValues[2][4] = "-2";

		arrDataValues[3][0] = "Average Transaction Response Time (Secs)";
		arrDataValues[3][1] = "6";
		arrDataValues[3][2] = "3";
		arrDataValues[3][3] = "2";
		arrDataValues[3][4] = "-2";

		arrDataValues[4][0] = "Transactions Completed";
		arrDataValues[4][1] = "6";
		arrDataValues[4][2] = "5";
		arrDataValues[4][3] = "2";
		arrDataValues[4][4] = "-2";

		arrDataValues[5][0] = "Transactions Success";
		arrDataValues[5][1] = "6";
		arrDataValues[5][2] = "6";
		arrDataValues[5][3] = "2";
		arrDataValues[5][4] = "0";

		/*
		 * Transactions Failures(All)/Minute	2	1	2	-1
		 * Transactions Failures/Minute	2	2	2	NA (get vec name as particular status. Eg: 1xx)
		 */

		arrDataValues[6][0] = "Transactions Failures(All)/Minute";
		arrDataValues[6][1] = "10";
		arrDataValues[6][2] = "1";
		arrDataValues[6][3] = "2";
		arrDataValues[6][4] = "-1";

		arrDataValues[7][0] = "Transactions Failures/Minute";
		arrDataValues[7][1] = "10";
		arrDataValues[7][2] = "2";
		arrDataValues[7][3] = "2";
		arrDataValues[7][4] = "NA";

		/*
		 * Transaction Time (Sec)	102	1	2	-2
		 * Transaction Completed/Sec	102	2	2	-2
		 * Transaction Successful/Sec	102	3	2	0
		 * Transaction Failures/Sec	102	4	2	-1
		 */

		arrDataValues[8][0] = "Transaction Time (Sec)";
		arrDataValues[8][1] = "102";
		arrDataValues[8][2] = "1";
		arrDataValues[8][3] = "2";
		arrDataValues[8][4] = "-2";

		arrDataValues[9][0] = "Transaction Completed/Sec";
		arrDataValues[9][1] = "102";
		arrDataValues[9][2] = "2";
		arrDataValues[9][3] = "2";
		arrDataValues[9][4] = "-2";

		arrDataValues[10][0] = "Transaction Successful/Sec";
		arrDataValues[10][1] = "102";
		arrDataValues[10][2] = "3";
		arrDataValues[10][3] = "2";
		arrDataValues[10][4] = "0";

		arrDataValues[11][0] = "Transactions Failures/Sec";
		arrDataValues[11][1] = "102";
		arrDataValues[11][2] = "4";
		arrDataValues[11][3] = "2";
		arrDataValues[11][4] = "-1";

		/*
		 * Transaction Completed	103	1	2	-2
		 * Transaction Successful	103	2	2	0
		 * Transaction Failures	103	3	2	-1
		 */

		arrDataValues[12][0] = "Transaction Completed";
		arrDataValues[12][1] = "103";
		arrDataValues[12][2] = "1";
		arrDataValues[12][3] = "2";
		arrDataValues[12][4] = "-2";

		arrDataValues[13][0] = "Transaction Successful";
		arrDataValues[13][1] = "103";
		arrDataValues[13][2] = "2";
		arrDataValues[13][3] = "2";
		arrDataValues[13][4] = "0";

		arrDataValues[14][0] = "Transactions Failures";
		arrDataValues[14][1] = "103";
		arrDataValues[14][2] = "3";
		arrDataValues[14][3] = "2";
		arrDataValues[14][4] = "-1";


		/*
		 * 
		 * URL
		 * Requests Sent/Sec	3	6	0	-2
		 * Requests Completed/Sec	3	1	0	-2
		 * Requests Successful/Sec	3	2	0	-2
		 * Average Response Time (Secs)	3	3	0	-2
		 * Requests Completed	3	4	0	-2
		 * Requests Successful	3	5	0	0
		 */

		arrDataValues[15][0] = "Requests Sent/Sec";
		arrDataValues[15][1] = "3";
		arrDataValues[15][2] = "6";
		arrDataValues[15][3] = "0";
		arrDataValues[15][4] = "-2";

		arrDataValues[16][0] = "Requests Completed/Sec";
		arrDataValues[16][1] = "3";
		arrDataValues[16][2] = "1";
		arrDataValues[16][3] = "0";
		arrDataValues[16][4] = "-2";

		arrDataValues[17][0] = "Requests Successful/Sec";
		arrDataValues[17][1] = "3";
		arrDataValues[17][2] = "2";
		arrDataValues[17][3] = "0";
		arrDataValues[17][4] = "-2";

		arrDataValues[18][0] = "Average Response Time (Secs)";
		arrDataValues[18][1] = "3";
		arrDataValues[18][2] = "3";
		arrDataValues[18][3] = "0";
		arrDataValues[18][4] = "-2";

		arrDataValues[19][0] = "Requests Completed";
		arrDataValues[19][1] = "3";
		arrDataValues[19][2] = "4";
		arrDataValues[19][3] = "0";
		arrDataValues[19][4] = "-2";

		arrDataValues[20][0] = "Requests Successful";
		arrDataValues[20][1] = "3";
		arrDataValues[20][2] = "5";
		arrDataValues[20][3] = "0";
		arrDataValues[20][4] = "0";

		/*
		 * Page
		 * Page Download Started/Minute	4	4	1	-2
		 * Page Download Completed/Minute	4	1	1	-2
		 * Success Page Responses/Minute	4	2	1	0
		 * Average Page Response Time (Secs)	4	3	1	-2
		 * Total Pages Completed	4	5	1	-2
		 * Total Pages Success	4	6	1	0
		 * Average Java Script processing Time (Secs)	4	7	1	-2
		 * Average Page processing Time (Secs)	4	8	1	-2
		 */

		arrDataValues[21][0] = "Page Download Started/Minute";
		arrDataValues[21][1] = "4";
		arrDataValues[21][2] = "4";
		arrDataValues[21][3] = "1";
		arrDataValues[21][4] = "-2";

		arrDataValues[22][0] = "Page Download Completed/Minute";
		arrDataValues[22][1] = "4";
		arrDataValues[22][2] = "1";
		arrDataValues[22][3] = "1";
		arrDataValues[22][4] = "-2";

		arrDataValues[23][0] = "Success Page Responses/Minute";
		arrDataValues[23][1] = "4";
		arrDataValues[23][2] = "2";
		arrDataValues[23][3] = "1";
		arrDataValues[23][4] = "0";

		arrDataValues[24][0] = "Average Page Response Time (Secs)";
		arrDataValues[24][1] = "4";
		arrDataValues[24][2] = "3";
		arrDataValues[24][3] = "1";
		arrDataValues[24][4] = "-2";

		arrDataValues[25][0] = "Total Pages Completed";
		arrDataValues[25][1] = "4";
		arrDataValues[25][2] = "5";
		arrDataValues[25][3] = "1";
		arrDataValues[25][4] = "-2";

		arrDataValues[26][0] = "Total Pages Success";
		arrDataValues[26][1] = "4";
		arrDataValues[26][2] = "6";
		arrDataValues[26][3] = "1";
		arrDataValues[26][4] = "0";

		arrDataValues[27][0] = "Average Java Script processing Time (Secs)";
		arrDataValues[27][1] = "4";
		arrDataValues[27][2] = "7";
		arrDataValues[27][3] = "1";
		arrDataValues[27][4] = "-2";

		arrDataValues[28][0] = "Average Page processing Time (Secs)";
		arrDataValues[28][1] = "4";
		arrDataValues[28][2] = "8";
		arrDataValues[28][3] = "1";
		arrDataValues[28][4] = "-2";

		/*
		 * Session
		 * Sessions Started/Minute	5	4	3	-2
		 * Sessions Completed/Minute	5	1	3	-2
		 * Successful Sessions/Minute	5	2	3	-2
		 * Average Session Response Time (Secs)	5	3	3	-2
		 * Total Session Completed	5	5	3	-2
		 * Total Session Success	5	6	3	0
		 */

		arrDataValues[29][0] = "Sessions Started/Minute";
		arrDataValues[29][1] = "5";
		arrDataValues[29][2] = "4";
		arrDataValues[29][3] = "3";
		arrDataValues[29][4] = "-2";

		arrDataValues[30][0] = "Sessions Completed/Minute";
		arrDataValues[30][1] = "5";
		arrDataValues[30][2] = "1";
		arrDataValues[30][3] = "3";
		arrDataValues[30][4] = "-2";

		arrDataValues[31][0] = "Successful Sessions/Minute";
		arrDataValues[31][1] = "5";
		arrDataValues[31][2] = "2";
		arrDataValues[31][3] = "3";
		arrDataValues[31][4] = "-2";

		arrDataValues[32][0] = "Average Session Response Time (Secs)";
		arrDataValues[32][1] = "5";
		arrDataValues[32][2] = "3";
		arrDataValues[32][3] = "3";
		arrDataValues[32][4] = "-2";

		arrDataValues[33][0] = "Total Session Completed";
		arrDataValues[33][1] = "5";
		arrDataValues[33][2] = "5";
		arrDataValues[33][3] = "3";
		arrDataValues[33][4] = "-2";

		arrDataValues[34][0] = "Total Session Success";
		arrDataValues[34][1] = "5";
		arrDataValues[34][2] = "6";
		arrDataValues[34][3] = "3";
		arrDataValues[34][4] = "0";

		/*
		 * HTTP Failures
		 * HTTP Failures(All)/Sec	7	1	0	-1
		 * HTTP Failures/Sec	7	2	0	-1
		 */

		arrDataValues[35][0] = "HTTP Failures(All)/Sec";
		arrDataValues[35][1] = "7";
		arrDataValues[35][2] = "1";
		arrDataValues[35][3] = "0";
		arrDataValues[35][4] = "-1";

		arrDataValues[36][0] = "HTTP Failures/Sec";
		arrDataValues[36][1] = "7";
		arrDataValues[36][2] = "2";
		arrDataValues[36][3] = "0";
		arrDataValues[36][4] = "-1";

		/*
		 * Page Failures
		 * Page Failures(All)/Minute	8	1	1	-1
		 * Page Failures/Minute	8	2	1	-1
		 */

		arrDataValues[37][0] = "Page Failures(All)/Minute";
		arrDataValues[37][1] = "8";
		arrDataValues[37][2] = "1";
		arrDataValues[37][3] = "1";
		arrDataValues[37][4] = "-1";

		arrDataValues[38][0] = "Page Failures/Minute";
		arrDataValues[38][1] = "8";
		arrDataValues[38][2] = "2";
		arrDataValues[38][3] = "1";
		arrDataValues[38][4] = "-1";

		/*
		 * Session Failures	9
		 * Sessions Failures(All)/Minute	9	1	3	-1
		 * Sessions Failures/Minute	9	2	3	-1
		 */

		arrDataValues[39][0] = "Sessions Failures(All)/Minute";
		arrDataValues[39][1] = "9";
		arrDataValues[39][2] = "1";
		arrDataValues[39][3] = "3";
		arrDataValues[39][4] = "-1";

		arrDataValues[40][0] = "Sessions Failures/Minute";
		arrDataValues[40][1] = "9";
		arrDataValues[40][2] = "2";
		arrDataValues[40][3] = "3";
		arrDataValues[40][4] = "-1";

		/*
		 * Transactions Failures	10
		 * Transactions Failures(All)/Minute	10	1	2	-1
		 * Transactions Failures/Minute	10	2	2	-1
		 */

		/*arrDataValues[41][0] = "Transactions Failures(All)/Minute";
		arrDataValues[41][1] = "10";
		arrDataValues[41][2] = "1";
		arrDataValues[41][3] = "2";
		arrDataValues[41][4] = "-1";

		arrDataValues[42][0] = "Transactions Failures/Minute";
		arrDataValues[42][1] = "10";
		arrDataValues[42][2] = "2";
		arrDataValues[42][3] = "2";
		arrDataValues[42][4] = "-1";*/

	}

	//Method to get the Object Type of Graph on the basis of Group Id and Graph Id of Graph.
	public String getObjectTypeOfGraph(String groupId, String graphId)
	{
		Log.debugLog(className, "getObjectTypeOfGraph", "", "", "Method Called.");
		try
		{

			if(groupId.equals("") && graphId.equals(""))
			{
				Log.debugLog(className, "getObjectTypeOfGraph", "", "", "Group Id or Graph Id is empty.");
				return "NA###NA";
			}
			else
			{
				for(int rowIndex = 0; rowIndex < arrDataValues.length; rowIndex++ )
				{
					if(arrDataValues[rowIndex][1].equals(groupId) && arrDataValues[rowIndex][2].equals(graphId))
					{
						return arrDataValues[rowIndex][3] + "###" + arrDataValues[rowIndex][4];
					}
				}
				return "NA###NA";
			}
		}
		catch(Exception e)
		{
			Log.stackTraceLog(className, "getObjectTypeOfGraph", "", "", "Exception in Getting Object Type--", e);
			e.printStackTrace();
			return "NA###NA";
		}
	}

	public static void main(String[] args)
	{
		ExecutionDrillDown exeDrillDown = new ExecutionDrillDown();
		System.out.println("The Object type is === "+exeDrillDown.getObjectTypeOfGraph("5", "6"));
		String []arr = exeDrillDown.getObjectTypeOfGraph("5", "6").split("###");
		System.out.println("1st value=="+arr[0]+"2nd value==="+arr[1]);
	}
}
