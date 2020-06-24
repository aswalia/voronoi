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
                ((DCEL) info).setUpLft(c.getUpLft());
                ((DCEL) info).setUpRgt(c.getUpRgt());
                ((DCEL) info).setDownLft(c.getDownLft());
                ((DCEL) info).setDownRgt(c.getDownRgt());
                info = ((DCEL) info).merge((Point) lft.info);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else if (rgt.isLeaf()) {
            try {
                lft.buildStructure();
                info = (DCEL) lft.info;
                ConveksHull c = ((DCEL) info).vor2CH();
                c.merge((Point) rgt.info);
                ((DCEL) info).setUpLft(c.getUpLft());
                ((DCEL) info).setUpRgt(c.getUpRgt());
                ((DCEL) info).setDownLft(c.getDownLft());
                ((DCEL) info).setDownRgt(c.getDownRgt());
                info = ((DCEL) info).merge((Point) rgt.info);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            try {
                lft.buildStructure();
                rgt.buildStructure();
                ConveksHull c = ((DCEL) lft.info).vor2CH();
                ConveksHull d = ((DCEL) rgt.info).vor2CH();
                c.merge(d);
                info = (DCEL) lft.info;
                ((DCEL) info).setUpLft(c.getUpLft());
                ((DCEL) info).setUpRgt(c.getUpRgt());
                ((DCEL) info).setDownLft(c.getDownLft());
                ((DCEL) info).setDownRgt(c.getDownRgt());
                info = ((DCEL) info).merge((DCEL) rgt.info);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    static class Test {
        public static void main(String[] argv) throws Exception {
            VoronoiTree v = new VoronoiTree();
            v.buildTree("src/test/resources/test2_2.it");
            long start = System.currentTimeMillis();
            v.buildStructure();
            long stop = System.currentTimeMillis();
            System.out.println("Time taken (msec): " + (stop - start));
            v.writeTree("src/test/resources/test2_2.out");
        }
    }
}
