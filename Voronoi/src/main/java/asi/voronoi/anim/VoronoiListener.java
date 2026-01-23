package asi.voronoi.anim;

import asi.voronoi.Line;
import asi.voronoi.Point;
import java.util.List;

public interface VoronoiListener {

    // Division
    default void onDivideStart(int depth, Rect bbox, Point pivot, Line splitLine,
            List<Point> leftPoints, List<Point> rightPoints) {
    }

    default void onDivideEnd(int depth) {
    }

    default void onBaseCaseSingle(Point p) {
    }

    default void onBaseCasePair(Point lft, Point rgt) {
    }

    // Merge
    default void onMergeStart(Point upLft, Point upRgt, Point downLft, Point downRgt) {
    }

    default void onSnapshot(List<Line> edges) {
    }

    default void onFinalized(List<Line> edges) {
    }

    // ---------------- BinaryTree traversal events ----------------
    default void onBtEnterNode(String nodeId, String parentId, String label) {
    }

    default void onBtVisitNode(String nodeId, String parentId, String label) {
    }

    default void onBtExitNode(String nodeId, String parentId, String label) {
    }

    default void onBtHighlightEdge(String childId, String parentId) {
    }
}
