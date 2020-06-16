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
                ConveksHull c = ((DCEL) info).vor2CH();
                c.merge((Point) lft.info);
                PointPair pp = c.getUpSupport();
                if (pp == null) {
                    throw new Exception("3 points on a line ");
                }
                ((DCEL) info).setUpLft(pp.getLft()); ((DCEL) info).setUpRgt(pp.getRgt());
                pp = c.getDownSupport();
                ((DCEL) info).setDownLft(pp.getLft()); ((DCEL) info).setDownRgt(pp.getRgt());
                info = ((DCEL) info).merge((Point) lft.info);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        } else if (rgt.isLeaf()) {
            try {
                lft.buildStructure();
                info = (DCEL) lft.info;
                ConveksHull c = ((DCEL) info).vor2CH();
                c.merge((Point) rgt.info);
                PointPair pp = c.getUpSupport();
                if (pp == null) {
                    throw new Exception("3 points on a line ");
                }
                ((DCEL) info).setUpLft(pp.getLft()); ((DCEL) info).setUpRgt(pp.getRgt());
                pp = c.getDownSupport();
                ((DCEL) info).setDownLft(pp.getLft()); ((DCEL) info).setDownRgt(pp.getRgt());
                info = ((DCEL) info).merge((Point) rgt.info);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        } else {
            try {
                lft.buildStructure();
                rgt.buildStructure();
                ConveksHull c = ((DCEL) lft.info).vor2CH();
                ConveksHull d = ((DCEL) rgt.info).vor2CH();
                c.merge(d);
                info = (DCEL) lft.info;
                PointPair pp = c.getUpSupport();
                if (pp == null) {
                    throw new Exception("3 points on a line ");
                }
                ((DCEL) info).setUpLft(pp.getLft()); ((DCEL) info).setUpRgt(pp.getRgt());
                pp = c.getDownSupport();
                ((DCEL) info).setDownLft(pp.getLft()); ((DCEL) info).setDownRgt(pp.getRgt());
                info = ((DCEL) info).merge((DCEL) rgt.info);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    static class Test {
        public static void main(String[] argv) throws Exception {
            VoronoiTree v = new VoronoiTree();
            v.buildTree(argv[0]);
            long start = System.currentTimeMillis();
            v.buildStructure();
            long stop = System.currentTimeMillis();
            System.out.println("Time taken (msec): " + (stop - start));
            System.out.println(v);
        }
    }
}
