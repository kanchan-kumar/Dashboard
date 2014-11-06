package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;


public class CavBankAccountData
{
  static String className = "CavBankAccountData";
  String lineSep = System.getProperty("line.separator");
  
  public CavBankAccountData()
  {
  }
 
  private String readFile(String filepath)
  {
      try
      { 
        System.out.println("CavBankAccountData.readFile() method called. File = " + filepath);

        StringBuffer strBuffResponse = new StringBuffer();

        File dataFile = new File(filepath);
        
        if(!dataFile.exists())
        {
          System.out.println("CavBankAccountData.getAccountDetail() - File is not present. File = " + filepath);
          return strBuffResponse.toString();
        }
        
        String strLine;
        FileInputStream fis = new FileInputStream(filepath);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        while((strLine = br.readLine()) != null)
        {
      	 if(strBuffResponse.equals(""))
             strBuffResponse.append(strLine + lineSep);
           else
             strBuffResponse.append(strLine + lineSep);
        }
        br.close();
        fis.close();
        
        
        System.out.println("CavBankAccountData.getAccountDetail(). Data = " + strBuffResponse.toString());
        return strBuffResponse.toString();
      }
      catch(Exception e)
      {
        e.printStackTrace();
        return("Exception = " + e);
      }
  }

  public String getAccountDetail(String requestID, String userInforMation)
  {
    System.out.println("CavBankAccountData.getAccountDetail() method called");
    String filepath = Config.getWorkPath() + "/webapps/cavbank/xml/" + userInforMation + requestID + ".xml";
    return(readFile(filepath));
  }


  public String getAccountSummary(String requestID, String userInforMation)
  {
    System.out.println("CavBankAccountData.getAccountSummary() method called");
    String filepath = Config.getWorkPath() + "/webapps/cavbank/xml/" + userInforMation + requestID + ".xml";
    return(readFile(filepath));
  }

  public String getAccountActivity(String requestID, String accountNumber)
  {
    System.out.println("CavBankAccountData.getAccountActivity() method called");
    String filepath = Config.getWorkPath() + "/webapps/cavbank/xml/" + accountNumber + requestID + ".xml";
    return(readFile(filepath));
  }

  public String getCreditAccountActivity(String requestID, String accountNumber)
  {
    System.out.println("CavBankAccountData.getCreditAccountActivity() method called");
    String filepath = Config.getWorkPath() + "/webapps/cavbank/xml/" + accountNumber + requestID + ".xml";
    return(readFile(filepath));
  }
}
