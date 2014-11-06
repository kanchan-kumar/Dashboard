/*--------------------------------------------------------------------
  @Name    : RptDataInfo.java
  @Author  : Prabhat
  @Purpose : To keep report data that will be use by JSP to display data in some formats like(.jpeg, html etc.)
  @Modification History:
    03/24/09 : Prabhat --> Initial Version


----------------------------------------------------------------------*/

package pac1.Bean;

public class RptDataInfo {
	private final String className = "RptDataInfo";

	/**
	 * 	 Array to Store paragraphs can be used to describe an Image or table
	 * 	or can be used as a Heading. 
	 */
	String arrParaInfo[];
	
	/**
	 * 
	 * Array to store Images paths
	 */
	String arrImgPath[];
	
	/**
	 * Array to store tabular data
	 * 
	 */
	String arrTableData[][][];
	
	public RptDataInfo()
	{
		arrParaInfo = new String[0];
		arrImgPath = new String[0];
		arrTableData = new String[0][0][0];
	}

	/**
	 * @return the arrParaInfo
	 */
	public String[] getArrParaInfo() {
		return arrParaInfo;
	}

	/**
	 * @param arrParaInfo the arrParaInfo to set
	 */
	public void setArrParaInfo(String[] arrParaInfo) {
		this.arrParaInfo = arrParaInfo;
	}

	/**
	 * @return the arrImgPath
	 */
	public String[] getArrImgPath() {
		return arrImgPath;
	}

	/**
	 * @param arrImgPath the arrImgPath to set
	 */
	public void setArrImgPath(String[] arrImgPath) {
		this.arrImgPath = arrImgPath;
	}

	/**
	 * @return the arrTableData
	 */
	public String[][][] getArrTableData() {
		return arrTableData;
	}

	/**
	 * @param arrTableData the arrTableData to set
	 */
	public void setArrTableData(String[][][] arrTableData) {
		this.arrTableData = arrTableData;
	}

	
}