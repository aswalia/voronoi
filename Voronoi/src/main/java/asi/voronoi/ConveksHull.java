package asi.voronoi;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
    
    public void connect(ConveksHull prv, ConveksHull cur) {
        cur.head.connect(prv.head);
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
    
    public ConveksHull merge(Point p) {
        ConveksHull ret = this;
        if (size() == 1) {
            Point o = head.get(0);
            if (head.get(0).isLess(p)) {
                setUpSupport(o, p);
                setDownSupport(p, o);
                ret = new ConveksHull(head.get(0), p);
            } else {
                setUpSupport(p, o);
                setDownSupport(o, p);
                ret = new ConveksHull(p, head.get(0));
            }
        } else {
            // check if point makes a line with head and head-1 and head+1        
            if ((Point.area(p, head.get(0), head.get(-1)) == 0) &&
                (Point.area(p, head.get(0), head.get(1)) == 0)) {
                int i = 0;
                // if CH is a line and
                // new point also on same line
                if ((p.x() > lft.x()) &&
                    (p.x() < rgt.x())) {
                    // new point between the two edges
                    boolean done = false;
                    do {
                        if ((p.x() < head.get(i).x()) &&
                            (p.x() > head.get(i+1).x())) {
                            head.add(i, i-1, p); 
                            done = true;
                        }
                        i++;
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
                // find potential line segments where p would fit in
                int i = 0;
                Point start = head.get(i);
                Set<Integer> candidateIndex = new HashSet();
                double a;
                do {
                    a = Point.areaDouble(head.get(i), p, head.get(i+1));
                    // any head, p and head+1 line-segments in "positive" 
                    // direction is a candidate
                    if ((a > 0) || 
                        ((a == 0) && 
                         (Point.direction(head.get(i), p, head.get(i+1)) > 0))) {
                        candidateIndex.add(i);
                    }
                    i++;
                } while (!head.get(i).equals(start));
                // true line-segment is the one where head-1, head and p
                // make a conveks bend (positive or 0 area)
                for (Integer index: candidateIndex) {
                    if (Point.area(head.get(index-1), head.get(index), p) >= 0) {
                        i = index;
                    }
                }
                int j = i;
                i++;
                // find which point of current CH is to be next point in merges CH
                while (Point.area(p, head.get(i), head.get(i+1)) < 0) {
                    // current next point is not part of merged CH
                    i++;
                }
                if (rgt.isLess(p)) { // p to the rgt of CH
                    setUpSupport(head.get(i), p);
                    setDownSupport(head.get(j), p);
                } else {
                    setUpSupport(p, head.get(j));
                    setDownSupport(p, head.get(i));                
                }
                head.add(i, j, p);
            }
        }
        setPerimeter(p);
        return ret;
    }
    
    public ConveksHull merge(ConveksHull subCH) {
        ConveksHull ret = this;
        if (size() == 1) {
            return subCH.merge(head.get(0));
        } else if (subCH.size() == 1) {
            return merge(subCH.head.get(0));
        } else {
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
        return ret;
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

    public ConveksHull add(Point p) {
        // build a CH by adding 1 Point at a time
        // only the first adding of the first two points 
        // is interesting as the rest can bedone with merge
        if (head == null) {
            return new ConveksHull(p);
        } else {
            return merge(p);
        }
    }

    public static ConveksHull fetch(String filename) throws java.io.IOException, ClassNotFoundException {
        return (ConveksHull) Serializer.fetch(filename);
    }
    
    public void store(int grp, List<String> r) {
        // save ConveksHull as a list of strings
        // each item representing a row in the
        // database on ConveksHulls table
        Point start = head.get(0);
        Point prv = start;
        Point cur = head.get(1);
        Point nxt = head.get(2);
        // one point CH
        if (prv.equals(cur) && cur.equals(nxt)) {
            int index = DatabaseHandler.getIndexFromPoint(cur, grp);
            r.add("" + index + " , " + grp + " ," + index + " , " + index);
        } else if (prv.equals(nxt) && !prv.equals(cur)) {
        // two points CH
            int index = DatabaseHandler.getIndexFromPoint(cur, grp);
            int im1 = DatabaseHandler.getIndexFromPoint(prv, grp);
            r.add("" + im1 + " , " + grp + " ," + index + " , " + index);
            r.add("" + index + " , " + grp + " ," + im1 + " , " + im1);
        } else {
        // 3 or more points
            for (int i=1; i<=size(); i++) {
                int index = DatabaseHandler.getIndexFromPoint(cur, grp);
                int im1 = DatabaseHandler.getIndexFromPoint(prv, grp);
                int ip1 = DatabaseHandler.getIndexFromPoint(nxt, grp);
                r.add("" + index + " , " + grp + " ," + ip1 + " , " + im1);
                prv = cur;
                cur = nxt;
                nxt = head.get(i+2);
            }
        }
    }

    public void storeAsLinesegments(int grp, List<Properties> r) throws SQLException {
        // save ConveksHull as a list of strings
        // each item representing a row in the
        // database on ConveksHullsAsLinesegments table
        Point start = head.get(0);
        Point prv = start;
        Point cur = head.get(1);
        Point nxt = head.get(2);
        // one point CH        
        Properties rp = new Properties();
        if (prv.equals(cur) && cur.equals(nxt)) {
            int index = DatabaseHandler.insertConveksHullLinesegment(grp);
            if (index > 0) {
                int indexpoint = DatabaseHandler.getIndexFromPoint(cur, grp);
                rp.put("COLUMN:beginpoint", ""+indexpoint);
                rp.put("COLUMN:endpoint", ""+indexpoint);
                rp.put("PRIMARY:id", ""+index);
                rp.put("PRIMARY:grp", ""+grp);
                r.add(rp);
            }
        } else if (prv.equals(nxt) && !prv.equals(cur)) {
        // two points CH
            int index = DatabaseHandler.insertConveksHullLinesegment(grp);
            if (index > 0) {
                int ic = DatabaseHandler.getIndexFromPoint(cur, grp);
                int im1 = DatabaseHandler.getIndexFromPoint(prv, grp);
                rp.put("COLUMN:beginpoint", ""+im1);
                rp.put("COLUMN:endpoint", ""+ic);
                rp.put("PRIMARY:id", ""+index);
                rp.put("PRIMARY:grp", ""+grp);
                r.add(rp);
            }
        } else {
        // 3 or more points
            for (int i=1; i<=size(); i++) {
                int index = DatabaseHandler.insertConveksHullLinesegment(grp);
                if (index > 0) {
                    int ic =  DatabaseHandler.getIndexFromPoint(cur, grp);
                    int ip1 = DatabaseHandler.getIndexFromPoint(nxt, grp);
                    rp.put("COLUMN:beginpoint", ""+ic);
                    rp.put("COLUMN:endpoint", ""+ip1);
                    rp.put("PRIMARY:id", ""+index);
                    rp.put("PRIMARY:grp", ""+grp);
                    r.add(rp);
                    cur = nxt;
                    nxt = head.get(i+2);
                    rp = new Properties();
                }
            }
        }
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
