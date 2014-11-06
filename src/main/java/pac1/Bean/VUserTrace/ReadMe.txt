/*--------------------------------------------------------------------
  @Name      : ReadMe File
  @Author    : Ankit Khanijau
  @Purpose   : To read XML File under path workPath + "/webapps/logs/TRXXXX/user_trace/groupname_NVMId_UserId_SessionNumber/User_Trace.xml
  @Modification History:
      07/15/2011 -> Ankit Khanijau

----------------------------------------------------------------------*/

// 'user_trace.xsd' is used to create following files

   CheckPoint.java
   EndSession.java
   ObjectFactory.java
   Page.java
   Pages.java
   Parameter.java
   Parameterization.java
   SearchParameter.java
   SearchParameters.java
   StartSession.java
   UserTrace.java
   Validations.java
   
   by the command --
     
     SYNTAX:
     xjc <.xsd File Name> -d <generated file will go into this DIR> -p <specify the target package>
     
     Example:
     
     ### If 'user_trace.xsd' file is kept under 'C:\home\netstorm\work\webapps\netstorm\java\com\cavisson\netstorm\gui\java\client\common' PATH
     
     C:\home\netstorm\work\webapps\netstorm\java\com\cavisson\netstorm\gui\java\client\common> 
              xjc user_trace.xsd -d UserTrace <-p is optional if want to place in current directory>
     
     ** --> while specifying the target path, sub directory name to be given by '.' instead of '/'
            
            For example: 
              If files to be kept under C:\home\...\common\UserTrace\abc
               
               then,
                   xjc user_trace.xsd -d UserTrace -p UserTrace.abc
               
             