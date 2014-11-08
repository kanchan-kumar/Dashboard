/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cavisson.gui.dashboard.components.charts.model;

import java.util.ArrayList;
import pac1.Bean.GraphUniqueKeyDTO;


public class DefaultPanelGraphs
{
  private ArrayList<GraphUniqueKeyDTO[]> arrPanelGraphList = new ArrayList<GraphUniqueKeyDTO[]>();
  
  public DefaultPanelGraphs()
  {
    GraphUniqueKeyDTO []panelOneGraph = new GraphUniqueKeyDTO[1];
    
    panelOneGraph[0] = new GraphUniqueKeyDTO(1, 1, "NA");
    //panelOneGraph[1] = new GraphUniqueKeyDTO(1, 2, "NA");
    //panelOneGraph[2] = new GraphUniqueKeyDTO(1, 3, "NA");
    
    GraphUniqueKeyDTO []panelTwoGraph = new GraphUniqueKeyDTO[6];
  
    panelTwoGraph[0] = new GraphUniqueKeyDTO(2, 1, "NA");
    panelTwoGraph[1] = new GraphUniqueKeyDTO(2, 2, "NA");
    panelTwoGraph[2] = new GraphUniqueKeyDTO(2, 3, "NA");
    panelTwoGraph[3] = new GraphUniqueKeyDTO(2, 5, "NA");
    panelTwoGraph[4] = new GraphUniqueKeyDTO(2, 6, "NA");
    panelTwoGraph[5] = new GraphUniqueKeyDTO(2, 7, "NA");
    
    
    GraphUniqueKeyDTO []panelThreeGraph = new GraphUniqueKeyDTO[4];
  
    panelThreeGraph[0] = new GraphUniqueKeyDTO(3, 1, "NA");
    panelThreeGraph[1] = new GraphUniqueKeyDTO(3, 2, "NA");
    panelThreeGraph[2] = new GraphUniqueKeyDTO(3, 3, "NA");
    panelThreeGraph[3] = new GraphUniqueKeyDTO(3, 4, "NA");
    
    GraphUniqueKeyDTO []panelFourGraph = new GraphUniqueKeyDTO[4];
  
    panelFourGraph[0] = new GraphUniqueKeyDTO(4, 1, "NA");
    panelFourGraph[1] = new GraphUniqueKeyDTO(4, 2, "NA");
    panelFourGraph[2] = new GraphUniqueKeyDTO(4, 3, "NA");
    panelFourGraph[3] = new GraphUniqueKeyDTO(4, 4, "NA");
    
    GraphUniqueKeyDTO []panelFiveGraph = new GraphUniqueKeyDTO[4];
  
    panelFiveGraph[0] = new GraphUniqueKeyDTO(5, 1, "NA");
    panelFiveGraph[1] = new GraphUniqueKeyDTO(5, 2, "NA");
    panelFiveGraph[2] = new GraphUniqueKeyDTO(5, 3, "NA");
    panelFiveGraph[3] = new GraphUniqueKeyDTO(5, 4, "NA");
    
    GraphUniqueKeyDTO []panelSixGraph = new GraphUniqueKeyDTO[4];
  
    panelSixGraph[0] = new GraphUniqueKeyDTO(6, 1, "NA");
    panelSixGraph[1] = new GraphUniqueKeyDTO(6, 2, "NA");
    panelSixGraph[2] = new GraphUniqueKeyDTO(6, 3, "NA");
    panelSixGraph[3] = new GraphUniqueKeyDTO(6, 4, "NA");
    
    arrPanelGraphList.add(panelOneGraph);
    arrPanelGraphList.add(panelTwoGraph);
    arrPanelGraphList.add(panelThreeGraph);
    arrPanelGraphList.add(panelFourGraph);
    arrPanelGraphList.add(panelFiveGraph);
    arrPanelGraphList.add(panelSixGraph);
  }
  
  public GraphUniqueKeyDTO[] getGraphDTOArrayByPanel(int panelNum)
  {
    return arrPanelGraphList.get(panelNum);
  }
}
