package pac1.Bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Utility for making deep copies (vs. clone()'s shallow copies) of objects.
 * Objects are first serialized and then deserialized. Error checking is fairly
 * minimal in this implementation. If an object is encountered that cannot be
 * serialized (or that references an object that cannot be serialized) an error
 * is printed to System.err and null is returned. Depending on your specific
 * application, it might make more sense to have copy(...) re-throw the
 * exception.
 */
public class DeepCopy implements Serializable
{
  public static String className = "DeepCopy";
  private static String workPath = Config.getWorkPath();

  /**
   * Returns a copy of the object, or null if the object cannot be serialized.
   */
  public static Object copy(Object orig , String ObjName)
  {
    Log.debugLog(className, "copy", "", "", "Method called. objName = " + ObjName);
    Object obj = null;
    try
    {
      String debugFlag;

      debugFlag = Config.getValue("debugFlag");
      
      if(debugFlag.equals("on"))
      {
        long srcObjSize = getObjectSize(orig, ObjName);
        Log.debugLogAlways(className, "copy", "", "", ObjName + ": original object size = " + srcObjSize);
      }
      
      // Write the object out to a byte array
      FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(fbos);
      out.writeObject(orig);
      out.flush();
      out.close();

      // Retrieve an input stream from the byte array and read
      // a copy of the object back in.
      ObjectInputStream in = new ObjectInputStream(fbos.getInputStream());
      obj = in.readObject();

      if(debugFlag.equals("on"))
      {
        long deepObjSize = getObjectSize(obj, ObjName);
        Log.debugLogAlways(className, "copy", "", "", ObjName + ": copied object size = " + deepObjSize);
      }

      return obj;
    }
    catch (IOException e)
    {
      Log.stackTraceLog(className, "copy", "", "", "Exception - ", e);
      return obj;
    }
    catch (ClassNotFoundException cnfe)
    {
      Log.stackTraceLog(className, "copy", "", "", "Exception - ", cnfe);
      return obj;
    }
  }

  // Method to get the size of any object.
  // We didn't found any method to get the object size so we are serializing the
  // object and getting the file size
  public static long getObjectSize(Object objToGetSize, String objName)
  {
    Log.debugLog(className, "getObjectSize", "", "","Method called. objName = " + objName);
    try
    {
      long objSize = 0;
      String objectFilePath = workPath + "/webapps/netstorm/temp/";
      Log.debugLog(className, "getObjectSize", "", "", "objectFilePath = " + objectFilePath);
      
      File fileExist = new File(objectFilePath + "/" + objName + ".obj");
      if (fileExist.exists())
        fileExist.delete();

      FileOutputStream fos = new FileOutputStream(objectFilePath + "/" + objName + ".obj");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(objToGetSize);
      oos.flush();
     
      oos.close(); 
      fos.close();
      
      File F1 = new File(objectFilePath + "/" + objName + ".obj");
      objSize = F1.length();
      
      if (F1.exists())
        F1.delete();
      
      Log.debugLog(className, "getObjectSize", "", "", "Object name =  " + objName + " and size = " + objSize);

      return objSize;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getObjectSize", "", "", "Exception - ", ex);
      return 0;
    }
  }

  // Prabhat - This method return the testRunData that is created by reading rtgMessage.dat file in offline mode
  // testViewMode --> true for offline, false for online
  public static TestRunData getTestRunDataIfOffLine(int tRNum, boolean getCompleteTestRunData, int numberOfPanels, String userName, boolean testViewMode)
  {
    Log.debugLogAlways(className, "setTestRunDataIfOffLine", "", "", "Method Starts. Test Run = " + tRNum + " getCompleteTestRunData = " + getCompleteTestRunData + " numberOfPanels = " + numberOfPanels + ", testViewMode(true for offline, false for online) = " + testViewMode);

    try
    {
      long startTimeMillis = System.currentTimeMillis();

      ReportData rptData = new ReportData(tRNum);

      // Test run data type (0 --> Normal Test Run Data, 1 --> Baseline Test Run Data, 2 --> Tracked Test Run Data)
      int testRunDataType = 0;
      int avgCount = -1; // -1 is for auto averaging

      TestRunData testRunData = (rptData.getTestRunData(avgCount, testRunDataType, getCompleteTestRunData, numberOfPanels, userName, testViewMode));
      long endTimeMillis = System.currentTimeMillis();
      Log.debugLog(className, "setTestRunDataIfOffLine", "", "", "Time taken to get test run data in Milliseconds: StartTime = " + startTimeMillis + " , End Time = "+ endTimeMillis +", Total Time Taken = "+ (endTimeMillis - startTimeMillis)+" MilliSec" );
      
      return testRunData;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "setTestRunDataIfOffLine", "", "", "Exception - ", e);
      return null;
    }
  }
  
  public static void main(String[] args)
  {
    int testRunNum = Integer.parseInt(args[0]);
    boolean isCompleteTestRunData = Boolean.parseBoolean(args[1]);
    int numPanel = Integer.parseInt(args[2]);
    String userName = args[3];
    boolean testViewMode = Boolean.parseBoolean(args[4]);
    
    TestRunData testRunDataObj = getTestRunDataIfOffLine(testRunNum, isCompleteTestRunData, numPanel, userName, testViewMode);

    long TRDSize = getObjectSize(testRunDataObj, "TRD");
    //long TRD_GraphNameSize = getObjectSize(testRunDataObj.graphNames, "TRD_graphName");
    
    System.out.println("TRDSize = "  + TRDSize);
    //System.out.println("TRD_GraphNameSize = " + TRD_GraphNameSize);
    
    TestRunData testRundataObj_deep = (TestRunData) copy(testRunDataObj, "name");
    
    long Copy_TRDSize = getObjectSize(testRundataObj_deep, "Copy_TRD");
    //long Copy_TRD_GraphNameSize = getObjectSize(testRundataObj_deep.graphNames, "Copy_TRD_graphName");
    
    System.out.println("Copy_TRDSize = " + Copy_TRDSize);
    //System.out.println("Copy_TRD_GraphNameSize = " + Copy_TRD_GraphNameSize);    
  }
}
