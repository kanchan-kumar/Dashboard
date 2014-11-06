// Name    : NDGenerateSequenceDiagram.java
// Author  : Sangeeta sahu
// Purpose : To generate flow chart
// Modification History:
//  06/02/12:Sangeeta: Initial Version
//  25/09/13:Sai Manohar:Enhancement For Filter Criteria 
// Purpose : To Provide Filter Option at Package,Class,Method level
// 06/05/14 : Added Method For Getting Tree Based Sequence Diagram
package pac1.Bean;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;

import java.awt.image.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class NDGenerateSequenceDiagram
{
  private String className = "NDGenerateSequenceDiagram";
  //shell output
  //EventId|NotReceived|FPSignature|TimeStamp|ThreadId|TierId|TierName|MethodId|MethodName|WallTime|CpuTime|BCIArg
  //after adding index for filtering
  //idx|EventId|NotReceived|FPSignature|TimeStamp|ThreadId|TierId|TierName|MethodId|MethodName|WallTime|CpuTime|BCIArg
  private final int idxOfEventId = 1;
  private final int idxOfMethodId = 8;
  private final int idxOfMethodName = 9;
  private final int idxOfWallTime = 10;
  private final int idxOfInfo = 12;
  private final int idxOfCompareInfo = 13;
  private final int idxOfReceivedStatus = 2;
  private final int defaultHeight = 50; //it will be used to calculate the Bar Length
  private final int incrementSpace = 25; //it will be used to calculate the Bar Length
  private int filterGreaterThan = -1; //This is to filter data whose wall time are greater Than this value.
  private int filterTop = -1; //This is to filter data whose wall time are top 10 or top 20 in the records.

  /* methodCallingSequenceList contain the sequence of methodId calling and exit in memory
   * and eventCallingSequenceList contain sequence of eventIds called in memory.
   * for examle one flow is like this-
   * MethodId   EventId
   * 2333        0
   * 4555        0
   * 6777        0
   * 6777        1
   * 4555        1
   * 2333        1
   * now methodCallingSequenceList will contain (2333, 4555, 6777, 6777, 4555, 2333)
   * and eventCallingSequenceList will contain (0, 0, 0, 1, 1, 1)
   * which will be used to know the parent of each method For Example
   * the parent of methodId 6777 is 4555
   * and the parent of methodId 4555 is 2333
   * which is required to know when we are drawing sequence diagram
   */
  private ArrayList<String> methodCallingSequenceList = new ArrayList<String>();
  private ArrayList<String> eventCallingSequenceList = new ArrayList<String>();
  //This arraylist give the time taken by methods.
  private ArrayList<String> timestampList = new ArrayList<String>();
  /*
   * uniqueListOfMethodIdCalled will contain the unique list of methodIds called in the flow. If any methodId is called again then we make it unique
   * by add "#" at the end of methodId.
   */
  private ArrayList<String> uniqueListOfMethodIdCalled = new ArrayList<String>();
  private ArrayList<Double> timeStampForUniqueMethodId = new ArrayList<Double>();
  private ArrayList<String> InfoForUniqueMethodId = new ArrayList<String>();
  private ArrayList<String> compareInfoForUniqeMethodId = new ArrayList<String>();
  private ArrayList<String> tmpExitReceivedStaus = new ArrayList<String>();
  private ArrayList<String> exitReceivedStaus = new ArrayList<String>();
  private ArrayList<String> pkgList = new ArrayList<String>();
  private ArrayList<String> clsList = new ArrayList<String>();
  private ArrayList<Integer> pkgSizeList = new ArrayList<Integer>();
  
  /**
   * ArrayList  for identifying Coordinates 
   */
  private ArrayList<String> coordinatesArrayList = new ArrayList<String>();

  /*
   * Bar height for methodId is calculated based on the number of methods called by any method.
   */
  private ArrayList<Double> barHeightForMethodId = new ArrayList<Double>();
  private ArrayList<Integer> barYPositionFotMethodId = new ArrayList<Integer>();


  private HashMap<String, Integer> methodIdLevelMap = new HashMap<String, Integer>();
  private HashMap<Integer, Integer> clsIdLevelMap = new HashMap<Integer, Integer>();
  private HashMap<Integer, String> clsIdPkgNameMap = new HashMap<Integer, String>();
  private HashMap<Integer, Integer> pkgLevelMap = new HashMap<Integer, Integer>();
  private HashMap<String, String> mapForMethodIdName = new HashMap<String, String>();

  private int startX = 20;
  private int startY = 20;
  private int widthOfBox = 300;
  private int heightOfBox = 25;
  private int gapBetweenBox = 2;
  private int widthOfBar = 5;  //vertical bars drawn for methods
  private int hightlightWallTime = 1500;
  private int barb;
  private double phi;

  private Color colorOfFirstTierBox[] =  {new Color(39,176,26), new Color(228,78,212), new Color(222,195,33),new Color(39,176,26), new Color(228,78,212), new Color(222,195,33),new Color(39,176,26), new Color(228,78,212),new Color(39,176,26), new Color(228,78,212), new Color(222,195,33),new Color(39,176,26), new Color(228,78,212)};
  private Color colorOfSecondTierBoxArr[] = {new Color(220,220,220), new Color(245, 189, 92), new Color(220, 197, 33), new Color(255,188,217), new Color(0,183,235), new Color(251, 236, 93)};
  private Color borderColorOfBox = Color.black;
  private Color colorOfStringInsideBox = Color.black;
  private Color colorOfDottedLine = Color.black;
  private Color colorOfBarBorder = Color.black;   //Bar are the verticals line drawn for method
  private Color colorOfBar = Color.green;
  private Color colorOfArrowLine = Color.blue;

  private Rectangle2D rectFirstTierArr[] = null;
  private Rectangle2D rectSecondTierArr[] = null;

  private float dash1[] = {2.0f};
  private Stroke strokeForLineThinkness = new BasicStroke(1);
  private BasicStroke StrokeForDashline = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dash1, 0.0f);
  private boolean isForCompare = false;
  
  private int width;
  private int height;



public NDGenerateSequenceDiagram()
  {
    phi = Math.toRadians(15);
    barb = 12;
  }
  
  /**
   * Getters method to get Height of Image.
   */
  public int getHeight()
  {
    return height;  
  }
  
  /**
   * Getters Method to get Width of Image.
   */
  public int getWidth()
  {
    return width;
  }
  
  
  /**
   * Setter for store width
   */
  public void setWidth(int width)
  {
    this.width = width;
  }
  
  
  /**
   * Setter for store height
   */
  public void setHeight(int height)
  {
    this.height = height;
  }
  
  /**
   * Returns ArrayList with Cordinates
   * @return
   */
  public ArrayList<String> getCoordinatesArrayList() {
		return coordinatesArrayList;
	}
   
  public void createSequenceDiagram(String[][] flowData, String imageName, StringBuffer msg)
  {
    Log.debugLog(className, "createSequenceDiagram", "", "", "method called");
    try
    {
    long startProcessingTime = System.currentTimeMillis();
    setDataToMethodCallExitVector(filterData(flowData));
    setMethodNameWithTimeStamp();
    setDataToPkgAndClass();
    setRectOfSecondTierArr();
    setRectOfFirstTierArr();

    if(barHeightForMethodId.size() == 0)
    {
      msg.append("No data found according to current filter setting.");
      Log.debugLog(className, "createSequenceDiagram", "", "", "No data found according to current filter setting.");
      return;
    }
    
    Log.debugLog(className, "createSequenceDiagram", "", "", "No of methods: "+barHeightForMethodId.size());

    int totalSize = 0;
    for(int i = 0; i < pkgList.size(); i++)
    {
      totalSize += pkgSizeList.get(i);
    }

    int width = startX + totalSize*(widthOfBox + gapBetweenBox) + 20;
    // for filtering only put a check, this is only for testing
    int hightFilter = defaultHeight*(uniqueListOfMethodIdCalled.size()-1) + incrementSpace*(uniqueListOfMethodIdCalled.size());
   
    int height = (int)(startY + (heightOfBox + incrementSpace)*2 + hightFilter );//works for both filter and general
    String imagePath = Config.getWorkPath()+"/webapps/netstorm/temp"+"/"+imageName;
    
    long endProcessingTime = System.currentTimeMillis();
    String totalTime = rptUtilsBean.convertMilliSecToSecs(endProcessingTime - startProcessingTime);
    Log.debugLog(className, "createSequenceDiagram", "", "", "time taken in parsing of data: "+totalTime);
    
    Log.debugLog(className, "createSequenceDiagram", "", "", "going to create image width width:"+width +" and height:"+height);
    
    //Setting Width and Height of Image for Use in JSP.
    setWidth(width);
    setHeight(height);
    
    //adding try catch block due to exception of heap size in case of large image.
    BufferedImage image = null;
    try
    {
      image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }
    catch(OutOfMemoryError e)
    {
      //Log.stackTraceLog(className, "createSequenceDiagram", "", "", "", e);
      Log.debugLog(className, "createSequenceDiagram", "", "", "not able to create image due to large size of image.");
      msg.append("Error in generating sequence diagram as this flowpath has too many methods. Use Filters to Reduce Number of Methods or Click Method Call Tree to View Method Call Hierarchy");
      
      return;
    }
    catch(ArrayIndexOutOfBoundsException ai)
    {
    	Log.debugLog(className, "createSequenceDiagram", "", "", "not able to create image due to large data.");
    	msg.append("Error in generating sequence diagram due to large data. Please Verify the logs.");
    	
    	return;
    	
    }
    catch(NumberFormatException n)
    {
    	Log.debugLog(className, "createSequenceDiagram", "", "", "not able to create image due to mismatch of data.");
    	msg.append("Error in generating sequence diagram due to large data. Please Verify the logs.");
    	return;
    	
    }
    catch (Exception e) {
    	Log.debugLog(className, "createSequenceDiagram", "", "", "not able to create image due to exception."+e);
    	msg.append("Error in generating sequence diagram due to large data. Please Verify the logs.");
    	return;
	}
    
    Graphics g = image.getGraphics();

    g.setColor(Color.white);
    g.fillRect(0, 0, width, height);

    Graphics2D g1 = (Graphics2D)g;
    Font f = new Font("Arial", Font.PLAIN, 12);
    g1.setFont(f);
    //draw second tier
    int j = 0;
    for(int i = 0; i < rectSecondTierArr.length; i++)
    {
      g1.setStroke(strokeForLineThinkness);
      g1.setPaint(borderColorOfBox);
      g1.draw(rectSecondTierArr[i]);
      if(j == colorOfSecondTierBoxArr.length)
        j = 0;
      g1.setPaint(colorOfSecondTierBoxArr[j++]);
      g1.fill(rectSecondTierArr[i]);
      g1.setPaint(colorOfStringInsideBox);

      //int X = (int)rectSecondTierArr[i].getCenterX() - getLimitedLengthString(pkgList.get(i)).toString().length()*3;
      int X = (int)rectSecondTierArr[i].getCenterX() - getLimitedLengthStringBasedOnSize(pkgList.get(i), (int)rectSecondTierArr[i].getWidth()).toString().length()*3;
      int Y = (int)rectSecondTierArr[i].getCenterY() + 3;
      g1.drawString(getLimitedLengthStringBasedOnSize(pkgList.get(i), (int)rectSecondTierArr[i].getWidth()), X, Y);
    }

    //draw first tier
    j = 0;
    for(int i = 0; i < rectFirstTierArr.length; i++)
    {
      g1.setStroke(strokeForLineThinkness);
      g1.setPaint(borderColorOfBox);
      g1.draw(rectFirstTierArr[i]);
      if(j == colorOfFirstTierBox.length)
        j = 0;
      g1.setPaint(colorOfFirstTierBox[j++]);
      g1.fill(rectFirstTierArr[i]);
      g1.setPaint(colorOfStringInsideBox);

      //int X = (int)rectFirstTierArr[i].getCenterX() - getLimitedLengthString(clsList.get(i)).toString().length()*3;
      int X = (int)rectFirstTierArr[i].getCenterX() - getLimitedLengthStringBasedOnSize(clsList.get(i), (int)rectFirstTierArr[i].getWidth()).length()*3;
      int Y = (int)rectFirstTierArr[i].getCenterY() + 3;

      //g1.drawString(getLimitedLengthString(clsList.get(i)), X, Y);
      g1.drawString(getLimitedLengthStringBasedOnSize(clsList.get(i), (int)rectFirstTierArr[i].getWidth()), X, Y);
    }
    
    //Draw circle
    int startXOfDottedLine = startX + widthOfBox/2;
    int startYOfDottedLine = (startY + 2*heightOfBox + gapBetweenBox);
    int heightOfDottedLines = (int)(hightFilter + incrementSpace*2);//changed here--bug here too?
    int barX = startXOfDottedLine - 2;
    int barY = startYOfDottedLine + incrementSpace;
    coordinatesArrayList.add(startX + "," + startYOfDottedLine + "," + barX + "," + barY + ":" + getMethodNameFromMethodSignature(0));
    int widthOfArrowLine = widthOfBox + gapBetweenBox;

    String labelMsg = "";
    int startXofLabel = 0;
    int startYofLabel = 0;

    //this is to draw circle at the start point
    int circleStartX = 10;
    int circleStarty = (startY + 2*heightOfBox + gapBetweenBox);
    g1.setStroke(strokeForLineThinkness);
    Shape circle = new Ellipse2D.Float(circleStartX, circleStarty + incrementSpace-5, 10.0f, 10.0f);
    g1.draw(circle);
    g1.setPaint(colorOfArrowLine);
    g1.fill(circle);
    g1.drawLine(circleStartX, circleStarty + incrementSpace, startXOfDottedLine, circleStarty + incrementSpace);
    drawArrowHead(g1, new Point(startXOfDottedLine, circleStarty+ incrementSpace), new Point(circleStartX, circleStarty + incrementSpace), colorOfArrowLine);
    String methodNameStr = getMethodNameFromMethodSignature(uniqueListOfMethodIdCalled.get(0),startXOfDottedLine - circleStartX);
    
    //g1.drawString(getMethodNameFromMethodSignature(uniqueListOfMethodIdCalled.get(0), startXOfDottedLine - circleStartX), circleStartX, circleStarty + incrementSpace-5);
    
    f = new Font("Arial", Font.PLAIN, 10);
    g1.setFont(f);
    g1.setPaint(colorOfDottedLine);
    
    g1.setPaint(colorOfArrowLine);
    f = new Font("Arial", Font.PLAIN, 12);
    g1.setFont(f);
    String timeStr = "";
    
      /*if(isForCompare)
        g1.drawString("[" + compareInfoForUniqeMethodId.get(0) + "] ms", circleStartX + 65, circleStarty + incrementSpace - 5);
      else
        g1.drawString("  [" + timeStampForUniqueMethodId.get(0).intValue() + " ms]", circleStartX + 123, circleStarty + incrementSpace - 5);*/
    
    if(isForCompare)
    	timeStr = "  [" + compareInfoForUniqeMethodId.get(0) + "] ms";
    else
    	timeStr = "  [" + timeStampForUniqueMethodId.get(0).intValue() + " ms]";
    
    String methodNameWithTimeStr = methodNameStr + "  " + timeStr;
    
    g1.drawString(methodNameWithTimeStr, circleStartX, circleStarty + incrementSpace-5);
    
    f = new Font("Arial", Font.PLAIN, 10);
    g1.setFont(f);
    g1.setPaint(colorOfDottedLine);
    g1.drawString(InfoForUniqueMethodId.get(0), circleStartX + 15, circleStarty + incrementSpace+15);
    
    f = new Font("Arial", Font.PLAIN, 12);
    g1.setFont(f);
    for(int i = 0; i < uniqueListOfMethodIdCalled.size(); i++)
    {
      String fullSignature = ""; 
      //draw dotted Line
      g1.setStroke(StrokeForDashline);
      g1.setPaint(colorOfDottedLine);
      g1.drawLine(startXOfDottedLine, startYOfDottedLine, startXOfDottedLine, startYOfDottedLine + heightOfDottedLines);
      startXOfDottedLine += widthOfBox + gapBetweenBox;

      //draw thick bar
      g1.setStroke(strokeForLineThinkness);
      if(i != 0)
      {
        barX = getBarX(uniqueListOfMethodIdCalled.get(i));
        barY = barY + incrementSpace;
        barY = getBarY(uniqueListOfMethodIdCalled.get(i), barY);
        
        widthOfArrowLine = getLengthOfArrowLine(uniqueListOfMethodIdCalled.get(i));
        g1.setPaint(colorOfArrowLine);

        g1.drawLine(barX - widthOfArrowLine + widthOfBar, barY, barX, barY);
        fullSignature = getMethodNameFromMethodSignature(i);
        
        coordinatesArrayList.add(barX - widthOfArrowLine + widthOfBar + "," + ((int)(barY) - 10)  + "," + barX + "," + barY + ":" + fullSignature);
        
        drawArrowHead(g1, new Point(barX, barY), new Point(barX - widthOfArrowLine+widthOfBar, barY), colorOfArrowLine);

        labelMsg = getMethodNameFromMethodSignature(uniqueListOfMethodIdCalled.get(i), Math.abs(widthOfArrowLine));
        
        //String labelInfo = getLimitedLengthStringBasedOnSize(InfoForUniqueMethodId.get(i), Math.abs(widthOfArrowLine));
        String labelInfo = InfoForUniqueMethodId.get(i);
        
        startXofLabel = (barX - widthOfArrowLine + widthOfBar + barX)/2 - labelMsg.length()*4;
        startYofLabel = barY - 5;
        
        String timestr = "";
        
        if(isForCompare)
        	timestr = "[" + compareInfoForUniqeMethodId.get(i) + "] ms";
        else
        	if(timeStampForUniqueMethodId.get(i).intValue() < 1)
        		timestr = "[ < 1 ms]";
        	else
        		timestr = "[" + timeStampForUniqueMethodId.get(i).intValue()+" ms]";
        
        labelMsg = labelMsg + "  " + timestr;
        g1.drawString(labelMsg, startXofLabel, startYofLabel);
        
       /* int startXofLabel1 = (barX - widthOfArrowLine + widthOfBar + barX)/2 + labelMsg.length()*3;
        
        if(isForCompare)
          g1.drawString("[" + compareInfoForUniqeMethodId.get(i) + "] ms", startXofLabel1, startYofLabel);
        else
        {
        	//Checking whether method time is less than 1 ms
        	if(timeStampForUniqueMethodId.get(i).intValue() < 1)
        		g1.drawString("  [ < 1 ms]", startXofLabel1, startYofLabel);
        	else
        	    g1.drawString("  [" + timeStampForUniqueMethodId.get(i).intValue()+" ms]", startXofLabel1, startYofLabel);
        }
        */
        
        
        
        g1.setPaint(colorOfDottedLine);
        
        startXofLabel = (barX - widthOfArrowLine + widthOfBar + barX)/2 - labelInfo.length()*3;
        startYofLabel = barY + 15;
        f = new Font("Arial", Font.PLAIN, 10);
        g1.setFont(f);
        g1.drawString(labelInfo, startXofLabel, startYofLabel);
        g1.setPaint(colorOfArrowLine);

        f = new Font("Arial", Font.PLAIN, 12);
        g1.setFont(f);
        /*if(exitReceivedStaus.get(i).equals("1"))
          g1.setStroke(StrokeForDashline);*/
        g1.drawLine(barX - widthOfArrowLine + widthOfBar, barY + barHeightForMethodId.get(i).intValue(), barX, barY + barHeightForMethodId.get(i).intValue());
        
        coordinatesArrayList.add(barX - widthOfArrowLine + widthOfBar + "," + ((int)(barY + barHeightForMethodId.get(i).intValue()) - 10)  + "," + barX + "," + (int)(barY + barHeightForMethodId.get(i).intValue()) + ":" + fullSignature);
        
        drawArrowHead(g1, new Point(barX - widthOfArrowLine + widthOfBar, barY + barHeightForMethodId.get(i).intValue()), new Point(barX, barY + barHeightForMethodId.get(i).intValue()), colorOfArrowLine);

        startXofLabel = (barX - widthOfArrowLine + widthOfBar + barX)/2 - labelMsg.length()*3;
        startYofLabel = barY + barHeightForMethodId.get(i).intValue() - 5 ;
        g1.drawString(labelMsg, startXofLabel, startYofLabel);
      }

      //g1.setPaint(colorOfBarBorder);
      //Rectangle2D rect = new Rectangle(barX, barY, widthOfBar, barHeightForMethodId.get(i).intValue());
      //g1.draw(rect);
      if(isForCompare)
      {
        g1.setPaint(colorOfBar);
        String tmp[] = compareInfoForUniqeMethodId.get(i).split(",");
        int idxDiff = 2;
        for(int ll = 0; ll < tmp.length; ll++)
        {
          if(ll == idxDiff && Double.parseDouble(tmp[ll]) >= hightlightWallTime)
          {
            g1.setPaint(Color.red);
            break;
          }
          else if(ll == idxDiff)
            idxDiff += 2;
        }
      }
      else
      {
        if(timeStampForUniqueMethodId.get(i) < hightlightWallTime)
          g1.setPaint(colorOfBar);
        else
          g1.setPaint(Color.red);
      }
      if(exitReceivedStaus.get(i).equals("1"))
      {
        Color c = g1.getColor();
        g1.setPaint(colorOfBarBorder);
        Rectangle2D rect = new Rectangle(barX, barY, 1, barHeightForMethodId.get(i).intValue());
        g1.draw(rect);
        g1.setPaint(Color.white);
        g1.fill(rect);
        g1.setPaint(c);
        float[] dash = {7F, 7F};  
        Stroke dashedStroke = new BasicStroke(5F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1F, dash, 0F);
        g1.fill( dashedStroke.createStrokedShape(rect));
      }
      else
      {
        Color c = g1.getColor();
        g1.setPaint(colorOfBarBorder);
        Rectangle2D rect = new Rectangle(barX, barY, widthOfBar, barHeightForMethodId.get(i).intValue());
        g1.draw(rect);
        g1.setPaint(c);
        g1.fill(rect);
      }

      g1.setPaint(colorOfArrowLine);
     // g1.drawString(timeStampForUniqueMethodId.get(i).intValue()+" ms", barX + widthOfBar + 5, barY + (int)(barHeightForMethodId.get(i).intValue()/2));
      
      /*if(isForCompare)
        g1.drawString("(" + compareInfoForUniqeMethodId.get(i) + ") ms", barX + widthOfBar + 5, barY  + 10);
      else
        g1.drawString(timeStampForUniqueMethodId.get(i).intValue()+" ms", barX + widthOfBar + 5, barY  + 10);*/
      barYPositionFotMethodId.add(barY + barHeightForMethodId.get(i).intValue());
    }

    //this is to draw circle at the end point
    /*int circleEndX = startX + widthOfBox + incrementSpace;
    int circleEndy = (startY + 2*heightOfBox + gapBetweenBox + barHeightForMethodId.get(0).intValue());
    startXOfDottedLine = startX + widthOfBox/2;
    g1.setStroke(strokeForLineThinkness);
    Shape circle2 = new Ellipse2D.Float(circleEndX, circleEndy + incrementSpace-5, 10.0f, 10.0f);
    g1.draw(circle2);
    g1.setPaint(colorOfArrowLine);
    g1.fill(circle2);
    g1.drawLine(startXOfDottedLine, circleEndy + incrementSpace, circleEndX, circleEndy + incrementSpace);
    drawArrowHead(g1, new Point(circleEndX, circleEndy + incrementSpace), new Point(startXOfDottedLine, circleEndy+ incrementSpace), colorOfArrowLine);
    g.drawString(getMethodNameFromMethodSignature(uniqueListOfMethodIdCalled.get(0)), circleEndX, circleEndy + incrementSpace-5);*/

    //now create the Image
    FileOutputStream flt = null;
    try
    {
      flt = new FileOutputStream(new File(imagePath));
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    OutputStream sos = flt;

    try
    {
      ImageIO.write(image, "png", sos);
      sos.flush();
      sos.close();
    } catch (IOException e)
    {
      e.printStackTrace();
    }

    g.dispose();
    long endProcessingTimeOfImgae = System.currentTimeMillis();
    totalTime = rptUtilsBean.convertMilliSecToSecs(endProcessingTimeOfImgae - endProcessingTime);
    Log.debugLog(className, "createSequenceDiagram", "", "", "time taken to create image: "+totalTime);
    totalTime = rptUtilsBean.convertMilliSecToSecs(endProcessingTimeOfImgae - startProcessingTime);
    Log.debugLog(className, "createSequenceDiagram", "", "", "total time in parsing and creating image: "+totalTime);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "createSequenceDiagram", "", "", "", e);
      e.printStackTrace();
    }
  }

  private int getBarX(String methodId)
  {
    String clsName = getClassName(methodId);
    String pkgName = getPackageName(methodId);
    int level = methodIdLevelMap.get(methodId);
    //System.out.println(methodId + ", "+level );
    int barX = startX + widthOfBox/2 - 2;

    if(clsList.size() >= 0 && clsList.get(0).equals(clsName) && clsIdPkgNameMap.get(0).equals(pkgName) && level == 1)
    {
      return barX;
    }
    if(clsList.size() >= 0 && clsList.get(0).equals(clsName) && clsIdPkgNameMap.get(0).equals(pkgName) && level > 1)
    {
      return barX + (widthOfBox + gapBetweenBox)*(level-1);
    }

    for(int i = 1; i < clsList.size(); i++)
    {
      //System.out.println("clsList.get(i) = "+clsList.get(i)+" , "+clsName);
      //System.out.println("clsIdPkgNameMap.get(i) = "+clsIdPkgNameMap.get(i)+" , "+pkgName);
      if(clsList.get(i).equals(clsName) && clsIdPkgNameMap.get(i).equals(pkgName))
      {
        barX += (widthOfBox + gapBetweenBox)*(clsIdLevelMap.get(0) - 1) + (widthOfBox + gapBetweenBox)*level;
        break;
      }
      else
      {
        barX += clsIdLevelMap.get(i)*(widthOfBox + gapBetweenBox);
        //System.out.println("clsIdLevelMap.get(i) = "+clsIdLevelMap.get(i));
      }
    }
    return barX;
  }

  private int getBarY(String methodId, int barY)
  {
    int j = 0;
    for (int i = 0; i < methodCallingSequenceList.size(); i++)
    {
      if(methodId.equals(methodCallingSequenceList.get(i)))
      {
        j = i;
        break;
      }
    }

    if(eventCallingSequenceList.get(j-1).equals("0"))
      return barY;
    else
    {
      for(int i = 0; i < uniqueListOfMethodIdCalled.size(); i++)
      {
        if(methodCallingSequenceList.get(j-1).toString().equals(uniqueListOfMethodIdCalled.get(i)))
        {
          barY = (int)Double.parseDouble(barYPositionFotMethodId.get(i).toString()) + incrementSpace;
          break;
        }
      }
    }
    return barY;
  }

  private int getLengthOfArrowLine(String methodId)
  {
    String parent = getParentId(methodId);
   // System.out.println(methodId + " parent --> "+parent);
    int length = getBarX(methodId) - getBarX(parent);
    return length;
  }

  /*
   * This function is used to get the parent methodId of any MethodId
   */
  private String getParentId(String methodId)
  {
    String parentId = "";
    for (int i = 1; i < methodCallingSequenceList.size(); i++)
    {
      if(methodId.equals(methodCallingSequenceList.get(i)) )
      {
        if(eventCallingSequenceList.get(i-1).endsWith("0"))
        {
          parentId = methodCallingSequenceList.get(i-1);
        }
        else
        {
          parentId = getParentId(methodCallingSequenceList.get(i-1));
        }
        break;
      }
    }
    return parentId;
  }

  private void drawArrowHead(Graphics2D g2, Point tip, Point tail, Color color)
  {
    g2.setPaint(color);
    double dy = tip.y - tail.y;
    double dx = tip.x - tail.x;
    double theta = Math.atan2(dy, dx);
    double x, y, rho = theta + phi;
    for(int j = 0; j < 2; j++)
    {
      x = tip.x - barb * Math.cos(rho);
      y = tip.y - barb * Math.sin(rho);
      g2.draw(new Line2D.Double(tip.x, tip.y, x, y));
      rho = theta - phi;
    }
  }

  //this function get the blob data from file
  private String getBlobDataFromFile()
  {
    String fileName = "c:/home/netstorm/work/p2.txt";
    Log.debugLog(className, "getBlobDataFromFile", "", "", "Method start fileName = "+fileName);
    String blobData = "";
    try
    {
      File file = new File(fileName);
      if(!file.isFile())
      {
        Log.debugLog(className, "getBlobDataFromFile", "", "", "File not found");
        return null;
      }
      FileInputStream fis = new FileInputStream(fileName);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      String line = "";
      while((line = br.readLine()) != null)
      {
        if(line.trim().startsWith("#"))
          continue;
        else
          blobData = line;
      }
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getBlobDataFromFile", "", "", e.getMessage());
    }
    return blobData;
  }

  /* This function return the list of unique methodIds present in the flow
   * Input parameter is a string separated by '_'  containing data in following format:-
   * mathodId_eventId_Timestamp_CPUTime
   * CPUTime will be present in case of eventId 1
   * for example :-
   * 2334_0_45655787656_4554_0_45546567677_4554_1_455667889976_23_3444_0_45556765789676_3444_1_45568889998_12_2334_1_455667767765_45
   * then it will return a vector contaning data {2334, 4554, 3444}
   */
  private ArrayList<String> getMethodIdsFromBlobInfo(String str)
  {
    Log.debugLog(className, "getMethodIdsFromBlobInfo", "", "", "Method Started ");
    int noOfPipe = 3; //this is no of sepatretor
    int count = 0;
    ArrayList<String> tempVec = new ArrayList<String>();
    String tempStr = "";

    for(int i = 0; i < str.length(); i++)
    {
      if(count == 1 && str.charAt(i) == '0')
        noOfPipe = 3;
      else if(count == 1 && str.charAt(i) == '1')
        noOfPipe = 4;

      if(str.charAt(i) == '_')
        count++;

      if(count == noOfPipe)
      {
        tempStr = "";
        count = 0;
      }
      else if(count == 1)
      {
        if(!tempVec.contains(tempStr))
        {
          tempVec.add(tempStr);
          Log.debugLog(className, "getMethodIdsFromBlobInfo", "", "", "MethodId added to vector = "+tempStr);
        }
      }
      else
        tempStr += str.charAt(i);
    }
    return tempVec;
  }

  //this function return the methodId and name in a hashMap. Id as a key and Name as a value.
  private HashMap<String, String> getMethodNameFromMappingFile()
  {
    Log.debugLog(className, "getMethodNameFromMappingFile", "", "", "method start.");
    HashMap<String, String> methodIdName = new HashMap<String, String>();
    try
    {
      String fileName = "c:/home/netstorm/work/p1.txt";
      File file = new File(fileName);
      if(!file.isFile())
      {
        Log.debugLog(className, "getMethodNameFromMappingFile", "", "", "mapping file not found");
        return null;
      }

      FileInputStream fis = new FileInputStream(fileName);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      String line = "";
      String tempStr[] = null;

      while((line = br.readLine()) != null)
      {
        if(line.trim().startsWith("#"))
          continue;
        else
        {
          tempStr = line.split(" ");
          methodIdName.put(tempStr[0], tempStr[1]);
        }
      }
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getMethodNameFromMappingFile", "", "", e.getMessage());
    }
    return methodIdName;
  }

  /* This fuction read the input String Character by Character and
   * add the information related to one method to the ArrayList.
   *
   * InputParameter is the String that we are getting from blob which contain '_' separated data in following order
   * methodId_eventId_Timestamp_CpuTime
   * CPUTime will come if event id is 1.
   * for example:-
   * 2333_0_23456778743_4555_0_4556576778_45551_1_44568795646_23_2333_1_455565786877_30
   *
   * so we are adding to ArrayList :-
   * {2333_0_23456778743, 4555_0_4556576778, 45551_1_44568795646_23, 2333_1_455565786877_30}.
   */
  private ArrayList<String> getDataOfMethodCallingAfterParsingBlobData(String blobData)
  {
    Log.debugLog(className, "getDataOfMethodCallingAfterParsingBlobData", "", "", "Method Started ");
    int noOfPipe = 3; //this is no of separator
    int count = 0;
    ArrayList<String> tempVec = new ArrayList<String>();
    String tempStr = "";

    for(int i = 0; i < blobData.length(); i++)
    {
      if(count == 1 && blobData.charAt(i) == '0')
        noOfPipe = 3;
      else if(count == 1 && blobData.charAt(i) == '1')
        noOfPipe = 4;

      if(blobData.charAt(i) == '_')
        count++;

      if(count == noOfPipe)
      {
        tempVec.add(tempStr);
        Log.debugLog(className, "getDataOfMethodCallingAfterParsingBlobData", "", "", "string added to List = "+tempStr);
        tempStr = "";
        count = 0;
      }
      else if(i == (blobData.length() - 1))
      {
        tempStr += blobData.charAt(i);
        tempVec.add(tempStr);
        Log.debugLog(className, "getDataOfMethodCallingAfterParsingBlobData", "", "", "string added to list = "+tempStr);
      }
      else
        tempStr += blobData.charAt(i);
    }
    return tempVec;
  }

  private void setDataToMethodCallExitVector(String[][] flowData)
  {
    try
    {
      for(int i = 0; i < flowData.length; i++)
      {
        methodCallingSequenceList.add(flowData[i][idxOfMethodId]);   //add methodId
        eventCallingSequenceList.add(flowData[i][idxOfEventId]);    //add eventId
        timestampList.add(flowData[i][idxOfWallTime]);    //add timeStamp
        tmpExitReceivedStaus.add(flowData[i][idxOfReceivedStatus]);
        if(flowData[i][idxOfEventId].equals("0"))
        {
          //InfoForUniqueMethodId.add(flowData[i][idxOfInfo]); //add information.
          //Check In some cases the shell will give blank data. Handling for avoiding exception.
          if(flowData[i].length > idxOfInfo)
            InfoForUniqueMethodId.add(flowData[i][idxOfInfo]); //add information.
           else
             InfoForUniqueMethodId.add("");
          
          if(isForCompare)
            compareInfoForUniqeMethodId.add(flowData[i][idxOfCompareInfo]);
        }
        mapForMethodIdName.put(flowData[i][idxOfMethodId], flowData[i][idxOfMethodName]);
      }
    }
    catch(Exception e)
    {
      Log.errorLog(className, "setDataToMethodCallExitVector", "", "", "Error:- some error is present in blob data");
      e.printStackTrace();
    }
  }

  /*
   * this function does the following task-
   * --add the methodId and event id to their vector methodCallingSequenceList, eventCallingSequenceList
   * to keep sequence of method calling in memory.
   * --if any function start with event id 0 and not end with 1 then we will not add the method to vector.
   * --calculate the bar length for each method.
   * --it will return a vector containg unique methodList in following format
   * methodId|BarLength|TimeStamp
   *
   * If any method get called 2 or 3 times or more
   * Then we add #1 at the end of method name to keep unique list of method name
   * so that we can findout the parent of each method.
   * If same method is called 2nd time then we will add # at the end of methodId
   * if same method is called 3rd time then we will add ## at the end of methodId.
   *
   */
  private void setMethodNameWithTimeStamp()
  {
    Log.debugLog(className, "getMethodNameWithTimeStamp", "", "", "Method Started ");
    for(int i = 0; i < methodCallingSequenceList.size(); i++)
    {
      String methodId = methodCallingSequenceList.get(i);
      int countOfMatch = 0;
      int countOfInnerMethodCalled = 0;
      if(eventCallingSequenceList.get(i).equals("1"))
        continue;

      for(int j = (i + 1); j < methodCallingSequenceList.size(); j++)
      {
        if(methodId.equals(methodCallingSequenceList.get(j)) && eventCallingSequenceList.get(j).equals("0"))
        {
          countOfMatch++;
        }
        else if(methodId.equals(methodCallingSequenceList.get(j)) && eventCallingSequenceList.get(j).equals("1") && countOfMatch == 0)
        {
          methodId = checkDuplicacyInUniqueMethodList(methodCallingSequenceList.get(i));
          uniqueListOfMethodIdCalled.add(methodId);
          if(!methodId.equals(methodCallingSequenceList.get(i)))
          {
            methodCallingSequenceList.remove(i);
            methodCallingSequenceList.add(i, methodId);
            methodCallingSequenceList.remove(j);
            methodCallingSequenceList.add(j, methodId);
          }
          String exitStatus = tmpExitReceivedStaus.get(j);
          exitReceivedStaus.add(exitStatus);
          //timeStampForUniqueMethodId.add(Double.parseDouble(timestampList.get(j)) - Double.parseDouble(timestampList.get(i)));
          timeStampForUniqueMethodId.add(Double.parseDouble(timestampList.get(j)));
          barHeightForMethodId.add(getBarHeight(countOfInnerMethodCalled));
          break;
        }
        else if(methodId.equals(methodCallingSequenceList.get(j)) && eventCallingSequenceList.get(j).equals("1"))
        {
          countOfMatch--;
        }
        if(eventCallingSequenceList.get(j).equals("1"))
          countOfInnerMethodCalled++;

        if(j == (methodCallingSequenceList.size() - 1))
        {
          Log.debugLog(methodId, "getMethodNameWithTimeStamp", "", "", "Error:- no exit found for methodId = "+methodId);
          methodCallingSequenceList.remove(i);
          eventCallingSequenceList.remove(i);
          i = i-1;
        }
      }
    }

    /*for (int i = 0; i < methodCallingSequenceList.size(); i++)
    {
     // System.out.println("methodId = "+methodCallingSequenceList.get(i) + ", eventId = "+eventCallingSequenceList.get(i));
    }*/

    /*for (int i = 0; i < uniqueListOfMethodIdCalled.size(); i++)
    {
    //System.out.println(uniqueListOfMethodIdCalled.get(i));
    }*/

    //now release the memory of timestampList because now it will not get used
    timestampList = null;
    tmpExitReceivedStaus = null;
  }

  /*
   * This method sets the levels for class, package and methodId.
   */
  private void setDataToPkgAndClass()
  {
    int level = 1;
    for(int i = 0; i < methodCallingSequenceList.size(); i++)
    {
      if(i > 0)
      {
        String currCls = getClassName(methodCallingSequenceList.get(i));
        String currPkg = getPackageName(methodCallingSequenceList.get(i));
        String parentId = getParentId(methodCallingSequenceList.get(i));
        String parentCls = getClassName(parentId);
        String parentPkg = getPackageName(parentId);

        if(parentCls.equals(currCls) && parentPkg.equals(currPkg))
        {
          level = methodIdLevelMap.get(parentId) + 1;
        }
        else
        {
          level = 1;
        }
        if(eventCallingSequenceList.get(i).equals("1"))
          continue;

        setLevelOfClassAndPkg(methodCallingSequenceList.get(i), level);
      }
      else
      {
        setLevelOfClassAndPkg(methodCallingSequenceList.get(i), level);
      }
    }

    ArrayList<String> pkgListTemp = new ArrayList<String>();
    //process pkgList to make consecutive same package Name as one package.
    for(int i = 0; i < pkgList.size(); i++)
    {
      if(i > 0 )
      {
        if(pkgList.get(i-1).equals(pkgList.get(i)))
        {
          int newSize = pkgSizeList.get(pkgSizeList.size() - 1) + pkgLevelMap.get(i);
          pkgSizeList.remove(pkgSizeList.size() - 1);
          pkgSizeList.add(newSize);
        }
        else
        {
          pkgListTemp.add(pkgList.get(i));
          pkgSizeList.add(pkgLevelMap.get(i));
        }
      }
      else
      {
        pkgListTemp.add(pkgList.get(i));
        pkgSizeList.add(pkgLevelMap.get(i));
      }
    }

    //now release the memory of not used objects.
    pkgList = null;
    pkgLevelMap = null;
    pkgList = pkgListTemp;

    /*for (int i = 0; i < uniqueListOfMethodIdCalled.size(); i++)
    {
    // System.out.println(uniqueListOfMethodIdCalled.get(i) + " , " +methodIdLevelMap.get(uniqueListOfMethodIdCalled.get(i)));
    }*/
  }

  private void setLevelOfClassAndPkg(String methodId, int level)
  {
    //System.out.println("methodId = "+methodId + "level = "+level);
    String clsName = getClassName(methodId);
    String pkgName = getPackageName(methodId);
    if(!clsList.contains(clsName))
    {
      clsList.add(clsName);
      pkgList.add(pkgName);
      methodIdLevelMap.put(methodId, level);

      clsIdLevelMap.put((clsList.size() - 1), level);
      clsIdPkgNameMap.put((clsList.size() - 1), pkgName);
      pkgLevelMap.put((pkgList.size() - 1), level);
    }
    else
    {
      boolean found = false;
      for(int i = 0; i < clsList.size(); i++)
      {
        if(clsList.get(i).equals(clsName) && clsIdPkgNameMap.get(i).equals(pkgName) &&  level > clsIdLevelMap.get(i))
        {
          clsIdLevelMap.put(i, level);
          pkgLevelMap.put(i, level);
          found = true;
          break;
        }
        else if(clsList.get(i).equals(clsName) && clsIdPkgNameMap.get(i).equals(pkgName) &&  level <= clsIdLevelMap.get(i))
        {
          found = true;
          break;
        }
      }
      if(!found)
      {
        clsList.add(clsName);
        pkgList.add(pkgName);
        clsIdLevelMap.put((clsList.size() - 1), 1);
        clsIdPkgNameMap.put((clsList.size() - 1), pkgName);
        pkgLevelMap.put((pkgList.size() - 1), 1);
      }
      methodIdLevelMap.put(methodId, level);
    }
  }

  private void setRectOfFirstTierArr()
  {
	String str = "";
	int YCord = 0;
    int rectX = startX;
    int rectY = startY + heightOfBox + gapBetweenBox;
    int boxSize = widthOfBox;
    int size = 1;
    rectFirstTierArr = new Rectangle[clsList.size()];
    for(int i = 0; i < clsList.size(); i++)
    {
      size = clsIdLevelMap.get(i);
      boxSize = ((widthOfBox*size) + (gapBetweenBox*(size - 1)));
      rectFirstTierArr[i] = new Rectangle(rectX, rectY, boxSize, heightOfBox);
      
      if(i == 0)
          YCord = rectX + 2*heightOfBox;
      
      str = rectX + "," + rectY;
      rectX = rectX + boxSize + gapBetweenBox;
      str = str + "," + rectX + "," + YCord + ":" + clsList.get(i);
      coordinatesArrayList.add(str);
    }
  }

  private void setRectOfSecondTierArr()
  {
	String str = "";
	int YCord = 0;
    int rectX = startX;
    int rectY = startY;
    rectSecondTierArr = new Rectangle[pkgList.size()];
    int size = 1;
    int boxSize = widthOfBox;
    for(int i = 0; i < pkgList.size(); i++)
    {
      //size = pkgLevelMap.get(i);
      size = pkgSizeList.get(i);
      boxSize = ((widthOfBox*size) + (gapBetweenBox*(size - 1)));
      rectSecondTierArr[i] = new Rectangle(rectX, rectY, boxSize, heightOfBox);
      
      if(i == 0)
        YCord = rectX + heightOfBox;
      
      str = rectX + "," + rectY;
      rectX = rectX + boxSize + gapBetweenBox;
      str = str + "," + rectX + "," + YCord + ":" + pkgList.get(i);
      coordinatesArrayList.add(str);
    }
  }

  private String checkDuplicacyInUniqueMethodList(String methodId)
  {
    if(uniqueListOfMethodIdCalled.contains(methodId))
    {
      methodId = checkDuplicacyInUniqueMethodList(methodId+"#");
    }
    return methodId;
  }

  private double getBarHeight(int count)
  {
    int heightOfBar = 0;
    if(count > 0)
      heightOfBar = defaultHeight*(count + 1) + incrementSpace*(count - 1);
    else
      heightOfBar = defaultHeight;

    return heightOfBar;
  }

  private String getMethodNameFromMethodSignature(String methodId, int width)
  {
	String methodName = ""; 
    int index = methodId.indexOf("#");
    if(index != -1)
      methodId = methodId.substring(0, index);

    String methodSignature = mapForMethodIdName.get(methodId);
    int lastIndexOfDot = methodSignature.lastIndexOf(".");
    if(lastIndexOfDot == -1)
      return methodSignature;

    int lastIndexOfPara = methodSignature.lastIndexOf("(");
    if(isForCompare)
    {
      methodName = methodSignature.substring(lastIndexOfDot+1, lastIndexOfPara);
      return getLimitedLengthStringBasedOnSize(methodName + "()", width);
    }
    else
    {
      methodName = methodSignature.substring(lastIndexOfDot+1, methodSignature.length());
      return getLimitedLengthStringBasedOnSize(methodName , width);
    }
  }
  
  /**
   * This Method takes methodId as the input and returns complete method Signature
   * which is used for showing complete method signature on the tool tip on the mouse over.
   * @param methodId
   * @return
   */
  private String getMethodNameFromMethodSignature(int eleIndex)
  {
	  try 
	  {
		  String methodId = uniqueListOfMethodIdCalled.get(eleIndex);
		  
		  int index = methodId.indexOf("#");
		  
		  if(index != -1)
			  methodId = methodId.substring(0, index);
		  
		  String methodSignature = mapForMethodIdName.get(methodId) + "," + timeStampForUniqueMethodId.get(eleIndex);
		  
		  return methodSignature;
	  } 
	  catch (Exception e) 
	  {
         Log.stackTraceLog(className, "", "", "", "Exception in getting method Name from Method Signature", e);
         return "";
	  }
  }
  
  

  private String getClassName(String methodId)
  {
    int index = methodId.indexOf("#");
    if(index != -1)
      methodId = methodId.substring(0, index);

    String methodSignature = mapForMethodIdName.get(methodId);
    int lastIndexOfDot = methodSignature.lastIndexOf(".");
    if(lastIndexOfDot == -1)
      return methodSignature;
    else
    {
      String tempStr = methodSignature.substring(0, lastIndexOfDot);
      lastIndexOfDot = tempStr.lastIndexOf(".");

      if(lastIndexOfDot == -1)
        return tempStr;

      tempStr = tempStr.substring(lastIndexOfDot+1, tempStr.length());
      return tempStr;
    }
  }

  private String getPackageName(String methodId)
  {
    int index = methodId.indexOf("#");
    if(index != -1)
      methodId = methodId.substring(0, index);

    String methodSignature = mapForMethodIdName.get(methodId);
    int lastIndexOfDot = methodSignature.lastIndexOf(".");
    if(lastIndexOfDot == -1)
      return methodSignature;
    else
    {
      String tempStr = methodSignature.substring(0, lastIndexOfDot);
      lastIndexOfDot = tempStr.lastIndexOf(".");

      if(lastIndexOfDot == -1)
        return tempStr;
      tempStr = tempStr.substring(0, lastIndexOfDot);
      return tempStr;
    }
  }

  public void setFilterGreaterThan(int value)
  {
    filterGreaterThan = value;
  }

  public void setFilterTop(int value)
  {
    filterTop = value;
  }
  
  public void setWidthOfBox(int value)
  {
    widthOfBox = value;
  }
  
  public void setHightlightWallTime(int value)
  {
    hightlightWallTime = value;
  }
  
  public void setForCompare(boolean value)
  {
    isForCompare = value;
  }

  public int getFilterGreaterThan()
  {
    return filterGreaterThan;
  }

  public int getFilterTop()
  {
    return filterTop;
  }
  
  public int getWidthOfBox()
  {
    return widthOfBox;
  }

  /* This function filter the data.
   * For Example: wallTime > 2 or Top 20 or Both.
   */
  private String[][] filterData(String[][] arrBlobData)
  {
    Log.debugLog(className, "filterData", "", "", "Filter data for filterGreaterThan = "+filterGreaterThan + " and top = "+filterTop);
    ArrayList<String[]> filteredArrayList = getArraListFromArrayAfterAddingIndex(arrBlobData);
    String[][] arrFilterBlobData = null;
    if(filterTop > -1)
    {
      arrFilterBlobData = getArrayFromArrayListOfOnlyEntryMethods(filteredArrayList);
      
      Arrays.sort(arrFilterBlobData,  new ColumnComparator(idxOfWallTime, false));
      ArrayList<String> listOfSortedIndex = new ArrayList<String>();
      
      for(int i = 0; i < filterTop && i < arrFilterBlobData.length; i++)
      {
        for(int j = 0; j < filteredArrayList.size(); j++)
        {
          //match start index
          if(filteredArrayList.get(j)[0].equals(arrFilterBlobData[i][0]))
          {
            //add start index
            listOfSortedIndex.add(filteredArrayList.get(j)[0]);
            int countOfMatch = 0;
            String methodId = filteredArrayList.get(j)[idxOfMethodId];
            //match end index
            for(int k = j+1; k < filteredArrayList.size(); k++)
            {
               if(filteredArrayList.get(k)[idxOfMethodId].equals(methodId) && filteredArrayList.get(k)[idxOfEventId].equals("0"))
                 countOfMatch++;
               else if(filteredArrayList.get(k)[idxOfMethodId].equals(methodId) && filteredArrayList.get(k)[idxOfEventId].equals("1") && countOfMatch == 0)
               {
                 //add end index
                 listOfSortedIndex.add(filteredArrayList.get(k)[0]);
               }
               else if(filteredArrayList.get(k)[idxOfMethodId].equals(methodId) && filteredArrayList.get(k)[idxOfEventId].equals("1"))
                 countOfMatch--;
            }
          }
        }
      }
      
      

      //remove extra data from filteredArrayList
      for(int i = 0; i < filteredArrayList.size(); i++)
      {
        if(!listOfSortedIndex.contains(filteredArrayList.get(i)[0]))
        {
          filteredArrayList.remove(i);
          i = i-1;
        }
      }
    }

    //Check final filtered data for testing purpose.
    /*System.out.println("filteredArrayList.size() = "+filteredArrayList.size());
    for(int i = 0; i < filteredArrayList.size(); i++)
    {
      for(int j = 0; j < filteredArrayList.get(i).length; j++)
        System.out.print(filteredArrayList.get(i)[j]+"-");
      System.out.println();
    }*/

    arrFilterBlobData = getArrayFromArrayList(filteredArrayList);
    return arrFilterBlobData;
  }

  /* This function add the uniqueIndex at the start of each row and add to a ArrayList<String[]> whose wallTime
   * is greater than filterGreaterThan.
   */
  private ArrayList<String[]> getArraListFromArrayAfterAddingIndex(String[][] arrBlobData)
  {
    ArrayList<String[]> filteredArrayList = new ArrayList<String[]>();
    int idxNum = 0;
    String[] arrTmp = null;
    for(int i = 1; i < arrBlobData.length; i++) //starting from 1 to ingnore header
    {
      if(Integer.parseInt(arrBlobData[i][idxOfWallTime-1]) >= filterGreaterThan)
      {
        arrTmp = new String[arrBlobData[i].length + 1]; // adding 1 for index.
        arrTmp[0] = idxNum + "";
        for(int j = 0; j < arrBlobData[i].length; j++)
          arrTmp[j+1] = arrBlobData[i][j];
        filteredArrayList.add(arrTmp);
        idxNum++;
      }
    }
    return filteredArrayList;
  }

  /* This function returns a 2D array of those data only who have the event id as 0
   * means return a 2 array of only method entry records.
   */
  private String[][] getArrayFromArrayListOfOnlyEntryMethods(ArrayList<String[]> listData)
  {
    String[][] arrBlobData = new String[listData.size()/2][];
    int idx = 0;
    for(int i = 0; i < listData.size() && idx < arrBlobData.length; i++)
    {
      if(listData.get(i)[idxOfEventId].equals("0"))
        arrBlobData[idx++] = listData.get(i);
    }

    return arrBlobData;
  }

  /*
   * This method return an array String[][] from arraylist<String[]>.
   */
  private String[][] getArrayFromArrayList(ArrayList<String[]> listData)
  {
    String[][] arrBlobData = new String[listData.size()][];
    for(int i = 0; i < listData.size(); i++)
    {
      arrBlobData[i] = listData.get(i);
    }

    return arrBlobData;
  }

  //Class that extends Comparator for sorting
  class ColumnComparator implements Comparator
  {
    int columnToSort;
    boolean ascending;
    ColumnComparator(int columnToSort, boolean ascending)
    {
      this.columnToSort = columnToSort;
      this.ascending = ascending;
    }
    //overriding compare method
    public int compare(Object o1, Object o2)
    {
      String[] row1 = (String[]) o1;
      String[] row2 = (String[]) o2;
     //compare the columns to sort
     int cmp = 0;
     if(columnToSort != idxOfWallTime)
     {
       cmp = row1[columnToSort].compareToIgnoreCase(row2[columnToSort]);
       return ascending ? cmp : -cmp;
     }
     else
     {
       double d1 = 0.0;
       double d2 = 0.0;
       if(!row1[columnToSort].equals(""))
         d1 = Double.parseDouble(row1[columnToSort]);
       if(!row2[columnToSort].equals(""))
         d2 = Double.parseDouble(row2[columnToSort]);
       if (d1 < d2)
         cmp = -1;
       else if (d1 > d2)
         cmp = 1;
       else
         return 0;
        return ascending ? cmp : -cmp;
      }
    }
  }

  private String getLimitedLengthStringBasedOnSize(String str, int width)
  {
    int letters = (width - 24)/8;
    //System.out.println("width=" + width+ ", letters = "+letters);
    str = str.trim();
    if(str.length() > letters)
    {
      str = str.substring(0, letters) + "...";
    }
    return str;
  }
  

  /**
   * Added Code for Filtering for Unique Packages
   * @param flowData
   * @return
   */
  public ArrayList<String> getUniqueSignatureForFilter(String flowData[][])
  {
    ArrayList<String> UniqueArraylist = new ArrayList<String>();
    
    try
    {
      for(int i = 1; i < flowData.length; i ++)
      {
        String fullSign = flowData[i][8];   
        
	//check if string is not empty.
	if(!fullSign.trim().equals(""))
        {
	  if(!UniqueArraylist.contains(fullSign))		  
	    UniqueArraylist.add(fullSign);
	}
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getUniqueSignatureForFilter", "", "", "Exception = ", e);
      e.printStackTrace();
    }
    return UniqueArraylist;       	  
  }
  
  /**
   * This method done the package based filtering on row data.
   * @param flowData
   * @param packArray
   * @param classArray
   * @param uniqueSign
   * @return
   */

  public String[][] getArrayBasedOnFilterOnlyIfPckg(String[][] flowData,String[] packArray, StringBuffer errorMsg)
  {
    try
    {		
      //check for empty package List. 	
      if(packArray == null)
        return flowData;

      boolean found = false;
      int count =0;
      int count1=0;
      
      //checking the start method of the flowpath is selected or not
     for(int set=0;set<packArray.length;set++)
     {
       found =  (flowData[1][8]).contains(packArray[set]);
         if(found)
         break;
     }

      
      
      ArrayList<String[]> arrFilteredList  = new ArrayList<String[]>();
  
      //Adding Header Information.
      arrFilteredList.add(flowData[0]);
     
      //Finding Each Package in the Row Data.
      for(int i = 0; i < flowData.length; i++)
      {
	for(int k = 0; k < packArray.length; k++)
	{
	 //Adding the start method at the start
	 if(found == false && count==0)
	 {
            arrFilteredList.add(flowData[1]);
            count++;
	 }
	  if(packArray[k].trim().equals(flowData[i][8]))
            arrFilteredList.add(flowData[i]);
	}
      }
      //Adding the start method at the end
      if(!found && count1 ==0)
      {
      arrFilteredList.add(flowData[flowData.length-1]);
      count1++;
      }
  	 
      String[][] pack2DArray = new String[arrFilteredList.size()][];
      for (int l = 0; l < arrFilteredList.size(); l++)
      {
	pack2DArray[l] = arrFilteredList.get(l);
      }
      return pack2DArray;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "getArrayBasedOnFilterOnlyIfPckg", "", "", "Exception = ", e);
      errorMsg.append("Error in Filtering data due to following error = "+e.getStackTrace());
      return flowData;
    }
  }
  
  /**
   * This method is used to get array for tree based sequence diagram after sorting on Level
   * @param arrBlobData
   * @return
   */
  public String[][] getArrayForTreeBasedSequenceDiagram(String[][] arrBlobData) 
  {
  	String[][] arrTreeBasedData = new String[arrBlobData.length-1][5];
  	try
  	{
  		for(int i = 0; i < arrBlobData.length; i++)
  		{
  		   for(int j = 0; j < arrBlobData[i].length; j++)
  		   {  			   
  			   if(i != 0 && arrBlobData[0][j].trim().equals("MethodName"))
  				   arrTreeBasedData[i-1][0] = arrBlobData[i][j-1] + "." + arrBlobData[i][j];
  			   if(i != 0 && arrBlobData[0][j].trim().equals("WallTime"))
  			   {
  				   //checking if method time is equal to zero
                   if(Integer.parseInt(arrBlobData[i][j]) < 1)
                	 arrTreeBasedData[i-1][1] = "< 1";
                   else
  				     arrTreeBasedData[i-1][1] = arrBlobData[i][j];
  			   }
  			   if(i != 0 && arrBlobData[0][j].trim().equals("Level"))
  				   arrTreeBasedData[i-1][2] = arrBlobData[i][j];
  			   if(i != 0 && arrBlobData[0][j].trim().equals("FQMethodSignature"))
  				   arrTreeBasedData[i-1][3] = arrBlobData[i][j];
			   if(i != 0 && arrBlobData[0][j].trim().equals("MethodId"))
                                   arrTreeBasedData[i-1][4] = arrBlobData[i][j];	
  		   }
  		  }
  		return arrTreeBasedData;
  	}
  	catch(Exception e)
  	{
        Log.stackTraceLog(className, "getArrayForTreeBasedSequenceDiagram", "", "", "Exception = ", e);
        return arrTreeBasedData;
  	}
  }
 

  public static void main(String args[])
  {
    NDGenerateSequenceDiagram obj = new NDGenerateSequenceDiagram();
   // Vector<String> vecBlobData = rptUtilsBean.readFileInVector("/home/netstorm/fileOutput.txt");
   Vector<String> vecBlobData = rptUtilsBean.readFileInVector("/home/netstorm/Back_Up/Satendra/file.txt");
    String[][] arrBlobData = rptUtilsBean.getRecFlds(vecBlobData, "", "", "|");
    //obj.setFilterGreaterThan(2);
    // obj.setFilterTop(3);
    //obj.setForCompare(true);
    obj.setHightlightWallTime(10);
    StringBuffer msg = new StringBuffer();
    obj.createSequenceDiagram(arrBlobData, "ImageSeq.png", msg);
  }
}
