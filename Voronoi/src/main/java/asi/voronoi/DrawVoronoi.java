package asi.voronoi;

public class DrawVoronoi extends DrawObject {
    private java.util.LinkedList<DCELNode> ll;

    public DrawVoronoi(VoronoiTree v) {
        mo = v;
        ll = new java.util.LinkedList();
        drawVoronoi(((DCEL) v.getInfo()).node);
        ((DCEL) v.getInfo()).node.resetMark();
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
        ll.add(dn);
        if (!dn.used) {
            dn.used = true;
            if (dn.p_b != null) {
                drawVoronoi(dn.p_b.node);
            }
            if (dn.p_e != null) {
                drawVoronoi(dn.p_e.node);
            }
        }
    }

    private void drawEdge(GraphicData g) {
        for (DCELNode dn : ll) {
            drawPoint(g,dn.f_l);
            drawLine(g,dn.drawEdge());
        }
    }
}
