package asi.voronoi.tree;

import asi.voronoi.ModelObject;
import asi.voronoi.Point;
import asi.voronoi.Serializer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.StringTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BinaryTree implements java.io.Serializable, ModelObject {
    private static final Logger LOG = LogManager.getLogger(BinaryTree.class);
    private static BufferedReader br;
    protected BinaryTree lft, rgt;
    protected Point p;
    
    public BinaryTree() {
        
    }
    
    public BinaryTree(Point p) {
        this.p = p;
        lft = rgt = null;
        LOG.debug("Construct: "+p);
    }

    public BinaryTree buildBinaryTree(String filename) throws IOException {
        FileReader fr;
        fr = new FileReader(filename);
        br = new BufferedReader(fr);
        return buildTreeFromFile();
    }

    public BinaryTree newNode(Point p) {
        return new BinaryTree(p);
    }

    public boolean isLeaf() {
        return (lft == null) && (rgt == null);
    }

    public Point getP() {
        return p;
    }

    public BinaryTree lft() {
        return lft;
    }

    public BinaryTree rgt() {
        return rgt;
    }
    
    public BinaryTree findNode(Point t) {
        BinaryTree ret = this;
        if (p.equals(t)) {
            return ret;
        } else if (p.isLess(t)) {
            ret = (rgt == null) ? null : rgt.findNode(t);
        } else {
            ret = (lft == null) ? null : lft.findNode(t);
        }
        return ret;
    }

    public boolean inTree(Point t) {
        return (findNode(t) != null);
    }

    public BinaryTree insertNode(Point t) {
        // non recursive insert
         if (inTree(t)) {
             LOG.debug("Already exists"+p);
            // already in tree
            return this;
        } 
        BinaryTree root, tmp;
        root = tmp = this; 
        while (((tmp.lft != null) && t.isLess(tmp.p)) ||
               ((tmp.rgt != null) && tmp.p.isLess(t))) {
            if (tmp.p.isLess(t)) {
                tmp = tmp.rgt;
            } else {
                tmp = tmp.lft;
            }
        }    
        // insert the new node and adjust subtree
        addNode(t, tmp);
        return root;
    }

    protected void addNode(Point t, BinaryTree tmp) {
        BinaryTree t1 = newNode(t);
        if (tmp.p.isLess(t)) {
            tmp.rgt = t1;
        } else {
            tmp.lft = t1;
        }
        LOG.debug("Parent: "+tmp+" "+t);
    }

    protected int height(BinaryTree b) {
        if (b == null) {
            return 0;
        } else {
            return Math.max(height(b.lft),height(b.rgt))+1;
        }
    }
    


    public Point max() {
        Point ret;
        if (rgt == null) {
            ret = p;
        } else {
            ret = rgt.max();
        }
        return ret;
    }

    public Point min() {
        Point ret;
        if (lft == null) {
            ret = p;
        } else {
            ret = lft.min();
        }
        return ret;
    }

    @Override
    public double minY() {
        double ret = p.y();
        if (!isLeaf()) {
            double lret, rret;
            if (lft != null) {
                if (ret > (lret = lft.minY())) {
                    ret = lret;
                }
            }
            if (rgt != null) {
                if (ret > (rret = rgt.minY())) {
                    ret = rret;
                }
            }
        }
        return ret;
    }

    @Override
    public double maxY() {
        double ret = p.y();
        if (!isLeaf()) {
            double lret, rret;
            if (lft != null) {
                if (ret < (lret = lft.maxY())) {
                    ret = lret;
                }
            }
            if (rgt != null) {
                if (ret < (rret = rgt.maxY())) {
                    ret = rret;
                }
            }
        }
        return ret;
    }

    @Override
    public double minX() {
        return min().x();
    }

    @Override
    public double maxX() {
        return max().x();
    }
    private static LinkedList<PointElem> pList;

    private static class PointElem {
        BinaryTree b;
        int level;

        PointElem(BinaryTree b, int level) {
            this.b = b;
            this.level = level;
        }
    }

    private int level() {
        if (pList.isEmpty()) {
            return 0;
        } else {
            PointElem pObj = pList.getFirst();
            return pObj.level;
        }
    }

    @Override
    public String toString() {
        String ret = "";
        int level = 1;
        pList = new LinkedList();
        PointElem pe = new PointElem(this, level);
        pList.add(pe);
        do {
            pe = pList.removeFirst();
            String eol = (pe.level == level()) ? " " : "\n";
            level = pe.level;
            if (pe.b == null) {
                LOG.debug("Null");
                ret += "N";
            } else if (!pe.b.isLeaf()) {
                level++;
                LOG.debug("Leaf: "+level);
                ret += pe.b.p.toString();
                pList.add(new PointElem(pe.b.lft, level));
                pList.add(new PointElem(pe.b.rgt, level));
            } else {
                ret += "[" + pe.b.p + "]";
            }
            ret += eol;
            LOG.debug(ret);
        } while (!pList.isEmpty());
        return ret;
    }

    public int count() {
        int ret;
        if (isLeaf()) {
            ret = 1;
        } else if (lft == null) {
            ret = rgt.count() + 1;
        } else if (rgt == null) {
            ret = lft.count() + 1;
        } else {
            ret = lft.count() + rgt.count() + 1;
        }
        return ret;
    }
    
    public static BinaryTree fetch(String filename) throws java.io.IOException, ClassNotFoundException {
        return (BinaryTree) Serializer.fetch(filename);
    }

    private Point parsePoint(String s) {
        String delim = " " + ":" + "(" + ")" + "," + "\t\n\r\f";        
        String iToken;
        StringTokenizer st = new StringTokenizer(s, delim);
        double x, y;
        iToken = st.nextToken(); // x-coordinat
        x = Double.parseDouble(iToken);
        iToken = st.nextToken(); // y-coordinat
        y = Double.parseDouble(iToken);

        return new Point(x, y);
    }
    
    private BinaryTree buildTreeFromFile() throws IOException {
        String s = br.readLine();
        BinaryTree ret = newNode(parsePoint(s));
        while ((s = br.readLine())!=null) {
            ret = ret.insertNode(parsePoint(s));
        }
        return ret;
    }
}
