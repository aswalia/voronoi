/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asi.voronoi.javafx;

import asi.voronoi.PointSet;

/**
 *
 * @author asi
 */
public class PointInteractor {
    
    private PointModel viewModel;
    private PointData pointData;
    private PointSet pointSet;
    
    public PointInteractor(PointModel viewModel) {
        this.viewModel = viewModel;
    }
    
    public void getPointData() {
        pointData = null;
    }

}
