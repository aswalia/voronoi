package asi.voronoi;

public class DCEL implements Constant, java.io.Serializable {
    DCELNode node;
    Point upLft, downLft, upRgt, downRgt;

    public DCEL() {
        node = null;
    }

    public DCEL(DCEL subVor) {
        node = subVor.node.copy();
    }

    public DCEL(Point lft, Point rgt) {
        node = new DCELNode(lft, rgt);
    }

    DCEL(DCELNode d) {
        node = d;
    }

    @Override
    public String toString() {
        String ret = "Vor:\n" + node.printDCEL();
        node.resetMark();
        return ret;
    }

    public void toFile() throws Exception {
        node.vor2file();
        node.resetMark();
    }

    public ConveksHull vor2CH() {
        ConveksHull ret;
        if (node.edgeType() == ENDLESS) {
            ret = new ConveksHull(node.f_l, node.f_r);
        } else {
            DCEL d = findOuterEdge();
            Point start = d.node.f_l;
            Point next = start;
            ret = new ConveksHull();
            do {
                ret.add(next);
                d = d.findNextCHPoint(next);
                next = d.otherPoint(next);
            } while (!next.equals(start));
        }
        return ret;
    }

    public DCEL merge(Point p) throws Exception {
        DCEL lftNext, rgtNext;
        DCEL current = new DCEL(upLft, upRgt);
        DCEL ret = current;
        DCEL next, tmp;
        java.util.LinkedList mergeList = new java.util.LinkedList();
        DCELPair listElem;
        Point lftPoint = upLft, rgtPoint = upRgt;
        if (node.f_l.isLess(p)) // p is to the left of current vor diagram
        {
            rgtNext = initCell(upRgt);
            do {
                rgtNext = rgtNext.initEdge(rgtPoint, false);
                rgtNext = rgtNext.trackSet(current, false);
                next = current.findNextEdge(null, rgtNext);
                rgtPoint = next.otherPoint(rgtPoint);
                tmp = current;
                current = (new DCEL(lftPoint, rgtPoint)).createSigmaChain(current, next);
                mergeList.addLast(new DCELPair(tmp, next, current));
            } while (!rgtPoint.equals(downRgt));
        } else {
            lftNext = initCell(upLft);
            do {
                lftNext = lftNext.initEdge(lftPoint, true);
                lftNext = lftNext.trackSet(current, true);
                next = current.findNextEdge(lftNext, null);
                lftPoint = next.otherPoint(lftPoint);
                tmp = current;
                current = (new DCEL(lftPoint, rgtPoint)).createSigmaChain(current, next);
                mergeList.addLast(new DCELPair(tmp, next, current));
            } while (!lftPoint.equals(downLft));
        }
        do {
            listElem = (DCELPair) mergeList.removeFirst();
            listElem.nextElem.adjustSigmaChain(listElem.currentElem, listElem.cutBy);
        } while (mergeList.size() > 0);
        return ret;
    }

    public DCEL merge(DCEL subVor) throws Exception {
        DCEL lftNext, rgtNext;
        Point lftPoint = upLft, rgtPoint = upRgt;
        DCEL next, tmp;
        DCEL current = new DCEL(upLft, upRgt);
        DCEL ret = current;
        java.util.LinkedList mergeList = new java.util.LinkedList();
        DCELPair listElem;
        if (node.f_l.isLess(subVor.node.f_l)) // subVor to the left to current vor
        {
            lftNext = subVor.initCell(upLft);
            rgtNext = initCell(upRgt);
        } else {
            lftNext = initCell(upLft);
            rgtNext = subVor.initCell(upRgt);
        }
        lftNext = lftNext.initEdge(lftPoint, true);
        rgtNext = rgtNext.initEdge(rgtPoint, false);
        do {
            lftNext = lftNext.trackSet(current, true);
            rgtNext = rgtNext.trackSet(current, false);
            next = current.findNextEdge(lftNext, rgtNext);
            if (next == lftNext) {
                lftPoint = next.otherPoint(lftPoint);
                lftNext = lftNext.initEdge(lftPoint, true);
            } else {
                rgtPoint = next.otherPoint(rgtPoint);
                rgtNext = rgtNext.initEdge(rgtPoint, false);
            }
            tmp = current;
            current = (new DCEL(lftPoint, rgtPoint)).createSigmaChain(current, next);
            mergeList.addLast(new DCELPair(tmp, next, current));
        } while (!lftPoint.equals(downLft) || !rgtPoint.equals(downRgt));
        do {
            listElem = (DCELPair) mergeList.removeFirst();
            listElem.nextElem.adjustSigmaChain(listElem.currentElem, listElem.cutBy);
        } while (mergeList.size() > 0);
        return ret;
    }

    public static DCEL fetch(String filename) throws java.io.IOException, ClassNotFoundException {
        return (DCEL) Serializer.fetch(filename);
    }

    void setUpLft(Point p) {
        upLft = p;
    }

    void setUpRgt(Point p) {
        upRgt = p;
    }

    void setDownLft(Point p) {
        downLft = p;
    }

    void setDownRgt(Point p) {
        downRgt = p;
    }

    private Point otherPoint(Point p) {
        Point ret;
        if (node.f_l.equals(p)) {
            ret = node.f_r;
        } else if (node.f_r.equals(p)) {
            ret = node.f_l;
        } else {
            ret = p;
        }
        return ret;
    }

    private DCEL findOuterEdge() {
        DCEL ret = this;
        if (node.edgeType() == CLOSED) {
            do {
                ret = ret.node.p_b;
            } while (ret.node.edgeType() == SEMI);
        }
        return ret;
    }

    private DCEL findNextCHPoint(Point p) {
        DCEL ret = this;
        do {
            ret = ret.positivDir(p);
        } while (ret.node.edgeType() == CLOSED);
        return ret;
    }

    private DCEL positivDir(Point p) {
        short e;
        DCEL ret = null;
        e = node.edgeType();
        switch (e) {
            case ENDLESS:
                ret = this;
                break;
            case SEMI:
                if (node.p_b != null) {
                    ret = node.p_b;
                } else {
                    ret = node.p_e;
                }
                break;
            case CLOSED:
                if (node.p_e.node.samePoint(p)) {
                    ret = node.p_e;
                } else {
                    ret = node.p_b;
                }
                break;
        }
        return ret;
    }

    private DCEL negetivDir(Point p) {
        short e;
        DCEL ret = null;
        e = node.edgeType();
        switch (e) {
            case ENDLESS:
                ret = this;
                break;
            case SEMI:
                if (node.p_b != null) {
                    ret = node.p_b.positivDir(p);
                } else {
                    ret = node.p_e.positivDir(p);
                }
                break;
            case CLOSED:
                if (node.p_b.node.samePoint(p)) {
                    ret = node.p_e.positivDir(p);
                } else {
                    ret = node.p_b.positivDir(p);
                }
                break;
        }
        return ret;
    }

    private short testCut(DCEL next) {
        Point l;
        boolean nCut, cCut, is4Point;
        short ret;
        nCut = cCut = is4Point = false;
        l = next.node.cutPoint(node);
        if (!l.equals(ZERO)) {
            switch (next.node.edgeType()) {
                case ENDLESS:
                    nCut = true;
                    break;
                case SEMI:
                    if (next.node.p_b != null) {
                        is4Point = next.node.a_b == l.x();
                        nCut = l.x() > next.node.a_b;
                    } else {
                        is4Point = next.node.a_e == l.x();
                        nCut = l.x() < next.node.a_e;
                    }
                    break;
                case CLOSED:
                    is4Point = (l.x() == next.node.a_b) || (l.x() == next.node.a_e);
                    nCut = (l.x() > next.node.a_b) && (l.x() < next.node.a_e);
                    break;
            }
            switch (node.edgeType()) {
                case ENDLESS:
                    cCut = true;
                    break;
                case SEMI:
                    is4Point &= l.y() < node.a_e;
                    cCut = l.y() < node.a_e;
                    break;
            }
            if (is4Point) {
                ret = FOURPOINTS;
            } else if (nCut && cCut) {
                ret = YES;
            } else {
                ret = NO;
            }
        } else {
            ret = PARALLEL;
        }
        return ret;
    }

    private DCEL createSigmaChain(DCEL edge1, DCEL edge2) {
        Point q = edge1.node.cutPoint(edge2.node);
        Point p = edge2.node.cutPoint(node);
        edge1.node.a_b = q.x();
        node.a_e = p.y();
        edge1.node.p_b = this;
        node.p_e = edge1;
        return this;
    }

    private DCEL adjustSigmaChain(DCEL edge1, DCEL edge2) {
        Point q = edge1.node.cutPoint(edge2.node);
        Point p = edge2.node.cutPoint(node);
        edge1.node.a_b = q.x();
        node.a_e = p.y();
        if (edge2.node.samePoint(edge1.node.f_l)) {
            if (edge2.node.f_l.equals(edge1.node.f_l)) {
                edge2.node.a_e = q.y();
                edge1.node.p_b = edge2;
                edge2.node.p_e = this;
                node.p_e = edge1;
            } else {
                edge2.node.a_b = q.y();
                edge1.node.p_b = edge2;
                edge2.node.p_b = this;
                node.p_e = edge1;
            }
        } else if (edge2.node.f_r.equals(edge1.node.f_r)) {
            edge2.node.a_e = q.y();
            edge1.node.p_b = this;
            node.p_e = edge2;
            edge2.node.p_e = edge1;
        } else {
            edge2.node.a_b = q.y();
            edge1.node.p_b = this;
            node.p_e = edge2;
            edge2.node.p_b = edge1;
        }
        return this;
    }

    private DCEL trackSet(DCEL edge, boolean left) {
        DCEL ret = this;
        if (left) {
            while ((edge.testCut(ret) == NO)
                    && (((ret.positivDir(edge.node.f_l)).node.samePoint(edge.node.f_l)
                    && (ret.node.edgeType() == SEMI))
                    || (ret.node.edgeType() == CLOSED))) {
                ret = ret.positivDir(edge.node.f_l);
                if (ret == this) {
                    break;
                }
            }
        } else {
            while ((edge.testCut(ret) == NO)
                    && ((!(ret.positivDir(edge.node.f_r)).node.samePoint(edge.node.f_r)
                    && (ret.node.edgeType() == SEMI))
                    || (ret.node.edgeType() == CLOSED))) {
                ret = ret.negetivDir(edge.node.f_r);
                if (ret == this) {
                    break;
                }
            }
        }
        if (edge.testCut(ret) == NO) // no candidate; reset
        {
            ret = this;
        }
        return ret;
    }

    private DCEL findNextEdge(DCEL lftEdge, DCEL rgtEdge) throws Exception {
        short lftCut = NO, rgtCut = NO;
        double lftCutPoint, rgtCutPoint;
        Point l, r;
        DCEL ret = this;
        if ((lftEdge != null) && (rgtEdge != null)) {
            lftCut = testCut(lftEdge);
            rgtCut = testCut(rgtEdge);
            l = lftEdge.node.cutPoint(node);
            r = rgtEdge.node.cutPoint(node);
            lftCutPoint = ((lftEdge.node.d.mult(l.x())).add(lftEdge.node.p)).y();
            rgtCutPoint = ((rgtEdge.node.d.mult(r.x())).add(rgtEdge.node.p)).y();
            if (((lftCut == FOURPOINTS) && (rgtCut == YES) && (lftCutPoint >= rgtCutPoint))
                    || ((rgtCut == FOURPOINTS) && (lftCut == YES) && (rgtCutPoint >= lftCutPoint))
                    || ((lftCut == FOURPOINTS) && (rgtCut != YES))
                    || ((rgtCut == FOURPOINTS) && (lftCut != YES))) {
                throw new Exception("4 points co-circular " + node.f_l + " " + node.f_r);
            }
            if ((lftCut == YES) && (rgtCut == YES)) {
                if (lftCutPoint > rgtCutPoint) {
                    ret = lftEdge;
                } else if (rgtCutPoint > lftCutPoint) {
                    ret = rgtEdge;
                } else {
                    throw new Exception("Left and Right meet with current " + node.f_l + " " + node.f_r);
                }
            } else if (lftCut == YES) {
                ret = lftEdge;
            } else if (rgtCut == YES) {
                ret = rgtEdge;
            } else {
                throw new Exception("No next edge " + node.f_l + " " + node.f_r);
            }
        } else if (lftEdge != null) {
            lftCut = testCut(lftEdge);
            ret = lftEdge;
        } else if (rgtEdge != null) {
            rgtCut = testCut(rgtEdge);
            ret = rgtEdge;
        } else {
            throw new Exception("Both sets are null " + node.f_l + " " + node.f_r);
        }
        if (((lftCut == PARALLEL) && (rgtCut != YES)) || ((rgtCut == PARALLEL) && (lftCut != YES))) {
            throw new Exception("3 or 4 points on a line " + node.f_l + " " + node.f_r);
        }
        return ret;
    }

    private DCEL initCell(Point cellPoint) {
        DCEL ret = findOuterEdge();
        Point vorPoint = ret.node.f_l; // random choice;
        while (!ret.node.samePoint(cellPoint)) {
            if (ret.node.edgeType() == SEMI) {
                vorPoint = ret.otherPoint(vorPoint);
            }
            ret = ret.findNextCHPoint(vorPoint);
        }
        return ret;
    }

    private DCEL initEdge(Point cellPoint, boolean left) {
        DCEL ret = this, start = this;
        if (node.edgeType() != ENDLESS) {
            if (left) {
                while (((ret.node.edgeType() == SEMI) && !(ret.positivDir(cellPoint)).node.samePoint(cellPoint))
                        || ((ret.node.edgeType() == CLOSED) && (ret.negetivDir(cellPoint) != start))) {
                    ret = ret.negetivDir(cellPoint);
                }
            } else {
                while (((ret.node.edgeType() == SEMI) && (ret.positivDir(cellPoint)).node.samePoint(cellPoint))
                        || ((ret.node.edgeType() == CLOSED) && (ret.positivDir(cellPoint) != start))) {
                    ret = ret.positivDir(cellPoint);
                }
            }
        }
        return ret;
    }

    static class Test {
        public static void main(String[] args) throws Exception {
            DCEL a1 = new DCEL(new Point(4, 19), new Point(5, 9));
            ConveksHull aa1 = new ConveksHull(new Point(4, 19), new Point(5, 9));
            aa1.merge(new Point(4, 0));
            a1.setUpLft(aa1.getUpLft());
            a1.setUpRgt(aa1.getUpRgt());
            a1.setDownLft(aa1.getDownLft());
            a1.setDownRgt(aa1.getDownRgt());
            a1 = a1.merge(new Point(4, 0));
            aa1.merge(new Point(1, 14));
            a1.setUpLft(aa1.getUpLft());
            a1.setUpRgt(aa1.getUpRgt());
            a1.setDownLft(aa1.getDownLft());
            a1.setDownRgt(aa1.getDownRgt());
            a1 = a1.merge(new Point(1, 14));
            System.out.println("a1: " + a1);
            DCEL b1 = new DCEL(new Point(10, 12), new Point(10, 20));
            ConveksHull bb1 = new ConveksHull(new Point(10, 12), new Point(10, 20));
            bb1.merge(new Point(8, 5));
            b1.setUpLft(bb1.getUpLft());
            b1.setUpRgt(bb1.getUpRgt());
            b1.setDownLft(bb1.getDownLft());
            b1.setDownRgt(bb1.getDownRgt());
            b1 = b1.merge(new Point(8, 5));
            DCEL b2 = new DCEL(new Point(11, 6), new Point(12, 16));
            ConveksHull bb2 = new ConveksHull(new Point(11, 6), new Point(12, 16));
            bb1.merge(bb2);
            b1.setUpLft(bb1.getUpLft());
            b1.setUpRgt(bb1.getUpRgt());
            b1.setDownLft(bb1.getDownLft());
            b1.setDownRgt(bb1.getDownRgt());
            b1 = b1.merge(b2);
            bb1.merge(new Point(8, 0));
            b1.setUpLft(bb1.getUpLft());
            b1.setUpRgt(bb1.getUpRgt());
            b1.setDownLft(bb1.getDownLft());
            b1.setDownRgt(bb1.getDownRgt());
            b1 = b1.merge(new Point(8, 0));
            System.out.println("b1: " + b1);
            aa1.merge(bb1);
            a1.setUpLft(aa1.getUpLft());
            a1.setUpRgt(aa1.getUpRgt());
            a1.setDownLft(aa1.getDownLft());
            a1.setDownRgt(aa1.getDownRgt());
            a1 = a1.merge(b1);
            System.out.println("merge: " + a1);

            DCEL c1 = new DCEL(new Point(16, 12), new Point(17, 17));
            ConveksHull cc1 = new ConveksHull(new Point(16, 12), new Point(17, 17));
            DCEL c2 = new DCEL(new Point(18, 19), new Point(18, 23));
            ConveksHull cc2 = new ConveksHull(new Point(18, 19), new Point(18, 23));
            cc1.merge(cc2);
            c1.setUpLft(cc1.getUpLft());
            c1.setUpRgt(cc1.getUpRgt());
            c1.setDownLft(cc1.getDownLft());
            c1.setDownRgt(cc1.getDownRgt());
            c1 = c1.merge(c2);
            cc1.merge(new Point(19, 13));
            c1.setUpLft(cc1.getUpLft());
            c1.setUpRgt(cc1.getUpRgt());
            c1.setDownLft(cc1.getDownLft());
            c1.setDownRgt(cc1.getDownRgt());
            c1 = c1.merge(new Point(19, 13));
            System.out.println("c1: " + c1);
            DCEL d1 = new DCEL(new Point(19, 16), new Point(22, 7));
            ConveksHull dd1 = new ConveksHull(new Point(19, 16), new Point(22, 7));
            DCEL d2 = new DCEL(new Point(22, 12), new Point(22, 16));
            ConveksHull dd2 = new ConveksHull(new Point(22, 12), new Point(22, 16));
            dd1.merge(dd2);
            d1.setUpLft(dd1.getUpLft());
            d1.setUpRgt(dd1.getUpRgt());
            d1.setDownLft(dd1.getDownLft());
            d1.setDownRgt(dd1.getDownRgt());
            d1 = d1.merge(d2);
            System.out.println("d1: " + d1);
            cc1.merge(dd1);
            c1.setUpLft(cc1.getUpLft());
            c1.setUpRgt(cc1.getUpRgt());
            c1.setDownLft(cc1.getDownLft());
            c1.setDownRgt(cc1.getDownRgt());
            c1 = c1.merge(d1);
            System.out.println("c1: " + c1);
            cc1.merge(new Point(15, 9));
            c1.setUpLft(cc1.getUpLft());
            c1.setUpRgt(cc1.getUpRgt());
            c1.setDownLft(cc1.getDownLft());
            c1.setDownRgt(cc1.getDownRgt());
            c1 = c1.merge(new Point(15, 9));
            System.out.println("merge: " + c1);

            aa1.merge(cc1);
            a1.setUpLft(aa1.getUpLft());
            a1.setUpRgt(aa1.getUpRgt());
            a1.setDownLft(aa1.getDownLft());
            a1.setDownRgt(aa1.getDownRgt());
            a1 = a1.merge(c1);
            System.out.println("merge: " + a1);
        }
    }
}
