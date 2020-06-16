package asi.voronoi;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

abstract public class DrawObject {
    protected double s = 1, tX = 0, tY = 0, ox = 0, oy = 0;
    protected double fx = 1, fy = 1;
    protected ModelObject mo;

    public abstract String objectTitle();

    public void drawRepresentation(GraphicData g) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.g;
        g2.drawString("Not implemented", 100, 100);
    }

    public void drawGeometry(GraphicData g) {
        fx = g.d.width / (mo.maxX() - mo.minX()) / 2;
        fy = g.d.height / (mo.maxY() - mo.minY()) / 2;
        ox = g.d.width / 4;
        oy = g.d.height / 2;
        
    }

    public Point transformPoint(Point p) {
        double x = s * p.x() * fx + tX + ox;
        double y = s * (-p.y()) * fy + tY + oy;
        return new Point(x,y);
    }

    public void drawPoint(GraphicData g, Point pp) {
        Graphics2D g2 = (Graphics2D) g.g;
        Point p = transformPoint(pp);
        g2.draw(new Ellipse2D.Double(p.x(), p.y(), 2.0, 2.0));
    }

    public void drawLine(GraphicData g, Line l) {
        Graphics2D g2 = (Graphics2D) g.g;
        Point p1 = transformPoint(l.getP1());
        Point p2 = transformPoint(l.getP2());
        g2.draw(new Line2D.Double(p1.x(), p1.y(), p2.x(), p2.y()));
    }

    public void setScale(double ps) {
        if (ps > 0) {
            s *= 1.1;
        } else if (ps < 0) {
            s *= 0.9;
        }
    }

    public void setTranslation(double tx, double ty) {
        tX += tx;
        tY += ty;
    }
}
