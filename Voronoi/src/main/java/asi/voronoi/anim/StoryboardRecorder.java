package asi.voronoi.anim;

import asi.voronoi.Line;
import asi.voronoi.Point;
import java.util.*;
import java.util.function.Consumer;

public class StoryboardRecorder implements VoronoiListener {

    public static class Frame {

        public final int depth;
        public final Rect bbox;
        public final Point pivot;
        public final Line split;
        public final List<Point> leftPts;
        public final List<Point> rightPts;
        public final List<Line> edges;
        public final List<Point> marks;
        public final String label;

        public Frame(int depth, Rect bbox, Point pivot, Line split,
                List<Point> left, List<Point> right,
                List<Line> edges, List<Point> marks, String label) {
            this.depth = depth;
            this.bbox = bbox;
            this.pivot = pivot;
            this.split = split;
            this.leftPts = left != null ? new ArrayList<>(left) : List.of();
            this.rightPts = right != null ? new ArrayList<>(right) : List.of();
            this.edges = edges != null ? new ArrayList<>(edges) : List.of();
            this.marks = marks != null ? new ArrayList<>(marks) : List.of();
            this.label = label;
        }
    }

    private final List<Frame> frames = new ArrayList<>();
    private List<Line> currentEdges = new ArrayList<>();
    private final List<Point> mergeMarks = new ArrayList<>();

    public List<Frame> getFrames() {
        return Collections.unmodifiableList(frames);
    }

    // Division
    @Override
    public void onDivideStart(int depth, Rect bbox, Point pivot, Line split,
            List<Point> L, List<Point> R) {
        frames.add(new Frame(depth, bbox, pivot, split, L, R, currentEdges, List.copyOf(mergeMarks),
                "divide-start depth=" + depth));
    }

    @Override
    public void onDivideEnd(int depth) {
        frames.add(new Frame(depth, null, null, null, null, null, currentEdges, List.copyOf(mergeMarks),
                "divide-end depth=" + depth));
    }

    @Override
    public void onBaseCaseSingle(Point p) {
        frames.add(new Frame(0, null, null, null, List.of(p), List.of(), currentEdges, List.copyOf(mergeMarks),
                "base-1"));
    }

    @Override
    public void onBaseCasePair(Point l, Point r) {
        frames.add(new Frame(0, null, null, null, List.of(l), List.of(r), currentEdges, List.copyOf(mergeMarks),
                "base-2"));
    }

    // Merge
    @Override
    public void onMergeStart(Point a, Point b, Point c, Point d) {
        mergeMarks.clear();
        if (a != null) {
            mergeMarks.add(a);
        }
        if (b != null) {
            mergeMarks.add(b);
        }
        if (c != null) {
            mergeMarks.add(c);
        }
        if (d != null) {
            mergeMarks.add(d);
        }
        frames.add(new Frame(0, null, null, null, null, null, currentEdges, List.copyOf(mergeMarks),
                "merge-start"));
    }

    @Override
    public void onSnapshot(List<Line> edges) {
        currentEdges = new ArrayList<>(edges);
        frames.add(new Frame(0, null, null, null, null, null, currentEdges, List.copyOf(mergeMarks),
                "snapshot"));
    }

    @Override
    public void onFinalized(List<Line> edges) {
        currentEdges = new ArrayList<>(edges);
        frames.add(new Frame(0, null, null, null, null, null, currentEdges, List.copyOf(mergeMarks),
                "final"));
    }

    // ---------------- BinaryTree storyboard ----------------
    public enum BtEventType { ENTER_NODE, VISIT_NODE, EXIT_NODE, HIGHLIGHT_EDGE }

    public static final class BtFrame {
        private final BtEventType type;
        private final String nodeId;
        private final String parentId;
        private final String label;
        public BtFrame(BtEventType type, String nodeId, String parentId, String label) {
            this.type = type; this.nodeId = nodeId; this.parentId = parentId; this.label = label;
        }
        public BtEventType type() { return type; }
        public String nodeId() { return nodeId; }
        public String parentId() { return parentId; }
        public String label() { return label; }
        @Override public String toString() {
            return "BtFrame{" + type + ", node=" + nodeId +
                   (parentId != null ? ", parent=" + parentId : "") +
                   (label != null ? ", label=" + label : "") + "}";
        }
    }

    private final List<BtFrame> btFrames = new ArrayList<>();
    public void clearBt() { btFrames.clear(); }
    public List<BtFrame> getBtFrames() { return Collections.unmodifiableList(btFrames); }
    private void btRecord(BtEventType t, String n, String p, String lab) {
        btFrames.add(new BtFrame(t, n, p, lab));
    }

    // VoronoiListener: BinaryTree events -> frames
    @Override public void onBtEnterNode(String nodeId, String parentId, String label) {
        btRecord(BtEventType.ENTER_NODE, nodeId, parentId, label);
    }

    @Override public void onBtVisitNode(String nodeId, String parentId, String label) {
        btRecord(BtEventType.VISIT_NODE, nodeId, parentId, label);
    }

    @Override public void onBtExitNode(String nodeId, String parentId, String label) {
        btRecord(BtEventType.EXIT_NODE, nodeId, parentId, label);
    }

    @Override public void onBtHighlightEdge(String childId, String parentId) {
        btRecord(BtEventType.HIGHLIGHT_EDGE, childId, parentId, null);
    }

    // (Valgfri) afspilning ved behov
    public void playBt(Consumer<BtFrame> onFrame) {
        for (BtFrame f : btFrames) onFrame.accept(f);
    }


}
