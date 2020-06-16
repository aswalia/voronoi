package asi.voronoi;

import java.util.LinkedList;

public class BinaryTree implements java.io.Serializable, ModelObject {
    protected BinaryTree lft, rgt;
    protected Point p;

    public BinaryTree(Point p) {
        this.p = p;
        lft = rgt = null;
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

    public boolean inTree(Point t) {
        boolean ret;
        if (p.equals(t)) {
            ret = true;
        } else if (p.isLess(t)) {
            ret = (rgt == null) ? false : rgt.inTree(t);
        } else {
            ret = (lft == null) ? false : lft.inTree(t);
        }
        return ret;
    }

    public BinaryTree insertNode(Point p) {
        if (!this.p.equals(p)) {
            if (this.p.isLess(p)) {
                rgt = (rgt == null) ? newNode(p) : rgt.insertNode(p);
            } else {
                lft = (lft == null) ? newNode(p) : lft.insertNode(p);
            }
        }
        return this;
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
                ret += "N";
            } else if (!pe.b.isLeaf()) {
                level++;
                ret += pe.b.p.toString();
                pList.add(new PointElem(pe.b.lft, level));
                pList.add(new PointElem(pe.b.rgt, level));
            } else {
                ret += "[" + pe.b.p + "]";
            }
            ret += eol;
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

    static class Test {
        public static void main(String[] argv) {
            Point p = new Point((int) (Math.random() * 100), (int) (Math.random() * 100));
            BinaryTree t = new BinaryTree(p);
            for (int i = 0; i < Integer.parseInt(argv[0]); i++) {
                p = new Point((int) (Math.random() * 100), (int) (Math.random() * 100));
                t = t.insertNode(p);
            }
            System.out.println(t);
            System.out.println("#items: " + t.count() + " max: " + t.max() + " min: " + t.min());
            System.out.println("maxX: " + t.maxX() + " maxY: " + t.maxY() + " minX: " + t.minX() + " minY: " + t.minY());
        }
    }
}
