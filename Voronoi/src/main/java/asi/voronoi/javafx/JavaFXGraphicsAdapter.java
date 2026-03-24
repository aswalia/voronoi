package asi.voronoi.javafx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class JavaFXGraphicsAdapter implements GraphicsAdapter {

    private final GraphicsContext gc;

    public JavaFXGraphicsAdapter(GraphicsContext gc) {
        this.gc = gc;
    }

    @Override
    public void setFill(Color color) {
        gc.setFill(color);
    }

    @Override
    public void fillRect(double x, double y, double width, double height) {
        gc.fillRect(x, y, width, height);
    }

    @Override
    public void fillOval(double centerX, double centerY, double width, double height) {
        gc.fillOval(centerX, centerY, width, height);
    }

    @Override
    public void setStroke(Color color) {
        gc.setStroke(color);
    }

    @Override
    public void setLineWidth(double width) {
        gc.setLineWidth(width);
    }

    @Override
    public void strokeLine(double x1, double y1, double x2, double y2) {
        gc.strokeLine(x1, y1, x2, y2);
    }
}