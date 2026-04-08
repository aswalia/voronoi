/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asi.voronoi.javafx;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

/**
 *
 * @author arvin
 */
public class MenuView { 
    private final MenuBar menuBar = new MenuBar();
    private final Menu source = new Menu("Source");
    private final Menu view = new Menu("View");

    private final Main main;
    private final Stage stage;
    
    private final BooleanBinding dataSetEmpty;
    
    public MenuView(Main m, Stage s) {
        main = m;
        stage = s;
        
        dataSetEmpty = Bindings.lessThan(Main.dataSetSize, 1);
        
        menuBar.getMenus().add(source);
        source.disableProperty().bind(Bindings.and(Main.noDbGroup, 
                                                   Main.notPointType));
        menuBar.getMenus().add(view);
        view.disableProperty().bind(dataSetEmpty);
    }
    
    public MenuBar getMenuBar() {
        setSourceMenuItems();
        setViewMenuItems();
        return menuBar;
    }   

    private void setSourceMenuItems() {
        MenuItem canvasMenuItem = new MenuItem("Canvas");
        canvasMenuItem.setOnAction(e -> main.setFromCanvas());
        source.getItems().add(canvasMenuItem);
        canvasMenuItem.disableProperty().bind(Main.notPointType);
        
        MenuItem fileMenuItem = new MenuItem("File");
        fileMenuItem.setOnAction(e -> main.setFromFile(stage));
        source.getItems().add(fileMenuItem);
        fileMenuItem.disableProperty().bind(Main.notPointType);
        
        MenuItem dbMenuItem = new MenuItem("Database");
        dbMenuItem.setOnAction(e -> main.setFromDB());
        source.getItems().add(dbMenuItem);
        dbMenuItem.disableProperty().bind(Main.noDbGroup);
    }

    private void setViewMenuItems() {
        MenuItem representationMenuItem = new MenuItem("Representational");
        representationMenuItem.setOnAction(e -> main.drawRepresentation());
        view.getItems().add(representationMenuItem);
        
        Menu geometricMenuItem = new Menu("Geometric");
        view.getItems().add(geometricMenuItem);
        
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioMenuItem picMenuItem = new RadioMenuItem("Picture");
        picMenuItem.setOnAction(e1 -> main.drawStatic());
        picMenuItem.setToggleGroup(toggleGroup);
        RadioMenuItem animMenuItem = new RadioMenuItem("Animation");
        animMenuItem.setOnAction(e2 -> main.drawAnimation());
        animMenuItem.setToggleGroup(toggleGroup);
        geometricMenuItem.getItems().addAll(picMenuItem, animMenuItem);
    }
}
