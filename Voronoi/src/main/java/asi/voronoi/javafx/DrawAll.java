package asi.voronoi.javafx;

import asi.voronoi.DCEL;
import asi.voronoi.tree.VoronoiTree;

public class DrawAll extends DrawObject {
    private DrawObject dv, dch, dit;

    public DrawAll(VoronoiTree v) {
        mo = v;
        dv = new DrawVoronoi(v);
        dch = new DrawConveksHull(((DCEL) v.getInfo()).vor2CH());
        dit = new DrawIntervalTree(v);
    }

    @Override
    public String objectTitle() {
        return "All";
    }

    @Override
    public void drawGeometry(GraphicData g) {
        super.drawGeometry(g);
        dit.drawGeometry(g);
        dv.drawGeometry(g);
        dch.drawGeometry(g);
    }
}
