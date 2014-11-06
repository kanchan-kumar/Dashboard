package pac1.Bean;


public class HarPageSizeExcelInfo
{
//HTML count
  private int HTMLCount = 0;
  //Size of HTML data
  private String HTMLByte = "";
  //Percent of HTML in TPS
  private double percHTML = 0;
  
  //JavaScript count
  private int JSCount = 0;
  //Size of JavaScript data
  private String JSByte = "";
  //Percent of JavaScript in TPS
  private double percJS = 0;
  
  //CSS count
  private int CSSCount = 0;
  //Size of CSS data
  private String CSSByte = "";
  //Percent of CSS in TPS
  private double percCSS = 0;
  
  //Image count
  private int ImageCount = 0;
  //Size of Image data
  private String ImageByte = "";
  //Percent of Iamge in TPS
  private double percImage = 0;
  
  //Others count
  private int OthersCount = 0;
  //Size of Others data
  private String OthersByte = "";
  //Percent of Others in TPS
  private double percOthers = 0;
  
  //Total Page Size count
  private int totalCount = 0;
  //Size of Total Page Size data
  private String totalByte = "";
  
  public int getHTMLCount()
  {
    return HTMLCount;
  }

  public void setHTMLCount(int hTMLCount)
  {
    HTMLCount = hTMLCount;
  }

  public String getHTMLByte()
  {
    return HTMLByte;
  }

  public void setHTMLByte(String hTMLByte)
  {
    HTMLByte = hTMLByte;
  }

  public double getPercHTML()
  {
    return percHTML;
  }

  public void setPercHTML(double percHTML)
  {
    this.percHTML = percHTML;
  }

  public int getJSCount()
  {
    return JSCount;
  }

  public void setJSCount(int jSCount)
  {
    JSCount = jSCount;
  }

  public String getJSByte()
  {
    return JSByte;
  }

  public void setJSByte(String jSByte)
  {
    JSByte = jSByte;
  }

  public double getPercJS()
  {
    return percJS;
  }

  public void setPercJS(double percJS)
  {
    this.percJS = percJS;
  }

  public int getCSSCount()
  {
    return CSSCount;
  }

  public void setCSSCount(int cSSCount)
  {
    CSSCount = cSSCount;
  }

  public String getCSSByte()
  {
    return CSSByte;
  }

  public void setCSSByte(String cSSByte)
  {
    CSSByte = cSSByte;
  }

  public double getPercCSS()
  {
    return percCSS;
  }

  public void setPercCSS(double percCSS)
  {
    this.percCSS = percCSS;
  }

  public int getImageCount()
  {
    return ImageCount;
  }

  public void setImageCount(int imageCount)
  {
    ImageCount = imageCount;
  }

  public String getImageByte()
  {
    return ImageByte;
  }

  public void setImageByte(String imageByte)
  {
    ImageByte = imageByte;
  }

  public double getPercImage()
  {
    return percImage;
  }

  public void setPercImage(double percImage)
  {
    this.percImage = percImage;
  }

  public int getOthersCount()
  {
    return OthersCount;
  }

  public void setOthersCount(int othersCount)
  {
    OthersCount = othersCount;
  }

  public String getOthersByte()
  {
    return OthersByte;
  }

  public void setOthersByte(String othersByte)
  {
    OthersByte = othersByte;
  }

  public double getPercOthers()
  {
    return percOthers;
  }

  public void setPercOthers(double percOthers)
  {
    this.percOthers = percOthers;
  }

  public int getTotalCount()
  {
    return totalCount;
  }

  public void setTotalCount(int totalCount)
  {
    this.totalCount = totalCount;
  }

  public String getTotalByte()
  {
    return totalByte;
  }

  public void setTotalByte(String totalByte)
  {
    this.totalByte = totalByte;
  }
}
