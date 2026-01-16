package asi.voronoi.anim;

import asi.voronoi.Line;
import asi.voronoi.Point;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class VoronoiEvents {

    private static final CopyOnWriteArrayList<VoronoiListener> LS = new CopyOnWriteArrayList<>();

    private VoronoiEvents() {
    }

    public static void add(VoronoiListener l) {
        if (l != null) {
            LS.add(l);
        }
    }

    public static void clear() {
        LS.clear();
    }

    // Division
    public static void fireDivideStart(int depth, Rect bbox, Point pivot, Line split,
            List<Point> L, List<Point> R) {
        LS.forEach(l -> l.onDivideStart(depth, bbox, pivot, split, L, R));
    }

    public static void fireDivideEnd(int depth) {
        LS.forEach(l -> l.onDivideEnd(depth));
    }

    public static void fireBaseCaseSingle(Point p) {
        LS.forEach(l -> l.onBaseCaseSingle(p));
    }

    public static void fireBaseCasePair(Point l, Point r) {
        LS.forEach(e -> e.onBaseCasePair(l, r));
    }

    // Merge
    public static void fireMergeStart(Point a, Point b, Point c, Point d) {
        LS.forEach(l -> l.onMergeStart(a, b, c, d));
    }

    public static void fireSnapshot(List<Line> edges) {
        LS.forEach(l -> l.onSnapshot(edges));
    }

    public static void fireFinalized(List<Line> edges) {
        LS.forEach(l -> l.onFinalized(edges));
    }
}
