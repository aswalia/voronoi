package asi.voronoi.javafx;

import asi.voronoi.DCEL;
import asi.voronoi.DCELNode;
import asi.voronoi.tree.VoronoiTree;

public class DrawVoronoi extends DrawObject {
    private final java.util.LinkedList<DCELNode> ll;

    public DrawVoronoi(VoronoiTree v) {
        mo = v;
        ll = new java.util.LinkedList();
        drawVoronoi(((DCEL) v.getInfo()).getNode());
        ((DCEL) v.getInfo()).getNode().resetMark();
    }

    @Override
    public String objectTitle() {
        return "Voronoi Diagram";
    }

    @Override
    public void drawGeometry(GraphicData g) {
        super.drawGeometry(g);
        drawEdge(g);
    }

    private void drawVoronoi(DCELNode dn) {
        if (!dn.isUsed()) {
            ll.add(dn);
            dn.used();
            if (dn.getP_b() != null) {
                drawVoronoi(dn.getP_b().getNode());
            }
            if (dn.getP_e() != null) {
                drawVoronoi(dn.getP_e().getNode());
            }
        }
    }

    private void drawEdge(GraphicData g) {
        for (DCELNode dn : ll) {
            drawPoint(g,dn.getF_l());
            drawLine(g,dn.drawEdge());
        }
    }
}
