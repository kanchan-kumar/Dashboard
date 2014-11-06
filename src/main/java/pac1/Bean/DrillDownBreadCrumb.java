/**DrillDownBreadCrumb
 * Purpose: work as cache
**/

package pac1.Bean;

import java.util.Vector;

public class DrillDownBreadCrumb
{
  private String breadCrumbLabel = ""; //Label to show in bread crumb
  private String breadCrumbURL = "";  //Url
  private String filterArgument = ""; //argument of query like: --testrun 123 --object 1 --order page
  private String filterArgumentFirst = "";
  private String filterCriteria = ""; //criteria to show like Status = ALL, Start TIme = 00:00:00
  private String limit = ""; //Limit to show records in table
  private String topFilter = "150";
  private String clsNameWidthPixelFilter = "300";
  private String methodTimeFilter = "1"; 
  private String highlightWallTimeFilter = "1500";
  private String percentGreaterThanFilter = "";
  private String diffGreaterThanFilter = "";
  private String offset = ""; //in pagination offset value vary
  private String defaultSortIndicator = ""; //sorting indicator on which column
  private String sortColumnType = "";
  private String totalRecord = ""; //total number of records
  private int breadCrumbCounter = 0; //counter as per each page
  private String totalRecordFirst = "0";
  private String totalRecordSecond = "0";
  private String totalRecordThird = "0";
  private String totalRecordFourth = "0";
  private String totalRecordSum = "0";
  private String totalAvg = "0.0";
  
  private String testRun = "-1";
  private String queryName = "Query1";
  private String [][]arrBreadCrumbData = null;  
  private String [][]arrBreadCrumbDataFirst = null; 
  private String [][]arrBreadCrumbDataSecond = null;
  private String [][]arrBreadCrumbDataThird = null;
  private String[][] arrBreadCrumbDataFourth = null;
  private String currTabName = "method"; //it is used in flow report page for different tab.
  private String[] wholeSignArray = null;//used in sequence Diagram
  private String noPrvs = "0";//used in sequence Diagram to determine whether user has selected any thing in previous
  
 


private Vector vecData = new Vector();
  
  public int breadCrumbID = -1;
  //constructor
  public DrillDownBreadCrumb(String testRun, String queryName, int breadCrumbCounter)
  {
    this.testRun = testRun;
    this.queryName = queryName;
    this.breadCrumbCounter = breadCrumbCounter;
    
    breadCrumbID++;
  }
   
/**************************************Setter method*********************************/
  
  public void setBreadCrumbCounter(int breadCrumbCounter)
  {
    this.breadCrumbCounter = breadCrumbCounter;
  } 
  
  public void setBreadCrumbLabel(String breadCrumbLabel)
  {
    if(breadCrumbLabel != null)
      this.breadCrumbLabel = breadCrumbLabel;
    else
      breadCrumbLabel = "";	  
  } 
  
  public void setBreadCrumbURL(String breadCrumbURL)
  {
    if(breadCrumbURL != null)
      this.breadCrumbURL = breadCrumbURL;
    else
      breadCrumbURL = "";	  
  }  
  
  public void setBreadCrumbVectorData(Vector vecData)
  {
    if(vecData != null)
      this.vecData = vecData;
    else
      vecData = null;	  
  } 
   
  public void setFilterArgument(String filterArgument)
  {
    if(filterArgument != null)
      this.filterArgument = filterArgument;
    else
      filterArgument = "";	  
  }   

  public void setFilterArgumentFirst(String filterArgumentFirst)
  {
    if(filterArgumentFirst != null)
      this.filterArgumentFirst = filterArgumentFirst;
    else
      filterArgumentFirst = "";    
  }  
  
  public void setFilterCriteria(String filterCriteria)
  {
    if(filterCriteria != null)
      this.filterCriteria = filterCriteria;
    else
      filterCriteria = "";	  
  }  
  
  public void setLimit(String limit)
  {
    if(limit != null)
      this.limit = limit;
    else
      limit = "";	  
  }  
  
  public void setTopFilter(String topFilter)  
  {
     if(topFilter != null)
       this.topFilter = topFilter;
     else  
       topFilter = "" ;
  } 
 
  public void setClsNameWidthPixelFilter(String clsNameWidthPixelFilter)
  {
     if(clsNameWidthPixelFilter != null)
       this.clsNameWidthPixelFilter = clsNameWidthPixelFilter;
     else
       clsNameWidthPixelFilter = "" ;
  }


  public void setMethodTimeFilter(String methodTimeFilter)
  {
     if(methodTimeFilter != null)
       this.methodTimeFilter = methodTimeFilter;
     else
       methodTimeFilter = "" ;
  } 
  
  public void sethighlightWallTimeFilter(String highlightWallTimeFilter)
  {
     if(highlightWallTimeFilter != null)
       this.highlightWallTimeFilter = highlightWallTimeFilter;
     else
       highlightWallTimeFilter = "" ;
  } 
  
  public void setpercentGreaterThanFilter(String percentGreaterThanFilter)
  {
     if(percentGreaterThanFilter != null)
       this.percentGreaterThanFilter = percentGreaterThanFilter;
     else
       percentGreaterThanFilter = "" ;
  } 
  
  public void setdiffGreaterThanFilter(String diffGreaterThanFilter)
  {
     if(diffGreaterThanFilter != null)
       this.diffGreaterThanFilter = diffGreaterThanFilter;
     else
       diffGreaterThanFilter = "" ;
  } 

  public void setOffset(String offset)
  {
    if(offset != null)
      this.offset = offset;
    else
      offset = "";	  
  }  
  
  public void setDefaultSortIndicator(String defaultSortIndicator)
  {
    if(defaultSortIndicator != null)
      this.defaultSortIndicator = defaultSortIndicator;
    else
      defaultSortIndicator = "";	  
  } 
  
  public void setSortColumnType(String sortColumnType)
  {
    if(sortColumnType != null)
      this.sortColumnType = sortColumnType;
    else
      sortColumnType = "";    
  } 
  
  public void setTotalRecord(String totalRecord)
  {
    if(totalRecord != null)
      this.totalRecord = totalRecord;
    else
      totalRecord = "0";	  
  }
  
  public void setTotalRecordSum(String totalRecordSum)
  {
    if(totalRecordSum != null)
      this.totalRecordSum = totalRecordSum;
    else
      this.totalRecordSum = "0";
  }
  
  public void setTotalAvg(String totalAvg)
  {
    if(totalAvg != null)
      this.totalAvg = totalAvg;
    else
      this.totalAvg = "0.0";
  }
  
  public void setTotalRecordFirst(String totalRecord)
  {
    totalRecordFirst = totalRecord;
  }
  
  public void setTotalRecordSecond(String totalRecord)
  {
    totalRecordSecond = totalRecord;
  }
  
  public void setTotalRecordThird(String totalRecord)
  {
    totalRecordThird = totalRecord;
  }
  
  public void setTotalRecordFourth(String totalRecord)
  {
    totalRecordFourth = totalRecord;
  }
  
  public void setCurrTabName(String tabName)
  {
    currTabName = tabName;
  }
  
  public void setBreadCrumbData(String [][]arrBreadCrumbData1)
  {
    arrBreadCrumbData = new String[arrBreadCrumbData1.length][];
    for(int i=0; i < arrBreadCrumbData1.length; i++)
    {
      arrBreadCrumbData[i] = arrBreadCrumbData1[i].clone();
    }			
  }
   
  public void setBreadCrumbDataFirst(String [][]arrBreadCrumbData2)
  {
    arrBreadCrumbDataFirst = new String[arrBreadCrumbData2.length][];
    for(int i=0; i < arrBreadCrumbData2.length; i++)
    {
      arrBreadCrumbDataFirst[i] = arrBreadCrumbData2[i].clone();
    }     
  }
  
  public void setBreadCrumbDataSecond(String [][]arrBreadCrumbData3)
  {
    arrBreadCrumbDataSecond = new String[arrBreadCrumbData3.length][];
    for(int i=0; i < arrBreadCrumbData3.length; i++)
    {
      arrBreadCrumbDataSecond[i] = arrBreadCrumbData3[i].clone();
    }
  }
  
  public void setBreadCrumbDataThird(String [][]arrBreadCrumbData4)
  {
    arrBreadCrumbDataThird = new String[arrBreadCrumbData4.length][];
    for(int i=0; i < arrBreadCrumbData4.length; i++)
    {
      arrBreadCrumbDataThird[i] = arrBreadCrumbData4[i].clone();
    }
  }
  
/**
 * Needed for default checking of sequence Diagram
 * @param wholeArray
 */

public void setWholeSignArray(String[] WholeSign)
{
    if(WholeSign != null)
    {
	wholeSignArray = new String[WholeSign.length];
    for(int i=0;i<WholeSign.length;i++)
     	{
	wholeSignArray[i] = WholeSign[i];
     	}
    }
}
/**
 * Checking whether user has selected any thing Previous or not
 * @param noPrvs
 */
public void setNoPrvs(String noPrvs) {
    this.noPrvs = noPrvs;
}

  /*****************************Getter method*********************************/
public String getNoPrvs() {
    return noPrvs;
} 

public String[] getWholeSignArray()
  {
    return wholeSignArray;
  }

  public int getBreadCrumbIDs()
  {
    return breadCrumbID;
  }
  
  public int getBreadCrumbCounter()
  {
    return breadCrumbCounter;
  }
  
  public String getTotalRecord()
  {
    return totalRecord;
  }  
 
  public String getTopFilter()
  {
    return topFilter;
  }
  public String getClsNameWidthPixelFilter()
  {
    return clsNameWidthPixelFilter;
  } 
  public String getMethodTimeFilter()
  {
    return methodTimeFilter;
  }
  public String gethighlightWallTimeFilter()
  {
    return highlightWallTimeFilter;
  }
  public String getpercentGreaterThanFilter()
  {
    return percentGreaterThanFilter;
  }
  public String getdiffGreaterThanFilter()
  {
    return diffGreaterThanFilter;
  }

  public String getTotalRecordFirst()
  {
    return totalRecordFirst;
  }
  
  public String getTotalRecordSecond()
  {
    return totalRecordSecond;
  }
  
  public String getTotalRecordThird()
  {
    return totalRecordThird;
  }
  
  public String getTotalRecordFourth()
  {
    return totalRecordFourth;
  }
  
  public String getCurrTabName()
  {
    return currTabName;
  }
  
  public String getDefaultSortIndicator()
  {
    return defaultSortIndicator;
  }
  
  public String getSortColumnType()
  {
    return sortColumnType;    
  }
  
  public String getOffset()
  {
    return offset;
  }
   
  public String getLimit()
  {
    return limit;
  }
  
  public String getFilterCriteria()
  {
    return filterCriteria;
  }
  
  public String getFilterArument()
  {
    return filterArgument;
  }
  
  public String getFilterArumentFirst()
  {
    return filterArgumentFirst;
  }
  
  public String getBreadCrumbURL()
  {
    return breadCrumbURL;
  }
  
  public Vector getBreadCrumbVectorData()
  {
    return vecData;
  }
  
  public String getBreadCrumbLabel()
  {
    return breadCrumbLabel;
  }
  
  public String[][] getBreadCrumbData()
  {
    return arrBreadCrumbData;
  }
  
  public String[][] getBreadCrumbDataFirst()
  {
    return arrBreadCrumbDataFirst;
  } 
  
  public String[][] getBreadCrumbDataSecond()
  {
    return arrBreadCrumbDataSecond;
  } 
  
  public String[][] getBreadCrumbDataThird()
  {
    return arrBreadCrumbDataThird;
  }
  
  public String getTotalRecordSum()
  {
    return totalRecordSum;
  }
  
  public String getTotalAvg()
  {
    return totalAvg;
  }

  /**
   * Used for Cache Purpose in Method Timing Table Below Sequence Diagram
   * @param arrResult
   */
  public void setBreadCrumbDataFourth(String[][] arrResult) 
  {
	 this.arrBreadCrumbDataFourth = arrResult;
  }

  /*
   * Getter for arrBreadCrumbFourth
   */
  public String[][] getBreadCrumbDataFourth() 
  {
	return arrBreadCrumbDataFourth;
  }
}
