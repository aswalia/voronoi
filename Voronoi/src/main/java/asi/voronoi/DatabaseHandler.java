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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author asi
 */
public class DatabaseHandler {
    
    private static String url;
    private static final Logger LOG = LogManager.getLogger(DatabaseHandler.class);
    
    public static boolean dropDatabase(String fileName) {
        // delete database file - that is drop database in SQLite
        File myObj = new File(fileName); 
        return myObj.delete();
    }

    public static void connectToDatabase(String fileName) {

        // SQLite connection string
        url = "jdbc:sqlite:" + fileName;

        try ( Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                LOG.info("The driver name is " + meta.getDriverName());
            }

        } catch (SQLException e) {
            LOG.error(e.getMessage());
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

        sql = """
              CREATE TABLE IF NOT EXISTS points (
               id INTEGER,
               grp INTEGER,
               x REAL NOT NULL,
               y REAL NOT NULL,
               PRIMARY KEY (id, grp)
              );""";
        createNew(sql);

        sql = """
              CREATE TABLE IF NOT EXISTS linesegments (
               id INTEGER,
               grp INTEGER,
               beginpoint INTEGER,
               endpoint INTEGER,
               midpoint INTEGER,
               direction INTEGER,
               PRIMARY KEY (id, grp),
               FOREIGN KEY(beginpoint, grp) REFERENCES points(id, grp),
               FOREIGN KEY(endpoint, grp) REFERENCES points(id, grp),
               FOREIGN KEY(midpoint, grp) REFERENCES points(id, grp),
               FOREIGN KEY(direction, grp) REFERENCES points(id, grp)
              );""";
        createNew(sql);

        sql = """
              CREATE TABLE IF NOT EXISTS binaryTrees (
               point INTEGER,
               grp INTEGER,
               left INTEGER,
               right INTEGER,
               PRIMARY KEY (point, grp),
               FOREIGN KEY(point, grp) REFERENCES points(id, grp),
               FOREIGN KEY(left, grp) REFERENCES points(id, grp),
               FOREIGN KEY(right, grp) REFERENCES points(id, grp)
              );""";
        createNew(sql);

        sql = """
              CREATE TABLE IF NOT EXISTS conveksHulls (
               point INTEGER,
               grp INTEGER,
               next INTEGER,
               previous INTEGER,
               PRIMARY KEY (point, grp),
               FOREIGN KEY(point, grp) REFERENCES points(id, grp),
               FOREIGN KEY(next, grp) REFERENCES points(id, grp),
               FOREIGN KEY(previous, grp) REFERENCES points(id, grp)
              );""";
        createNew(sql);

        sql = """
              CREATE TABLE IF NOT EXISTS conveksHullsAsLinesegments (
               linesegment INTEGER,
               grp INTEGER,
               PRIMARY KEY (linesegment, grp),
               FOREIGN KEY(linesegment, grp) REFERENCES linesegments(id, grp)
              );""";
        createNew(sql);

        sql = """
              CREATE TABLE IF NOT EXISTS dcels (
               edge INTEGER,
               grp INTEGER,
               f_l INTEGER NOT NULL,
               f_r INTEGER NOT NULL,
               bp_ref INTEGER,
               ep_ref INTEGER,
               PRIMARY KEY (edge, grp),
               FOREIGN KEY(f_l, grp) REFERENCES points(id, grp),
               FOREIGN KEY(f_r, grp) REFERENCES points(id, grp),
               FOREIGN KEY(edge, grp) REFERENCES linesegments(id, grp),
               FOREIGN KEY(bp_ref, grp) REFERENCES linesegments(id, grp),
               FOREIGN KEY(ep_ref, grp) REFERENCES linesegments(id, grp)
              );""";
        createNew(sql);

        sql = """
              CREATE INDEX idx_points_grp
              ON points(grp)
              """;
        createNew(sql);

        sql = """
              CREATE INDEX idx_linesegments_grp
              ON linesegments(grp)
              """;
        createNew(sql);

        sql = """
              CREATE INDEX idx_binaryTrees_grp
              ON binaryTrees(grp)
              """;
        createNew(sql);

        sql = """
              CREATE INDEX idx_conveksHulls_grp
              ON conveksHulls(grp)
              """;
        createNew(sql);

        sql = """
              CREATE INDEX idx_conveksHullsAsLinesegments_grp
              ON conveksHullsAsLinesegments(grp)
              """;
        createNew(sql);

        sql = """
              CREATE INDEX idx_dcels_grp
              ON dcels(grp)
              """;
        createNew(sql);
    }
    
    public static void insertContent(String table, List<String> rows) throws SQLException {
        String sql;
        try ( Connection conn = DriverManager.getConnection(url);
              Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            for (String r : rows) {
                sql = "INSERT INTO " + table + " VALUES (" + r + ")";
                stmt.execute(sql);
            }
            conn.commit();
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
            conn.setAutoCommit(false);
            String setClause, whereClause;
            for (Properties prop:rows) {                
                setClause = setClause(prop);
                whereClause = whereClause(prop);
                sql = "UPDATE " + table + 
                      " SET " + setClause + 
                      " WHERE " + whereClause + ";";
                stmt.execute(sql);
            }
            conn.commit();
        }
    }
    

    public static void updateDcels(int grp) throws SQLException {
        String sql1 = """
                      SELECT dx.edge, dy.edge
                      FROM dcels dx, dcels dy, linesegments lx, linesegments ly, points px, points py
                      WHERE (dx.grp = ?) AND
                            ((lx.id = dx.edge) AND (lx.grp = dx.grp)) AND
                            ((ly.id = dy.edge) AND (ly.grp = dy.grp)) AND
                      \t     ((dx.f_l = dy.f_l) OR (dx.f_l = dy.f_r)) AND
                      \t     ((lx.beginpoint = px.id) AND (lx.grp = px.grp)) AND
                      \t     (dx.edge != dy.edge) AND
                      \t     (((ly.endpoint = py.id) AND (ly.grp = py.grp) AND
                      \t       (round(px.x,6) = round(py.x,6)) AND (round(px.y,6) = round(py.y,6))) OR
                      \t      ((ly.beginpoint = py.id) AND (ly.grp = py.grp) AND
                      \t       (round(px.x,6) = round(py.x,6)) AND (round(px.y,6) = round(py.y,6))));""";      
        String sql2 = """
                      SELECT dx.edge, dy.edge
                      FROM dcels dx, dcels dy, linesegments lx, linesegments ly, points px, points py
                      WHERE (dx.grp = ?) AND
                            ((lx.id = dx.edge) AND (lx.grp = dx.grp)) AND
                            ((ly.id = dy.edge) AND (ly.grp = dy.grp)) AND
                            ((dx.f_r = dy.f_l) OR (dx.f_r = dy.f_r)) AND
                      \t     ((lx.endpoint = px.id) AND (lx.grp = px.grp)) AND
                      \t     (dx.edge != dy.edge) AND
                      \t     (((ly.endpoint = py.id) AND (ly.grp = py.grp) AND
                      \t       (round(px.x,6) = round(py.x,6)) AND (round(px.y,6) = round(py.y,6))) OR
                      \t      ((ly.beginpoint = py.id) AND (ly.grp = py.grp) AND
                      \t       (round(px.x,6) = round(py.x,6)) AND (round(px.y,6) = round(py.y,6))));""";      
        List<Properties> rows = new LinkedList<>();
        try ( Connection conn = DriverManager.getConnection(url);  
              PreparedStatement pstmt1 = conn.prepareStatement(sql1);
              PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
            pstmt1.setInt(1, grp);
            ResultSet rs1 = pstmt1.executeQuery();
            while (rs1.next()) {
                Properties row = new Properties();
                int xid = rs1.getInt(1);
                int yid = rs1.getInt(2);
                row.setProperty("COLUMN:bp_ref", "" + yid);
                row.setProperty("PRIMARY:edge", "" + xid);
                rows.add(row);
            }            
            pstmt2.setInt(1, grp);
            ResultSet rs2 = pstmt2.executeQuery();
            while (rs2.next()) {
                Properties row = new Properties();
                int xid = rs2.getInt(1);
                int yid = rs2.getInt(2);
                row.setProperty("COLUMN:ep_ref", "" + yid);
                row.setProperty("PRIMARY:edge", "" + xid);
                rows.add(row);
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        DatabaseHandler.updateContent("dcels", rows);
    }
    
    public static void storeDcels(int grp, List<DCELNode> ldn, List<Properties> r) throws SQLException {
        int pId = DatabaseHandler.getLargestPointId(grp);
        int lId = DatabaseHandler.getLargestLinesegmentId(grp);
        String ibp = "null";
        String iep = "null";
        String imp, idp;
        try ( Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            String sql,row;
            Line ls;
            int ifl, ifr;
            Point bp, ep;
            for (DCELNode n:ldn) {
                ls = n.getLineSegment();
                ifl = DatabaseHandler.getIndexFromPoint(n.f_l, grp);
                ifr = DatabaseHandler.getIndexFromPoint(n.f_r, grp);
                if (ls.getBeginP() != null) {
                    bp = new Point(ls.getBeginP().x(), ls.getBeginP().y());
                    row = "" + pId + ", " + grp + ", " + bp.x() +", " + bp.y();
                    sql = "INSERT INTO points VALUES (" + row + ");";
                    stmt.execute(sql);
                    ibp = "" + pId;
                    pId++;                        
                }
                if (ls.getEndP() != null) {
                    ep = new Point(ls.getEndP().x(), ls.getEndP().y());
                    row = "" + pId + ", " + grp + ", " + ep.x() +", " + ep.y();
                    sql = "INSERT INTO points VALUES (" + row + ");";
                    stmt.execute(sql);
                    iep = "" + pId;
                    pId++;
                }
                Point mp = new Point(ls.getMidP().x(),ls.getMidP().y());
                row = "" + pId + ", " + grp + ", " + mp.x() + ", " + mp.y();
                sql = "INSERT INTO points VALUES (" + row + ");";
                stmt.execute(sql);
                imp = "" + pId;
                pId++;
                Point dp = new Point(ls.getDir().x(),ls.getDir().y());
                row = "" + pId + ", " + grp + ", " + dp.x() +", " + dp.y();
                sql = "INSERT INTO points VALUES (" + row + ");";
                stmt.execute(sql);
                idp = "" + pId;
                pId++;
                row = "" + lId + ", " + grp + ", " + ibp + ", " + iep + ", " + imp + ", " + idp;
                sql = "INSERT INTO linesegments VALUES (" + row + ");";
                stmt.execute(sql);
                row = "" + lId + ", " + grp + ", " + ifl + ", " + ifr + ", null, null";
                sql = "INSERT INTO dcels VALUES (" + row + ");";
                stmt.execute(sql);
                // reset variables
                lId++;
                ibp = "null";
                iep = "null";
            }
            conn.commit();
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
            LOG.error(e.getMessage());
            return null;
        }
    }
    
    public static int getIndexFromPoint(Point p, int grp) {
        String sql = """
                     SELECT id FROM points
                     WHERE ( ( (ABS(x - ?) < 1e-11) AND (ABS(y - ?) < 1e-11) ) AND (grp = ?) ) 
                     """;

        try ( Connection conn = DriverManager.getConnection(url);  
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the value
            pstmt.setDouble(1, p.x());
            pstmt.setDouble(2, p.y());
            pstmt.setInt(3, grp);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt("id");
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return -1;
        }
    }
    
    public static int getIndexOfBinarytTreeRoot(int grp) {
        String sql = """
                     SELECT point FROM binaryTrees WHERE grp = ? AND point not in (
                       SELECT left FROM binaryTrees WHERE (left is not null)
                       UNION
                       SELECT right FROM binaryTrees WHERE (right is not null)
                     )""";
        
        try ( Connection conn = DriverManager.getConnection(url);  
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the value
            pstmt.setInt(1, grp);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt("point");
        } catch (SQLException e) {
            LOG.error(e.getMessage());
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
        String sql = """
                     SELECT point 
                     FROM binaryTrees 
                     WHERE grp = ?""";
        
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
            LOG.error(e.getMessage());
            return null;
        }        
    }
    
    public static BinaryTree getBinaryTreeByGroup(int grp) {
        Map<Integer,BinaryTreeNode> m = new HashMap<>();
        Map<Integer,BinaryTree> mbt = new HashMap<>();
        String sql = """
                     SELECT binaryTrees.point, binaryTrees.left, binaryTrees.right, points.x, points.y 
                     FROM points, binaryTrees 
                     WHERE (
                            (binaryTrees.grp = ?) AND 
                            (points.id = binaryTrees.point) AND 
                            (points.grp = binaryTrees.grp))"""; 
        
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
            LOG.error(e.getMessage());
            return null;
        }        
    }

    private static class ConveksHullNode {
        Point p;
        int prv;

        ConveksHullNode(Point p, int prv, int nxt) {
            this.p = p;
            this.prv = prv;
        }
    }
    
    public static ConveksHull getConveksHullByGroup(int grp) {
        Map<Integer,ConveksHullNode> m = new HashMap<>();
        Map<Integer,ConveksHull> mch = new HashMap<>();
        String sql = """
                     SELECT conveksHulls.point, conveksHulls.next, conveksHulls.previous, points.x, points.y 
                     FROM points, conveksHulls
                     WHERE ((points.id = conveksHulls.point) AND (points.grp = conveksHulls.grp) 
                           AND (conveksHulls.grp = ?))
                     ORDER BY points.x, points.y ASC""";
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
            LOG.error(e.getMessage());
            return null;
        }        
    }
    
    private static class DcelStruct {
        DCELNode dn = new DCELNode();
        int bp_ref, ep_ref;
        
        DcelStruct(double a_b, double a_e, Point mp, Point dir, int bp_ref, int ep_ref, Point f_l, Point f_r) {
            dn.a_b = a_b;
            dn.a_e = a_e;
            dn.p = mp;
            dn.d = dir;
            dn.f_l = f_l;
            dn.f_r = f_r;
            this.bp_ref = bp_ref;
            this.ep_ref = ep_ref;
        }
    }
    
    public static DCEL getVoronoiDiagramByGroup(int grp) {
        String sql1 = """
                      SELECT d.edge, d.bp_ref, d.ep_ref, bp.x, bp.y, ep.x, ep.y, mp.x, mp.y, dir.x, dir.y, lp.x, lp.y, rp.x, rp.y
                      FROM dcels d, linesegments l, points bp, points ep, points mp, points dir, points lp, points rp
                      WHERE (d.grp = ?) AND
                            ((l.id = d.edge) AND (l.grp = d.grp)) AND
                            ((bp.id = l.beginpoint) AND (bp.grp = l.grp)) AND
                            ((ep.id = l.endpoint) AND (ep.grp = l.grp)) AND
                            ((mp.id = l.midpoint) AND (mp.grp = l.grp)) AND
                            ((dir.id = l.direction) AND (dir.grp = l.grp)) AND
                            ((lp.id = d.f_l) AND (lp.grp = d.grp)) AND
                            ((rp.id = d.f_r) AND (rp.grp = d.grp));""";
        String sql2 = """
                      SELECT d.edge, d.bp_ref, bp.x, bp.y, mp.x, mp.y, dir.x, dir.y, lp.x, lp.y, rp.x, rp.y
                      FROM dcels d, linesegments l, points bp, points mp, points dir, points lp, points rp
                      WHERE (d.grp = ?) AND
                            ((l.id = d.edge) AND (l.grp = d.grp)) AND
                            ((bp.id = l.beginpoint) AND (bp.grp = l.grp)) AND
                            (l.endpoint IS NULL) AND
                            ((mp.id = l.midpoint) AND (mp.grp = l.grp)) AND
                            ((dir.id = l.direction) AND (dir.grp = l.grp)) AND
                            ((lp.id = d.f_l) AND (lp.grp = d.grp)) AND
                            ((rp.id = d.f_r) AND (rp.grp = d.grp));""";
        String sql3 = """
                      SELECT d.edge, d.ep_ref, ep.x, ep.y, mp.x, mp.y, dir.x, dir.y, lp.x, lp.y, rp.x, rp.y
                      FROM dcels d, linesegments l, points ep, points mp, points dir, points lp, points rp
                      WHERE (d.grp = ?) AND
                            ((l.id = d.edge) AND (l.grp = d.grp)) AND
                            (l.beginpoint IS NULL) AND
                            ((ep.id = l.endpoint) AND (ep.grp = l.grp)) AND
                            ((mp.id = l.midpoint) AND (mp.grp = l.grp)) AND
                            ((dir.id = l.direction) AND (dir.grp = l.grp)) AND
                            ((lp.id = d.f_l) AND (lp.grp = d.grp)) AND
                            ((rp.id = d.f_r) AND (rp.grp = d.grp));""";
        try ( Connection conn = DriverManager.getConnection(url);  
              PreparedStatement pstmt1 = conn.prepareStatement(sql1);
              PreparedStatement pstmt2 = conn.prepareStatement(sql2);
              PreparedStatement pstmt3 = conn.prepareStatement(sql3)) {
            Map<Integer,DcelStruct> m = new HashMap<>();
            // set the value
            pstmt1.setInt(1, grp);
            ResultSet rs = pstmt1.executeQuery();
            DcelStruct ds;
            while(rs.next()) {                
                int id = rs.getInt(1);
                int bp_ref = rs.getInt(2);
                Point mp = new Point(rs.getDouble(8), rs.getDouble(9));                    
                Point dir = new Point(rs.getDouble(10), rs.getDouble(11));                    
                Point bp = new Point(rs.getDouble(4), rs.getDouble(5));
                double a_b = (bp.x() - mp.x())/dir.x();
                int ep_ref = rs.getInt(3);
                Point ep = new Point(rs.getDouble(6), rs.getDouble(7));                    
                double a_e = (ep.x() - mp.x())/dir.x();
                Point f_l = new Point(rs.getDouble(12), rs.getDouble(13));
                Point f_r = new Point(rs.getDouble(14), rs.getDouble(15));
                ds = new DcelStruct(a_b, a_e, mp, dir, bp_ref, ep_ref, f_l, f_r); 
                m.put(id, ds);
            }
            // set the value
            pstmt2.setInt(1, grp);
            rs = pstmt2.executeQuery();
            while(rs.next()) {                
                int id = rs.getInt(1);
                int bp_ref = rs.getInt(2);
                Point mp = new Point(rs.getDouble(5), rs.getDouble(6));                    
                Point dir = new Point(rs.getDouble(7), rs.getDouble(8));                    
                Point bp = new Point(rs.getDouble(3), rs.getDouble(4));
                double a_b = (bp.x() - mp.x())/dir.x();
                Point f_l = new Point(rs.getDouble(9), rs.getDouble(10));
                Point f_r = new Point(rs.getDouble(11), rs.getDouble(12));
                ds = new DcelStruct(a_b, 0.0, mp, dir, bp_ref, 0, f_l, f_r); 
                m.put(id, ds);
            }
            // set the value
            pstmt3.setInt(1, grp);
            rs = pstmt3.executeQuery();
            while(rs.next()) {                
                int id = rs.getInt(1);
                int ep_ref = rs.getInt(2);
                Point mp = new Point(rs.getDouble(5), rs.getDouble(6));                    
                Point dir = new Point(rs.getDouble(7), rs.getDouble(7));                    
                Point ep = new Point(rs.getDouble(3), rs.getDouble(4));
                double a_e = (ep.x() - mp.x())/dir.x();
                Point f_l = new Point(rs.getDouble(9), rs.getDouble(10));
                Point f_r = new Point(rs.getDouble(11), rs.getDouble(12));
                ds = new DcelStruct(0.0, a_e, mp, dir, 0, ep_ref, f_l, f_r); 
                m.put(id, ds);
            }
            Set<Integer> mi = m.keySet();
            Map<Integer, DCELNode> mdn = new HashMap<>();
            for(Integer i : mi) {
                ds = m.get(i);
                DCELNode dn = new DCELNode(ds.dn);
                mdn.put(i, dn);
            }    
            for(Integer i : mi) {
                ds = m.get(i);
                DCELNode dn = mdn.get(i);
                if (ds.bp_ref != 0) {
                    dn.p_b = new DCEL(mdn.get(ds.bp_ref));
                }
                if (ds.ep_ref != 0) {
                    dn.p_e = new DCEL(mdn.get(ds.ep_ref));
                }
            }    
            DCEL ret = new DCEL(mdn.get(1));            
            return ret;
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }
    
    public static int getLargestPointId(int grp) throws SQLException {
        String sql = """
                     SELECT id FROM points
                     WHERE (grp = ?)
                     ORDER BY points.id DESC""";
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
    
    public static int getLargestLinesegmentId(int grp) throws SQLException {
        String sql = """
                     SELECT id FROM linesegments
                     WHERE (grp = ?)
                     ORDER BY linesegments.id DESC""";
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
        String sql = """
                     SELECT id FROM linesegments
                     WHERE (grp = ?)
                     ORDER BY linesegments.id DESC""";
        int ni = -1;
        try ( Connection conn = DriverManager.getConnection(url);  
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the value
            pstmt.setInt(1, grp);
            ResultSet rs = pstmt.executeQuery();
            ni = rs.getInt("id") + 1;
        } catch (SQLException e) {
            LOG.error(e.getMessage());
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
}