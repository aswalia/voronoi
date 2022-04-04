package asi.voronoi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DCELNode implements Constant, java.io.Serializable {
    private static FileWriter fw;
    private static BufferedWriter bw;
    double a_b, a_e;
    Point f_l, f_r, p, d;
    boolean used;
    DCEL p_e, p_b;

    private static void initFile() throws java.io.IOException {
        String dateString = (new SimpleDateFormat("MMddhhmm")).format(new Date());
        String fileName = "src/main/resources/output/v" + dateString + ".txt";
        System.out.println("FileName: " + fileName);
        fw = new FileWriter(fileName);
        bw = new BufferedWriter(fw);
    }

    @Override
    public String toString() {
        String ret;
        Point top = null, bottom = null;
        double t = Math.sqrt((Math.pow(f_l.x() - p.x(), 2) + Math.pow(f_l.y() - p.y(), 2)) / (Math.pow(d.x(), 2) + Math.pow(d.y(), 2)));
        if ((p_b != null) && (p_e != null)) {
            top = Point.coordinat(d, p, a_e);
            bottom = Point.coordinat(d, p, a_b);
        } else if (p_b != null) {
            if (p.x() < 0) {
                t = -t;
            }
            top = Point.coordinat(d, p, t);
            bottom = Point.coordinat(d, p, a_b);
        } else if (p_e != null) {
            if (p.x() > 0) {
                t = -t;
            }
            top = Point.coordinat(d, p, a_e);
            bottom = Point.coordinat(d, p, t);
        }
        ret = top + " " + bottom + "\n";
        ret += "lft and rgt: " + f_l + " " + f_r + "\n";
        return ret;
    }
    
    public boolean isUsed() {
        return used;
    }
    
    public void used() {
        used = true;
    }
    
    public DCEL getP_b() {
        return p_b;
    }

    public DCEL getP_e() {
        return p_e;
    }
    
    public Point getF_l() {
        return f_l;
    }

    public Point getF_r() {
        return f_r;
    }

    public DCELNode() {
        a_e = a_b = 0.0;
        f_l = f_r = null;
        p = d = null;
        p_e = p_b = null;
    }

    public DCELNode(DCELNode c) {
        a_e = c.a_e;
        a_b = c.a_b;
        f_l = c.f_l;
        f_r = c.f_r;
        p = c.p;
        d = c.d;
        p_e = p_b = null;
    }

    public DCELNode(Point p) {
        a_e = a_b = 0.0;
        f_l = new Point(p);
        f_r = new Point(p);
        this.p = new Point(0,0);
        d = new Point(0,0);
        used = false;
        p_e = p_b = null;
    }

    public DCELNode(Point lft, Point rgt) {
        a_e = a_b = 0.0;
        f_l = new Point(lft);
        f_r = new Point(rgt);
        p = Point.average(lft, rgt);
        d = Point.transpose(lft, rgt);
        used = false;
        p_e = p_b = null;
    }

    public void vor2file() throws Exception {
        try {
            initFile();
            v2f();
            DCELNode.bw.close();
        } catch (java.io.IOException ioe) {
            System.out.println("Failed: " + ioe);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DCELNode)) {
            return false;
        } else if (!(f_l.equals(((DCELNode)o).f_l)) || !(f_r.equals(((DCELNode)o).f_r))) {
            return false;
        } else if (!(p.equals(((DCELNode)o).p)) || !(d.equals(((DCELNode)o).d))) {
            return false;
        } else {
            return !((a_e != ((DCELNode)o).a_e) || (a_b != ((DCELNode)o).a_b));
        }        
    }

    public Line drawEdge() {
        Line ret;
        Point top = ZERO, bottom = ZERO;
        double t = 5 * Math.sqrt((Math.pow(f_l.x() - p.x(), 2) + Math.pow(f_l.y() - p.y(), 2))
                / (Math.pow(d.x(), 2) + Math.pow(d.y(), 2)));
        if ((p_b != null) && (p_e != null)) {
            top = Point.coordinat(d, p, a_e);
            bottom = Point.coordinat(d, p, a_b);
        } else if (p_b != null) {
            if (p.x() < 0) {
                t = -t;
            }
            top = Point.coordinat(d, p, t + a_b);
            bottom = Point.coordinat(d, p, a_b);
        } else if (p_e != null) {
            if (p.x() > 0) {
                t = -t;
            }
            top = Point.coordinat(d, p, a_e);
            bottom = Point.coordinat(d, p, a_e + t);
        }
        ret = new Line(bottom, top);
        return ret;
    }

    public String printDCEL() {
        String ret = "";
        if (!used) {
            used = true;
            ret += this.toString();
            if (p_b != null) {
                ret += p_b.node.printDCEL();
            }
            if (p_e != null) {
                ret += p_e.node.printDCEL();
            }
        }
        return ret;
    }

    public DCELNode copyDCEL() {
        DCELNode ret = this;
        if (!used) {
            used = true;
            ret = new DCELNode(this);
            if (p_b != null) {
                ret.p_b = new DCEL(p_b.node.copyDCEL());
            }
            if (p_e != null) {
                ret.p_e = new DCEL(p_e.node.copyDCEL());
            }
        }
        return ret;
    }

    public void resetMark() {
        if (used) {
            used = false;
            if (p_b != null) {
                p_b.node.resetMark();
            }
            if (p_e != null) {
                p_e.node.resetMark();
            }
        }
    }

    public DCELNode copy() {
        DCELNode ret = copyDCEL();
        resetMark();
        return ret;
    }

    short edgeType() {
        short ret;
        if ((p_e != null) && (p_b != null)) {
            ret = CLOSED;
        } else if ((p_e == null) && (p_b == null)) {
            ret = ENDLESS;
        } else {
            ret = SEMI;
        }
        return ret;
    }

    boolean samePoint(Point p) {
        return (f_l.equals(p) || f_r.equals(p));
    }

    Point cutPoint(DCELNode l) {
        double det;
        Point a = new Point(this.d);
        Point b = new Point(l.d.negate());
        Point ret;
        det = Point.determinant(a, b);
        if (((det - Double.MIN_VALUE) > 0) || ((det + Double.MIN_VALUE) < 0)) {
            Point c = new Point(l.p.add(this.p.negate()));
            ret = new Point(Point.determinant(c, b) / det, Point.determinant(a, c) / det);
        } else {
            ret = ZERO;
        }
        return ret;
    }

    private void v2f() throws Exception {
        if (!used) {
            used = true;
            DCELNode.bw.write(toString());
            if (p_b != null) {
                p_b.node.v2f();
            }
            if (p_e != null) {
                p_e.node.v2f();
            }
        }
    }

}
