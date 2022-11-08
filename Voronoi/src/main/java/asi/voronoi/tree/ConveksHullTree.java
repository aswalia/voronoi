package asi.voronoi.tree;

import asi.voronoi.ConveksHull;

public class ConveksHullTree {
    private ConveksHull info;
    private ConveksHullTree lft, rgt;
    
    @Override
    public String toString() {
        return "" + info;
    }

    public void buildStructure(BinaryTree b) {
        if (b == null) {
            info = null;
        } else if (b.isLeaf()) {
            info = new ConveksHull(b.p);  
        } else if (b.lft == null) {
            info = new ConveksHull(b.p);
            rgt = new ConveksHullTree();
            rgt.buildStructure(b.rgt);
            info = info.merge(rgt.info);
        } else if (b.rgt == null) {
            info = new ConveksHull(b.p);
            lft = new ConveksHullTree();
            lft.buildStructure(b.lft);
            info = info.merge(lft.info);
        } else {
            info = new ConveksHull(b.p);
            lft = new ConveksHullTree();
            lft.buildStructure(b.lft);
            info = info.merge(lft.info);
            rgt = new ConveksHullTree();
            rgt.buildStructure(b.rgt);
            info = info.merge(rgt.info);
        }
    }
    
    public ConveksHull getInfo() {
        return info;
    }
}
