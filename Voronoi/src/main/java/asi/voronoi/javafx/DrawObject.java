package asi.voronoi.javafx;

import asi.voronoi.Line;
import asi.voronoi.ModelObject;
import asi.voronoi.Point;
import javafx.scene.canvas.GraphicsContext;

abstract public class DrawObject {
    protected double s = 1, tX = 0, tY = 0, ox = 0, oy = 0;
    protected double fx = 1, fy = 1;
    protected ModelObject mo;

    public abstract String objectTitle();

    public void drawRepresentation(GraphicData g) {
//        GraphicsContext g2 = g.c.getGraphicsContext2D();
        g.c.fillText("Not implemented", 100, 100);
    }

    public void drawGeometry(GraphicData g) {
        fx = g.b.getWidth() / (mo.maxX() - mo.minX()) / 2;
        fy = g.b.getHeight() / (mo.maxY() - mo.minY()) / 2;
        ox = g.b.getWidth() / 4;
        oy = g.b.getHeight() / 2;
        
    }

    public Point transformPoint(Point p) {
        double x = s * (p.x() * fx + tX + ox);
        double y = s * ((-p.y()) * fy + tY + oy);
        return new Point(x,y);
    }

    public void drawPoint(GraphicData g, Point pp) {
//        GraphicsContext g2 = g.c.getGraphicsContext2D();
        Point p = transformPoint(pp);
        g.c.fillOval(p.x(), p.y(), 2.0, 2.0);
    }

    public void drawLine(GraphicData g, Line l) {
//        GraphicsContext g2 = g.c.getGraphicsContext2D();
        Point p1 = transformPoint(l.getP1());
        Point p2 = transformPoint(l.getP2());
        g.c.moveTo(p1.x(), p1.y());
        g.c.lineTo(p2.x(), p2.y());
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
