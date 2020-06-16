package asi.voronoi;

import java.util.LinkedList;

public class DrawIntervalTree extends DrawObject {
    private LinkedList<Point> ll;

    public DrawIntervalTree(IntervalTree it) {
        mo = it;
        ll = new LinkedList();
        drawSet(it);
    }

    @Override
    public String objectTitle() {
        return "IntervalTree";
    }

    @Override
    public void drawGeometry(GraphicData g) {
        super.drawGeometry(g);
        drawPoint(g);
    }

    private void drawSet(IntervalTree it) {
        if (!it.isLeaf()) {
            if (it.lft() != null) {
                drawSet(it.lft());
            }
            if (it.rgt() != null) {
                drawSet(it.rgt());
            }
        } else {
            ll.add((Point)it.getInfo());
        }
    }

    private void drawPoint(GraphicData g) {
        for (Point p : ll) {
            drawPoint(g,p);
        }
    }

    static class Test {
        public static void main(String[] argv) {
            Point p = new Point((int) (Math.random() * 100), (int) (Math.random() * 100));
            AVLTree t = new AVLTree(p);
            for (int i = 0; i < Integer.parseInt(argv[0]); i++) {
                p = new Point((int) (Math.random() * 100), (int) (Math.random() * 100));
                t = (AVLTree) t.insertNode(p);
            }
            IntervalTree it = new IntervalTree();
            it.buildTree(t);
            DrawIntervalTree dbt = new DrawIntervalTree(it);
            DrawingBoard db = new DrawingBoard(dbt);
            db.setGeometry(true);
        }
    }
}
