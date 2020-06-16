package asi.voronoi;

public class DrawBinaryTree extends DrawObject {
    private java.util.LinkedList<BinaryTree> ll;

    public DrawBinaryTree(BinaryTree bt) {
        mo = bt;
        ll = new java.util.LinkedList();
        drawSet(bt);
    }

    @Override
    public String objectTitle() {
        return "BinaryTree";
    }

    @Override
    public void drawGeometry(GraphicData g) {
        super.drawGeometry(g);
        drawPoint(g);
    }

    private void drawSet(BinaryTree bt) {
        ll.add(bt);
        if (!bt.isLeaf()) {
            if (bt.lft() != null) {
                drawSet(bt.lft());
            }
            if (bt.rgt() != null) {
                drawSet(bt.rgt());
            }
        }
    }

    private void drawPoint(GraphicData g) {
        for (BinaryTree bt : ll) {
            drawPoint(g,bt.getP());
//            Graphics2D g2 = (Graphics2D) g.g;
//            g2.drawString(p.toString(), (int) p.x(), (int) p.y());
        }
    }

    static class Test {
        public static void main(String[] argv) {
            Point p = new Point((int) (Math.random() * 100), (int) (Math.random() * 100));
            BinaryTree t = new BinaryTree(p);
            for (int i = 0; i < Integer.parseInt(argv[0]); i++) {
                p = new Point((int) (Math.random() * 100), (int) (Math.random() * 100));
                t = t.insertNode(p);
            }
            DrawBinaryTree dbt = new DrawBinaryTree(t);
            DrawingBoard db = new DrawingBoard(dbt);
            db.setGeometry(true);
        }
    }
}
