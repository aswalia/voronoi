package asi.voronoi;

public class AVLTree extends BinaryTree {
    private short bf;

    public AVLTree(Point p) {
        super(p);
        bf = 0;
    }

    @Override
    public BinaryTree newNode(Point p) {
        return new AVLTree(p);
    }

    private int hight() {
        if (this == null) {
            return 0;
        }
        else if (isLeaf()) {
            return 1;
        } else {
            return Math.max(lft == null ? 0 : ((AVLTree)lft).hight(),rgt == null ? 0 : ((AVLTree)rgt).hight()) + 1;
        }
    }
    
    private void adjustLevel(Point t) {
        if (!p.equals(t)) {
            int lc = (lft == null) ? 0 : ((AVLTree)lft).hight();
            int rc = (rgt == null) ? 0 : ((AVLTree)rgt).hight();
            bf = (short)Math.abs(lc - rc);
            if (p.isLess(t)) {
                ((AVLTree)rgt).adjustLevel(t);
            } else {
                ((AVLTree)lft).adjustLevel(t);            
            }
        }
    }
    
    private AVLTree leftLeft(AVLTree gp, AVLTree p, AVLTree me) {
        AVLTree self = p;
        p.rgt = gp;
        gp.lft = null;
        return self;
    }
    
    private AVLTree leftRight(AVLTree gp, AVLTree p, AVLTree me) {
        AVLTree self;
        gp.lft = me;
        me.lft = p;
        p.rgt = null;
        self = leftLeft(gp,me,p);
        return self;
    }
    
    private AVLTree rightRight(AVLTree gp, AVLTree p, AVLTree me) {
        AVLTree self = p;
        p.lft = gp;
        gp.rgt = null;
        return self;
    }
    
    private AVLTree rightLeft(AVLTree gp, AVLTree p, AVLTree me) {
        AVLTree self;
        gp.rgt = me;
        me.rgt = p;
        p.lft = null;
        self = rightRight(gp,me,p);
        return self;
    }
    
    private AVLTree trace(Point cp) {
        if (!p.equals(cp)) {
            if (p.isLess(cp)) {
                if (rgt.p.equals(cp)) {
                    return this;
                }
                return ((AVLTree)rgt).trace(cp);
            } else {
                if (lft.p.equals(cp)) {
                    return this;
                }
                return ((AVLTree)lft).trace(cp);                
            }
        } else {
            return this; 
        }
    }
    
    private AVLTree checkAndBalance(Point t) {
        AVLTree self, ggp = this;
        AVLTree grandparent, parent, me;
        if (!p.equals(t)) {
            if (bf > 1) {
                me = (AVLTree)findNode(t);
                parent = trace(t);
                grandparent = trace(parent.p);
                ggp = trace(grandparent.p);
                if ((grandparent.lft != null) && 
                    (grandparent.lft.p.equals(parent.p))) {
                    // left
                    if ((parent.lft != null) && 
                        (parent.lft.p.equals(me.p))) {
                        // left
                        self = grandparent.leftLeft(grandparent,parent,me);
                    } else {
                        // right
                        self = grandparent.leftRight(grandparent,parent,me);
                    }
                    if (!ggp.equals(grandparent)) {
                        ggp.lft = self;
                    } else {
                        ggp = self;
                    }
                } else {
                    if ((parent.lft != null) && 
                        (parent.lft.p.equals(me.p))) {
                        // left
                        self = grandparent.rightLeft(grandparent,parent,me);
                    } else {
                        // right
                        self = grandparent.rightRight(grandparent,parent,me);
                    }
                    if (!ggp.equals(grandparent)) {
                        ggp.rgt = self;
                    } else {
                        ggp = self;
                    }
                }
            }
        }
        return ggp;
    }
    
    @Override
    public BinaryTree insertNode(Point t) {
        BinaryTree ret;
        super.insertNode(t);
        // adjust level
        adjustLevel(t);
        //check and rebalance if needed
        ret = checkAndBalance(t);
        return ret;
    }
}
