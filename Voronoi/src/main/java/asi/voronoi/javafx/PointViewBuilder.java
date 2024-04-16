/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asi.voronoi.javafx;

import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Builder;

/**
 *
 * @author asi
 */
public class PointViewBuilder implements Builder<Region> {
    
    private final PointModel viewModel;
    private final Consumer<Runnable> pointFetcher;
    
    public PointViewBuilder(PointModel viewModel, Consumer<Runnable> pointFetcher) {
        this.viewModel = viewModel;
        this.pointFetcher = pointFetcher;
    }
    
    @Override
    public Region build() {
        BorderPane results = new BorderPane();
        results.setTop(setUpTop(pointFetcher));
        results.setCenter(setUpCentre());
        results.setBottom(setUpBottom());
        return results;
    }

    private Node setUpTop(Consumer<Runnable> fetchPoint) {
        Label label = new Label("PointSet file: ");
        TextField fileName = new TextField("Filename");
        Button button = new Button("Get Points");
        button.setOnAction(evt -> {
            button.setDisable(true);
            fetchPoint.accept(() -> {
                button.setDisable(false);
            });
        });
        return new HBox(label, fileName, button);
        
    }
    
    
    private Node setUpCentre() {
        TextArea points = new TextArea("bla bla bla");
        points.textProperty().bind(viewModel.pointsProperty());
        HBox hBox = new HBox(10, points);
        return hBox;
    }

    private Node setUpBottom() {
        TextArea message = new TextArea("bla bla bla");
        HBox hBox = new HBox(10, message);
        return hBox;
    }
}
