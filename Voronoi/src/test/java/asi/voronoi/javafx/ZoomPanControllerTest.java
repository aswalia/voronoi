package asi.voronoi.javafx;

import javafx.scene.canvas.Canvas;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;


public class ZoomPanControllerTest {

    private ZoomPanController zoomPanController;
    private Canvas canvas;

    @Before
    public void setUp() {
        canvas = new Canvas(800, 600); // Fake canvas size
        zoomPanController = new ZoomPanController(canvas, 10); // 10px padding
        WorldBounds bounds = new WorldBounds(0, 0, 1000, 1000);
        zoomPanController.setWorldBounds(bounds);
    }

    @Test
    public void testWorldToScreenConversion() {
        double sx = zoomPanController.sx(0); // World (0,0) should map correctly
        double sy = zoomPanController.sy(0);
        assertTrue("Screen x should be within the canvas width", sx >= 0 && sx <= canvas.getWidth());
        assertTrue("Screen y should be within the canvas height", sy >= 0 && sy <= canvas.getHeight());
    }

    @Test
    public void testScreenToWorldConversion() {
        double wx = zoomPanController.screenToWorldX(400); // Midpoint of canvas
        double wy = zoomPanController.screenToWorldY(300);
        assertTrue("World x should map back to bounds", wx >= 0 && wx <= 1000);
        assertTrue("World y should map back to bounds", wy >= 0 && wy <= 1000);
    }

    @Test
    public void testResetView() {
        zoomPanController.zoomAtCenter(2); // Zoom in (2x)
        zoomPanController.resetView(); // Reset view to initial bounds
        assertEquals(1000, zoomPanController.getWorldBounds().width(), 0.001);
    }

    @Test
    public void testPanByScreen() {
        zoomPanController.panByScreen(100, 100); // Pan by 100x100 pixels
        // Assert that the view has shifted
        assertTrue("View xmin should decrease after panning by positive dx", zoomPanController.getViewXmin() < 0);
        assertTrue("View ymin should increase after panning by positive dy", zoomPanController.getViewYmin() > 0);
    }
}