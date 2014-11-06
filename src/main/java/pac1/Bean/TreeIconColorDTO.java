package pac1.Bean;

import java.io.Serializable;

public class TreeIconColorDTO implements Serializable
{
  private static final long serialVersionUID = 5398660973447369964L;
  private String colorValue = null;
  private String collapsedIcon = null;
  private String expendIcon = null;

  private String icon = null;
  
  
  public String getIcon() {
	return icon;
}

public void setIcon(String icon) {
	this.icon = icon;
}

/**
   * @return the colorValue
   */
  public String getColorValue()
  {
    return colorValue;
  }

  /**
   * @param colorValue
   *          the colorValue to set
   */
  public void setColorValue(String colorValue)
  {
    this.colorValue = colorValue;
  }

  /**
   * @return the collapsedIcon
   */
  public String getCollapsedIcon()
  {
    return collapsedIcon;
  }

  /**
   * @param collapsedIcon
   *          the collapsedIcon to set
   */
  public void setCollapsedIcon(String collapsedIcon)
  {
    this.collapsedIcon = collapsedIcon;
  }

  /**
   * @return the expendIcon
   */
  public String getExpendIcon()
  {
    return expendIcon;
  }

  /**
   * @param expendIcon
   *          the expendIcon to set
   */
  public void setExpendIcon(String expendIcon)
  {
    this.expendIcon = expendIcon;
  }

  public String toString()
  {
    return "Color Value = " + colorValue + ", Collapsed Icon = " + collapsedIcon + ", Expand Icon = " + expendIcon;
  }
}
