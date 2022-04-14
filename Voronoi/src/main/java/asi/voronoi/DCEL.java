package asi.voronoi;
import java.util.LinkedList;

public class DCEL implements Constant, java.io.Serializable {
    DCELNode node;
    Point upLft, downLft, upRgt, downRgt;

    public DCEL() {
        node = null;
    }
    
    public DCEL(Point p) {
        node = new DCELNode(p);        
    }

    public DCEL(DCEL subVor) {
        node = subVor.node.copy();
    }

    public DCEL(Point lft, Point rgt) {
        node = new DCELNode(lft, rgt);
    }

    public DCEL(DCELNode d) {
        node = d;
    }
    
    public DCELNode getNode() {
        return node;
    }

    @Override
    public String toString() {
        if (node == null) {
            return "";
        } else {
            String ret = "Vor:\n" + node.printDCEL();
            node.resetMark();
            return ret;
        }
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
            Point start;
            if (d.node.p_b != null) {
            // left point
                start = d.node.f_l;
            } else {
                start = d.node.f_r;
            }
            Point next = start;
            ret = new ConveksHull();
            do {
                ret = ret.add(next);
                d = d.findNextCHPoint(next);
                next = d.otherPoint(next);
            } while (!next.equals(start));
        }
        return ret;
    }
    
    private void support(ConveksHull ch) throws Exception {
        String methodName = getClass().getName() + " : " + 
                            new Exception().getStackTrace()[0].getMethodName() + 
                            " : ";
        String msg;
        PointPair pp = ch.getUpSupport();
        if (pp == null) {
            msg = "3 points on a line";
            throw new Exception(methodName + msg);        
        }
        upLft = pp.getLft(); upRgt = pp.getRgt();
        pp = ch.getDownSupport();
        downLft = pp.getLft(); downRgt = pp.getRgt();
        if (upLft.equals(downLft) &&
            upRgt.equals(downRgt)) {
            msg = "4 points on a line ";
            throw new Exception(methodName + msg + upLft + " " + upRgt);        
        }         
    }
    
    private void setSupportPoints(Point p) throws Exception {
        ConveksHull c = vor2CH();
        c.merge(p);     
        support(c);
    }

    private void setSupportPoints(DCEL subVor) throws Exception {
        ConveksHull ch1 = vor2CH();
        ConveksHull ch2 = subVor.vor2CH();
        ch1.merge(ch2);
        support(ch1);
    }
    
    private boolean sizeIsOne() {
        return node.f_l.equals(node.f_r);
    }
    
    public DCEL merge(Point p) throws Exception {
        // check if DCEL consists of one point
        if (sizeIsOne()) {
            if (node.f_l.isLess(p)) {
                return new DCEL(node.f_l,p);
            } else {
                return new DCEL(p, node.f_l);
            }
        } else {
            // build initial and final edge of DCEL
            setSupportPoints(p);                
            DCEL lftNext, rgtNext;
            DCEL current = new DCEL(upLft, upRgt);
            DCEL ret = current;
            DCEL next, tmp;
            LinkedList mergeList = new LinkedList();
            DCELPair listElem;
            Point lftPoint = upLft, rgtPoint = upRgt;
            if (node.f_l.isLess(p)) // p is to the right of current vor diagram
            {
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
            } else { // p is to the left of current vor diagram
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
            }
            do {
                listElem = (DCELPair) mergeList.removeFirst();
                listElem.nextElem.adjustSigmaChain(listElem.currentElem, listElem.cutBy);
            } while (!mergeList.isEmpty());
            return ret;
        }
    }

    public DCEL merge(DCEL subVor) throws Exception {
        if (sizeIsOne()) {
        // DCEL consists of one point
            return subVor.merge(node.f_l);
        } else if (subVor.sizeIsOne()) {
        // subVor-DCEL consists of one point
            return merge(subVor.node.f_l);
        } else {
            // build initial and final edge of DCEL
            setSupportPoints(subVor);
            DCEL lftNext, rgtNext;
            Point lftPoint = upLft, rgtPoint = upRgt;
            DCEL next, tmp;
            DCEL current = new DCEL(upLft, upRgt);
            DCEL ret = current;
            LinkedList mergeList = new LinkedList();
            DCELPair listElem;
            if (node.f_l.isLess(subVor.node.f_l)) // subVor to the right to current vor
            {
                lftNext = initCell(upLft);
                rgtNext = subVor.initCell(upRgt);
            } else {
                lftNext = subVor.initCell(upLft);
                rgtNext = initCell(upRgt);
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
            } while (!mergeList.isEmpty());
            return ret;
        }
    }

    public static DCEL fetch(String filename) throws java.io.IOException, ClassNotFoundException {
        return (DCEL) Serializer.fetch(filename);
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
            } while (ret.node.edgeType() != SEMI);
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
        Point ll, rr;
        DCEL ret = this;
        String methodName = getClass().getName() + " : " + 
                            new Exception().getStackTrace()[0].getMethodName() + 
                            " : ";
        String msg;
        if ((lftEdge != null) && (rgtEdge != null)) {
            ll = lftEdge.node.f_l.equals(node.f_l) ? lftEdge.node.f_r : lftEdge.node.f_l;
            rr = rgtEdge.node.f_l.equals(node.f_r) ? rgtEdge.node.f_r : rgtEdge.node.f_l;
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
                msg = "4 points co-circular ";                
                throw new Exception(methodName + msg + 
                                    node.f_l + " " + node.f_r + " " + ll + " " + rr);        
            }
            if ((lftCut == YES) && (rgtCut == YES)) {
                if (lftCutPoint > rgtCutPoint) {
                    ret = lftEdge;
                } else if (rgtCutPoint > lftCutPoint) {
                    ret = rgtEdge;
                } else {
                msg = "Left and Right meet with current ";                
                throw new Exception(methodName + msg + 
                                    node.f_l + " " + node.f_r + " " + ll + " " + rr);        
                }
            } else if (lftCut == YES) {
                ret = lftEdge;
            } else if (rgtCut == YES) {
                ret = rgtEdge;
            } else {
                msg = "No next edge ";                
                throw new Exception(methodName + msg + 
                                    node.f_l + " " + node.f_r + " " + ll + " " + rr);        
            }
        } else if (lftEdge != null) {
                ll = lftEdge.node.f_l.equals(node.f_l) ? lftEdge.node.f_r : lftEdge.node.f_l;
            lftCut = testCut(lftEdge);
            if (lftCut == PARALLEL) {
                msg = "3 or 4 points on a line ";                
                throw new Exception(methodName + msg + 
                                    node.f_l + " " + node.f_r + " " + ll);        
            }
            if (lftCut == FOURPOINTS) {
                msg = "4 points co-circular ";                
                throw new Exception(methodName + msg + 
                                    node.f_l + " " + node.f_r + " " + ll);        
            }
            ret = lftEdge;
        } else if (rgtEdge != null) {
                rr = rgtEdge.node.f_l.equals(node.f_r) ? rgtEdge.node.f_r : rgtEdge.node.f_l;
            rgtCut = testCut(rgtEdge);
            if (rgtCut == PARALLEL) {
                msg = "3 or 4 points on a line ";                
                throw new Exception(methodName + msg + 
                                    node.f_l + " " + node.f_r + " " + rr);        
            }
            if (rgtCut == FOURPOINTS) {
                msg = "4 points co-circular ";                
                throw new Exception(methodName + msg + 
                                    node.f_l + " " + node.f_r + " " + rr);        
            }
            ret = rgtEdge;
        } else {
                msg = "Both sets are null ";                
                throw new Exception(methodName + msg + 
                                    node.f_l + " " + node.f_r);        
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
}
