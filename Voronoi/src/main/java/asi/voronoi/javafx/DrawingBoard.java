package asi.voronoi.javafx;


import asi.voronoi.Point;
import asi.voronoi.tree.AVLTree;
import asi.voronoi.tree.VTree;
import java.awt.Graphics;
import javafx.application.Application;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DrawingBoard extends Application {
    private static final Color bg = Color.WHITE;
    private static final Color fg = Color.BLUE;
    private static final int FACTOR = 50;
    private static VTree v;
    private static AVLTree t;
    private static DrawObject dObj;

    private final boolean isGeometry = true;
    private Image myOffScreenImage;
    private Graphics myOffScreenGraphics;
//    JPanel p;
//    JScrollBar horizontalMove, horizontalZoom;
    int zoomIn = 0;
    private static void generateVoronoi(int noOfPoints) {
        boolean success = false;
        for (int j = 0; (j < 10) && (!success); j++) {
            t = new AVLTree(new Point((int) (Math.random() * FACTOR * noOfPoints),
            (int) (Math.random() * FACTOR * noOfPoints)));
            for (int i = 0; i < (noOfPoints - 1); i++) {
                t = (AVLTree) t.insertNode(new Point((int) (Math.random() * FACTOR * noOfPoints),
                (int) (Math.random() * FACTOR * noOfPoints)));
            }
            try {
                int no = (int) (Math.random() * FACTOR * noOfPoints);
                //	                Serializer.store("AVL"+no,t);
                v = new VTree();
                System.out.println("# points: " + t.count());
                v.buildStructure(t);
                success = true;
            } catch (Exception e) {
                System.out.println("Failed: " + e);
            }  
        }
        
    }
    

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
    public void start(Stage stage) throws Exception {
//        p = new JPanel();
        stage.setTitle("DrawingBoard");
//        stage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
/*        getContentPane().add("Center", p);
        horizontalMove = new JScrollBar(JScrollBar.HORIZONTAL, 0, 10, -200, 200);
        JScrollBar verticalMove = new JScrollBar(JScrollBar.VERTICAL, 0, 10, -200, 200);
        getContentPane().add("South", horizontalMove);
        getContentPane().add("East", verticalMove);
        horizontalZoom = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, -100, 100);
        getContentPane().add("North", horizontalZoom);
        horizontalMove.addAdjustmentListener(new ScrollEvent());
        verticalMove.addAdjustmentListener(new ScrollEvent());
        horizontalZoom.addAdjustmentListener(new ScrollEvent());
        setBackground(bg);
        setForeground(fg);
        pack();
        setSize(new Dimension(700, 700));
*/

        Group root = new Group();
//        Canvas canvas = new Canvas(300, 250);
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//        drawShapes(gc);




//        ScrollPane scrollPane = new ScrollPane();

        
        Canvas c = new Canvas(700, 500);
        
        System.out.println("Ready to draw");

        
        if (dObj != null) {
            System.out.println("Got into drawing");
            Bounds b = new BoundingBox(0, 0, 500, 700);
            GraphicData gd = new GraphicData(c.getGraphicsContext2D(), b);
            if (isGeometry) {
                stage.setTitle("DrawingBoard - Geometric View - " + dObj.objectTitle());
                dObj.drawGeometry(gd);
            } else {
                stage.setTitle("DrawingBoard - Object Representation View - " + dObj.objectTitle());
                dObj.drawRepresentation(gd);
            }
        }

        root.getChildren().add(c);
//        scrollPane.setContent(c);

//        VBox vbox = new VBox(scrollPane);
//        Scene scene = new Scene(vbox, 700, 500);
        Scene scene = new Scene(root);

        stage.setScene(scene);
        



        
        
        stage.show();
    }
    
    public static  void main(String[] argv) {
        int noOfPoints = Integer.parseInt(argv[1]);
        generateVoronoi(noOfPoints);
        dObj = new DrawVoronoi(v);
        Application.launch(argv);
    }

/*    private class ScrollEvent implements AdjustmentListener {
        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            double s, tx = 0, ty = 0;
            if (e.getAdjustable().getOrientation() == Adjustable.HORIZONTAL) {
                // one of the horirontal scrollbars
                if (e.getSource().equals(horizontalZoom)) {
                    // the top scrollbar
//                    if (e.getValue() > zoomIn) {
//                        // scrolling to the right
//                        s = 1;
//                    } else {
//                        s = -1;
//                    }
                    s = e.getValue();
                    dObj.setScale(s);
                } else {
                    // the bottom scrollbar
                    tx = e.getValue();
                    dObj.setTranslation(tx, ty);
                }
            } else {
                // the vertical scrollbar
                ty = e.getValue();
                dObj.setTranslation(tx, ty);
            }
            repaint();
        }
    }

    public DrawingBoard(DrawObject dObj) {
        this.dObj = dObj;
    }

    public void setGeometry(boolean geo) {
        isGeometry = geo;
    }

    public boolean getGeometry() {
        return isGeometry;
    }

    @Override
    public void update(Graphics g) {
        myOffScreenImage = createImage(getWidth(), getHeight());
        myOffScreenGraphics = myOffScreenImage.getGraphics();
        myOffScreenGraphics.clearRect(0, 0, getWidth(), getHeight());
        paint(myOffScreenGraphics);
        g.drawImage(myOffScreenImage, 0, 0, p);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (dObj != null) {
            GraphicsContext g2 = g.c.getGraphicsContext2D();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Dimension d = getSize();
            GraphicData gd = new GraphicData(g2, d);
            if (isGeometry) {
                setTitle("DrawingBoard - Geometric View - " + dObj.objectTitle());
                dObj.drawGeometry(gd);
            } else {
                setTitle("DrawingBoard - Object Representation View - " + dObj.objectTitle());
                dObj.drawRepresentation(gd);
            }
        }
    }

*/
}