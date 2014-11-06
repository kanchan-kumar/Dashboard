/**----------------------------------------------------------------------------
 * Name       ParameterAPIParsing.java
 * Purpose    This file is used for parsing the API
 *            Now this is using in nsl_cookie_var
 * @author    Ritesh Sharma
 * @version   1.0
 * Modification History
 *
 *---------------------------------------------------------------------------**/
package pac1.Bean;
import java.util.ArrayList;
import java.util.Iterator;

public class ParameterAPIParsing 
{
  String className ="ParameterAPIParsingjava";

  /*-------------------------------------------------------------------------------------------------
  Name: ParseAPI
  Purpose: To get the list after parsing the given API
  Input:
    It takes the string which contains the API with parameters
  Ouput:
    It returns the ArrayList, which contains following element
    1. The first element of list is String as API name
    2. The second name is integer type value, which contain the number of token used in API 
    3. The Third element is the ArrayList which contain the one value that is, parameter name
    4. And Other element which contains the ArrayList and arraylist hold the two string value i.e. attribute and value
  Algorithm:
    Algorithm is based on following facts:
    1. The given String contains the one API.
    2. Given string starts with the api name (as example nsl_cookie_var)and after that all values inclosed in '()'
    3. Now firstly we fetch the api name, check the format of api is correct or not and then add into list
    4. After getting the api name, fetch the string which is inclosed in ()
    5. Then remain string is split with comma (,) and store into string array
    6. array length is add into list, which is described the number of attributes 
    6. Fetching the array, then split with equal sign (=) and fetch the the attribute and value store into sublist
    7. first value is the parameter name, check with escape character as (",\) and add into list
    8. Other attribute and value check with escape character and store into sublist and add into list
  -------------------------------------------------------------------------------------------------*/
  public ArrayList parseAPI(String Api)
  {
    Log.debugLog(className, "parseAPI", "", "", "Method started");
    //this list will contain API name, number of tokens and parameters sublists
    ArrayList listOfElements = new ArrayList();
    String apiName = "";
    String parameterString = "";
    String numberOfToken = "0";
    if(!(Api.contains("(") && Api.contains(")")))
    {
      Log.debugLog(className, "parseAPI", "", "", "This Api format is wrong");
    }
    else
    {
      try
      {
        int index = Api.indexOf("(") ;
        //fetch the API name form string
        apiName = Api.substring(0, index);
        //add the API name into list
        listOfElements.add(apiName);
        //fetch the parameter string which is in ()
        parameterString = Api.substring( index + 1 );
        index = parameterString.lastIndexOf(")");
        parameterString = parameterString.substring(0,index);
        //Split the token by comma(,) and put into array for further parsing
        String paramArr[] = parameterString.split(",");
        //add the number of token using in the API
        listOfElements.add(paramArr.length);
        
        for (int i = 0; i < paramArr.length ; i++)
        {
          //create the sublist which contain attribute and values
          ArrayList keyValue = new ArrayList();
          {  
            //split with equals to (=) and store into array
            String tempArr[] = paramArr[i].split("=");
            //add first value, before adding into list it will check the escape characters
            keyValue.add(checkEscapeCharacters(tempArr[0]));
            if(tempArr.length > 1)
            {
              String temp = tempArr[1];
              //if the value contain '=' character the will be split, now it will be added into value string
              if(tempArr.length > 2)
                temp = temp + "=" +tempArr[2];
              //check for escape characters
              temp = checkEscapeCharacters(temp);
              //add value into sublist
              keyValue.add(temp); 
            }  
          } 
          //every sublist add into main list
          listOfElements.add(keyValue);
        }   
      }
      catch (Exception ex) 
      {
        Log.stackTraceLog(className, "getCookieVarByURL", "", "", "Exception - ", ex);
        return listOfElements; 
      }
      
    }   
    return listOfElements;  
  }
  
  /*
   * This function checks the value in double quotes and slash, 
   * if the value in escape character it will be remove.
   */
  public String checkEscapeCharacters(String Value)
  {
    Log.debugLog(className, "checkEscapeCharacters", "", "", "Mehod called");
    //check for double quotes in given string
    if(Value.startsWith("\\")&&(Value.endsWith("\\")))
    {
      Value = Value.substring(Value.indexOf("\\")+1,Value.length());
      Value = Value.substring(0,Value.lastIndexOf("\\")-1);
    } 
    //check for slash (\) in given string
    if(Value.startsWith("\"")&&(Value.endsWith("\"")))
    {
      Value = Value.substring(Value.indexOf("\"")+1,Value.length());
      Value = Value.substring(0,Value.lastIndexOf("\""));
    }  
    
    Log.debugLog(className, "checkEscapeCharacters", "", "", "Return the value =" + Value);
    
    return Value;
  }
  
  /*
   * This is main method 
   */
  public static void main(String []args)
  {
    ParameterAPIParsing p = new ParameterAPIParsing();
    ArrayList list = new ArrayList();
    list = p.parseAPI("nsl_search_var(TR069CompleteTime, LB=\"<cwmp: CompleteTime>\", RB=, Search=VARIABLE, VAR=TR069DownloadBody, PAGE=*);");
    System.out.println("The api name is : " + list.get(0).toString());
    list.remove(0);
    list.remove(0);
    Iterator itr = list.iterator();
    while(itr.hasNext())
    {
      ArrayList Sublist = new ArrayList();
      Sublist = (ArrayList)itr.next();
      if(Sublist.size()>0)
      {
        System.out.print(Sublist.get(0).toString() + " : ");
        if(Sublist.size() > 1)
        {
          System.out.print(Sublist.get(1).toString());
        }
        System.out.println("\n");
      }  
    }  
  }
}
