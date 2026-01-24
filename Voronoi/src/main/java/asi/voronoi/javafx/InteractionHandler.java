package asi.voronoi.javafx;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class InteractionHandler {
    private final ZoomPanController zoomPan;
    private final Canvas canvas;

    public InteractionHandler(Canvas canvas, ZoomPanController zoomPan) {
        this.canvas = canvas;
        this.zoomPan = zoomPan;

        setupListeners();
    }

    private void setupListeners() {
        // Mouse drag (panning)
        canvas.setOnMouseDragged(this::handleMouseDrag);

        // Zoom using scroll
        canvas.setOnScroll(this::handleScroll);
    }

    private void handleMouseDrag(MouseEvent event) {
        zoomPan.panByScreen(event.getSceneX(), event.getSceneY());
    }

    private void handleScroll(ScrollEvent event) {
        if (event.getDeltaY() > 0) {
            zoomPan.zoomAt(event.getX(), event.getY(), 1.1); // Zoom in
        } else {
            zoomPan.zoomAt(event.getX(), event.getY(), 0.9); // Zoom out
        }
    }
}