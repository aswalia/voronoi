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
    
    private void adjustLevel(Point t) {
        if (!p.equals(t)) {
            int lc, rc;
            lc = rc = 0;
            if (lft != null) {
                lc = lft.count();
            }
            if (rgt != null) {
                rc = rgt.count();
            }
            bf = (short)Math.abs(lc - rc);
            if (p.isLess(t)) {
                ((AVLTree)rgt).adjustLevel(t);
            } else {
                ((AVLTree)lft).adjustLevel(t);            
            }
        }
    }
    
    private AVLTree leftLeft() {
        AVLTree self, gp, p, me, tmp;
        gp = p = me = this;
        tmp = (AVLTree)p.rgt;
        self = p;
        p.rgt = gp;
        gp.rgt = tmp;
        gp.bf = p.bf = me.bf = 0;
        return self;
    }
    
    private AVLTree leftRight() {
        AVLTree self, gp, p, me, tmp;
        self = gp = p = me = this;
        me.lft = p;
        p.rgt = null;
        gp.lft = me;
        self = leftLeft();
        return self;
    }
    
    private AVLTree rightRight() {
        AVLTree self, gp, p, me, tmp;
        gp = p = me = this;
        tmp = (AVLTree)p.lft;
        self = p;
        p.lft = gp;
        gp.lft = tmp;
        gp.bf = p.bf = me.bf = 0;
        return self;
    }
    
    private AVLTree rightLeft() {
        AVLTree self, gp, p, me, tmp;
        self = gp = p = me = this;
        me.rgt = p;
        p.lft = null;
        gp.rgt = me;
        self = rightRight();
        return self;
    }
    
    private AVLTree checkAndBalance(Point t) {
        AVLTree self = this;
        AVLTree grandparent, parent, me;
        grandparent = parent = me = this;
        if (p.equals(t)) {
            if (bf > 1) {
                if (grandparent.lft.equals(parent)) {
                    // left
                    if (parent.lft.equals(me)) {
                        // left
                        self = grandparent.leftLeft();
                    } else {
                        // right
                        self = grandparent.leftRight();
                    }
                } else {
                    if (parent.lft.equals(me)) {
                        // left
                        self = grandparent.rightLeft();
                    } else {
                        // right
                        self = grandparent.rightRight();
                    }
                }
            }
        }
        return self;
    }

    @Override
    public BinaryTree insertNode(Point t) {
        super.insertNode(t);
        // adjust level
        adjustLevel(t);
        //check and rebalance if needed
        AVLTree self = this;
        self = checkAndBalance(t);
/*        AVLTree self = this;
        if (!inTree(t)) {
            AVLTree a, b, c, p1, q;
            BinaryTree f, y;
            short d;
            a = p1 = this;
            f = q = null;
            do {
                if (p1.bf != 0) {
                    a = p1;
                    f = q;
                }
                q = p1;
                p1 = p1.p.isLess(t) ? (AVLTree) p1.rgt : (AVLTree) p1.lft;
            } while (p1 != null);
            y = newNode(t);
            if (q.p.isLess(t)) {
                q.rgt = y;
            } else {
                q.lft = y;
            }
            p1 = a.p.isLess(t) ? (AVLTree) a.rgt : (AVLTree) a.lft;
            d = a.p.isLess(t) ? (short) -1 : (short) 1;
            b = p1;
            while (p1 != y) {
                p1.bf = p1.p.isLess(t) ? (short) -1 : (short) 1;
                p1 = p1.p.isLess(t) ? (AVLTree) p1.rgt : (AVLTree) p1.lft;
            }
            if (a.bf == 0) {
                a.bf = d;
            } else if ((a.bf + d) == 0) {
                a.bf = 0;
            } else {
                if (d == 1) {
                    if (b.bf == 1) {
                        a.lft = b.rgt;
                        b.rgt = a;
                        a.bf = b.bf = 0;
                    } else {
                        c = (AVLTree) b.rgt;
                        b.rgt = c.lft;
                        a.lft = c.rgt;
                        c.lft = b;
                        c.rgt = a;
                        switch (c.bf) {
                            case 1:
                                a.bf = -1;
                                b.bf = 0;
                                break;
                            case - 1:
                                a.bf = 0;
                                b.bf = 1;
                                break;
                            case 0:
                                a.bf = b.bf = 0;
                                break;
                        }
                        c.bf = 0;
                        b = c;
                    }
                } else if (b.bf == -1) {
                    a.rgt = b.lft;
                    b.lft = a;
                    a.bf = b.bf = 0;
                } else {
                    c = (AVLTree) b.lft;
                    b.lft = c.rgt;
                    a.rgt = c.lft;
                    c.lft = a;
                    c.rgt = b;
                    switch (c.bf) {
                        case 1:
                            a.bf = 0;
                            b.bf = -1;
                            break;
                        case - 1:
                            a.bf = 1;
                            b.bf = 0;
                            break;
                        case 0:
                            a.bf = b.bf = 0;
                            break;
                    }
                    c.bf = 0;
                    b = c;
                }
                if (f == null) {
                    self = b;
                } else if (f.lft == a) {
                    f.lft = b;
                } else if (f.rgt == a) {
                    f.rgt = b;
                }
            }
        }
*/        return self;
    }
}
