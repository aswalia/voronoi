/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asi.voronoi.javafx;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

/**
 *
 * @author arvin
 */
public class VoronoiMenuView { 
    private final MenuBar menuBar = new MenuBar();
    private final Menu source = new Menu("Source");
//    private final Menu func = new Menu("Function");
    private final Menu view = new Menu("View");
    private final Menu temporal = new Menu("Temporal");

    private final Main main;
    private final Stage stage;
    
    public VoronoiMenuView(Main m, Stage s) {
        main = m;
        stage = s;
        
        menuBar.getMenus().add(source);
 //       menuBar.getMenus().add(func);
        menuBar.getMenus().add(view);
        menuBar.getMenus().add(temporal);
    }
    
    public MenuBar getVoronoiMenuBar() {
        setSourceMenuItems();
//        setFunctionMenuItems();
        setViewMenuItems();
        setTemporalMenuItems();
        return menuBar;
    }   

    private void setSourceMenuItems() {
        MenuItem canvasMenuItem = new MenuItem("Canvas");
        canvasMenuItem.setOnAction(e -> main.setFromCanvas());
        source.getItems().add(canvasMenuItem);
        
        MenuItem fileMenuItem = new MenuItem("File");
        fileMenuItem.setOnAction(e -> main.setFromFile(stage));
        source.getItems().add(fileMenuItem);
        
        MenuItem dbMenuItem = new MenuItem("Database");
        dbMenuItem.setOnAction(e -> main.setFromDB());
        source.getItems().add(dbMenuItem);
    }

/*    
    private void setFunctionMenuItems() {
        MenuItem pointMenuItem = new MenuItem("Points");
        pointMenuItem.setOnAction(e -> main.doPoints());
        func.getItems().add(pointMenuItem);
        
        MenuItem bTreeMenuItem = new MenuItem("Binary Tree");
        bTreeMenuItem.setOnAction(e -> main.doBTree());
        func.getItems().add(bTreeMenuItem);
        
        MenuItem conveksHullMenuItem = new MenuItem("Conveks Hull");
        conveksHullMenuItem.setOnAction(e -> main.doConveksHull());
        func.getItems().add(conveksHullMenuItem);
        
        MenuItem voronoiMenuItem = new MenuItem("Voronoi Diagram");
        voronoiMenuItem.setOnAction(e -> main.doVoronoi());
        func.getItems().add(voronoiMenuItem);
    }
*/

    private void setViewMenuItems() {
        MenuItem representationMenuItem = new MenuItem("Representational");
        representationMenuItem.setOnAction(e -> main.drawRepresentation());
        view.getItems().add(representationMenuItem);
        
        MenuItem geometricMenuItem = new MenuItem("Geometric");
        geometricMenuItem.setOnAction(e -> main.drawGeometric());
        view.getItems().add(geometricMenuItem);
    }

    private void setTemporalMenuItems() {
        MenuItem staticMenuItem = new MenuItem("Static");
        staticMenuItem.setOnAction(e -> main.drawStatic());
        temporal.getItems().add(staticMenuItem);
        
        MenuItem animationMenuItem = new MenuItem("Animation");
        animationMenuItem.setOnAction(e -> main.drawAnimation());
        temporal.getItems().add(animationMenuItem);
    }
}
