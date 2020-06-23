package asi.voronoi;

public class VoronoiTree extends IntervalTree {

    @Override
    public String toString() {
        return "" + info;
    }

    public void toFile() throws Exception {
        ((DCEL) info).toFile();
    }

    @Override
    public IntervalTree newNode() {
        return new VoronoiTree();
    }

    @Override
    public void buildStructure() {
        if (lft.isLeaf() && rgt.isLeaf()) {
            info = new DCEL((Point) lft.info, (Point) rgt.info);
        } else if (lft.isLeaf()) {
            try {
                rgt.buildStructure();
                info = (DCEL) rgt.info;
                info = ((DCEL) info).merge((Point) lft.info);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        } else if (rgt.isLeaf()) {
            try {
                lft.buildStructure();
                info = (DCEL) lft.info;
                info = ((DCEL) info).merge((Point) rgt.info);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        } else {
            try {
                lft.buildStructure();
                rgt.buildStructure();
                info = (DCEL) lft.info;
                info = ((DCEL) info).merge((DCEL) rgt.info);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}
