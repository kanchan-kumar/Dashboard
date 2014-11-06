package pac1.Bean;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import pac1.Bean.nfLayouts.InterfaceComponent;
import pac1.Bean.nfLayouts.InterfaceSimulationComponent;
import pac1.Bean.nfLayouts.InterfaceSimulationProperties;
import pac1.Bean.nfLayouts.LayoutComponent;
import pac1.Bean.nfLayouts.LayoutProperties;
import pac1.Bean.nfLayouts.Layouts;

public class NFLayoutsReadXML
{
  private final String className = "NFLayoutsReadXML";

  private CorrelationService corrSer = null;
  private final String DEF_XML_XTENSION = ".xml"; 
  
  public NFLayoutsReadXML()
  {
    corrSer = new CorrelationService();
  }

  public String getLayoutDirPath()
  {
    //presently host name is "default"
    return corrSer.getLayoutPath("default");
  }

  private File openFile(String path)
  {
    return openFile(path, false);
  }
  
  private File openFile(String path, boolean isCheckingDirPath)
  {
    File fileObj = new File(path);

    if(!fileObj.exists())
    {
      if(!isCheckingDirPath)
      {
        Log.errorLog(className, "openFile", "", "", "Invalid Path: " + path);
        return null;
      }
      else
      {
        fileObj.mkdir();
        Log.debugLog(className, "openFile", "", "", "Dir created at: " + fileObj.getPath());
      }
    }

    return fileObj;
  }

  public boolean deleteFile(String fileName)
  {
    Log.debugLog(className, "deleteFile", "", "", "Method Starts. File path: " + fileName + DEF_XML_XTENSION);
   
    String fileNameWithPath = getLayoutDirPath() + fileName + DEF_XML_XTENSION;
    File fileObj = openFile(fileNameWithPath);
    
    if(fileObj != null)
    {
      if(!fileObj.delete())
        return false;
    }
    else
      Log.debugLog(className, "deleteFile", "", "", "File not found at path: " + fileNameWithPath);
        
    Log.debugLog(className, "deleteFile", "", "", "Method Ends. File path: " + fileNameWithPath);
    return true;
  }
  
  
  /*
   * This method calculate the list of xml files resides in Layout directory
   * return the layouts Name from files
   */
  public String[] getLayoutsName()
  {
    Log.debugLog(className, "getLayoutsName", "", "", "Method Starts");

    //String array that contains the list of xml files
    String[] xmlFiles = null;

    File xmlFilesDir = null;

    if((xmlFilesDir = openFile(getLayoutDirPath(), true)) != null)
    {
      //Absolute path should be directory
      if(xmlFilesDir.isDirectory())
      {
        Log.debugLog(className, "getLayoutsName", "", "", "Directory path = " + xmlFilesDir.getAbsolutePath());

        File[] listOfFiles = xmlFilesDir.listFiles();

        //list of  files that resides in Layout directory
        if(listOfFiles != null)
        {
          xmlFiles = new String[listOfFiles.length];
          for(int i = 0; i < listOfFiles.length; i++)
            xmlFiles[i] = listOfFiles[i].getName().substring(0, listOfFiles[i].getName().indexOf(".xml"));
        }
      }
      else
        Log.errorLog(className, "getLayoutsName", "", "", "Given path is not a directory, path: " + xmlFilesDir.getAbsolutePath());
    }
    else
      Log.errorLog(className, "getLayoutsName", "", "", "Given path is invalid as, path: " + getLayoutDirPath());
      

    return xmlFiles;
  }

  public Layouts readXMLFile(String fileName)
  {
    Log.debugLog(className, "readXMLFile", "", "", "Method Starts. File name: " + fileName);

    Layouts xmlObj = null;
    File xmlFileObj = null;
    String comFilePath = getLayoutDirPath() + fileName + DEF_XML_XTENSION;
    Log.debugLog(className, "readXMLFile", "", "", "Reading Layout. File name: " + comFilePath);

    if((xmlFileObj = openFile(comFilePath)) != null)
    {
      xmlObj = getXMLObject(xmlFileObj);
    }
    else
      Log.errorLog(className, "readXMLFile", "", "", "File not found at path: " + comFilePath);

    return xmlObj;
  }

  private Layouts getXMLObject(File layoutXMLFile)
  {
    Log.debugLog(className, "getXMLObject", "", "", "Method Start.");

    try
    {
      JAXBContext jaxbObj = JAXBContext.newInstance("pac1.Bean.nfLayouts");
      Unmarshaller unmarshalObj = jaxbObj.createUnmarshaller();

      Layouts xmlObj = (Layouts)unmarshalObj.unmarshal(new FileInputStream(layoutXMLFile));

      xmlObj = refreshModeAndStateOfService(xmlObj);
      Log.debugLog(className, "getXMLObject", "", "", "Method End." + xmlObj.getLayoutDescription() + ", xmlObj name = " + xmlObj.getLayoutName()); 
      Log.debugLog(className, "getXMLObject", "", "", "Method End.");

      return xmlObj;
    }
    catch(JAXBException e)
    {
      Log.stackTraceLog(className, "getXMLObject", "", "", "JAXBException - ", e);
      return null;
    }
    catch(FileNotFoundException e)
    {
      Log.stackTraceLog(className, "getXMLObject", "", "", "FileNotFoundException - ", e);
      return null;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getXMLObject", "", "", "Exception - ", e);
      return null;
    }
  }

  private Layouts refreshModeAndStateOfService(Layouts xmlObj)
  {
    Log.debugLog(className, "refreshModeAndStateOfService", "", "", "Method Starts.");
    
    List<InterfaceSimulationComponent> listInterSimComp = xmlObj.getInterfaceSimulationComponent();
    Iterator<InterfaceSimulationComponent> iterator3 = listInterSimComp.iterator();
    InterfaceSimulationComponent interSimComp = null;
    
    while(iterator3.hasNext())
    {
      interSimComp = iterator3.next();

      Log.debugLog(className, "refreshModeAndStateOfService", "", "", "Get details for service: " + interSimComp.getServiceName());
      
      String stateAndMode[] = corrSer.getStateAndModeOfService("default", interSimComp.getServiceName());
      
      Log.debugLog(className, "refreshModeAndStateOfService", "", "", "Service Type: " + stateAndMode[0] + ", Service Mode: " + stateAndMode[1]);
     
      //interSimComp.getSimulationProperties().setServiceType(stateAndMode[0]);
      //interSimComp.getSimulationProperties().setServiceMode(stateAndMode[1]);
    
      interSimComp.setServiceType(stateAndMode[0]);
      interSimComp.setServiceMode(stateAndMode[1]);
    }
    return xmlObj;
  }
  
  /*
   *This method convert layout info into XML format
   *@param NFLayout info Object in hashTable
   */
  public boolean convertLayoutInfoIntoXML(Hashtable<Long, CreateShape> htabDiagramDetail)
  {
    Log.debugLog(className, "convertLayoutInfoIntoXML", "", "", "Method Starts. Size of Hash table: " + htabDiagramDetail.size());

    // show all diagram in hashTable htabDiagramDetail
    Set set = htabDiagramDetail.keySet(); // get set-view of keys
    Iterator itr = set.iterator(); // get iterator

    //XML root elemet
    Layouts layout = new Layouts();
    //get the layout Component property
    List<LayoutComponent> layoutComlist = layout.getLayoutComponent();
    //get the Interface Component property
    List<InterfaceComponent> InterComlist = layout.getInterfaceComponent();
    //get the Interface Simulation Component property
    List<InterfaceSimulationComponent> InterSimComList = layout.getInterfaceSimulationComponent();

    while(itr.hasNext())
    {
      long keyID = Long.parseLong(itr.next().toString());
      Log.debugLog(className, "convertLayoutInfoIntoXML", "", "", "Id: " + keyID);

      CreateShape diagram = htabDiagramDetail.get(keyID);

      //diagram is layout component
      if(diagram.isLayoutComp())
      {
        Log.debugLog(className, "convertLayoutInfoIntoXML", "", "", "Component is Layout at id: " + keyID);

        //LayoutComponent description
        LayoutComponent layoutcomponent = new LayoutComponent();

        //setting layout component ID
        layoutcomponent.setLayoutCompId(String.valueOf(keyID));
        //setting layout component name
        layoutcomponent.setLayoutCompName(diagram.getShapeCaption());

        //layout component description properties
        LayoutProperties layProperty = new LayoutProperties();
        //setting x and y axis and width, height 
        layProperty.setXAxisLayoutProp(String.valueOf(diagram.getxAxisPos()));
        layProperty.setYAxisLayoutProp(String.valueOf(diagram.getyAxisPos()));
        layProperty.setWidthLayoutProp(String.valueOf(diagram.getWidth()));
        layProperty.setHeightLayoutProp(String.valueOf(diagram.getHeight()));
        //foreground and background color are not set
        layProperty.setBgColorLayoutProp(null);
        layProperty.setFgColorLayoutProp(null);
        //setting layout component properties
        layoutcomponent.setLayoutProperties(layProperty);

        //adding layout component to Layout 
        layoutComlist.add(layoutcomponent);

      }
      //diagram is Interface component
      else if(diagram.isInterface())
      {
        Log.debugLog(className, "convertLayoutInfoIntoXML", "", "", "Component is Interface at id: " + keyID);

        //interface description
        InterfaceComponent interfaceComonent = new InterfaceComponent();

        //setting Interface component ID
        interfaceComonent.setInterfaceCompId(String.valueOf(keyID));
        //setting Interface component Name
        interfaceComonent.setInterfaceCompName(diagram.getShapeCaption());
        //setting Interface component starting object
        interfaceComonent.setBegCompId(String.valueOf(getIDOfComponents(htabDiagramDetail, diagram.getBegVertxObj(), true)));
        //setting Interface component ending object
        interfaceComonent.setEndCompId(String.valueOf(getIDOfComponents(htabDiagramDetail, diagram.getEndVertxObj(), true)));

        /*Interface properties are not set
        InterfaceProperties interfaceProp = new InterfaceProperties();
        interfaceProp.setXAxisInterfaceProp("123");
        interfaceProp.setYAxisInterfaceProp("123");
        interfaceProp.setLengthInterfaceProp("213");
        interfaceProp.setBgColorInterfaceProp("2qe4");

        interfaceComonent.setInterfaceProperties(interfaceProp);
        */

        //adding interface component to interface component list of layout
        InterComlist.add(interfaceComonent);
      }
      //diagram is Interface Simulator component
      else if(diagram.isInterfaceSimulator())
      {
        Log.debugLog(className, "convertLayoutInfoIntoXML", "", "", "Component is Interface Simulator at id: " + keyID);

        //InterfaceSimulationComponent description
        InterfaceSimulationComponent interfaceSimCom = new InterfaceSimulationComponent();

        //setting the interface Simulator ID
        interfaceSimCom.setInterfaceSimulationCompId(String.valueOf(keyID));
        //setting the interface Simulator name
        if(diagram.getShapeCaption().contains("(F)") || diagram.getShapeCaption().contains("(S)") || diagram.getShapeCaption().contains("(M)"))
          interfaceSimCom.setServiceName(diagram.getShapeCaption().substring(0, diagram.getShapeCaption().lastIndexOf("(")));
        else
          interfaceSimCom.setServiceName(diagram.getShapeCaption());
        //setting the interface Component ID
        interfaceSimCom.setInterfaceId(String.valueOf(getIDOfComponents(htabDiagramDetail, diagram.getParentEdge(), false)));

        //setting service Type and Mode
        interfaceSimCom.setServiceType(diagram.getServiceType());
        interfaceSimCom.setServiceMode(diagram.getServiceMode());

        //InterfaceSimulationComponent properties description
        InterfaceSimulationProperties intComProp = new InterfaceSimulationProperties();
        //setting x and y axis and width, height 
        intComProp.setXAxisInterfaceSimulationProp(String.valueOf(diagram.getxAxisPos()));
        intComProp.setYAxisInterfaceSimulationProp(String.valueOf(diagram.getyAxisPos()));
        intComProp.setWidthInterfaceSimulationProp(String.valueOf(diagram.getWidth()));
        intComProp.setHeightInterfaceSimulationProp(String.valueOf(diagram.getHeight()));
        
        //foreground and background color are not set
        intComProp.setBgColorInterfaceSimulationProp(null);
        intComProp.setFgColorInterfaceSimulationProp(null);


        //setting InterfaceSimulationComponent properties 
        interfaceSimCom.setInterfaceSimulationProperties(intComProp);

        /*
        //Simulation description
        SimulationProperties SimProp = new SimulationProperties();

        //setting x and y axis and weight and height properties
        SimProp.setXAxisSimulationProp(String.valueOf(diagram.getTriVertxObj().getxAxisPos()));
        SimProp.setYAxisSimulationProp(String.valueOf(diagram.getTriVertxObj().getyAxisPos()));
        SimProp.setWidthSimulationProp(String.valueOf(diagram.getTriVertxObj().getWidth()));
        SimProp.setHeightSimulationProp(String.valueOf(diagram.getTriVertxObj().getHeight()));

        //setting service Type and Mode
        SimProp.setServiceType(diagram.getTriVertxObj().getServiceType());
        SimProp.setServiceMode(diagram.getTriVertxObj().getServiceMode());

        //foreground and background color are not set 
        SimProp.setFgColorSimulationProp(null);
        SimProp.setBgColorSimulationProp(null);

        //setting simuation properties
        interfaceSimCom.setSimulationProperties(SimProp);
        */

        //adding simulation component to interface simulation components
        InterSimComList.add(interfaceSimCom);

      }
      //layout info added at last index of map
      else if(keyID == (htabDiagramDetail.size() - 1))
      {
        Log.debugLog(className, "convertLayoutInfoIntoXML", "", "", "Layout information started at id: " + keyID);
        
        layout.setCanvasHeight(String.valueOf(diagram.getHeight()));
        layout.setCanvasWidth(String.valueOf(diagram.getHeight()));
        layout.setLayoutDescription(diagram.getLayoutDescription());
        layout.setLayoutName(diagram.getShapeCaption());
        
        Log.debugLog(className, "convertLayoutInfoIntoXML", "", "", "Layout Name:" + diagram.getShapeCaption() + ", Desc: " + diagram.getLayoutDescription());
      }
      else
      {
        Log.errorLog(className, "convertLayoutInfoIntoXML", "", "", "Error: Inalid info stored in map at Id: " + keyID);
        //Log.debugLog(className, "convertLayoutInfoIntoXML", "", "", "ELSE Layout Name:" + diagram.getShapeCaption() + ", Desc: " + diagram.getLayoutDescription());
      }

    }// end of while

    // create JAXB context and instantiate marshaller
    JAXBContext context = null;
    try
    {
      context = JAXBContext.newInstance(Layouts.class);

      Marshaller m = context.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      String compFilePath = getLayoutDirPath() + layout.getLayoutName() + DEF_XML_XTENSION;
      
      //this is to delete existing file in case of editing layout.
      /*if(!deleteFile(compFilePath))
      {
        Log.errorLog(compFilePath, "convertLayoutInfoIntoXML", "", "", "Error while deleting file at path: " + compFilePath);
        return false;
      }*/
      
      // Output Stream overwrite file(if exist) before writing into old file
      OutputStream outStream = new FileOutputStream(compFilePath);
      // Write to file
      m.marshal(layout, outStream);

      outStream.close();
    }

    catch(JAXBException jAXB)
    {
      Log.stackTraceLog(className, "convertLayoutInfoIntoXML", "", "", "Exception in JAXB = ", jAXB);
      return false;
    }
    catch(FileNotFoundException fNFE)
    {
      Log.stackTraceLog(className, "convertLayoutInfoIntoXML", "", "", "Exception in JAXB = ", fNFE);
      return false;
    }
    catch(IOException iOE)
    {
      Log.stackTraceLog(className, "convertLayoutInfoIntoXML", "", "", "Exception in JAXB = ", iOE);
      return false;
    }

    return true;
  }

  /*
   * parse  Interface or layout component name from hash table and return its ID
   * @param Hashtable<Long, CreateShape> htabDiagramDetail
   * @param CreateShape createShape
   * @param boolean IsLayoutCompOrInterface
   */
  private long getIDOfComponents(Hashtable<Long, CreateShape> htabDiagramDetail, CreateShape createShape, boolean isLayoutComp)
  {
    Set set = htabDiagramDetail.keySet();
    Iterator itr = set.iterator();

    while(itr.hasNext())
    {
      long compIndx = Long.parseLong(itr.next().toString());
      CreateShape tempCreateShapeObj = htabDiagramDetail.get(compIndx);

      //if createShape is layout
      boolean condition = tempCreateShapeObj.isLayoutComp();

      //if create shape is interface
      if(!isLayoutComp)
        condition = tempCreateShapeObj.isInterface();

      //for both interface and layout components
      if(condition)
      {
        if(createShape.hashCode() == tempCreateShapeObj.hashCode())
          return compIndx;
      }
      
    }// end of while
    return -1;

  }
}
