package asi.voronoi;

public class LinkedList implements java.io.Serializable {
    class ListNode {
        Point p;
        ListNode front, back;

        ListNode(Point p) {
            this.p = p;
            front = back = this;
        }

        void addNode(ListNode t) {
            ListNode last = this.front;
            this.front = t;
            t.front = last;
            t.back = this;
            last.back = t;
        }

        void connectBack(ListNode t) {
            this.back = t;
            t.front = this;
        }

        void connectFront(ListNode t) {
            this.front = t;
            t.back = this;
        }

        @Override
        public String toString() {
            return "Point: " + p + "\n";
        }
    }
    private ListNode head;

    public LinkedList(Point p) {
        head = new ListNode(p);
    }

    @Override
    public String toString() {
        ListNode tmp = head;
        ListNode last = head.back;
        String ret = head.toString();
        while (tmp != last) {
            tmp = tmp.front;
            ret += tmp;
        }
        return ret;
    }

    public void add(Point p) {
        ListNode tmp = new ListNode(p);
        head.addNode(tmp);
    }

    public void addFront(Point p) {
        ListNode tmp = new ListNode(p);
        head.addNode(tmp);
        head = tmp;
    }

    private ListNode moveTo(Point p) {
        while (!head.p.equals(p)) {
            head = head.front;
        }
        return head;
    }

    public void mergeList(Point upLft, Point upRgt, Point downLft, Point downRgt, LinkedList subList) {
        ListNode tmpUpLft, tmpUpRgt, tmpDownLft, tmpDownRgt;
        if (this.element().isLess(subList.element())) // sublist to the left of this
        {
            tmpUpLft = subList.moveTo(upLft);
            tmpDownLft = subList.moveTo(downLft);
            tmpUpRgt = moveTo(upRgt);
            tmpDownRgt = moveTo(downRgt);
        } else {
            tmpUpLft = moveTo(upLft);
            tmpDownLft = moveTo(downLft);
            tmpUpRgt = subList.moveTo(upRgt);
            tmpDownRgt = subList.moveTo(downRgt);
        }
        tmpUpLft.connectBack(tmpUpRgt);
        tmpDownLft.connectFront(tmpDownRgt);
    }

    public void remove(Point next, Point stop) {
        ListNode tmp1 = head;
        ListNode tmp2;
        while (!tmp1.p.equals(next)) {
            tmp1 = tmp1.front;
        }
        tmp2 = tmp1 = tmp1.back;
        while (!tmp2.p.equals(stop)) {
            tmp2 = tmp2.front;
        }
        tmp1.front = tmp2;
        tmp2.back = tmp1;
    }

    public LinkedList copy() {
        LinkedList ret = new LinkedList(head.p);
        ListNode tmp = head.back;
        while (tmp != head) {
            ret.add(tmp.p);
            tmp = tmp.back;
        }
        return ret;
    }

    public void front() {
        head = head.front;
    }

    public void back() {
        head = head.back;
    }

    public Point element() {
        return head.p;
    }

    public Point previous() {
        return head.back.p;
    }

    public Point next() {
        return head.front.p;
    }

    static class Test {
        public static void main(String agv[]) {
            LinkedList t = new LinkedList(new Point(0, 0));
            LinkedList s;
            t.add(new Point(0, 2));
            t.add(new Point(1, 1));
            t.add(new Point(2, 0));
            t.add(new Point(2, 2));
            System.out.println(t);
            t.front();
            System.out.println(t);
            t.remove(new Point(1, 1), new Point(0, 0));
            System.out.println(t);
            s = t.copy();
            System.out.println(s);
        }
    }
}
