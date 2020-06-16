package asi.voronoi;

public class Main {
    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.out.println("Usage: java -jar voronoi.jar n\n n: number of points");
        } else {
            long startTime, endTime;
            int numberOfPoints = Integer.parseInt(argv[0]);
            boolean success = false;
            for (int j = 0; (j < 10) && (!success); j++) {
                AVLTree t = new AVLTree(new Point((int) (Math.random() * 5 * numberOfPoints),
                        (int) (Math.random() * 5 * numberOfPoints)));
                for (int i = 0; i < (numberOfPoints - 1); i++) {
                    t = (AVLTree) t.insertNode(new Point((int) (Math.random() * 5 * numberOfPoints),
                            (int) (Math.random() * 5 * numberOfPoints)));
                }

                try {
                    int no = (int) (Math.random() * 5 * numberOfPoints);
//	                Serializer.store("AVL"+no,t);
                    VoronoiTree v = new VoronoiTree();
                    v.buildTree(t);
                    System.out.println("# points: " + t.count());
                    startTime = System.currentTimeMillis();
                    v.buildStructure();
                    success = true;
//                    Serializer.store("vor"+no,v);
//                    DrawBinaryTree dbt = new DrawBinaryTree(t);
//                    DrawVoronoi dvt = new DrawVoronoi(v);
//                    DrawConveksHull dch = new DrawConveksHull(((DCEL)v.getInfo()).vor2CH());
//                    new DrawingBoard(dbt);
//                    new DrawingBoard(dvt);
//                    new DrawingBoard(dch);
//                    v.toFile();
                    DrawObject da = new DrawVoronoi(v);
                    DrawingBoard drawingBoard = new DrawingBoard(da);
//					v = (VoronoiTree)Serializer.fetch("vor"+no);
                    endTime = System.currentTimeMillis();
//					System.out.println(v);
//                    System.out.println();
//                    AVLTree a = (AVLTree)Serializer.fetch("AVL"+no);
//					System.out.println(a);
                    System.out.println("Used time (millisec): "+(endTime - startTime));
                } catch (Exception e) {
                    System.out.println("Failed: " + e);
                }
            }
        }
    }
}
