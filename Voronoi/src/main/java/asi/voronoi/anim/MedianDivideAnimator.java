package asi.voronoi.anim;

import asi.voronoi.Line;
import asi.voronoi.Point;
import java.util.*;
import static java.util.Comparator.comparingDouble;

public final class MedianDivideAnimator {

    private MedianDivideAnimator() {
    }

    public static void animateDivide(List<Point> pts) {
        animateDivide(pts, 0);
    }

    public static void animateDivide(List<Point> pts, int depth) {
        if (pts == null || pts.isEmpty()) {
            return;
        }
        if (pts.size() == 1) {
            VoronoiEvents.fireBaseCaseSingle(pts.get(0));
            return;
        }
        if (pts.size() == 2) {
            VoronoiEvents.fireBaseCasePair(pts.get(0), pts.get(1));
            return;
        }

        Rect bbox = bbox(pts);

        // Sortér efter (x, y) og vælg median
        List<Point> sorted = new ArrayList<>(pts);
        sorted.sort(comparingDouble(Point::x).thenComparingDouble(Point::y));
        int mid = sorted.size() / 2;
        Point pivot = sorted.get(mid);

        boolean allSameX = (sorted.get(0).x() == sorted.get(sorted.size() - 1).x());

        Line split = new Line();
        if (!allSameX) {
            // Lodret split: x = pivot.x
            split.setMidP(new Point(pivot.x(), (bbox.yMin() + bbox.yMax()) / 2.0));
            split.setDir(new Point(0, 1));
        } else {
            // Vandret split: y = median(y)
            List<Point> ySorted = new ArrayList<>(pts);
            ySorted.sort(comparingDouble(Point::y));
            Point yPivot = ySorted.get(mid);
            split.setMidP(new Point((bbox.xMin() + bbox.xMax()) / 2.0, yPivot.y()));
            split.setDir(new Point(1, 0));
            pivot = yPivot;
        }

        // Partition iht. din regel
        List<Point> left = new ArrayList<>(), right = new ArrayList<>();
        if (!allSameX) {
            for (Point p : pts) {
                if (p.x() < pivot.x() || (p.x() == pivot.x() && p.y() < pivot.y())) {
                    left.add(p);
                } else {
                    right.add(p);
                }
            }
        } else {
            double ym = pivot.y();
            for (Point p : pts) {
                if (p.y() < ym) {
                    left.add(p);
                } else {
                    right.add(p);
                }
            }
        }
        if (left.isEmpty() && !right.isEmpty()) {
            left.add(right.remove(0));
        }
        if (right.isEmpty() && !left.isEmpty()) {
            right.add(left.remove(left.size() - 1));
        }

        VoronoiEvents.fireDivideStart(depth, bbox, pivot, split, List.copyOf(left), List.copyOf(right));
        animateDivide(left, depth + 1);
        animateDivide(right, depth + 1);
        VoronoiEvents.fireDivideEnd(depth);
    }

    private static Rect bbox(List<Point> pts) {
        double xmin = Double.POSITIVE_INFINITY, ymin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
        for (Point p : pts) {
            xmin = Math.min(xmin, p.x());
            xmax = Math.max(xmax, p.x());
            ymin = Math.min(ymin, p.y());
            ymax = Math.max(ymax, p.y());
        }
        return new Rect(xmin, ymin, xmax, ymax);
    }
}
