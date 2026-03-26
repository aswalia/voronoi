package asi.voronoi.web;

import asi.voronoi.DCEL;
import asi.voronoi.DCELNode;
import asi.voronoi.Line;
import asi.voronoi.Point;
import asi.voronoi.anim.StoryboardRecorder;
import asi.voronoi.anim.VoronoiEvents;
import asi.voronoi.tree.AVLTree;
import asi.voronoi.tree.VTree;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
public class VoronoiController {

    public static record PointDTO(double x, double y) {}
    public static record ComputeRequest(double width, double height, List<PointDTO> points) {}
    public static record EdgeDTO(double x1, double y1, double x2, double y2) {}
    public static record AnimateRequest(double width, double height, List<PointDTO> points) {}
    public static record FrameDTO(int index, String label, List<EdgeDTO> edges) {}
    public static record AnimationResponse(List<FrameDTO> frames) {}
    
    @PostMapping("/compute")
    public List<EdgeDTO> computeVoronoi(@RequestBody ComputeRequest inputPoints) {
        if (inputPoints == null || inputPoints.points.size() < 3) {
            return List.of();
        }

        // Convert DTO -> your Point
        List<Point> pts = inputPoints.points.stream()
                .map(p -> new Point(p.x(), p.y()))
                .toList();

        AVLTree btree = new AVLTree(pts.get(0));
        for (int i = 1; i < pts.size(); i++) {
            btree = (AVLTree) btree.insertNode(pts.get(i));
        }

        VTree vTree = new VTree();
        vTree.buildStructure(btree);

        DCEL root = vTree.getInfo();
        if (root == null || root.getNode() == null) return List.of();

        List<EdgeDTO> edgesOut = new ArrayList<>();
        for (DCELNode dn : root.getNode().getVoronoiEdgeList()) {
            Line seg = dn.getLineSegment();
            edgesOut.add(drawLineSegment(seg));
        }
        return edgesOut;
    }
    
    @PostMapping("/animate")
    public AnimationResponse animate(@RequestBody AnimateRequest req) {
        if (req == null || req.points() == null || req.points().size() < 3) {
            return new AnimationResponse(List.of());
        }

        // Convert points
        List<Point> pts = req.points().stream()
                .map(p -> new Point(p.x(), p.y()))
                .toList();

        // Attach recorder to event bus
        var recorder = new StoryboardRecorder();
        VoronoiEvents.clear();
        VoronoiEvents.add(recorder);

        // Build tree + compute (this should fire snapshots/final)
        var btree = new AVLTree(pts.get(0));
        for (int i = 1; i < pts.size(); i++) {
            btree = (AVLTree) btree.insertNode(pts.get(i));
        }
        var vTree = new asi.voronoi.tree.VTree();
        vTree.buildStructure(btree);

        // Convert recorder frames to JSON-friendly DTOs
        List<FrameDTO> framesOut = new ArrayList<>();
        var frames = recorder.getFrames();

        for (int i = 0; i < frames.size(); i++) {
            var f = frames.get(i);

            List<EdgeDTO> edges = new ArrayList<>();
            for (Line l : f.edges) {
                edges.add(drawLineSegment(l));
            }

            framesOut.add(new FrameDTO(i, f.label, edges));
        }

        return new AnimationResponse(framesOut);
    }

    private EdgeDTO drawLineSegment(asi.voronoi.Line ln) {
        EdgeDTO dtoLine;
        var b = ln.getBeginP().orElse(null);
        var e = ln.getEndP().orElse(null);
        if (b != null && e != null) {
            dtoLine = new EdgeDTO(b.x(), b.y(), e.x(), e.y());
        } else if (b != null) {
            var d = ln.getDir();
            double x2 = b.x() + d.x()*10, y2 = b.y() + d.y()*10;
            dtoLine = new EdgeDTO(b.x(), b.y(), x2, y2);            
        } else if (e != null) {
            var d = ln.getDir();
            double x1 = e.x() - d.x()*10, y1 = e.y() - d.y()*10;
            dtoLine = new EdgeDTO(x1, y1, e.x(), e.y());
        } else {
            var m = ln.getMidP();
            var d = ln.getDir();
            double x1 = m.x() - d.x()*10, y1 = m.y() - d.y()*10;
            double x2 = m.x() + d.x()*10, y2 = m.y() + d.y()*10;
            dtoLine = new EdgeDTO(x1, y1, x2, y2);
        }
        return dtoLine;
    }   
}