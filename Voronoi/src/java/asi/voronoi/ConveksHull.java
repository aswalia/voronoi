package asi.voronoi;

public class ConveksHull implements Constant, java.io.Serializable, ModelObject {
    private LinkedList head;
    private Point upLft, downLft, upRgt, downRgt;

    public ConveksHull(ConveksHull c) {
        head = c.head.copy();
    }

    public ConveksHull() {
        head = null;
    }

    public ConveksHull(Point lft, Point rgt) {
        head = new LinkedList(lft);
        head.add(rgt);
    }

    public LinkedList getHead() {
        return head;
    }

    public void add(Point p) {
        if (head == null) {
            head = new LinkedList(p);
        } else {
            head.addFront(p);
        }
    }

    @Override
    public String toString() {
        String ret = "ConveksHull:" + "\n";
        ret += head;
        return ret;
    }

    public void merge(Point p) {
        if (head.element().isLess(p)) // p is to the left of this CH
        {
            upLft = downLft = p;
            downRgt = findUp(p);
            upRgt = findDown(p);
        } else {
            upRgt = downRgt = p;
            upLft = findUp(p);
            downLft = findDown(p);
        }
        head.mergeList(upLft, upRgt, downLft, downRgt, new LinkedList(p));
    }

    public void merge(ConveksHull subCH) {
        Point tmpLft, tmpRgt;
        if (head.element().isLess(subCH.head.element())) // subCH is to the left of this CH
        {
            do //locate bottom supporting points
            {
                tmpRgt = head.element();
                tmpLft = subCH.head.element();
                downLft = subCH.findDown(tmpRgt);
                downRgt = findUp(tmpLft);
            } while (!tmpLft.equals(downLft) || !tmpRgt.equals(downRgt));
            do // locate top supporting points
            {
                tmpRgt = head.element();
                tmpLft = subCH.head.element();
                upLft = subCH.findUp(tmpRgt);
                upRgt = findDown(tmpLft);
            } while (!tmpLft.equals(upLft) || !tmpRgt.equals(upRgt));
        } else // subCH is to the right of this
        {
            do //locate bottom supporting points
            {
                tmpRgt = subCH.head.element();
                tmpLft = head.element();
                downLft = findDown(tmpRgt);
                downRgt = subCH.findUp(tmpLft);
            } while (!tmpLft.equals(downLft) || !tmpRgt.equals(downRgt));
            do // locate top supporting points
            {
                tmpRgt = subCH.head.element();
                tmpLft = head.element();
                upLft = findUp(tmpRgt);
                upRgt = subCH.findDown(tmpLft);
            } while (!tmpLft.equals(upLft) || !tmpRgt.equals(upRgt));
        }
        head.mergeList(upLft, upRgt, downLft, downRgt, subCH.head);
    }

    public Point getUpLft() {
        return upLft;
    }

    public Point getUpRgt() {
        return upRgt;
    }

    public Point getDownLft() {
        return downLft;
    }

    public Point getDownRgt() {
        return downRgt;
    }

    @Override
    public double maxX() {
        double ret = head.element().x(), mx;
        Point last = head.previous();
        for (; !head.element().equals(last); head.front()) {
            if ((mx = head.element().x()) > ret) {
                ret = mx;
            }
        }
        if ((mx = head.element().x()) > ret) {
            ret = mx;
        }
        return ret;
    }

    @Override
    public double minX() {
        double ret = head.element().x(), mx;
        Point last = head.previous();
        for (; !head.element().equals(last); head.front()) {
            if ((mx = head.element().x()) < ret) {
                ret = mx;
            }
        }
        if ((mx = head.element().x()) < ret) {
            ret = mx;
        }
        return ret;
    }

    @Override
    public double maxY() {
        double ret = head.element().y(), mx;
        Point last = head.previous();
        for (; !head.element().equals(last); head.front()) {
            if ((mx = head.element().y()) > ret) {
                ret = mx;
            }
        }
        if ((mx = head.element().y()) > ret) {
            ret = mx;
        }
        return ret;
    }

    @Override
    public double minY() {
        double ret = head.element().y(), mx;
        Point last = head.previous();
        for (; !head.element().equals(last); head.front()) {
            if ((mx = head.element().y()) < ret) {
                ret = mx;
            }
        }
        if ((mx = head.element().y()) < ret) {
            ret = mx;
        }
        return ret;
    }

    public static ConveksHull fetch(String filename) throws java.io.IOException, ClassNotFoundException {
        return (ConveksHull) Serializer.fetch(filename);
    }

    private short test(Point p) {
        short back = Point.area(p, head.element(), head.previous());
        short front = Point.area(p, head.element(), head.next());
        short ret = (back * front >= 0) ? SUPPORT : ((back >= 0) ? REFLEX : CONCAVE);
        return ret;
    }

    private Point findUp(Point p) {
        boolean done = false;
        do {
            switch (test(p)) {
                case CONCAVE:
                    head.back();
                    break;
                case REFLEX:
                    head.front();
                    break;
                case SUPPORT:
                    switch (Point.area(p, head.element(), head.previous())) {
                        case 0:
                            if (head.next().equals(head.previous()) && (Point.direction(head.element(), head.previous(), p) < 0)) {
                                done = true;
                            } else {
                                head.back();
                            }
                            break;
                        case 1:
                            done = true;
                            break;
                        case - 1:
                            head.back();
                            break;
                    }
            }
        } while (!done);
        return head.element();
    }

    private Point findDown(Point p) {
        boolean done = false;
        do {
            switch (test(p)) {
                case CONCAVE:
                    head.front();
                    break;
                case REFLEX:
                    head.back();
                    break;
                case SUPPORT:
                    switch (Point.area(p, head.element(), head.next())) {
                        case 0:
                            if (head.next().equals(head.previous()) && (Point.direction(head.element(), head.next(), p) < 0)) {
                                done = true;
                            } else {
                                head.front();
                            }
                            break;
                        case 1:
                            head.front();
                            break;
                        case - 1:
                            done = true;
                            break;
                    }
            }
        } while (!done);
        return head.element();
    }

    static class Test {
        public static void main(String args[]) {
            /*
             * ConveksHull t = new ConveksHull(new Point(-1,0),new Point(-1,2)); ConveksHull s = new ConveksHull(new Point(2,0),new Point(2,2)); System.out.println(t.head); System.out.println(s.head);
             * s.mergePoint(new Point(1,1)); System.out.println(s.head); s.mergePoint(new Point(0,1)); System.out.println(s.head); s.mergePoint(new Point(3,1)); System.out.println(s.head);
             * s.mergeConveksHull(t); System.out.println(s.head); System.out.println("Points: "+s.upLft+s.upRgt+s.downLft+s.downRgt);
             *
             * ConveksHull a = new ConveksHull(new Point(0,4),new Point(1,0)); ConveksHull b = new ConveksHull(new Point(4,2),new Point(8,4)); ConveksHull c = new ConveksHull(new Point(11,0),new
             * Point(12,5)); ConveksHull d = new ConveksHull(new Point(15,2),new Point(19,4));
             *
             * a.mergeConveksHull(b); System.out.println("a: "+a.head); c.mergeConveksHull(d); System.out.println("c: "+c.head);
             *
             * a.mergeConveksHull(c); System.out.println("a: "+a.head);
             */

            ConveksHull a = new ConveksHull(new Point(0, 6), new Point(1, 0));
            System.out.println("a: " + a.head);
            ConveksHull b = new ConveksHull(new Point(4, 1), new Point(4, 6));
            b.merge(new Point(6, 4));
            System.out.println("b: " + b.head);
            ConveksHull c = new ConveksHull(new Point(7, 5), new Point(8, 2));
            c.merge(new Point(8, 7));
            System.out.println("c: " + c.head);
            ConveksHull d = new ConveksHull(new Point(11, 9), new Point(13, 1));
            System.out.println("d: " + d.head);
            a.merge(b);
            System.out.println("a: " + a.head);
            d.merge(c);
            System.out.println("c: " + c.head);
            d.merge(a);
            System.out.println("a: " + a.head);
            System.out.println(d);
            System.out.println("maxX: " + d.maxX());
            System.out.println("minX: " + d.minX());
            System.out.println("maxY: " + d.maxY());
            System.out.println("minY: " + d.minY());
        }
    }
}
