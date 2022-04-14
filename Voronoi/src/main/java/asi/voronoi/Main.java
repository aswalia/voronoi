package asi.voronoi;

import asi.voronoi.tree.AVLTree;
import asi.voronoi.javafx.DrawingBoard;
import asi.voronoi.javafx.DrawObject;
import asi.voronoi.tree.BinaryTree;
import asi.voronoi.tree.VTree;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger LOG = LogManager.getLogger(Main.class);
    private static final int FACTOR = 1;
    private static VTree v;
    private static BinaryTree t;
    private static DrawingBoard d;
    private static DrawObject dobj;
    
    private static void generateVoronoi(int noOfPoints) {
        boolean success = false;
        for (int j = 0; (j < 10) && (!success); j++) {
            t = new AVLTree(new Point((int) (Math.random() * FACTOR * noOfPoints),
            (int) (Math.random() * FACTOR * noOfPoints)));
            for (int i = 0; i < (noOfPoints - 1); i++) {
                t = t.insertNode(new Point((int) (Math.random() * FACTOR * noOfPoints),
                (int) (Math.random() * FACTOR * noOfPoints)));
            }
            try {
                int no = (int) (Math.random() * FACTOR * noOfPoints);
                //	                Serializer.store("AVL"+no,t);
                LOG.info("\n" + t.toString());
                v = new VTree();
                LOG.debug("# points: " + t.count());
                v.buildStructure(t);
                LOG.info(v.toString());
                success = true;
            } catch (Exception e) {
                LOG.error(e.getMessage());
                StackTraceElement[] st = e.getStackTrace();
                for (StackTraceElement ste : st) {
                    LOG.error(ste.getClassName() + " : " + ste.getMethodName() + " : " + ste.getLineNumber());                    
                }
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
                LOG.error(ex);
            }
        } else {
            if (Boolean.parseBoolean(argv[0])) {
                drawFromFile(argv);
            } else {
                drawRandom(argv);
            }
//            d.main(argv);
        }
    }

    public static void drawRandom(String[] argv) {
        int noOfPoints = Integer.parseInt(argv[1]);
        generateVoronoi(noOfPoints);
//        dobj = new DrawVoronoi(v);
//        d = new DrawingBoard();
//        d.setDrawObject(dobj);
    }
    
    public static void drawFromFile(String[] argv) {
        String folderName = "src/test/resources/";
        VTree c = new VTree();
        try {
            // build and write actual result files
            BinaryTree b = new AVLTree();
            b = b.buildBinaryTree(folderName+argv[1]);
            c.buildStructure(b);
            LOG.info(c.getInfo().toString());
        }
        catch (IOException ex) {
                LOG.error(ex);
        }
    }

}
