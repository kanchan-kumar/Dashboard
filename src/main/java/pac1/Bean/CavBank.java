package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class CavBank
{
  static String className = "CavBank";
  String relativePath = "NetFunctionServlet"; //relative which are defined in web.xml
  
  //static String confPath = Config.getWorkPath() + "webapps/cavbank/xml/config.ini";
  public static String cavBankIP = "";
  public static String cavBankPort = "";
  public static String cavBankURL = "";
  public static Document doc = null;
  
  public CavBank()
  {
    setCavIPPort();
    setCavURL();
  }
  
  public CavBank(String request)
  {
    
  }
  
  public void setCavIPPort()
  {
    cavBankIP = Config.getValue("cavbankip");
    cavBankPort = Config.getValue("cavbankport");
    
    if((cavBankIP == null) || (cavBankPort == null))
    {
      cavBankIP = "";
      cavBankPort = "";
    }
    System.out.println("cavBankIP " + cavBankIP);
    System.out.println("cavBankPort " + cavBankPort);
    
  }
  public void setCavURL()
  {
    cavBankURL = Config.getValue("cavisson.service.url");
    
    if((cavBankURL == null))
    {
      cavBankURL = "";
    }
    
    System.out.println("cavBankURL " + cavBankURL);
    
  }
  
  public boolean changeCavIPPort(String ip , String port)
  {
    String configPath = Config.pathToSys() + "config.ini";
    System.out.println("path = " + configPath);
    
    CorrelationService cs = new CorrelationService();
    Vector completeControlFile = cs.readFile(configPath, false);
    
    Vector modifiedVector = new Vector();
    
    for(int yy = 0; yy < completeControlFile.size(); yy++)
    {
      String line = completeControlFile.elementAt(yy).toString();
      if(line.trim().startsWith("cavbankip")) // Method found
      {
        String strIP = "cavbankip = " + ip;
        modifiedVector.add(strIP);
      }
      else if(line.trim().startsWith("cavbankport")) // Method found
      {
        String strPort = "cavbankport = " + port;
        modifiedVector.add(strPort);
      }
      else
        modifiedVector.add(line);
    }

    if(!writeToFile(configPath, modifiedVector))
      return false;

    System.out.println("cavBankIP " + cavBankIP);
    System.out.println("cavBankPort " + cavBankPort);
    
    return true;
    
  }
  
  public boolean changeCavURL(String URL)
  {
    String configPath = Config.pathToSys() + "config.ini";
    System.out.println("path = " + configPath);
    
    CorrelationService cs = new CorrelationService();
    Vector completeControlFile = cs.readFile(configPath, false);
    
    Vector modifiedVector = new Vector();
    boolean matchFound = false;
    for(int yy = 0; yy < completeControlFile.size(); yy++)
    {
      String line = completeControlFile.elementAt(yy).toString();
      if(line.trim().startsWith("cavisson.service.url")) // Method found
      {
        matchFound = true;
        String strURL = "cavisson.service.url = " + URL;
        modifiedVector.add(strURL);
      }
      else
        modifiedVector.add(line);
    }
    
    if(!matchFound)
    {
      String strURL = "cavisson.service.url = " + URL;
      modifiedVector.add(strURL);
    }

    if(!writeToFile(configPath, modifiedVector))
      return false;

    System.out.println("URL " + URL);
    
    return true;
    
  }
  
  public boolean restartTomcat()
  { 
	 CorrelationService cs = new CorrelationService();
	 String TomcatName = cs.getHPDWork();
     if(TomcatName.indexOf("hpd_") > -1)
	 {
	   TomcatName = TomcatName.substring(TomcatName.indexOf("hpd_") + 4);
	   TomcatName = "tomcat_" + TomcatName;
	 } 	
	      
	 if(TomcatName.equals("hpd"))
	   TomcatName = "apps";
	    
	 if(TomcatName.equals("hpd2"))
	   TomcatName = "apps2";
	    
	 if(TomcatName.equals("hpd3"))
	   TomcatName = "apps3";
	    
	 String cmdName = "/etc/init.d/" + TomcatName;
	 Log.debugLog(className, "restartHPD", "", "", "cmdName = " + cmdName);
	 String cmdArgs = "restart";
	 CmdExec objCmdExec = new CmdExec();
	 Vector vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");
	    
	 System.out.println("tomact has been started");
	 if((vecCmdOutPut.size() > 0) && ((String)vecCmdOutPut.lastElement()).startsWith("ERROR"))
     {
       return false;
     }
	 
	 return true ;
  }
  private boolean writeToFile(String fileWithPath, Vector vecModified)
  {
    Log.debugLog(className, "writeToFile", "", "", "Method called. FIle Name = " + fileWithPath);

    try
    {
      FileOutputStream out2 = new FileOutputStream(new File(fileWithPath));
      PrintStream requestFile = new PrintStream(out2);
      for(int ad = 0; ad < vecModified.size(); ad++)
        requestFile.println(vecModified.get(ad).toString());

      requestFile.close();
      out2.close();
      
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "writeToFile", "", "", "Exception - ", e);
      return false;
    }
  }
  
  public void getResponse(String strUrl, String currUrl, String requestInXML)
  {
    Log.debugLog(className, "getResponse", "", "", "Method Starts strUrl = " + strUrl + ", currUrl = " + currUrl);
    System.out.println("strUrl = " + strUrl + ", currUrl = " + currUrl);
    try
    {
      URL url = new URL(strUrl);
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();  
      
      connection.setRequestMethod("POST"); //method is post
      connection.setRequestProperty("Content-Length", "" + requestInXML.length()); //content length
      connection.setRequestProperty("Content-type","text/xml"); //sending xml request
      connection.setRequestProperty("Connection", "Keep-Alive");   
  
      connection.setDoOutput(true);
      connection.setDoInput(true);

      //Turn off caching
      connection.setUseCaches(false);
      connection.setDefaultUseCaches (false);
      
      //connection.getOutputStream().write(requestLength.getBytes());
      //connection.connect();
     /* ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
      System.out.println("requestInXML " + requestInXML);
      
      out.reset();
      out.writeObject(requestInXML); //sending request
      out.flush();
      connection.connect();*/
      OutputStream out = connection.getOutputStream();
      System.out.println("requestInXML " + requestInXML);
      out.write(requestInXML.getBytes());
      out.flush();
      Log.debugLog(className, "getResponse", "", "", "Get content type " + connection.getContentType());
      InputStream ois = connection.getInputStream();
      
      FileOutputStream fout = new FileOutputStream(currUrl, false);     
      PrintStream printStream = new PrintStream(fout);
      printStream.println(streamToString(ois));
      // now close the streams
      printStream.close();
      fout.close();
      
      out.close();  
      connection.disconnect();
      ois.close();
    }
    catch (Exception e) 
    {
       String filepath = currUrl;
       System.out.println(filepath);
       File dataFile = new File(filepath);
       if(dataFile.exists())
       {
         dataFile.delete();
       }
       
      Log.stackTraceLog(className, "getResponse", "", "", "Exception in getting response -", e);
    }
  }

 public String streamToString(InputStream in) 
 {
   String out = new String();
   try
   {
	 String lineSep = System.getProperty("line.separator");
	 BufferedReader br = new BufferedReader(new InputStreamReader(in));
	 for(String line = br.readLine(); line != null; line = br.readLine()) 
	 {	 
	    out += line + lineSep;
	    
	 }   
	 return out;
   }
   catch(Exception e)
   {
	 Log.stackTraceLog(className, "getResponse", "", "", "Exception in getting response -", e);
	 return "";
   }
  }

  public void xmlString(String xmlRecords)
  {
    try
    {
      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlRecords));

      doc = db.parse(is);
    }
    catch (Exception e) 
    {
      System.out.println("Exception " + e);
      doc = null;
    }
  }
  
  public String getTagValue(String tagName)
  {
    try
    {
      String strValue = "";

      if(doc == null)
        return "";
      
      System.out.println("tagName == " + tagName);
      
      NodeList nodes = doc.getElementsByTagName("soapenv:Body");

      for (int i = 0; i < nodes.getLength(); i++) 
      {
        Element element = (Element) nodes.item(i);

        NodeList name = element.getElementsByTagName(tagName);
        Element line = (Element) name.item(0);
        strValue = getCharacterDataFromElement(line);
        System.out.println("Name: " + getCharacterDataFromElement(line));
      }
      
      return strValue;
    }
    catch (Exception e) 
    {
      return "";
    }
  }
  
  public static String getCharacterDataFromElement(Element e) 
  {
    Node child = e.getFirstChild();
    if (child instanceof CharacterData) 
    {
      CharacterData cd = (CharacterData) child;
      return cd.getData();
    }
    return "";
  }
  //sending response
  public String sendResponse(String requestType)
  {
    String strResponse = "";
   
    try
    {
      xmlString(requestType);
      
      String requestID = getTagValue("NF_REQ_TYPE"); 
      String userName = getTagValue("NF_REQ_LOGIN"); 
      String balance = getTagValue("NF_REQ_BALANCE"); 
      String accountNumber = getTagValue("NF_REQ_ACCOUNT_NUMBER");
      String userInforMation = getTagValue("NF_USER_ACCOUNT");
      
      String lineSep = System.getProperty("line.separator");

      StringBuffer xmlResponseHeader = new StringBuffer();
      StringBuffer xmlResponseFooter = new StringBuffer();

      xmlResponseHeader.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSep);
      xmlResponseHeader.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.W3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" + lineSep);
      xmlResponseHeader.append("  <soap:Body>" + lineSep);
      xmlResponseHeader.append("    <NF_RESPONSE_MSG xmlns=\"http://www.fds.com/schemas/XSCM6410\" >" + lineSep);
      xmlResponseHeader.append("      <NF_RESPONSE_VAR>" + lineSep);

      xmlResponseFooter.append("      </NF_RESPONSE_VAR>" + lineSep);
      xmlResponseFooter.append("    </NF_RESPONSE_MSG>" + lineSep);
      xmlResponseFooter.append("  </soap:Body>" + lineSep);
      xmlResponseFooter.append("</soap:Envelope>");
    
      StringBuffer strBuffResponse = new StringBuffer();
      System.out.println("#!!!!!!!!!!!!!1 = " + requestID);

      if(requestID.equals("AccountDetail")) //account detail
      {
        CavBankAccountData cavBankData = new CavBankAccountData();
        return cavBankData.getAccountDetail(requestID, userInforMation);
      }
      else if(requestID.equals("AccountSummary")) //account summary
      {   
        CavBankAccountData cavBankData = new CavBankAccountData();
        return cavBankData.getAccountSummary(requestID, userInforMation);
      }
      else if(requestID.equals("AccountActivity")) //account summary
      {   
        CavBankAccountData cavBankData = new CavBankAccountData();
        return cavBankData.getAccountActivity(requestID, accountNumber);
      }
      else if(requestID.equals("CreditAccountActivity")) //transfer
      {
        CavBankAccountData cavBankData = new CavBankAccountData();
        return cavBankData.getCreditAccountActivity(requestID, accountNumber);
      }
      else if(requestID.equals("4")) //add manage beneficiary
      {
        String benificaryName = getTagValue("NF_REQ_BENEFICIARY_NAME");
        strBuffResponse.append(xmlResponseHeader.toString());  
        strBuffResponse.append("        <NF_RSP_RESPONSE_USERNAME>" + userName + "</NF_RSP_RESPONSE_USERNAME>" + lineSep);
        strBuffResponse.append("        <NF_RSP_RESPONSE_NUMBER>4</NF_RSP_RESPONSE_NUMBER>" + lineSep); 
        strBuffResponse.append("        <NF_RSP_BENEFICIARY_NAME>" + benificaryName + "</NF_RSP_BENEFICIARY_NAME>" + lineSep);
        strBuffResponse.append("        <NF_RSP_BENEFICIARY_CODE>1002</NF_RSP_BENEFICIARY_CODE>" + lineSep);
        //strBuffResponse.append("        <NF_RSP_AVAILABLE_BALANCE>" + balance + "</NF_RSP_AVAILABLE_BALANCE>" + lineSep);         
        strBuffResponse.append("        <NF_RSP_RETURN_MESSAGE>" + "Add Beneficiary successfully"+ "</NF_RSP_RETURN_MESSAGE>" + lineSep);
        strBuffResponse.append(xmlResponseFooter.toString());                 
      }
      else if(requestID.equals("5")) //add manage beneficiary
      {
        strBuffResponse.append(xmlResponseHeader.toString());  
        strBuffResponse.append("        <NF_RSP_RESPONSE_USERNAME>" + userName + "</NF_RSP_RESPONSE_USERNAME>" + lineSep);
        strBuffResponse.append("        <NF_RSP_RESPONSE_NUMBER>5</NF_RSP_RESPONSE_NUMBER>" + lineSep); 
        strBuffResponse.append("        <NF_RSP_CREDIT_CARD_NUMBER>" + "5437056100920" + "</NF_RSP_CREDIT_CARD_NUMBER>" + lineSep);
        strBuffResponse.append("        <NF_RSP_CREDIT_CARD_NAME>1002</NF_RSP_CREDIT_CARD_NAME>" + lineSep);
        strBuffResponse.append("        <NF_RSP_AVAILABLE_CREDIT_CARD_LIMIT>100000</NF_RSP_AVAILABLE_CREDIT_CARD_LIMIT>" + lineSep);
        strBuffResponse.append("        <NF_RSP_AVAILABLE_CASH_LIMIT>" + balance + "</NF_RSP_AVAILABLE_CASH_LIMIT>" + lineSep);         
        strBuffResponse.append("        <NF_RSP_RETURN_MESSAGE>" + "Credit card Detail"+ "</NF_RSP_RETURN_MESSAGE>" + lineSep);
        strBuffResponse.append(xmlResponseFooter.toString());                 
      }      
      return strBuffResponse.toString();       
    }
    catch (Exception e)
    {
      return strResponse;
    }
  }
  
  public static void main(String[] args) 
  { 
    CavBank bank = new CavBank("req");
    String lineSep = System.getProperty("line.separator");

    StringBuffer xmlRequestHeader = new StringBuffer();
    StringBuffer xmlResquestFooter = new StringBuffer();

    xmlRequestHeader.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSep);
    xmlRequestHeader.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + lineSep);
    xmlRequestHeader.append("  <soapenv:Body>" + lineSep);
    xmlRequestHeader.append("    <NF_REQUEST_MSG xsi:type=\"ns1:NF_REQUEST_MSGType\" xmlns=\"http://wsdl.fsg.com/XSCM6410\" xmlns:ns1=\"http://www.fds.com/schemas/XSCM6410\">" + lineSep);
    xmlRequestHeader.append("      <ns1:NF_REQ_MSG_HEADER_AREA>" + lineSep);
    xmlRequestHeader.append("      <ns1:NF_REQ_TYPE>" + "1" + "</ns1:NF_REQ_TYPE>" + lineSep);
    xmlRequestHeader.append("      <ns1:NF_REQ_LOGIN>" + "jjjjjj" + "</ns1:NF_REQ_LOGIN>" + lineSep);
    xmlRequestHeader.append("      <ns1:NF_REQ_UNIQUE_ID>" + 4444 + "</ns1:NF_REQ_UNIQUE_ID>" + lineSep);
  
    xmlRequestHeader.append("      </ns1:NF_REQ_MSG_HEADER_AREA>" + lineSep);
    xmlRequestHeader.append("      <ns1:NF_REQUEST_VAR>" + lineSep);

    xmlResquestFooter.append("      </ns1:NF_REQUEST_VAR>" + lineSep);
    xmlResquestFooter.append("    </NF_REQUEST_MSG>" + lineSep);
    xmlResquestFooter.append("  </soapenv:Body>" + lineSep);
    xmlResquestFooter.append("</soapenv:Envelope>");
  
    StringBuffer strBuffRequest = new StringBuffer();
   
    strBuffRequest.append(xmlRequestHeader.toString());
    strBuffRequest.append("      <ns1:NF_REQ_TYPE>1</ns1:NF_REQ_TYPE>" + lineSep);
    strBuffRequest.append("      <ns1:NF_REQ_UNIQUE_ID>" + "1111" + "</ns1:NF_REQ_UNIQUE_ID>" + lineSep);
    strBuffRequest.append("      <ns1:NF_REQ_ACCOUNT_NUMBER>" + "222" + "</ns1:NF_REQ_ACCOUNT_NUMBER>" + lineSep);
    strBuffRequest.append("      <ns1:NF_REQ_BALANCE>" + "55555" + "</ns1:NF_REQ_BALANCE>" + lineSep);
    strBuffRequest.append("        <ns1:NF_REQ_REQUEST_NUMBER>" + "5555" + "</ns1:NF_REQ_REQUEST_NUMBER>" + lineSep);  
    strBuffRequest.append("        <ns1:NF_REQ_ACCOUNT_NUMBER>" + "55555" + "</ns1:NF_REQ_ACCOUNT_NUMBER>" + lineSep);

    strBuffRequest.append(xmlResquestFooter.toString());
    
    //bank.getResponse("http://192.168.1.40:8002/netstorm/NetFunctionServlet", "C:/home/netstorm/work/webapps/cavbank/xml/accountDetail.xml", "kkkkkkkkkkkkkkkkk", "RRRRRRRRREEEEESSSSPONSE");
    //bank.getResponse("http://192.168.1.41:8000/netstorm/NetFunctionServlet", "C:/home/netstorm/work/webapps/cavbank/xml/accountDetail.xml", strBuffRequest.toString());
  
    bank.sendResponse(strBuffRequest.toString());
  }
}
