package pac1.Bean;
import java.io.*;
import java.net.*;
import pac1.Bean.*;

public class UrlDownload
{
  final static int size=1024;
  private static String className = "UrlDownload";

  public static boolean fileUrl(String fAddress, String localFileName, String destinationDir)
  {
    Log.debugLog(className, "fileUrl", "", "", "Method Starts fAddress=" + fAddress + ", destinationDir = " + destinationDir);
    OutputStream outStream = null;
    URLConnection  uCon = null;

    InputStream is = null;
    try
    {
      URL Url;
      byte[] buf;
      int ByteRead,ByteWritten=0;
      String connectionString = Config.getValue("Path_Latest_Build");
      //StringBuffer sb = new StringBuffer( "ftp://" );
      //sb.append("netstorm");
      //sb.append( ':' );
      //sb.append("Cavisson!");
      //sb.append( '@' );
      //sb.append( "ftp.cavisson.com:21" );
      //sb.append( '/' );
      //sb.append( fAddress );
      System.out.println("connectionString = " + connectionString + fAddress);
      Url= new URL(connectionString + fAddress);
      outStream = new BufferedOutputStream(new
      FileOutputStream(destinationDir));

      uCon = Url.openConnection();
      is = uCon.getInputStream();
      buf = new byte[size];
      while ((ByteRead = is.read(buf)) != -1)
      {
        outStream.write(buf, 0, ByteRead);
        ByteWritten += ByteRead;
      }
      return true;
      //System.out.println("Downloaded Successfully.");
      //System.out.println("File name:\""+localFileName+ "\"\nNo ofbytes :" + ByteWritten);
    }
    catch (Exception e)
    {
      //e.printStackTrace();
      Log.stackTraceLog(className, "fileUrl", "", "", "Exception ", e);
      return false;
    }
    finally
    {
      try
      {
        if(is != null)
          is.close();
        if(outStream != null)
          outStream.close();
      }
      catch (IOException e)
      {
        //e.printStackTrace();
        Log.stackTraceLog(className, "fileUrl", "", "", "Exception ", e);
      }
    }
  }

  public static boolean fileDownload(String fAddress, String destinationDir)
  {
    int slashIndex =fAddress.lastIndexOf('/');
    int periodIndex =fAddress.lastIndexOf('.');

    String fileName=fAddress.substring(slashIndex + 1);

    if (periodIndex >=1 &&  slashIndex >= 0 && slashIndex < fAddress.length()-1)
    {
      return fileUrl(fAddress,fileName,destinationDir);
    }
    else
    {
      Log.debugLog(className, "fileDownload", "", "", "Error in downloading file " + fAddress);


      return false;
    }
  }

  public static void main(String[] args)
  {
    if(args.length >=2)
    {
      for (int i = 1; i < args.length; i++)
      {
        if(fileDownload(args[i],args[0]))
        {
          System.out.println("Downloaded Successfully.");
        }
      }
    }
  }
}