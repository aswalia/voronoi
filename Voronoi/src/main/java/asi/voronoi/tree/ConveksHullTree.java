package asi.voronoi.tree;

import asi.voronoi.ConveksHull;
import asi.voronoi.Point;
import asi.voronoi.tree.IntervalTree;

public class ConveksHullTree extends IntervalTree {
    @Override
    public IntervalTree newNode() {
        return new ConveksHullTree();
    }

    @Override
    public String toString() {
        return info.toString();
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
}
