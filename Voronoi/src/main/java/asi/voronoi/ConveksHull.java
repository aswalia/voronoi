package asi.voronoi;

public class ConveksHull implements Constant, java.io.Serializable, ModelObject {
    private CircularLinkedList head;
    private Point lft, up, rgt, down;
    private PointPair upSupport, downSupport;

    public ConveksHull(ConveksHull c) {
        head = c.head.copy();
        lft = c.getLft(); rgt = c.getRgt();
        up = c.getUp(); down = c.getDown();
    }

    public ConveksHull() {
    }
    
    public ConveksHull(Point p) {
        head = new CircularLinkedList(p);
        lft = rgt = up = down = p;
    }
    
    public ConveksHull(Point lft, Point rgt) {       
        head = new CircularLinkedList(lft);
        head.add(0, -1, rgt);
        this.lft = lft;
        this.rgt = rgt;
        if (lft.y() >= rgt.y()) {
            up = lft;
            down = rgt;
        } else {
            up = rgt;
            down = lft;
        }
    }
    
    public int size() {
        return (head == null) ? 0 : head.length(); 
    }

    @Override
    public String toString() {
        String ret = "ConveksHull:" + "\n";
        ret += head+"\n";
        ret += "lft:" + lft + " rgt:" + rgt + " up:" + up + " down:" + down+"\n";
        return ret;
    }
    
    private void setPerimeter(Point p) {
        if (p.isLess(lft)) {
            lft = p;
        } else if (rgt.isLess(p)) {
            rgt = p;
        }
        if (up.y() < p.y()) {
            up = p;
        } else if (p.y() < down.y()) {
            down = p;
        }
        
    }

    public void setUpSupport(Point lft, Point rgt) {
        upSupport = new PointPair(lft,rgt);
    }
    
    public void setDownSupport(Point lft, Point rgt) {
        downSupport = new PointPair(lft,rgt);
    }
    
    public PointPair getUpSupport() {
        return upSupport;
    }
    
    public PointPair getDownSupport() {
        return downSupport;
    }
    
    public void merge(Point p) {
        int index=0;
        if (Point.area(p, head.get(0), head.get(1)) == 0) {
            // if CH is a line and
            // new point also on same line
            if ((p.x() > lft.x()) &&
                (p.x() < rgt.x())) {
                // new point between the two edges
                boolean done = false;
                do {
                    if ((p.x() < head.get(index).x()) &&
                        (p.x() > head.get(index+1).x())) {
                        head.add(index, index-1, p); 
                        done = true;
                    }
                    index++;
                } while (!done);
            }  else {
                // new point either to the left of head or
                // to the right of head.next.
                if (p.x() < lft.x()) {
                    lft = up = p;
                    rgt = down = head.get(-1);
                }  else {
                    lft = up = head.get(0);
                    rgt = down = p;
                }
                head.add(0, -1, p);
            }
        } else {
            if (p.isLess(head.get(-1)) && head.get(0).isLess(p) && 
                (Point.area(p, head.get(0), head.get(-1)) == 0)) {
                // new point in between, add it between head and -1
                head.add(0, -1, p);
            } else {
            boolean doneFront, doneBack;
            doneFront = doneBack = false;
            int addFront, addBack;
            addFront = addBack = index;
            do {
                if ((Point.area(p, head.get(index), head.get(index+1)) >= 0) &&
                    (Point.area(p, head.get(index), head.get(index-1)) > 0)) {
                    addFront = index;
                    doneFront = true;
                }
                if ((Point.area(p, head.get(index), head.get(index+1)) < 0) &&
                    (Point.area(p, head.get(index), head.get(index-1)) <= 0)) {
                    addBack = index;
                    doneBack = true;
                }
                index++;
            } while(!doneFront || !doneBack);
            if (rgt.isLess(p)) { // p to the rgt of CH
                setUpSupport(head.get(addFront), p);
                setDownSupport(head.get(addBack), p);
            } else {
                setUpSupport(p, head.get(addBack));
                setDownSupport(p, head.get(addFront));                
            }
            head.add(addFront, addBack, p);
        }
        }
        setPerimeter(p);
    }
    
    public void merge(ConveksHull subCH) {
        Point tmpUpLft, tmpDownLft, tmpUpRgt, tmpDownRgt, upLft, upRgt, downLft, downRgt;
        int index = 0;
        if (head.get(index).isLess(subCH.head.get(index))) // subCH is to the right of this CH
        {
            tmpUpRgt = tmpDownRgt = subCH.head.get(0);
            tmpUpLft = tmpDownLft = head.get(0);
            boolean done;
            do {
                downLft = findDown(tmpDownRgt);
                downRgt = subCH.findUp(tmpDownLft);
                upLft = findUp(tmpUpRgt);
                upRgt = subCH.findDown(tmpUpLft);
                done = true;
                if (!tmpUpLft.equals(upLft)) {
                    tmpUpLft = upLft;
                    done = false;
                }
                if (!tmpUpRgt.equals(upRgt)) {
                    tmpUpRgt = upRgt;
                    done = false;
                }
                if (!tmpDownLft.equals(downLft)) {
                    tmpDownLft = downLft;
                    done = false;
                }
                if (!tmpDownRgt.equals(downRgt)) {
                    tmpDownRgt = downRgt;
                    done = false;
                }
            } while (!done);
                
        } else // subCH is to the left of this
        {
            tmpUpLft = tmpDownLft = subCH.head.get(0);
            tmpUpRgt = tmpDownRgt = head.get(0);
            boolean done;
            do {
                downRgt = findUp(tmpDownLft);
                downLft = subCH.findDown(tmpDownRgt);
                upRgt = findDown(tmpUpLft);
                upLft = subCH.findUp(tmpUpRgt);
                done = true;
                if (!tmpUpLft.equals(upLft)) {
                    tmpUpLft = upLft;
                    done = false;
                }
                if (!tmpUpRgt.equals(upRgt)) {
                    tmpUpRgt = upRgt;
                    done = false;
                }
                if (!tmpDownLft.equals(downLft)) {
                    tmpDownLft = downLft;
                    done = false;
                }
                if (!tmpDownRgt.equals(downRgt)) {
                    tmpDownRgt = downRgt;
                    done = false;
                }
            } while (!done);
        }
        setUpSupport(upLft, upRgt);
        setDownSupport(downLft, downRgt);
        if (upLft.equals(downLft) && upRgt.equals(downRgt)) {
            // CH and subCH on a line
            if (head.get(0).isLess(subCH.head.get(0))) {
                head.mergeLinearCH(true, subCH.head);
            } else {
                head.mergeLinearCH(false, subCH.head);                
            }
        } else {
            head.mergeList(upLft, upRgt, downLft, downRgt, subCH.head);            
        }
        setPerimeter(subCH.lft);
        setPerimeter(subCH.rgt);
        setPerimeter(subCH.up);
        setPerimeter(subCH.down);
        subCH.head = null;
    }

    public Point getLft() {
        return lft;
    }

    public Point getRgt() {
        return rgt;
    }

    public Point getUp() {
        return up;
    }

    public Point getDown() {
        return down;
    }

    @Override
    public double maxX() {
        return rgt.x();
    }

    @Override
    public double minX() {
        return lft.x();
    }

    @Override
    public double maxY() {
        return up.y();
    }

    @Override
    public double minY() {
        return down.y();
    }

    public CircularLinkedList getHead() {
        return head;
    }

    public void add(Point p) {
        // build a CH by adding 1 Point at a time
        // only the first adding of the first two points 
        // is interesting as the rest can bedone with merge
        if (head == null) {
            head = new CircularLinkedList(p);
            lft = rgt = up = down = p;
        } else {
            merge(p);
        }
    }

    public static ConveksHull fetch(String filename) throws java.io.IOException, ClassNotFoundException {
        return (ConveksHull) Serializer.fetch(filename);
    }

    private short test(int index, Point p) {
        short back = Point.area(p, head.get(index), head.get(index-1));
        short front = Point.area(p, head.get(index), head.get(index+1));
        short ret = (back * front >= 0) ? SUPPORT : ((back >= 0) ? REFLEX : CONCAVE);
        return ret;
    }

    private Point findUp(Point p) {
        int i = 0;
        boolean done = false;
        do {
            switch (test(i,p)) {
                case CONCAVE:
                    i--;
                    break;
                case REFLEX:
                    i++;
                    break;
                case SUPPORT:
                    switch (Point.area(p, head.get(i), head.get(i-1))) {
                        case 0:
                            if (head.get(i+1).equals(head.get(i-1)) && (Point.direction(head.get(i), head.get(i-1), p) < 0)) {
                                done = true;
                            } else {
                                i--;
                            }
                            break;
                        case 1:
                            done = true;
                            break;
                        case - 1:
                            i--;
                            break;
                    }
            }
        } while (!done);
        return head.get(i);
    }

    private Point findDown(Point p) {
        int i = 0;
        boolean done = false;
        do {
            switch (test(i,p)) {
                case CONCAVE:
                    i++;
                    break;
                case REFLEX:
                    i--;
                    break;
                case SUPPORT:
                    switch (Point.area(p, head.get(i), head.get(i+1))) {
                        case 0:
                            if (head.get(i+1).equals(head.get(i-1)) && (Point.direction(head.get(i), head.get(i+1), p) < 0)) {
                                done = true;
                            } else {
                                i++;
                            }
                            break;
                        case 1:
                            i++;
                            break;
                        case - 1:
                            done = true;
                            break;
                    }
            }
        } while (!done);
        return head.get(i);
    }
}
