/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asi.voronoi;

/**
 *
 * @author asi
 */
public class CircularLinkedList {
    class Node {
        Point p;
        Node next, prev;
    }
    private Node head;
    
    private void connectBack(int index, Node other) {
        Node h = findNode(index);
        h.prev = other;
        other.next = h;
    }
    
    private void connectFront(int index, Node other) {
        Node h = findNode(index);
        h.next = other;
        other.prev = h ;
    }
    
    private Node findNode(int index) {
        int i = index;
        Node h = head;
        if (index < 0) {
            while (i < 0) {
                h = h.prev;
                i++;
            }
        } else {
            while (i > 0) {
                h = h.next;
                i--;
            }            
        }
        return h;
    }
    
    private void setHead() {
        // head points to the Node at the lower left point
        Node min = head;
        Node h = head.next;
        do {
            if (h.p.isLess(min.p)) {
                min = h;
            }
            h = h.next;
        } while (!h.equals(head));
        head = min;
    }
    
    private int moveTo(Point p) {
        int ret = -1;
        for(int i=0; i < length(); i++) {
            if (get(i).equals(p)) {
                ret = i;
                break;
            }
        }
        return ret;
    }
    
    public CircularLinkedList(Point p) {
        head = new Node();
        head.p = p;
        head.next = head.prev = head;
    }
    
    @Override
    public String toString() {
        String ret = head.p.toString();
        Node next = head.next;
        while (!next.p.equals(head.p)) {
            ret += next.p.toString();
            next = next.next;
        }
        return ret;
    }
    
    public Point get(int index) {
        return findNode(index).p;
    }

    public void add(int addFront, int addBack, Point p) {
        Node tmp = new Node();
        tmp.p = p;
        Node hFront = findNode(addFront);
        Node hBack = findNode(addBack);
        // remove all nodes between addBack and addFront
        while (!hBack.next.p.equals(hFront.p)) {
            int i = moveTo(hBack.next.p);
            remove(i);
        }
        tmp.next = hFront;
        hBack.next = tmp;
        tmp.prev = hBack;
        hFront.prev = tmp;
        // ensure that lower lft point is made "head"
        setHead();
    }
    
    public void remove(int index) {
        Node h = findNode(index);
        Node p = h.prev;
        Node n = h.next;
        p.next = n;
        n.prev = p;
        if (h.equals(head)) {
            // if removing Node pointed by head
            head = head.next;
        }
        h = null;
        // ensure that lower lft point is made "head"
        setHead();
    }
    
    public CircularLinkedList copy() {
        CircularLinkedList ret = new CircularLinkedList(head.p);
        Node h = head.next;
        while (!h.equals(head)) {
           ret.add(0, -1, h.p);
           h = h.next;
        }
        return ret;
    }
    
    public int length() {
        Node h = head;
        int ret = 0;
        do {
            ret++;
            h = h.next;
        } while (!h.equals(head));
        return ret;
    }
    
    public void mergeLinearCH(boolean subCHRgt, CircularLinkedList subList) {
        Node chLast = head.next;
        Node subListLast = subList.head.next;
        if (subCHRgt) // sublist to the right of this
        {
            head.next = subListLast;
            subListLast.prev = head;
            subList.head.next = chLast;
            chLast.prev = subList.head;
        } else { // sublist to the left of this
            subList.head.next = chLast;
            chLast.prev = subList.head;
            subListLast.prev = head;
            head.next = subListLast;
            // set head
            head = subList.head;
        }        
    }

    public void mergeList(Point upLft, Point upRgt, Point downLft, Point downRgt, CircularLinkedList subList) {
        int tmpUpLft, tmpUpRgt, tmpDownLft, tmpDownRgt;
        Node otherUp, otherDown;
        if (head.p.isLess(subList.head.p)) // sublist to the right of this
        {
            tmpUpLft = moveTo(upLft);
            tmpDownLft = moveTo(downLft);
            tmpUpRgt = subList.moveTo(upRgt);
            tmpDownRgt = subList.moveTo(downRgt);
            otherUp = subList.findNode(tmpUpRgt);
            otherDown = subList.findNode(tmpDownRgt);
            connectBack(tmpUpLft,otherUp);
            connectFront(tmpDownLft,otherDown);
        } else { // sublist to the left of this
            tmpUpLft = subList.moveTo(upLft);
            tmpDownLft = subList.moveTo(downLft);
            tmpUpRgt = moveTo(upRgt);
            tmpDownRgt = moveTo(downRgt);
            otherUp = subList.findNode(tmpUpLft);
            otherDown = subList.findNode(tmpDownLft);
            connectBack(tmpDownRgt,otherDown);
            connectFront(tmpUpRgt,otherUp);
        }
        setHead();
    }

}
