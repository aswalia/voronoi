package asi.voronoi.anim;

import asi.voronoi.Line;
import asi.voronoi.Point;
import java.util.*;

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

        public Frame() {
            // used for testing purpose
            this.depth = 0;
            this.bbox = null;
            this.pivot = null;
            this.split = null;
            this.leftPts = null;
            this.rightPts = null;
            this.edges = null;
            this.marks = null;
            this.label = null;            
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
}
