package asi.voronoi;

public class ConveksHullTree extends IntervalTree {
    @Override
    public IntervalTree newNode() {
        return new ConveksHullTree();
    }

    @Override
    public String toString() {
        return super.toString() + info;
    }

    @Override
    public void buildStructure() {
        if (lft.isLeaf() && rgt.isLeaf()) {
            info = new ConveksHull((Point) lft.info, (Point) rgt.info);
        } else if (lft.isLeaf()) {
            rgt.buildStructure();
            info = new ConveksHull((ConveksHull) rgt.info);
            ((ConveksHull) info).merge((Point) lft.info);
        } else if (rgt.isLeaf()) {
            lft.buildStructure();
            info = new ConveksHull((ConveksHull) lft.info);
            ((ConveksHull) info).merge((Point) rgt.info);
        } else {
            lft.buildStructure();
            rgt.buildStructure();
            info = new ConveksHull((ConveksHull) lft.info);
            ((ConveksHull) info).merge(new ConveksHull((ConveksHull) rgt.info));
        }
    }

    static class Test {
        public static void main(String[] argv) throws java.io.IOException {
            ConveksHullTree v = new ConveksHullTree();
            v.buildTree(argv[0]);
            v.buildStructure();
            System.out.println(v);
        }
    }
}
