package asi.voronoi;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static int FACTOR = 50;
    private VoronoiTree v;
    
    private void generateVoronoi(int noOfPoints) {
        boolean success = false;
        for (int j = 0; (j < 10) && (!success); j++) {
            AVLTree t = new AVLTree(new Point((int) (Math.random() * FACTOR * noOfPoints),
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
            m.generateVoronoi(Integer.parseInt(argv[0]));
            try {
                ((DCEL)m.v.getInfo()).toFile();
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            if (Boolean.parseBoolean(argv[0])) {
                m.drawFromFile(argv[1]);
            } else {
                m.drawRandom(Integer.parseInt(argv[1]));
            }
        }
    }

    public void drawRandom(int noOfPoints) {
        generateVoronoi(noOfPoints);
        DrawObject da = new DrawVoronoi(v);
        DrawingBoard drawingBoard = new DrawingBoard(da);
    }
    
    public void drawFromFile(String filename) {
        String folderName = "src/test/resources/";
        VoronoiTree c = new VoronoiTree();
        try {
            // build and write actual result files
            c.buildTree(folderName+filename);
            c.buildStructure();
            DrawObject da = new DrawVoronoi(c);
            DrawingBoard drawingBoard = new DrawingBoard(da);
        }
        catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
