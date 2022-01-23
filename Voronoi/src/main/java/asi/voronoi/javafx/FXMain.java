package asi.voronoi.javafx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FXMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        String path = "/media/public/Shared Pictures/pics/";
        StringBuffer sb = new StringBuffer(path);

        primaryStage.setTitle("ImageView Experiment");

        ScrollPane scrollPane = new ScrollPane();

        Button button = new Button("Select File");
        button.setOnAction((ActionEvent e) -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File(sb.toString()));
                File selectedFile = fileChooser.showOpenDialog(primaryStage);
                sb.delete(0, sb.length());
                sb.append(selectedFile.getParent());
                FileInputStream input = new FileInputStream(selectedFile);
                Image image = new Image(input);
                ImageView imageView = new ImageView(image);
                scrollPane.setContent(imageView);
                scrollPane.pannableProperty().set(true);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FXMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        HBox hbox = new HBox(button, scrollPane);

        Scene scene = new Scene(hbox, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
