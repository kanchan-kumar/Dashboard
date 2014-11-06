/**
 * Name : ExportMethodCallToHTML.java
 * Author: Sai Manohar
 * Purpose : This Class is mainly used for exporting method call tree in a HTML Format in to a file
 */
package pac1.Bean;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Vector;

public class ExportMethodCallToHTML 
{
    public static String className = "MethodCallHTML";
	private String[][] arrDataValues = null;
	private String[][] arrMethoDataValues = null;
	private int currentLevel = 1;
	private int unOrderListUsed = 1;
	private int defaultHighLightWallTime = 1500;
	private String testRun = "";
	private String filterCriteria = "";
	DecimalFormat df = new DecimalFormat("#.##");
	private String enableCpuTime = "0";
	private String enableWallTime = "1";
	private String startTime = "";
	private String fpDuration = "";
	
	
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public void setFpDuration(String fpDuration) {
		this.fpDuration = fpDuration;
	}

	public void setEnableCpuTime(String enableCpuTime) {
		this.enableCpuTime = enableCpuTime;
	}

	public void setEnableWallTime(String enableWallTime) {
		this.enableWallTime = enableWallTime;
	}

	public void setTestRun(String testRun) 
	{
		this.testRun = testRun;
	}

	public void setFilterCriteria(String filterCriteria) 
	{
		this.filterCriteria = filterCriteria;
	}
	
	public void setDefaultHighLightWallTime(int defaultHighLightWallTime) 
	{
		this.defaultHighLightWallTime = defaultHighLightWallTime;
	}
	
	public String getFilterCriteria() 
	{
		return filterCriteria;
	}

	public String getTestRun() 
	{
		return testRun;
	}
    
	public int getDefaultHighLightWallTime() 
	{
		return defaultHighLightWallTime;
	}

	/**
	 * This method will write all the code needed for exporting method calling tree in to a html format
	 */
	public boolean exportMethodCallToHTML(String[][] arrDataValues, String[][] arrMethoDataValues, String htmlName) 
	{
		try 
		{
	    set2DArray(arrDataValues);
	    set2DMethArray(arrMethoDataValues);
		String fileName = Config.getWorkPath() + "/webapps/netstorm/temp/" + htmlName;
        File fileHTML = new File(fileName);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(fileHTML));
        final PrintWriter writer = new PrintWriter(out);
        
        //In Case of Aggregate Sequence Diagram - start time is empty string
        if(!startTime.trim().equals("") && !fpDuration.trim().equals(""))
          filterCriteria = filterCriteria  + ", " + startTime + ", " + fpDuration;
        
        writer.println("<html>");
        writer.println("<title> Method Calling Tree - Test Run Number : " + testRun + "</title>");
        writer.println("<head><h1>Method Calling Tree - Test Run Number : " + testRun + "</h1></head></br>");
        writer.println("<h2> Filter Criteria : "+ filterCriteria +"</h2></br>");
        writer.println("</html>");
        writer.println(" <style type='text/css'> ");
        writer.println("body { font-family:verdana; font-size:12px; } li  { cursor:pointer;  display:block; color:#000080; } .Folder  { color:#000080; }");
        writer.println(" .ExpandCollapse  { float:left; margin-right:5px; width:8px; } ul { list-style-type:none; } #ScrollablePane { border: 1px solid #b7b7b7; }");
        writer.println("h1{ color:#000080; font-size:15px; font-weight:bold; height:18px; text-align: left; padding-left: 10px; text-align:center; border-left:1px solid #ECE9DA; border-right:1px solid #ECE9DA;}");
        writer.println("h2{ font-size:12px;font-weight:normal;}");
        writer.println(".tableTitle{color:#073991;background-color:#E8EFFA;font-size:11px;font-weight:bold;height:20px;text-align:left;padding-left: 10px;}");
        writer.println(".table{border-top:2px solid #BCD0EF;border-left:1px solid #BCD0EF;border-bottom:1px solid #BCD0EF;border-right:1px solid #BCD0EF;font-size:10px;color:blue;}");
        writer.println(".tableCell{border-top:1px solid white;border-right:1px solid #ADC6E9;border-bottom:1px solid #ADC6E9;border-left:1px solid white;overflow: hidden;text-overflow: ellipsis;}");
        writer.println("td.tableHeaderLockedEnhance, tr.tableHeaderLockedEnhance {color:#000080;font-size:10px;font-weight:bold;height:20px;left: expression(document.getElementById(" + "ScrollablePane" + ").scrollRight);position:relative;z-index: 10;overflow: hidden;;}");
        
        
        writer.println(".tableRowOddNew {background-color:FFFFFF;color:#000000;}");
        writer.println(".tableRowEvenNew {background-color:f7fcff;color:#000000;}");
          
         
        writer.println(" </style> ");
        writer.println("<script language=" + "\"" + "javascript" + "\"" +">");
        writer.println("var length = 0;");  
        writer.println("var methodArrayLen = 0;");
        writer.println("function resolveSrcMouseover(e) { var node = e.srcElement == undefined ? e.target : e.srcElement; if (node.nodeName != "+ "\"" + "UL" + "\"" + ") { node.style.fontWeight= " + "\"" + "bold" + "\"" + "; } }");
        writer.println("function resolveSrcMouseout(e) { var node = e.srcElement == undefined ? e.target : e.srcElement; node.style.fontWeight = " + "\"" + "normal" + "\"" + "; }");
        writer.println("function takeAction(e) { var node = e.srcElement == undefined ? e.target : e.srcElement; var id = node.getAttribute('id'); var nodeId = " + "\"" + "ExpandCollapse" + "\"" + "+id" + "; if(document.getElementById(nodeId)== null) return;  if (id != null && id.indexOf(" + "\"" +  "Folder" + "\"" + ") > -1) { if (node.innerHTML == " + "\"" + "-" + "\"" +"){node.innerHTML = " + "\"" + "+" + "\"" +";document.getElementById(nodeId).style.display = " + "\"" + "none" + "\"" + ";} else if (node.innerHTML == " + "\"" + "+" + "\"" +"){node.innerHTML = " + "\"" + "-" + "\"" + ";document.getElementById(nodeId).style.display = " + "\"" + "block" + "\"" + ";}}}");
        writer.println(" function collapseAllNodes() { for(var i = length;i >= 1; i--) { var folderId = " + "\"" + "Folder" + "\"" + " + i; if(document.getElementById(folderId) != null) { document.getElementById(folderId).click(); } else continue; }}");        
        writer.println("document.write(" + "\"" + "<div id='ScrollablePane' style ='max-height:570px; max-width:100%; overflow:scroll; solid #DFE9F7;'><table align = center width=98% border = 0 style='table-layout:fixed, height:300px;' class='resizable'>" + "\"" + ");");
        for (int i = 0; i < arrDataValues.length; i++) 
        {          
		  if(i == 0)
		  {	
			  writer.println("length = " + arrDataValues.length + ";");
			  writer.println("document.write("+"\""+"<ul onmouseover='resolveSrcMouseover(event);' onmouseout='resolveSrcMouseout(event);'  onclick='takeAction(event);'><li><div id='Folder"+ currentLevel +"'class='ExpandCollapse'>-</div><div class='Folder' id = '"+i+"' style='font-size:12px;' title='" +  getToolTipForEachNode(i) + "'>"+ replaceCharacters(arrDataValues[i][0]) + "() [" + arrDataValues[i][1]+"&nbsp;ms]&nbsp;"+getPercentageForEachMethod(i)+"</div></li>" + "\"" + ");" );  
		  }
		  else if(i != 0 && (Integer.parseInt(arrDataValues[i][2]) >= Integer.parseInt(arrDataValues[i-1][2])))
		  {
			  if((Integer.parseInt(arrDataValues[i][2]) > Integer.parseInt(arrDataValues[i-1][2])))
			  {
				 writer.println("document.write(" + "\"" + "<ul id='ExpandCollapseFolder"+currentLevel+"'>" + "\"" + ");");
				 currentLevel++;
				 unOrderListUsed++;
				 writer.println("document.write("+ "\"" + "<li><div id='Folder"+currentLevel+"' class='ExpandCollapse'>-</div><div class='Folder'  id = '"+i+"' style='font-size:12px;' title='"+getToolTipForEachNode(i)+"'>"+replaceCharacters(arrDataValues[i][0]) + "() [" + arrDataValues[i][1]+"&nbsp;ms]&nbsp;"+getPercentageForEachMethod(i)+"</div></li>" + "\"" + ");");
			  }
			  else
			  {
				  currentLevel++;
				  writer.println("document.write("+ "\"" + "<li><div id='Folder"+currentLevel+"' class='ExpandCollapse'>-</div><div class='Folder'  id = '"+i+"' style='font-size:12px;' title='"+getToolTipForEachNode(i)+"'>"+replaceCharacters(arrDataValues[i][0]) + "() [" + arrDataValues[i][1]+"&nbsp;ms]&nbsp;"+getPercentageForEachMethod(i)+"</div></li>" + "\"" + ");");        		
			  }
			  
			  int diff = 0;
			  if(i != arrDataValues.length-1)
			     diff = Integer.parseInt(arrDataValues[i][2]) - Integer.parseInt(arrDataValues[i+1][2]);
			  if(diff > 1)
			  {
				 for(int k =1; k < diff;k++)
				   writer.println("document.write("+ "\"" + "</ul>" + "\"" + ");");		  
			  }      
		  }
		  else
		  {
			  writer.println("document.write(" + "\"" + "</ul>" + "\"" +");");
			  currentLevel++;
			  writer.println("document.write("+ "\"" + "<li><div id='Folder" + currentLevel + "' class='ExpandCollapse'>-</div><div class='Folder' id = '"+i+"' style='font-size:12px;' title='"+getToolTipForEachNode(i)+"'>"+replaceCharacters(arrDataValues[i][0]) + "() [" + arrDataValues[i][1]+"&nbsp;ms]&nbsp;"+getPercentageForEachMethod(i)+"</div></li>" + "\"" + ");");
			  int diff = 0;
			  if(i != arrDataValues.length-1)
			     diff = Integer.parseInt(arrDataValues[i][2]) - Integer.parseInt(arrDataValues[i+1][2]);
			  if(diff > 1)
			  {
				 for(int k =1; k < diff;k++)
				   writer.println("document.write("+ "\"" + "</ul>" + "\"" + ");");		  
			  } 
		  }
		}
        for(int j =0;j < unOrderListUsed;j++)
        	 writer.println("document.write("+ "\"" + "</ul>" + "\"" + ");");
        
        writer.println("document.write("+ "\"" + "</table></div><br>" + "\"" + ");" );
        highLightWallTime(writer);
        //null in case of Aggregate Sequence Diagram
        if(this.arrMethoDataValues != null)
          createSummaryMethodTable(writer);
        writer.println("</script>");
        collapseAllNodes(writer);
        
        writer.close();
        return true;
		}
        catch (IOException e) 
		{
        	Log.stackTraceLog(className, "exportMethodCallToHTML", "", "", "Exception in Creating Blank File", e);
			return false;
		}
	}
	
	// Function for showing the download report for method timing 
	private void createSummaryMethodTable(PrintWriter writer) 
	{    
		 writer.println("document.write(" + "\"" + "<table border=1><tr><td>" + "\"" + ");");
		 writer.println("methodArrayLen = " + arrMethoDataValues.length + ";");
  		 writer.println("function createSummaryMethodTable() {");
         writer.println("var tableTile = " + "\"" + "Summary of Methods ( Number of methods : " + "\"" + "+ (methodArrayLen - 1) +" + "\"" + ")" +"\"" + ";");
		 writer.println("var tableHead = " + "\"" +"<table width=100% border=0><tr><td align=left style='font-size:12px;' class='tableTitle' width=40%>" + "\"" + " + tableTile +" + "\"" + "</td></tr></table>" + "\"" + ";");
    	 writer.println("document.write(" + "\"" + "<table align = center cellpadding='0' cellspacing='0' class='table' width=100% max-width=1200px><tr height = 22px>" + "\"" + ");" );
		 writer.println("document.write(" + "\"" + "<td>" + "\"" + "+ tableHead + " + "\"" + "</td></tr>" + "\""  + ");");
		 writer.println("document.write(" + "\"" + "<tr> <td align='center' valign='top'><table width='100%' cellpadding='0' cellspacing='0'>" + "\"" + ");");
		 writer.println("document.write(" + "\"" + "<br><tr><td><div id='ScrollablePane' STYLE='max-height:350px; max-width:100%; overflow:scroll; solid #DFE9F7;'>" + "\"" + ");");
		 writer.println("document.write(" + "\"" + "<table id='MethodCumulativeId' border=1 width='150%' style='{table-layout:fixed}' align=center cellpadding='3%' cellspacing=0  >" + "\"" + ");");
		 writer.println("document.write(" + "\"" + "<thead id = 'pfh' ><tr align=center class = tableHeaderLockedEnhance >" + "\"" + ");" );
		 
		 
		 // Creating headers for the method timing table
		 for(int i = 0; i < arrMethoDataValues[0].length; i++)
		    {
		     if(i >= 12 && i<= 14)
		         writer.println("document.write(" + "\"" + "<th class='tableCell' width=11% align=left style='font-size:11px;' wrap title='" + arrMethoDataValues[0][i] + "' >" +  arrMethoDataValues[0][i] + "</b></th>" + "\"" + ");" );
		     if(i == 15)
		    	 writer.println("document.write(" + "\"" + "<th class='tableCell' width=6% align=right style='font-size:11px;' wrap title='Percentage for Self Time'>" + arrMethoDataValues[0][i] + "</b></th>" +"\"" + ");");
		     if(i >= 16 && i<=17)
     		     writer.println("document.write(" + "\"" + "<th class='tableCell' width=8% align=right style='font-size:11px;' wrap title='" + arrMethoDataValues[0][i]  + "' >" + arrMethoDataValues[0][i]  + "</b></th>" + "\"" + ");" );
		    
     		  // CPU time columns will be added if it is enabled    
		     if(i == 18  && enableCpuTime.equals("1"))
		    	 writer.println("document.write(" + "\"" + "<th class='tableCell' width=8% align=right style='font-size:11px;' wrap title='" + arrMethoDataValues[0][i]  + "' >" + arrMethoDataValues[0][i]  + "</b></th>" + "\"" + ");" );
		     if(i == 19  && enableCpuTime.equals("1"))
		    	 writer.println("document.write(" + "\"" + "<th class='tableCell' width=8% align=right style='font-size:11px;' wrap title='" + arrMethoDataValues[0][i]  + "' >" + arrMethoDataValues[0][i]  + "</b></th>" + "\"" + ");" );
		     // Wall time columns will be added if it is enabled
		     if(i == 20 && enableWallTime.equals("1"))
	    		 writer.println("document.write(" + "\"" + "<th class='tableCell' width=8% align=right style='font-size:11px;' wrap title='" + arrMethoDataValues[0][i]  + "' >" + arrMethoDataValues[0][i]  + "</b></th>" + "\"" + ");" );	 
	   		 if(i == 21 && enableWallTime.equals("1"))
	   			 writer.println("document.write(" + "\"" + "<th class='tableCell' width=8% align=right wrap style='font-size:11px;' title='" + arrMethoDataValues[0][i]  + "' >" + arrMethoDataValues[0][i]  + "</b></th>" + "\"" + ");" ); 	     
    		 if(i == 22)
    			 writer.println("document.write(" + "\"" + "<th class='tableCell' width=6% align=right wrap style='font-size:11px;' title='" + arrMethoDataValues[0][i]  + "' >" + arrMethoDataValues[0][i]  + "</b></th>" + "\"" + ");" ); 	     
		     }
    		 writer.println("document.write(" + "\"" + "</tr></thead>" + "\"" +");");
		 
    		// Putting the data into the method timing table 
   		    for(int j = 1; j < arrMethoDataValues.length; j++)
    		{
   		       if(j%2==0)
    		   {
    		  	  writer.println("document.write(" + "\"" + "<tr>" +"\"" + ");");
    		   }
    		   writer.println("document.write(" + "\"" + "<td class='tableCell' align = left style='font-size:11px;' title=" +  arrMethoDataValues[j][14]  + " wrap>" +  arrMethoDataValues[j][14]  + "</td>" + "\"" + ");" );
    		   writer.println("document.write(" + "\"" + "<td class='tableCell' align = left style='font-size:11px;' title=" +  arrMethoDataValues[j][13]  + " wrap>" +  arrMethoDataValues[j][13]  + "</td>" + "\"" + ");" );
    		   writer.println("document.write(" + "\"" + "<td class='tableCell' align = left style='font-size:11px;' title=" +  replaceCharacters(arrMethoDataValues[j][12])  + " wrap>" +  replaceCharacters(arrMethoDataValues[j][12]) + "</td>" + "\"" + ");" );
    		   writer.println("document.write(" + "\"" + "<td class='tableCell' align = right style='font-size:11px;' title=" +  arrMethoDataValues[j][15]  + " wrap>" +  arrMethoDataValues[j][15]  + "</td>" + "\"" + ");" );
    		   writer.println("document.write(" + "\"" + "<td class='tableCell' align = right style='font-size:11px;' title=" +  arrMethoDataValues[j][16]  + " wrap>" +  arrMethoDataValues[j][16]  + "</td>" + "\"" + ");" );
    		   writer.println("document.write(" + "\"" + "<td class='tableCell' align = right style='font-size:11px;' title=" +  arrMethoDataValues[j][17]  + " wrap>" +  arrMethoDataValues[j][17]  + "</td>" + "\"" + ");" );
    		
               // Cpu time columns will be added if it is enabled    		   
    		   if(enableCpuTime.equals("1"))
    		   {
    			writer.println("document.write(" + "\"" + "<td class='tableCell' align = right style='font-size:11px;' title=" +  arrMethoDataValues[j][18]  + " wrap>" + arrMethoDataValues[j][18]  + "</td>" + "\"" + ");" );
    			writer.println("document.write(" + "\"" + "<td class='tableCell' align = right style='font-size:11px;' title=" + arrMethoDataValues[j][19]  + " wrap>" + arrMethoDataValues[j][19]  + "</td>" + "\"" + ");" );
    		   }
               // Wall time columns will be added if it is enabled
    		   if(enableWallTime.equals("1"))
    		   {
    			writer.println("document.write(" + "\"" + "<td class='tableCell' align = right style='font-size:11px;' title=" + arrMethoDataValues[j][20]  + "wrap>" +  arrMethoDataValues[j][20]  + "</td>" + "\"" + ");" );
    			writer.println("document.write(" + "\"" + "<td class='tableCell' align = right style='font-size:11px;' title=" +  arrMethoDataValues[j][21]  + "wrap>" +  arrMethoDataValues[j][21]  + "</td>" + "\"" + ");" );
    		   }
    		   writer.println("document.write(" + "\"" + "<td class='tableCell' align = right style='font-size:11px;' title=" +  arrMethoDataValues[j][22]  + " wrap>" + arrMethoDataValues[j][22]  + "</td>" + "\"" + ");" );
    		   
    		   writer.println("document.write(" + "\"" + "</tr>" + "\"" + ");" );
    		}
   		    
     		 writer.println("document.write(" + "\"" + "</table></td></tr></table>" + "\"" + ");"); 
     		 writer.println("}");
	}
	
	/**
	 * This Method will call the collapse function for Downloaded HTML
	 * @param writer
	 */
	private void collapseAllNodes(PrintWriter writer) 
	{
	 //null in case of Aggregate Sequence Diagram
	 if(this.arrMethoDataValues != null)
       writer.println("<script>collapseAllNodes();createSummaryMethodTable();</script>");
	 else
	   writer.println("<script>collapseAllNodes();</script>");
	}  
	
	private void highLightWallTime(PrintWriter writer) 
	{
		for (int i = 0; i < arrDataValues.length; i++) 
		{
			if(defaultHighLightWallTime == 0 && arrDataValues[i][1].trim().equals("< 1"))
			{
				writer.println("document.getElementById(" + i + ").style.color = " + "\"" + "#FF0000" + "\"" + ";");     
			}
			
			if(!arrDataValues[i][1].trim().equals("< 1") && Integer.parseInt(arrDataValues[i][1]) >= defaultHighLightWallTime )
			{
			   writer.println("document.getElementById(" + i + ").style.color = " + "\"" + "#FF0000" + "\"" + ";");
			}
			else
			 continue;
		  }  
   }

	private String getToolTipForEachNode(int index) 
	{
		String titleMsg = "";
		titleMsg = "Method Name : " + arrDataValues[index][3]; 
		return titleMsg;
	}

	private void set2DArray(String[][] arrDataValues) 
	{
		if(arrDataValues != null)
          this.arrDataValues  = arrDataValues;	
		
	}
	
	
	private void set2DMethArray(String[][] arrMethoDataValues) 
	{
		if(arrMethoDataValues != null)
          this.arrMethoDataValues  = arrMethoDataValues;	
		
	}
	

	public String replaceCharacters(String str)
	{
		str = str.replace("<","");
		str = str.replace(">","");
		return str;
	}
	
	public String getPercentageForEachMethod(int index)
	{
		long flowpathDuration = 0L;
		//In Case Service timing is less than 1 as Issue is Observed in Kohls
		//java.lang.NumberFormatException: For input string: "< 1"
		if(arrDataValues[0][1].trim().equals("< 1"))
		  flowpathDuration = 0;
		else
		  flowpathDuration = Long.parseLong(arrDataValues[0][1]);
		long currentMethodDuration = 0L;
		double percentage = 0; 
		
		if(arrDataValues[index][1].trim().equals("< 1"))
			currentMethodDuration = 0;
		else
		    currentMethodDuration = Long.parseLong(arrDataValues[index][1]);
 		percentage = ((double)currentMethodDuration * 100/flowpathDuration);
		return "["+ df.format(percentage) + "%"+"]";
	}

	public static void main(String[] args) 
	{
        ExportMethodCallToHTML methCall = new ExportMethodCallToHTML();
        //Getting Data
        NDGenerateSequenceDiagram ndGenSeqDiagRam = new NDGenerateSequenceDiagram();
        // taking flow path instance csv on local
        Vector<String> vecBlobData = rptUtilsBean.readFileInVector("/home/netstorm/fileOutput3.txt");
        String[][] arrBlobData = null;
        int colsize = rptUtilsBean.split(vecBlobData.get(0).toString(), ",").length;
        
        arrBlobData = new String[vecBlobData.size()][colsize];
        for(int i = 0; i < vecBlobData.size(); i++)
        {
        	String arrCsv[] = rptUtilsBean.split(vecBlobData.get(i).toString(), ",");
        	arrBlobData[i] = arrCsv.clone();
        }
        vecBlobData.clear(); // clearing all the data
        
        // taking method timing report csv on local
        vecBlobData = rptUtilsBean.readFileInVector("/home/netstorm/methSumm.txt");
        
        String[][] arrMethData = null;
        colsize = rptUtilsBean.split(vecBlobData.get(0).toString(), "|").length;
        arrMethData = new String[vecBlobData.size()][colsize];
        for(int i = 0; i < vecBlobData.size(); i++)
        {
        	String arrCsv[] = rptUtilsBean.split(vecBlobData.get(i).toString(), "|");
        	arrMethData[i] = arrCsv.clone();
        }
        
        arrBlobData = ndGenSeqDiagRam.getArrayForTreeBasedSequenceDiagram(arrBlobData);
        methCall.exportMethodCallToHTML(arrBlobData , arrMethData, "Test.html");
	}
}
