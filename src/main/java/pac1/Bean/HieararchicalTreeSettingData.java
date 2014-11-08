package pac1.Bean;

import java.util.HashMap;
import java.util.Vector;

public class HieararchicalTreeSettingData implements java.io.Serializable
{
  private static final long serialVersionUID = -780475663833787849L;
  private static String className = "HieararchicalTreeSettingData";
  private String workPath = Config.getWorkPath();
  private static String iniFileName = "hierarchicalTreeSettings.ini";
  private HashMap<String, TreeIconColorDTO> treeIconColorHM = new HashMap<String, TreeIconColorDTO>();

  private final byte COLOR_INDEX = 1;
  private final byte COLLAPSED_ICON_INDEX = 2;
  private final byte EXPAND_ICON_INDEX = 2;
  
  private final byte ICON_INDEX=2;

  public HieararchicalTreeSettingData()
  {
    initHieararchicalTreeSettingData();
  }

  /**
   * @return the treeIconColorHM
   */
  public HashMap<String, TreeIconColorDTO> getTreeIconColorHM()
  {
    return treeIconColorHM;
  }

  private void initHieararchicalTreeSettingData()
  {
    try
    {
      Log.debugLog(className, "getIconColorData", "", "", "Method called.");
      String filePath = workPath + "/webapps/netstorm/config/";
      String iniFileWithPath = filePath + iniFileName;
      Vector<String> treeIconColorData = rptUtilsBean.readFileInVector(iniFileWithPath);
      if (treeIconColorData == null)
      {
        Log.errorLog(className, "getIconColorData", "", "", "File " + iniFileName + " may not exist at path " + filePath);
        return;
      }

      // No need to get metadata info so we are running loop from 1.
      for (int i = 1; i < treeIconColorData.size(); i++)
      {
        String line = treeIconColorData.get(i);
        String[] tmpIconColorArr = rptUtilsBean.strToArrayData(line, "|");

        TreeIconColorDTO treeIconColorDTO = new TreeIconColorDTO();
        treeIconColorDTO.setCollapsedIcon(tmpIconColorArr[COLLAPSED_ICON_INDEX]);
        treeIconColorDTO.setExpendIcon(tmpIconColorArr[EXPAND_ICON_INDEX]);
        treeIconColorDTO.setColorValue(tmpIconColorArr[COLOR_INDEX]);
        treeIconColorDTO.setIcon(tmpIconColorArr[ICON_INDEX]);

        String key = tmpIconColorArr[0];
        treeIconColorHM.put(key, treeIconColorDTO);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readReport", "", "", "Exception - ", e);
    }
  }

  public static void main(String[] args)
  {
    HieararchicalTreeSettingData aa = new HieararchicalTreeSettingData();
    System.out.println(aa.treeIconColorHM.size());
  }
}
