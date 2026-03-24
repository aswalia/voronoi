package asi.voronoi.javafx;

import javafx.scene.paint.Color;

public interface GraphicsAdapter {
    // Fill operations
    void setFill(Color color);
    void fillRect(double x, double y, double width, double height);
    void fillOval(double centerX, double centerY, double width, double height);

    // Stroke operations
    void setStroke(Color color);
    void setLineWidth(double width);
    void strokeLine(double x1, double y1, double x2, double y2);
}