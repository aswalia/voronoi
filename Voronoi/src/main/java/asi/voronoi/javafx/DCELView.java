package asi.voronoi.javafx;

import asi.voronoi.DCELNode;
import asi.voronoi.Point;
import java.util.LinkedList;
import java.util.List;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

/**
 *
 * @author asi
 */
public class DCELView extends Pane {

    private double xMin, xMax, yMin, yMax, wM, hM, fx, fy;
    private final List<asi.voronoi.Line> ll = new LinkedList<>();

    DCELView(DCELNode dn) {
        List<asi.voronoi.Line> el = new LinkedList<>();
        for (DCELNode ls:dn.getVoronoiEdgeList()) {
            asi.voronoi.Line l = ls.getLineSegment();
            if (ls.getP_b() == null) {
                // bp = ep - (len(f_l,f_r))
                l.setBeginP(l.getEndP().add(Point.transpose(ls.getF_l(),ls.getF_r()).negate()));
                el.add(l);
            }
            if (ls.getP_e() == null) {
                // ep = bp + (len(f_l,f_r))
                l.setEndP(l.getBeginP().add(Point.transpose(ls.getF_l(),ls.getF_r())));
                el.add(l);
            }
            ll.add(l);
        }
        xMax = yMax = Double.NEGATIVE_INFINITY;
        xMin = yMin = Double.POSITIVE_INFINITY;
        for (asi.voronoi.Line l:el) {
            if (xMin > l.getBeginP().x()) {
                xMin = l.getBeginP().x();
            }
            if (xMin > l.getEndP().x()) {
                xMin = l.getEndP().x();
            }
            if (yMin > l.getBeginP().y()) {
                yMin = l.getBeginP().y();
            }
            if (yMin > l.getEndP().y()) {
                yMin = l.getEndP().y();
            }
            if (xMax < l.getBeginP().x()) {
                xMax = l.getBeginP().x();
            }
            if (xMax < l.getEndP().x()) {
                xMax = l.getEndP().x();
            }
            if (yMax < l.getBeginP().y()) {
                yMax = l.getBeginP().y();
            }
            if (yMax < l.getEndP().y()) {
                yMax = l.getEndP().y();
            }
        }
    }
    
    private void setView() {
        wM =  0.95 * getWidth();
        hM =  0.95 * getHeight();
        fx = wM / (xMax - xMin);
        fy = hM / (yMax - yMin);
    }
    
    public void displayDcelList() {
        this.getChildren().clear(); // Clear the pane        
        // Display list
        setView();
        for (asi.voronoi.Line l:ll) {
            displayLine(l);
        }
    }
    
    private void displayLine(asi.voronoi.Line l) {
        
        double bx = fx * (l.getBeginP().x() - xMin);
        double by = hM - fy * (l.getBeginP().y() - yMin);       
        double ex = fx * (l.getEndP().x() - xMin);
        double ey = hM - fy * (l.getEndP().y() - yMin);
        
        Line dl;
        dl = new Line(bx, by, ex, ey);
        getChildren().add(dl);     
    }
    
}

