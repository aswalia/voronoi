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
}
