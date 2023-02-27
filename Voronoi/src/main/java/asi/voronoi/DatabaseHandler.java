package asi.voronoi;

import asi.voronoi.tree.BinaryTree;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author asi
 */
public class DatabaseHandler {

    private static String url;
    
    public static boolean dropDatabase(String fileName) {
        // delete database file - that is drop database in SQLite
        File myObj = new File(fileName); 
        return myObj.delete();
    }

    public static void createNewDatabase(String fileName) {

        // SQLite connection string
        url = "jdbc:sqlite:" + fileName;

        try ( Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void createNew(String sql) throws SQLException {
        try ( Connection conn = DriverManager.getConnection(url);  
              Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public static void createContent() throws SQLException {
        String sql;

        sql = "CREATE TABLE IF NOT EXISTS points (\n"
                + " id INTEGER,\n"
                + " grp INTEGER,\n"
                + " x REAL NOT NULL,\n"
                + " y REAL NOT NULL,\n"
                + " PRIMARY KEY (id, grp)\n"
                + ");";
        createNew(sql);

        sql = "CREATE TABLE IF NOT EXISTS linesegments (\n"
                + " id INTEGER,\n"
                + " grp INTEGER,\n"
                + " beginpoint INTEGER,\n"
                + " endpoint INTEGER,\n"
                + " midpoint INTEGER,\n"
                + " direction INTEGER,\n"
                + " PRIMARY KEY (id, grp),\n"
                + " FOREIGN KEY(grp) REFERENCES points(grp),\n"
                + " FOREIGN KEY(beginpoint) REFERENCES points(id),\n"
                + " FOREIGN KEY(endpoint) REFERENCES points(id),\n"
                + " FOREIGN KEY(midpoint) REFERENCES points(id),\n"
                + " FOREIGN KEY(direction) REFERENCES points(id)\n"
                + ");";
        createNew(sql);

        sql = "CREATE TABLE IF NOT EXISTS binaryTrees (\n"
                + " point INTEGER,\n"
                + " grp INTEGER,\n"
                + " left INTEGER,\n"
                + " right INTEGER,\n"
                + " PRIMARY KEY (point, grp),\n"
                + " FOREIGN KEY(point) REFERENCES points(id),\n"
                + " FOREIGN KEY(grp) REFERENCES points(grp),\n"
                + " FOREIGN KEY(left) REFERENCES points(id),\n"
                + " FOREIGN KEY(right) REFERENCES points(id)\n"
                + ");";
        createNew(sql);

        sql = "CREATE TABLE IF NOT EXISTS conveksHulls (\n"
                + " point INTEGER,\n"
                + " grp INTEGER,\n"
                + " next INTEGER,\n"
                + " previous INTEGER,\n"
                + " PRIMARY KEY (point, grp),\n"
                + " FOREIGN KEY(point) REFERENCES points(id),\n"
                + " FOREIGN KEY(grp) REFERENCES points(grp),\n"
                + " FOREIGN KEY(next) REFERENCES points(id),\n"
                + " FOREIGN KEY(previous) REFERENCES points(id)\n"
                + ");";
        createNew(sql);

        sql = "CREATE TABLE IF NOT EXISTS conveksHullsAsLinesegments (\n"
                + " linesegment INTEGER,\n"
                + " grp INTEGER,\n"
                + " PRIMARY KEY (linesegment, grp),\n"
                + " FOREIGN KEY(linesegment) REFERENCES linesegments(id),\n"
                + " FOREIGN KEY(grp) REFERENCES linesegments(grp)\n"
                + ");";
        createNew(sql);

        sql = "CREATE TABLE IF NOT EXISTS dcels (\n"
                + " edge INTEGER,\n"
                + " grp INTEGER,\n"
                + " f_l INTEGER NOT NULL,\n"
                + " f_r INTEGER NOT NULL,\n"
                + " PRIMARY KEY (edge, grp),\n"
                + " FOREIGN KEY(grp) REFERENCES points(grp),\n"
                + " FOREIGN KEY(f_l) REFERENCES points(id),\n"
                + " FOREIGN KEY(f_r) REFERENCES points(id),\n"
                + " FOREIGN KEY(edge) REFERENCES linesegments(id)\n"
                + ");";
        createNew(sql);

        sql = "CREATE INDEX idx_points_grp\n"
                + "ON points(grp)\n";
        createNew(sql);

        sql = "CREATE INDEX idx_linesegments_grp\n"
                + "ON linesegments(grp)\n";
        createNew(sql);

        sql = "CREATE INDEX idx_binaryTrees_grp\n"
                + "ON binaryTrees(grp)\n";
        createNew(sql);

        sql = "CREATE INDEX idx_conveksHulls_grp\n"
                + "ON conveksHulls(grp)\n";
        createNew(sql);

        sql = "CREATE INDEX idx_conveksHullsAsLinesegments_grp\n"
                + "ON conveksHullsAsLinesegments(grp)\n";
        createNew(sql);

        sql = "CREATE INDEX idx_dcels_grp\n"
                + "ON dcels(grp)\n";
        createNew(sql);
    }

    
    public static void insertContent(String table, List<String> rows) throws SQLException {
        String sql;

        try ( Connection conn = DriverManager.getConnection(url);  
              Statement stmt = conn.createStatement()) {
            for (String r : rows) {
                sql = "INSERT INTO " + table + " VALUES (" + r + ")";
                stmt.execute(sql);
            }
        }
    }
    
    private static String clause(String clauseType, String separator, Properties p) {
        String ret = "";
        List<String> lc = new LinkedList<>();
        List<String> lv = new LinkedList<>();
        Set<String> s = p.stringPropertyNames();
        for (String i:s) {
            if (i.startsWith(clauseType)) {
                lc.add(i.substring(clauseType.length()));
                lv.add(p.getProperty(i));
            }
        }
        for (int i=0; i<lc.size()-1; i++) {
            ret += lc.get(i) + "=" + lv.get(i) + separator;
        }
        ret += lc.get(lc.size()-1) + "=" + lv.get(lc.size()-1);
        return ret;
        
    }
    
    private static String setClause(Properties p) {
        return clause("COLUMN:", ", ", p);
    }
    
    private static String whereClause(Properties p) {
        return clause("PRIMARY:", " AND ", p);

    }

    public static void updateContent(String table, List<Properties> rows) throws SQLException {
        String sql;
        try ( Connection conn = DriverManager.getConnection(url);  
              Statement stmt = conn.createStatement()) {
            String setClause, whereClause;
            for (Properties prop:rows) {                
                setClause = setClause(prop);
                whereClause = whereClause(prop);
                sql = "UPDATE " + table + 
                      " SET " + setClause + 
                      " WHERE " + whereClause + ";";
                stmt.execute(sql);
            }
        }
    }

    public static Map<Integer,Point> getPointsByGroup(int grp) {
        String sql = "SELECT id, x, y FROM points WHERE grp = ?";

        try ( Connection conn = DriverManager.getConnection(url);  
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the value
            pstmt.setInt(1, grp);
            ResultSet rs = pstmt.executeQuery();
            Map<Integer,Point> mp = new HashMap<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                Point p = new Point(x, y);
                mp.put(id, p);
            }
            return mp;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    
    public static int getIndexFromPoint(Point p, int grp) {
        String sql = "SELECT id FROM points\n"
                   + "WHERE ((x = ?) AND (y = ?)) AND (grp = ?)";

        try ( Connection conn = DriverManager.getConnection(url);  
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the value
            pstmt.setDouble(1, p.x());
            pstmt.setDouble(2, p.y());
            pstmt.setInt(3, grp);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt("id");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }
    
    public static int getIndexOfBinarytTreeRoot(int grp) {
        String sql = "SELECT point FROM binaryTrees "
                   + "WHERE grp = ? AND point not in (\n" 
                   + "  SELECT left FROM binaryTrees WHERE (left is not null)\n" 
                   + "  UNION\n" 
                   + "  SELECT right FROM binaryTrees WHERE (right is not null)\n"
                   + ")";
        
        try ( Connection conn = DriverManager.getConnection(url);  
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the value
            pstmt.setInt(1, grp);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt("point");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }        
    }
    
    private static class BinaryTreeNode {
        Point p;
        int lft, rgt;

        BinaryTreeNode(Point p, int lft, int rgt) {
            this.p = p;
            this.lft = lft;
            this.rgt = rgt;
        }
    }
    
    public static List<Integer> getIndexFromBinaryTree(int grp) {
        List<Integer> li = new LinkedList<>();
        String sql = "SELECT point \n"
                   + "FROM binaryTrees \n"
                   + "WHERE grp = ?";
        
        try ( Connection conn = DriverManager.getConnection(url);  
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the value
            pstmt.setInt(1, grp);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                var id = rs.getInt("binaryTrees.point");
                li.add(id);
            }
            return li;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }        
    }
    
    public static BinaryTree getBinaryTreeByGroup(int grp) {
        Map<Integer,BinaryTreeNode> m = new HashMap<>();
        Map<Integer,BinaryTree> mbt = new HashMap<>();
        String sql = "SELECT binaryTrees.point, binaryTrees.left, binaryTrees.right, points.x, points.y \n"
                   + "FROM points, binaryTrees \n"
                   + "WHERE (\n"
                   + "       (binaryTrees.grp = ?) AND \n"
                   + "       (points.id = binaryTrees.point) AND \n"
                   + "       (points.grp = binaryTrees.grp))"; 
        
        try ( Connection conn = DriverManager.getConnection(url);  
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the value
            pstmt.setInt(1, grp);
            ResultSet rs = pstmt.executeQuery();
            BinaryTreeNode btn;
            while(rs.next()) {
                int id = rs.getInt("point");
                int lft = rs.getInt("left");
                int rgt = rs.getInt("right");
                Point p = new Point(rs.getDouble("x"), 
                                    rs.getDouble("y"));
                btn = new BinaryTreeNode(p, lft, rgt); 
                m.put(id, btn);
            }
            int root = DatabaseHandler.getIndexOfBinarytTreeRoot(grp);
            BinaryTree bt;
            Set<Integer> mi = m.keySet();
            for(Integer i : mi) {
                btn = m.get(i);
                bt = new BinaryTree(btn.p);
                mbt.put(i, bt);
            }    
            mi = mbt.keySet();
            for(Integer i : mi) {
                bt = mbt.get(i);
                btn = m.get(i);
                bt.setLft(mbt.get(btn.lft));
                bt.setRgt(mbt.get(btn.rgt));
            }
            bt = mbt.get(root);
            return bt;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }        
    }

    private static class ConveksHullNode {
        Point p;
        int nxt, prv;

        ConveksHullNode(Point p, int prv, int nxt) {
            this.p = p;
            this.nxt = nxt;
            this.prv = prv;
        }
    }
    
    public static ConveksHull getConveksHullByGroup(int grp) {
        Map<Integer,ConveksHullNode> m = new HashMap<>();
        Map<Integer,ConveksHull> mch = new HashMap<>();
        String sql = "SELECT conveksHulls.point, conveksHulls.next, conveksHulls.previous, points.x, points.y \n"
                   + "FROM points, conveksHulls\n"
                   + "WHERE ((points.id = conveksHulls.point) AND (points.grp = conveksHulls.grp) \n"
                   + "      AND (conveksHulls.grp = ?))\n"
                   + "ORDER BY points.x, points.y ASC";
        try ( Connection conn = DriverManager.getConnection(url);  
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the value
            pstmt.setInt(1, grp);
            ResultSet rs = pstmt.executeQuery();
            ConveksHullNode chn;
            int first = -1;
            while(rs.next()) {                
                int id = rs.getInt("point");
                if (first < 0) {
                    first = id;
                }
                int prev = rs.getInt("previous");
                int next = rs.getInt("next");
                Point p = new Point(rs.getDouble("x"), 
                                    rs.getDouble("y"));
                chn = new ConveksHullNode(p, prev, next); 
                m.put(id, chn);
            }
            ConveksHull ch = null;
            ConveksHull head = null;
            Set<Integer> mi = m.keySet();
            for(Integer i : mi) {
                chn = m.get(i);
                ch = new ConveksHull(chn.p);
                mch.put(i, ch);
            }    
            mi = mch.keySet();
            for(Integer i : mi) {
                ch = mch.get(i);
                if (first == i) {
                    head = ch;
                }
                chn = m.get(i);
                ch.connect(mch.get(chn.prv),ch);
            }
            return head;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }        
    }
    
    public static int getLargestPointId(int grp) throws SQLException {
        String sql = "SELECT id FROM points\n"
                   + "WHERE (grp = ?)\n"
                   + "ORDER BY points.id DESC";
        int ni = -1;
        try ( Connection conn = DriverManager.getConnection(url);  
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the value
            pstmt.setInt(1, grp);
            ResultSet rs = pstmt.executeQuery();
            ni = rs.getInt("id") + 1;
        }
        return ni;        
    }
    
    private static int insertLinesegment(int grp) throws SQLException {
        String sql = "SELECT id FROM linesegments\n"
                   + "WHERE (grp = ?)\n"
                   + "ORDER BY linesegments.id DESC";
        int ni = -1;
        try ( Connection conn = DriverManager.getConnection(url);  
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the value
            pstmt.setInt(1, grp);
            ResultSet rs = pstmt.executeQuery();
            ni = rs.getInt("id") + 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return ni;
        }        
        List<String> lni = new LinkedList<>();
        lni.add("" + ni + ", " + grp + ", null, null, null, null");
        DatabaseHandler.insertContent("linesegments", lni);
        return ni;
    }
    
    public static int insertConveksHullLinesegment(int grp) throws SQLException {
        int ni = insertLinesegment(grp);
        List<String> lni = new LinkedList<>();
        lni.add("" + ni + ", " + grp);
        DatabaseHandler.insertContent("conveksHullsAsLinesegments", lni);
        return ni;            
    }

    public static int insertDCELs(int grp, String cols) throws SQLException {
        // cols must contain the balance of columns to make a row for dcels
        int ni = insertLinesegment(grp);
        List<String> lni = new LinkedList<>();
        lni.add("" + ni + ", " + grp + ", " + cols);
        DatabaseHandler.insertContent("dcels", lni);
        return ni;            
    }
}
