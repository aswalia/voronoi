package asi.voronoi.javafx;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class FXMain extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {

    Pane pane = new Pane();

    ReadOnlyDoubleProperty widthProperty = pane.widthProperty();
    widthProperty.addListener((ObservableValue<? extends Number> observableValue, Number oldVal, Number newVal) -> {
        System.out.println("widthProperty changed from "
                + oldVal.doubleValue() + " to " + newVal.doubleValue());
    });


    DoubleProperty prefWidthProperty = pane.prefWidthProperty();
    prefWidthProperty.addListener(
      (ObservableValue<? extends Number> prop,
        Number oldVal, Number newVal) -> {

        System.out.println("prefWidthProperty changed from "
          + oldVal.doubleValue() + " to " + newVal.doubleValue());
    });

    prefWidthProperty.set(123);

    Scene scene = new Scene(pane, 1024, 800, true);
    primaryStage.setScene(scene);
    primaryStage.setTitle("2D Example");

    primaryStage.show();
  }
}