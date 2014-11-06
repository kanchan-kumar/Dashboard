package pac1.Bean;

//import java.io.Serializable;

public class CreateShape implements java.io.Serializable 
{
  private final String className = "CreateShape";

  private String shapeCaption = null, serviceMode = null, serviceType = null, layoutDescription = null;
  private double xAxisPos = -1, yAxisPos = -1, width = -1, height = -1;
  private boolean isInterface = false, isLayoutComp = false, isInterfaceSimulator = false;

  private Object vertexObj = null, edgeObj = null;
  private CreateShape begVertxObj = null, endVertxObj = null, triVertxObj = null, triMixedVertxObj = null, parentEdge = null;
  
  public CreateShape()
  {
  }

  public String getLayoutDescription()
  {
    return layoutDescription;
  }
  
  public void setLayoutDescription(String layoutDescription)
  {
    this.layoutDescription = layoutDescription;
  }

  public String getClassName()
  {
    return className;
  }

  public String getShapeCaption()
  {
    return shapeCaption;
  }

  public String getServiceMode()
  {
    return serviceMode;
  }

  public String getServiceType()
  {
    return serviceType;
  }

  public double getxAxisPos()
  {
    return xAxisPos;
  }

  public double getyAxisPos()
  {
    return yAxisPos;
  }

  public double getWidth()
  {
    return width;
  }

  public double getHeight()
  {
    return height;
  }

  public boolean isInterface()
  {
    return isInterface;
  }

  public boolean isLayoutComp()
  {
    return isLayoutComp;
  }

  public boolean isInterfaceSimulator()
  {
    return isInterfaceSimulator;
  }

  public Object getVertexObj()
  {
    return vertexObj;
  }

  public Object getEdgeObj()
  {
    return edgeObj;
  }

  public CreateShape getBegVertxObj()
  {
    return begVertxObj;
  }

  public CreateShape getEndVertxObj()
  {
    return endVertxObj;
  }

  public CreateShape getTriVertxObj()
  {
    return triVertxObj;
  }

  public CreateShape getTriMixedVertxObj()
  {
    return triMixedVertxObj;
  }

  public CreateShape getParentEdge()
  {
    return parentEdge;
  }

  public void setShapeCaption(String shapeCaption)
  {
    this.shapeCaption = shapeCaption;
  }

  public void setServiceMode(String serviceMode)
  {
    this.serviceMode = serviceMode;
  }

  public void setServiceType(String serviceType)
  {
    this.serviceType = serviceType;
  }

  public void setxAxisPos(double xAxisPos)
  {
    this.xAxisPos = xAxisPos;
  }

  public void setyAxisPos(double yAxisPos)
  {
    this.yAxisPos = yAxisPos;
  }

  public void setWidth(double width)
  {
    this.width = width;
  }

  public void setHeight(double height)
  {
    this.height = height;
  }

  public void setInterface(boolean isInterface)
  {
    this.isInterface = isInterface;
  }

  public void setLayoutComp(boolean isLayoutComp)
  {
    this.isLayoutComp = isLayoutComp;
  }

  public void setInterfaceSimulator(boolean isInterfaceSimulator)
  {
    this.isInterfaceSimulator = isInterfaceSimulator;
  }

  public void setVertexObj(Object vertexObj)
  {
    this.vertexObj = vertexObj;
  }

  public void setEdgeObj(Object edgeObj)
  {
    this.edgeObj = edgeObj;
  }

  public void setBegVertxObj(CreateShape begVertxObj)
  {
    this.begVertxObj = begVertxObj;
  }

  public void setEndVertxObj(CreateShape endVertxObj)
  {
    this.endVertxObj = endVertxObj;
  }

  public void setTriVertxObj(CreateShape triVertxObj)
  {
    this.triVertxObj = triVertxObj;
  }

  public void setTriMixedVertxObj(CreateShape triMixedVertxObj)
  {
    this.triMixedVertxObj = triMixedVertxObj;
  }

  public void setParentEdge(CreateShape parentEdge)
  {
    this.parentEdge = parentEdge;
  }

}
