package asi.voronoi;

public class DrawConveksHull extends DrawObject {

    private LinkedList li;

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
        Point start = li.element();
        for (; !li.next().equals(start); li.front()) {
            Line l = new Line(li.element(),li.next());
            drawLine(g,l);
        }
        Line l = new Line(li.element(),start);
        drawLine(g,l);
    }

    static class Test {
        public static void main(String[] argv) throws Exception {
            Point p = new Point((int) (Math.random() * 100 + 10), (int) (Math.random() * 100 + 10));
            BinaryTree t = new BinaryTree(p);
            for (int i = 0; i < Integer.parseInt(argv[0]); i++) {
                p = new Point((int) (Math.random() * 100 + 10), (int) (Math.random() * 100 + 10));
                t = t.insertNode(p);
            }
            VoronoiTree vt = new VoronoiTree();
            vt.buildTree(t);
            vt.buildStructure();
            ConveksHull ch = ((DCEL) vt.getInfo()).vor2CH();
            DrawConveksHull dv = new DrawConveksHull(ch);
            DrawingBoard drawingBoard = new DrawingBoard(dv);
        }
    }
}
