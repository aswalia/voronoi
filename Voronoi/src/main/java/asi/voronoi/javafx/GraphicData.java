package asi.voronoi.javafx;

import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;



public class GraphicData {
    public GraphicsContext c;
    public Bounds b;

    public GraphicData(GraphicsContext c, Bounds b) {
        this.c = c;
        this.b = b;
    }
}
