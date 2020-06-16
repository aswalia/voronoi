package asi.voronoi;

public class DrawConveksHull extends DrawObject {

    private final CircularLinkedList li;

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
        Point start = li.get(0);
        for (int i=0; !li.get(i+1).equals(start); i++) {
            Line l = new Line(li.get(i),li.get(i+1));
            drawLine(g,l);
        }
        Line l = new Line(li.get(0),start);
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
