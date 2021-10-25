package asi.voronoi;

public class AVLTree extends BinaryTree {
    private enum RotationType {
        LL, LRa, LRbc, RLa, RLbc, RR, NA;
    }  
    
    private int bf;

    public AVLTree(Point p) {
        super(p);
        bf = 0;
    }

    @Override
    public BinaryTree newNode(Point p) {
        return new AVLTree(p);
    }
    
    private int balanceFactor(BinaryTree t) {
        if (t == null || t.isLeaf()) {
            return 0;
        } else {
            return height(t.lft) - height(t.rgt);
        }
    }
    
    private void adjustLevel(Point t) {
        if (!p.equals(t)) {
            bf = balanceFactor(this);
            if (p.isLess(t)) {
                ((AVLTree)rgt).adjustLevel(t);
            } else {
                ((AVLTree)lft).adjustLevel(t);            
            }
        }
    }
    
    private RotationType findRotation() {
        RotationType ret = RotationType.NA;
        if ((bf == 2) && ((AVLTree)lft).bf == 1) {
            ret = RotationType.LL;
        } else if ((bf == 2) && 
                   (((AVLTree)lft).bf == -1) && 
                   (((AVLTree)lft.rgt).bf == 0)) {
            ret = RotationType.LRa;
        } else if ((bf == 2) && 
                   (((AVLTree)lft).bf == -1)) {
            ret = RotationType.LRbc;
        }
        if ((bf == 2) && ((AVLTree)lft).bf == 1) {
            ret = RotationType.LL;
        } else if ((bf == -2) && 
                   (((AVLTree)rgt).bf == 1) && 
                   (((AVLTree)rgt.lft).bf == 0)) {
            ret = RotationType.RLa;
        } else if ((bf == -2) && 
                   (((AVLTree)rgt).bf == 1)) {            
            ret = RotationType.RLbc;
        } else if ((bf == -2) && ((AVLTree)rgt).bf == -1) {
            ret = RotationType.RR;
        }
        return ret;
    }
    
    private AVLTree parent(Point cp) {
        if (!p.equals(cp)) {
            if (p.isLess(cp)) {
                if (rgt.p.equals(cp)) {
                    return this;
                }
                return ((AVLTree)rgt).parent(cp);
            } else {
                if (lft.p.equals(cp)) {
                    return this;
                }
                return ((AVLTree)lft).parent(cp);                
            }
        } else {
            return this; 
        }
    }
    
    private AVLTree checkAndBalance(Point t) {
        AVLTree self = this;
        AVLTree gp, parent, a, c;
        if (!p.equals(t)) {
            parent = parent(t);
            gp = parent(parent.p);
            a = gp;
            while ((a != self) && ((Math.abs(balanceFactor(a)) <= 1) &&
                                   (Math.abs(balanceFactor(a)) <= 1))) {
                // go up one level
                a = parent(a.p);
            }
            if (Math.abs(a.bf) > 1) {
                parent = parent(a.p);
                // we have a unbalanced subtree 
                c = rebalance(a);
                // fix the parent of previous root 
                // of subtree to new root of subtree
                if (self == a) {
                    // root needs change
                    self = c;                    
                } else if (parent.lft.p.equals(a.p)) {
                    parent.lft = c;
                } else if (parent.rgt.p.equals(a.p)){
                    parent.rgt = c;
                }
            }        
        }
        return self;
    }

    private AVLTree rebalance(AVLTree a) {
        AVLTree b, bl, br, cl, cr, c = null;
        switch (a.findRotation()) {
            case LL: // init var for LL
                b = (AVLTree)a.lft;
                br = (AVLTree)b.rgt;
                // do transform
                b.rgt = a;
                a.lft = br;
                // set c to new root of subtree
                c = b;
                break;
            case LRa: // init var LRa
                b = (AVLTree)a.lft;
                c = (AVLTree)b.rgt;
                // do transform
                c.lft = b;
                c.rgt = a;
                b.rgt = null;
                a.lft = null;
                break;
            case LRbc: // init var for LRbc
                b = (AVLTree)a.lft;
                c = (AVLTree)b.rgt;
                cl = (AVLTree)c.lft;
                cr = (AVLTree)c.rgt;
                // do transform
                c.lft = b;
                c.rgt = a;
                b.rgt = cl;
                a.lft = cr;
                break;
            case RR: // init var for RR
                b = (AVLTree)a.rgt;
                bl = (AVLTree)b.lft;
                // do transform
                b.lft = a;
                a.rgt = bl;
                // set c to new root of subtree
                c = b;
                break;
            case RLa: // init var RLa
                b = (AVLTree)a.rgt;
                c = (AVLTree)b.lft;
                // do transform
                c.lft = a;
                c.rgt = b;
                b.lft = null;
                a.rgt = null;
                break;
            case RLbc: // init var for RLbc
                b = (AVLTree)a.rgt;
                c = (AVLTree)b.lft;
                cl = (AVLTree)c.lft;
                cr = (AVLTree)c.rgt;
                // do transform
                c.lft = a;
                c.rgt = b;
                a.rgt = cl;
                b.lft = cr;
                break;
        }
        return c;
    }
    
    @Override
    protected void addNode(Point t, BinaryTree tmp) {
        BinaryTree t1 = newNode(t);
        AVLTree parent = parent(tmp.p);
        if (tmp.p.isLess(t)) {
            tmp.rgt = t1;
            ((AVLTree)tmp).bf = ((AVLTree)tmp).bf - 1;
        } else {
            tmp.lft = t1;
            ((AVLTree)tmp).bf = ((AVLTree)tmp).bf + 1;
        }
        parent.bf = balanceFactor(parent);
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
