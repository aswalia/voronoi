package asi.voronoi;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String argv[]) {
        Main m = new Main();
        if (Boolean.parseBoolean(argv[0])) {
            m.drawFromFile(argv[1]);
        } else {
            m.drawRandom(Integer.parseInt(argv[1]));
        }
    }

    public void drawRandom(int noOfPoints) {
        boolean success = false;
        for (int j = 0; (j < 10) && (!success); j++) {
            AVLTree t = new AVLTree(new Point((int) (Math.random() * 5 * noOfPoints),
            (int) (Math.random() * 5 * noOfPoints)));
            for (int i = 0; i < (noOfPoints - 1); i++) {
                t = (AVLTree) t.insertNode(new Point((int) (Math.random() * 5 * noOfPoints),
                (int) (Math.random() * 5 * noOfPoints)));
            }
            try {
                int no = (int) (Math.random() * 5 * noOfPoints);
                //	                Serializer.store("AVL"+no,t);
                VoronoiTree v = new VoronoiTree();
                v.buildTree(t);
                System.out.println("# points: " + t.count());
                v.buildStructure();
                success = true;
                DrawObject da = new DrawVoronoi(v);
                DrawingBoard drawingBoard = new DrawingBoard(da);
            } catch (Exception e) {
                System.out.println("Failed: " + e);
            }  
        }
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
