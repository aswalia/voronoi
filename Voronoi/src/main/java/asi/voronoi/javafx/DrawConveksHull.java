package asi.voronoi.javafx;

import asi.voronoi.CircularLinkedList;
import asi.voronoi.ConveksHull;
import asi.voronoi.Line;

public class DrawConveksHull extends DrawObject {

    private final CircularLinkedList li;

    public DrawConveksHull(ConveksHull c) {
        mo = c;
        li = c.getHead();
    }

    @Override
    public String objectTitle() {
        return "Convekshull Diagram";
    }

    @Override
    public void drawGeometry(GraphicData g) {
        super.drawGeometry(g);
        drawConveksHull(g);
    }

    private void drawConveksHull(GraphicData g) {
        for (int i=0; i < li.length(); i++) {
            Line l = new Line(li.get(i),li.get(i+1));
            drawLine(g,l);
        }
    }
}
