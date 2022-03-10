package asi.voronoi;

import asi.voronoi.tree.AVLTree;
import asi.voronoi.tree.VoronoiTree;
import asi.voronoi.javafx.DrawingBoard;
import asi.voronoi.javafx.DrawObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final int FACTOR = 50;
    private static VoronoiTree v;
    private static AVLTree t;
    private static DrawingBoard d;
    private static DrawObject dobj;
    
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
                v = new VoronoiTree();
                v.buildTree(t);
                System.out.println("# points: " + t.count());
                v.buildStructure();
                success = true;
            } catch (Exception e) {
                System.out.println("Failed: " + e);
            }  
        }
        
    }

    public static void main(String argv[]) {
        Main m = new Main();
        
        if (argv.length == 1) {
            generateVoronoi(Integer.parseInt(argv[0]));
            try {
                ((DCEL)m.v.getInfo()).toFile();
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            if (Boolean.parseBoolean(argv[0])) {
                drawFromFile(argv);
            } else {
                drawRandom(argv);
            }
            d.main(argv);
        }
    }

    public static void drawRandom(String[] argv) {
/*        int noOfPoints = Integer.parseInt(argv[1]);
        generateVoronoi(noOfPoints);
        dobj = new DrawVoronoi(v);
        d = new DrawingBoard();
        d.setDrawObject(dobj);
*/
    }
    
    public static void drawFromFile(String[] argv) {
/*        String folderName = "src/test/resources/";
        VoronoiTree c = new VoronoiTree();
        try {
            // build and write actual result files
            c.buildTree(folderName+argv[1]);
            c.buildStructure();
            dobj = new DrawVoronoi(c);
            DrawingBoard d = new DrawingBoard();
            d.setDrawObject(dobj);
        }
        catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
*/
    }

}
