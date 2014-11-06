/**----------------------------------------------------------------------------
 * Name     :  cm_java_gc_ex.java
 * Purpose  :   For testing purpose 
 * @author  :  Bibhu Prasad Tripathy
 *   
 */
package pac1.Bean;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class ClassVersionTest
{
  private static final int JAVA_CLASS_MAGIC = 0xCAFEBABE;

  public static void main(String[] args)
  {
    try
    {
      Scanner a = new Scanner(System.in);
      System.out.println("Enter the java Class file");
      String s = a.next();
      File f = new File(s);
      if(!f.exists())
      {
        System.out.println("file not exists.Check with absolute Path.");
      }
      DataInputStream dis = new DataInputStream(new FileInputStream(f));
      int magic = dis.readInt();
      if(magic == JAVA_CLASS_MAGIC)
      {
        int minorVersion = dis.readUnsignedShort();
        int majorVersion = dis.readUnsignedShort();

        /**
         * majorVersion is ...
         * J2SE 6.0 = 50 (0x32 hex),
         * J2SE 5.0 = 49 (0x31 hex),
         * JDK 1.4 = 48 (0x30 hex),
         * JDK 1.3 = 47 (0x2F hex),
         * JDK 1.2 = 46 (0x2E hex),
         * JDK 1.1 = 45 (0x2D hex).
         */
        System.out.println("entered class was compiled with  " + majorVersion + "." + minorVersion);
        System.out.println("--------:IN DETAIL :--------");
        if(majorVersion == 50)
        {
          System.out.println("Compiled with j2se 6.0");
        }
        if(majorVersion == 49)
        {
          System.out.println("Compiled with j2se 5.0");
        }

        if(majorVersion == 48)
        {
          System.out.println("Compiled with j2se 1.4");
        }
        if(majorVersion == 47)
        {
          System.out.println("Compiled with j2se 1.3");
        }
        if(majorVersion == 46)
        {
          System.out.println("Compiled with j2se 1.2");
        }
        if(majorVersion == 45)
        {
          System.out.println("Compiled with j2se 1.1");
        }
        System.out.println("----------------:--------");
      }
      else
      {
        // not a class file
      }
    }
    catch(FileNotFoundException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
