package asi.voronoi.javafx;

import asi.voronoi.CircularLinkedList;
import asi.voronoi.ConveksHull;
import asi.voronoi.Point;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author asi
 */
public class CLLView extends Pane {

    private ConveksHull ch = new ConveksHull();
    private double xMin, xMax, yMin, yMax, wM, hM, fx, fy;
    private final Map<Point, Rectangle> rectMap;

    CLLView(ConveksHull c) {
        this.ch = c;
        rectMap = new HashMap<>();
    }
    
    private void setView() {
        xMin = ch.minX(); xMax = ch.maxX();
        yMin = ch.minY(); yMax = ch.maxY();
        wM =  0.8 * getWidth();
        hM =  0.8 * getHeight();
        fx = wM / (xMax - xMin);
        fy = hM / (yMax - yMin);
    }
    
    public void displayCircularLinkedList() {
        this.getChildren().clear(); // Clear the pane
        if (ch != null) {
            Point p, pm;
            // Display list
            setView();
            CircularLinkedList head = ch.getHead();
            int i = 0; 
            p = head.get(i);                
            do {
                pm = calculatePoint(p);
                displayPoint(p, 0.1 * wM + pm.x(), 0.1 * hM + pm.y());
                if (i > 0) {
                    displayLine(head.get(i-1),p);
                }
                i++;
                p = head.get(i);                
            } while (p != head.get(0));
            displayLine(head.get(i-1),p);            
        }
    }
    
    private Point calculatePoint(Point p) {
        Point ret;
        double xi = fx * (p.x() - xMin);
        double yi = hM - fy * (p.y() - yMin);       
        
//        double v = Math.PI - (2*Math.PI/ch.size())*i;        
//        ret = new Point(fx*Math.cos(v)*wM/2 + xMin, hM - fy*Math.sin(v)*hM/2 - yMin);
        ret = new Point(xi, yi);
        return ret;
    }
    
    private void displayPoint(Point p, double x, double y) {
        // Display a node
        int adjust = 5;
        Text pTxt = new Text(x, y, p.x() + "\n" + p.y());
        pTxt.setTextAlignment(TextAlignment.RIGHT);
        Bounds b = pTxt.getLayoutBounds();
        double w = b.getWidth();
        double h = b.getHeight();
        Rectangle rect = new Rectangle(x - adjust/2, y - h/2, w+adjust, h+adjust);
        rect.setFill(Color.WHITE);
        rect.setStroke(Color.BLACK);
        rectMap.put(p, rect);
        getChildren().addAll(rect, pTxt);     
    }
    
    private void displayLine(Point ps, Point pe) {
        // index to current point in CH
        Rectangle r_i, r_im1;
        Line l;
        // rect for current point
        r_i = rectMap.get(pe);
        double x_i = r_i.getX();
        double y_i = r_i.getY();
        double w_i = r_i.getWidth();
        double h_i = r_i.getHeight();
        Point top_i = new Point(x_i + w_i/2, y_i);
        Point bottom_i = new Point(x_i + w_i/2, y_i + h_i);
        Point lft_i = new Point(x_i, y_i + h_i/2);
        Point rgt_i = new Point(x_i + w_i, y_i + h_i/2);
        // rect for prev point
        r_im1 = rectMap.get(ps);
        double x_im1 = r_im1.getX();
        double y_im1 = r_im1.getY();
        double w_im1 = r_im1.getWidth();
        double h_im1 = r_im1.getHeight();
        Point top_im1 = new Point(x_im1 + w_im1/2, y_im1);
        Point bottom_im1 = new Point(x_im1 + w_im1/2, y_im1 + h_im1);
        Point lft_im1 = new Point(x_im1, y_im1 + h_im1/2);
        Point rgt_im1 = new Point(x_im1 + w_im1, y_im1 + h_im1/2);
        
        if (bottom_im1.y() < top_i.y()) {
            // if prev higher then current
            l = new Line(bottom_im1.x(), bottom_im1.y(), top_i.x(), top_i.y());
        } else if (bottom_i.y() < top_im1.y()) {
            // if current higher then prev
            l = new Line(top_im1.x(), top_im1.y(), bottom_i.x(), bottom_i.y());
        } else if (lft_i.x() > rgt_im1.x()) {
            // if prev is to the lft of current
            l = new Line(rgt_im1.x(), rgt_im1.y(), lft_i.x(), lft_i.y());
        } else {
            // if current is to the lft of prev
            l = new Line(lft_im1.x(), lft_im1.y(), rgt_i.x(), rgt_i.y());
        }
        getChildren().add(l);     
    }
    
}

