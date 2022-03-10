package asi.voronoi.javafx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FXMain extends Application {

/*    @Override
    public void start(Stage primaryStage) throws Exception {
        String path = "src/main/resources/media/pics/";
        StringBuilder sb = new StringBuilder(path);

        primaryStage.setTitle("ImageView Experiment");

        ScrollPane scrollPane = new ScrollPane();

        Button button = new Button("Select Picture");
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
        
        HBox hbox = new HBox(button);
        hbox.setAlignment(Pos.CENTER);
        
        Separator sep = new Separator();

        VBox vbox = new VBox(scrollPane, sep, hbox);

        Scene scene = new Scene(vbox, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

    }
*/
    
    @Override
    public void start(Stage stage) {
    //Creating a Path 
      Path path = new Path(); 
       
      //Moving to the starting point 
      MoveTo moveTo = new MoveTo(108, 71); 
        
      //Creating 1st line 
      LineTo line1 = new LineTo(321, 161);  
       
      //Creating 2nd line 
      LineTo line2 = new LineTo(126,232);       
       
      //Creating 3rd line 
      LineTo line3 = new LineTo(232,52);  
       
      //Creating 4th line 
      LineTo line4 = new LineTo(269, 250);   
       
      //Creating 4th line 
      LineTo line5 = new LineTo(108, 71);  
       
      //Adding all the elements to the path 
      path.getElements().add(moveTo); 
      path.getElements().addAll(line1, line2, line3, line4, line5);        
         
      //Creating a Group object  
      Group root = new Group(path); 
         
      //Creating a scene object 
      Scene scene = new Scene(root, 700, 500);  
      
      //Setting title to the Stage 
      stage.setTitle("Drawing an arc through a path");
      
      //Adding scene to the stage 
      stage.setScene(scene);
      
      //Displaying the contents of the stage 
      stage.show();         
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
