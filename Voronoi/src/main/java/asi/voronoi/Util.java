package asi.voronoi;

import asi.voronoi.tree.AVLTree;
import asi.voronoi.tree.BinaryTree;
import asi.voronoi.tree.VTree;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
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
        DatabaseHandler.connectToDatabase(fileName);
        DatabaseHandler.createContent();
    }

    public static void prepareDatabaseWithRandomPoints(int noOfPoints, int group) throws SQLException {
        double x, y;
        Map<Integer, Point> sp = new HashMap<>();
        for (int i = 0; i < noOfPoints; i++) {
            x = (int) (Math.random() * FACTOR * noOfPoints);
            y = (int) (Math.random() * FACTOR * noOfPoints);
            Point p = new Point(x, y);
            sp.put(i,p);
        }
        PointSet.store(group, sp);
    }

    public static void prepareDatabaseWithFixedPoints() throws SQLException {
        List<String> l = new LinkedList<>();
        Point p = new Point(6, 2);
        String r = "1 , 1" + " , " + p.x() + " , " + p.y();
        l.add(r);
        p = new Point(0, 3);
        r = "2 , 1" + " , " + p.x() + " , " + p.y();
        l.add(r);
        p = new Point(1, 12);
        r = "3 , 1" + " , " + p.x() + " , " + p.y();
        l.add(r);
        p = new Point(3, 13);
        r = "4 , 1" + " , " + p.x() + " , " + p.y();
        l.add(r);
        p = new Point(0, 11);
        r = "5 , 1" + " , " + p.x() + " , " + p.y();
        l.add(r);
        p = new Point(6, 5);
        r = "6 , 1" + " , " + p.x() + " , " + p.y();
        l.add(r);
        p = new Point(12, 8);
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

    public static BinaryTree bTreeFromPointSet(File file) throws Exception {
        PointSet ps = new PointSet();
        Map<Integer, Point> pointsFromFile = ps.buildPointMap(file);
        BinaryTree ret = null;
        Set<Integer> points = pointsFromFile.keySet();
        for (Integer i:points) {
            if (ret == null) {
                ret = new AVLTree(pointsFromFile.get(i));
            } else {
                ret = ret.insertNode(pointsFromFile.get(i));
            }
        }
        return ret;
    }

    public static Map<Integer, Point> getPoints(File file) throws Exception {
        PointSet ps = new PointSet();
//        Set<Point> pointsFromFile = ps.buildPointSet(file);
        return ps.buildPointMap(file);
    }

    public static BinaryTree generateBTree(File file) {
        BinaryTree tree = new AVLTree();
        try {
            tree = tree.buildBinaryTree(file);
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
        Map<Integer, Point> points = DatabaseHandler.getPointsByGroup(group);
        Collection<Point> pointSet = points.values();
        for (Point p : pointSet) {
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
//                v = new VTree();
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
        String dbFileName = "src/main/resources/VD.db";
        String psFileName = "src/test/resources/pointset_01.test";
        int grp = 1;
        Map<Integer, Point> pointsFromFile = null;
        try {
            DatabaseHandler.connectToDatabase(dbFileName);
            PointSet ps = new PointSet();
            pointsFromFile = ps.buildPointMap(new File(psFileName));
            PointSet.store(grp, pointsFromFile);
        } catch (Exception ex) {
            LOG.error("Unable to store Points in Database: " + ex.getMessage());
            System.exit(-1);
        }
        Set<Integer> sp = pointsFromFile.keySet();
        for (Integer p : sp) {
            if (t == null) {
                // first point in set
                t = new AVLTree(pointsFromFile.get(p));
            } else {
                // rest of the set
                t = t.insertNode(pointsFromFile.get(p));
            }
        }
        // Store BinaryTree to database
        List<String> l = new LinkedList<>();
        t.store(grp, l);
        try {
            DatabaseHandler.insertContent("binaryTrees", l);
        } catch (SQLException ex) {
            LOG.error("Unable to build BinaryTree: " + ex.getSQLState());
        }
//        v = new VTree();
//        v.buildStructure(t);
        LOG.info(v);
        ConveksHull ch = v.getInfo().vor2CH();
        LOG.info(ch);
        l.clear();
        ch.store(grp, l);
        try {
            DatabaseHandler.insertContent("conveksHulls", l);
        } catch (SQLException ex) {
            LOG.error("Unable to build ConveksHull: " + ex.getSQLState());
        }
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
        r.clear();
        DCELNode dcn = v.getInfo().getNode();
        try {
            dcn.storeInDatabase(grp, r);
        } catch (SQLException ex) {
            LOG.error("Unable to build Linesegments for DCELs: " + ex.getMessage());
        }
    }

    public static void drawRandom(String[] argv) {
        int noOfPoints = Integer.parseInt(argv[1]);
        generateVoronoi(noOfPoints);
    }

}
