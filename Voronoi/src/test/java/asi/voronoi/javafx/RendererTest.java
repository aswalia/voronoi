package asi.voronoi.javafx;

import asi.voronoi.Point;
import asi.voronoi.anim.StoryboardRecorder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.*;

import java.util.List;

import static org.mockito.Mockito.*;

public class RendererTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule(); // Initializes mocks for JUnit 4

    @InjectMocks
    private Renderer renderer; // Automatically inject mocks into Renderer

    @Mock
    private GraphicsAdapter adapter; // Mock GraphicsAdapter

    @Mock
    private ZoomPanController zoomPan; // Mock ZoomPanController

    @Before
    public void setUp() {
        // Define default mock behaviors for ZoomPanController
        lenient().when(zoomPan.sx(anyDouble())).thenReturn(100.0);
        lenient().when(zoomPan.sy(anyDouble())).thenReturn(100.0);
        lenient().when(zoomPan.getCanvasWidth()).thenReturn(800.0);
        lenient().when(zoomPan.getCanvasHeight()).thenReturn(600.0);
    }

    @Test
    public void testRenderFrameWithPoints() {
        List<Point> points = List.of(new Point(100, 200), new Point(300, 400));

        // Call the method under test
        renderer.renderFrame(adapter, null, points, zoomPan);

        // Verify draw calls
        verify(adapter, atLeastOnce()).fillOval(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    public void testRenderFrameWithEdges() {
        StoryboardRecorder.Frame frame = new StoryboardRecorder.Frame();

        // Call the method under test
        renderer.renderFrame(adapter, frame, null, zoomPan);

        // Verify draw calls
        verify(adapter, atLeastOnce()).setStroke(any());
//        verify(adapter, atLeastOnce()).strokeLine(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }
}