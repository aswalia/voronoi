/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asi.voronoi.javafx;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author asi
 */
public class PointModel {

    private final StringProperty pointSet = new SimpleStringProperty("");
    
    public String getPoints() {
        return pointSet.get();
    }

    public StringProperty pointsProperty() {
        return pointSet;
    }

    public void setPoints(String points) {
        this.pointSet.set(points);
    }

}
