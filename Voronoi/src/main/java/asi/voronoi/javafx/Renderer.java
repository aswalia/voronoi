package asi.voronoi.javafx;

import asi.voronoi.Point;
import asi.voronoi.anim.StoryboardRecorder;
import javafx.scene.paint.Color;

import java.util.List;

public class Renderer {

    public void renderFrame(GraphicsAdapter adapter, StoryboardRecorder.Frame frame, List<Point> allSites, ZoomPanController zoomPan) {
        // Clear the canvas
        adapter.setFill(Color.WHITE);
        adapter.fillRect(0, 0, zoomPan.getCanvasWidth(), zoomPan.getCanvasHeight());

        // Render all sites (as filled ovals for points)
        adapter.setFill(Color.web("#444444"));
        if (allSites != null) {
            for (Point site : allSites) {
                adapter.fillOval(zoomPan.sx(site.x()) - 2.5, zoomPan.sy(site.y()) - 2.5, 5, 5);
            }
        }

        // Render the frame
        if (frame != null) {
            // Bounding box
            if (frame.bbox != null) {
                var b = frame.bbox;
                adapter.setFill(Color.web("#6c5ce7", 0.1));
                adapter.fillRect(
                    zoomPan.sx(b.xMin()), zoomPan.sy(b.yMax()), // Top-left
                    zoomPan.sx(b.xMax()) - zoomPan.sx(b.xMin()), // Width
                    zoomPan.sy(b.yMin()) - zoomPan.sy(b.yMax())  // Height
                );
            }

            // Edges
            adapter.setStroke(Color.web("#2f80ed"));
            adapter.setLineWidth(2.0);
            if (frame.edges != null) {
                for (asi.voronoi.Line edge : frame.edges) {
                    drawEdge(adapter, edge, zoomPan);
                }
            }
        }
    }

    private void drawEdge(GraphicsAdapter adapter, asi.voronoi.Line edge, ZoomPanController zoomPan) {
        var start = edge.getBeginP().orElse(null);
        var end = edge.getEndP().orElse(null);
        if (start != null && end != null) {
            adapter.strokeLine(
                zoomPan.sx(start.x()), zoomPan.sy(start.y()),
                zoomPan.sx(end.x()), zoomPan.sy(end.y())
            );
        }
    }
}