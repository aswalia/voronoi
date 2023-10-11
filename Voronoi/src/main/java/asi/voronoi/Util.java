package asi.voronoi;

import asi.voronoi.tree.AVLTree;
import asi.voronoi.javafx.DrawingBoard;
import asi.voronoi.javafx.DrawObject;
import asi.voronoi.tree.BinaryTree;
import asi.voronoi.tree.VTree;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {

    private static final Logger LOG = LogManager.getLogger(Util.class);
    private static final int FACTOR = 10;
    private static VTree v;
    private static BinaryTree t;
    
    public static void createDatabase(String fileName) throws SQLException {
        DatabaseHandler.dropDatabase(fileName);
        DatabaseHandler.createNewDatabase(fileName);
        DatabaseHandler.createContent();        
    }
    
    
    public static void prepareDatabaseWithRandomPoints(int noOfPoints, int group) throws SQLException {
        double x, y;
        Set<Point> sp = new HashSet<>();
        for (int i=0; i < noOfPoints; i++) {
            x = (int) (Math.random() * FACTOR * noOfPoints);
            y = (int) (Math.random() * FACTOR * noOfPoints);
            Point p = new Point(x,y);
            sp.add(p);
        }
        List<String> l = new LinkedList<>();
        int count = 1;
        for (Point p: sp) {
            String r = count + " , " + group + " , " + p.x() + " , " + p.y();
            l.add(r);
            count++;
        }
        DatabaseHandler.insertContent("points", l);
    } 
    
    public static void prepareDatabaseWithFixedPoints() throws SQLException {
        List<String> l = new LinkedList<>();
        Point p = new Point(6,2);
        String r = "1 , 1" + " , " + p.x() + " , " + p.y();
        l.add(r);
        p = new Point(0,3);
        r = "2 , 1" + " , " + p.x() + " , " + p.y();
        l.add(r);
        p = new Point(1,12);
        r = "3 , 1" + " , " + p.x() + " , " + p.y();
        l.add(r);
        p = new Point(3,13);
        r = "4 , 1" + " , " + p.x() + " , " + p.y();
        l.add(r);
        p = new Point(0,11);
        r = "5 , 1" + " , " + p.x() + " , " + p.y();
        l.add(r);
        p = new Point(6,5);
        r = "6 , 1" + " , " + p.x() + " , " + p.y();
        l.add(r);
        p = new Point(12,8);
        r = "7 , 1" + " , " + p.x() + " , " + p.y();
        l.add(r);
        DatabaseHandler.insertContent("points", l);
        
    }
    
    public static BinaryTree generateBTree(int noOfPoints) {
        BinaryTree ret = new AVLTree(new Point((int) (Math.random() * FACTOR * noOfPoints),
                (int) (Math.random() * FACTOR * noOfPoints)));
        for (int i = 0; i < (noOfPoints - 1); i++) {
            ret = ret.insertNode(new Point((int) (Math.random() * FACTOR * noOfPoints),
                    (int) (Math.random() * FACTOR * noOfPoints)));
        }
        return ret;
    }
    
    public static BinaryTree bTreeFromPointSet(String filename) throws Exception {
        PointSet ps = new PointSet();
        Set<Point> pointsFromFile = ps.buildPointSet(filename);
        BinaryTree ret = null;
        for (Point p : pointsFromFile) {
            if (ret == null) {
                ret = new AVLTree(p);
            } else {
                ret = ret.insertNode(p);
            }
        }
        return ret;
    }
    
    public static BinaryTree generateBTree(String filename) {
        BinaryTree tree = new AVLTree();
        try {
            tree = tree.buildBinaryTree(filename);
        } catch (IOException ex) {
            tree = null;
            LOG.error(ex.getMessage());
        }
        return tree;
    }
    
    public static BinaryTree generateBTree(int noOfPoints, String dbName, int group) throws SQLException {
        Util.createDatabase(dbName);
        Util.prepareDatabaseWithRandomPoints(noOfPoints, group);
        BinaryTree ret = null;
        Map<Integer,Point> points = DatabaseHandler.getPointsByGroup(group);
        Collection<Point> pointSet = points.values();
        for (Point p:pointSet) {
            if (ret == null) {
                // first point in set
                ret = new AVLTree(p);
            } else {
                // rest of the set
                ret = ret.insertNode(new Point(p));
            }
        }
        return ret;
    }

    private static void generateVoronoi(int noOfPoints) {
        boolean success = false;
        for (int j = 0; (j < 10) && (!success); j++) {
            t = generateBTree(noOfPoints);
            try {
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
        String fileName = "src/main/resources/VD.db";
        try {
            Util.createDatabase(fileName);
            Util.prepareDatabaseWithRandomPoints(1000,1);
//            Util.prepareDatabaseWithFixedPoints();
        } catch(SQLException se) {
            LOG.error("Failed to prepare database: " + se.getSQLState());
        }
        int grp = 1;
        Map<Integer,Point> mp = DatabaseHandler.getPointsByGroup(grp);
        BinaryTree ret = null;
        Set<Integer> sk = mp.keySet();
        for (Integer i : sk) {
            Point p = mp.get(i);
            if (ret == null) {
                // first point in set
                ret = new AVLTree(p);
            } else {
                // rest of the set
                ret = ret.insertNode(new Point(p));
            }
        }
        // Store BinaryTree to database
        List<String> l = new LinkedList<>();
        ret.store(grp, l);
        try {
            DatabaseHandler.insertContent("binaryTrees", l);
        } catch (SQLException ex) {
            LOG.error("Unable to build BinaryTree: " + ex.getSQLState());
        }
        // reset BinaryTree
        ret = null;
        // Build BinaryTree from database
        ret = DatabaseHandler.getBinaryTreeByGroup(grp);
        System.out.println(ret);
        v = new VTree();
        v.buildStructure(ret);
        System.out.println();
        System.out.println(v);
        ConveksHull ch = v.getInfo().vor2CH();
        System.out.println();
        System.out.println(ch);
        l.clear();
        ch.store(grp, l);
        try {
            DatabaseHandler.insertContent("conveksHulls", l);
        } catch (SQLException ex) {
            LOG.error("Unable to build ConveksHull: " + ex.getSQLState());
        }
        ch = null;
        ch = DatabaseHandler.getConveksHullByGroup(grp);
        System.out.println();
        System.out.println(ch);
        List<Properties> r = new LinkedList<>();
        try {
            ch.storeAsLinesegments(grp, r);
        } catch (SQLException ex) {
            LOG.error("Unable to build Linesegments for ConveksHulls: " + ex.getSQLState());
        }
        try {
            DatabaseHandler.updateContent("linesegments", r);
        } catch (SQLException ex) {
            LOG.error("Unable to update Linesegments for ConveksHulls: " + ex.getSQLState());
        }
        System.out.println();
        System.out.println("Store Voronoi diagram");
        long timeStart = System.currentTimeMillis();
        r.clear();
        DCELNode dcn = v.getInfo().getNode();
        try {
            dcn.storeInDatabase(grp, r);
        } catch (SQLException ex) {
            LOG.error("Unable to build Linesegments for DCELs: " + ex.getMessage());
        }
        System.out.println();
        System.out.println("Done! took: " + (System.currentTimeMillis() - timeStart) + " millisec");

        
/*
        Util m = new Util();

        if (argv.length == 1) {
            generateVoronoi(Integer.parseInt(argv[0]));
            try {
                ((DCEL) m.v.getInfo()).toFile();
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

*/
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
            b = b.buildBinaryTree(folderName + argv[1]);
            c.buildStructure(b);
            LOG.info(c.getInfo().toString());
        } catch (IOException ex) {
            LOG.error(ex);
        }
    }

}
