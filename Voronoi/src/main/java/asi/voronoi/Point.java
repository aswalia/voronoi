package asi.voronoi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class Point implements java.io.Serializable {
    private double x, y;

    public Point(int ix, int iy) {
        this.x = ix;
        this.y = iy;
    }

    public Point(double dx, double dy) {
        this.x = dx;
        this.y = dy;
    }

    public Point(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    public Point() {
        x = y = -1;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public Point add(Point q) {
        return new Point(x + q.x, y + q.y);
    }

    public Point negate() {
        return new Point(-x, -y);
    }

    public Point mult(double dx) {
        return new Point(dx * x, dx * y);
    }

    public static double determinant(Point a, Point b) {
        return a.x * b.y - a.y * b.x;
    }

    public static Point average(Point min, Point max) {
        Point ret = new Point(min);
        ret.x += max.x;
        ret.y += max.y;
        ret.x /= 2;
        ret.y /= 2;
        return ret;
    }
    
    public static double areaDouble(Point a, Point b, Point c) {
        return a.x*(b.y-c.y) + b.x*(c.y-a.y) + c.x*(a.y-b.y);
    }

    public static short area(Point a, Point b, Point c) {
        double temp = (c.y - b.y) * (b.x - a.x) - (b.y - a.y) * (c.x - b.x);
        short ret = ((temp - Double.MIN_VALUE) > 0) ? (short) 1 : (((temp + Double.MIN_VALUE) < 0) ? (short) - 1 : (short) 0);
        return ret;
    }

    public static short direction(Point a, Point b, Point c) {
        double temp = (b.y - a.y) * (c.y - b.y) + (b.x - a.x) * (c.x - b.x);
        short ret = ((temp - Double.MIN_VALUE) > 0) ? (short) 1 : (((temp + Double.MIN_VALUE) < 0) ? (short) - 1 : (short) 0);
        return ret;
    }

    public static Point transpose(Point lft, Point rgt) {
        Point ret = new Point();
        ret.x = lft.y - rgt.y;
        ret.y = rgt.x - lft.x;
        return ret;
    }

    @Override
    public boolean equals(Object t) {
        Point s = (Point)t;
        return ((this.x == s.x) && (this.y == s.y));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }

    public boolean isLess(Point t) {
        return (this.x < t.x) || ((this.x == t.x) && (this.y < t.y));
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public static Point coordinat(Point d, Point p, double t) {
        return p.add(d.mult(t));
    }
    
}
