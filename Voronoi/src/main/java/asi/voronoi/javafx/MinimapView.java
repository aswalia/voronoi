package asi.voronoi.javafx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

import asi.voronoi.Point;
import asi.voronoi.anim.StoryboardRecorder;

/**
 * Responsible for minimap canvas, rendering a small overview and view rect.
 */
public class MinimapView {
    public enum MinimapPos { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    private final Canvas minimap;
    private MinimapPos pos = MinimapPos.BOTTOM_RIGHT;
    private boolean enabled = true;
    private double miniW = 200, miniH = 120;
    private final double pad = 4;

    public MinimapView() {
        minimap = new Canvas(miniW, miniH);
    }

    public Canvas getCanvas() { return minimap; }

    public void setSize(double w, double h) {
        this.miniW = w; this.miniH = h;
        minimap.setWidth(w); minimap.setHeight(h);
    }

    public void setPos(MinimapPos p) { this.pos = p; }

    public void setEnabled(boolean e) { this.enabled = e; minimap.setVisible(e); }

    /**
     * Layout the minimap within a parent of size parentW x parentH.
     */
    public void layoutMinimap(double parentW, double parentH) {
        minimap.setVisible(enabled);
        if (!enabled) return;
        double margin = 10;
        double x = 0, y = 0;
        switch (pos) {
            case TOP_LEFT -> {
                x = margin; y = margin;
            }
            case TOP_RIGHT -> {
                x = parentW - miniW - margin; y = margin;
            }
            case BOTTOM_LEFT -> {
                x = margin; y = parentH - miniH - margin;
            }
            case BOTTOM_RIGHT -> {
                x = parentW - miniW - margin; y = parentH - miniH - margin;
            }
        }
        minimap.setLayoutX(x);
        minimap.setLayoutY(y);
        if (minimap.getWidth() != miniW) minimap.setWidth(miniW);
        if (minimap.getHeight() != miniH) minimap.setHeight(miniH);
    }

    /**
     * Draw a minimap matching the original look: background, all sites (or captured points),
     * optional edges for the current frame (thin, semi-transparent), and the view rectangle.
     */
    public void drawMinimap(List<Point> miniSites, ZoomPanController zpc, StoryboardRecorder.Frame currentFrame, List<StoryboardRecorder.Frame> frames, int currentIndex) {
        if (!enabled || zpc.getWorldBounds() == null) {
            return;
        }
        GraphicsContext g = minimap.getGraphicsContext2D();

        // background/frame
        g.setFill(Color.rgb(250, 250, 250, 0.92));
        g.fillRect(0, 0, minimap.getWidth(), minimap.getHeight());
        g.setStroke(Color.GRAY);
        g.setLineWidth(1);
        g.strokeRect(0.5, 0.5, minimap.getWidth() - 1, minimap.getHeight() - 1);

        // compute world->minimap transform with letterboxing
        double contentW = minimap.getWidth() - 2 * pad;
        double contentH = minimap.getHeight() - 2 * pad;
        WorldBounds world = zpc.getWorldBounds();
        double wW = world.xmax - world.xmin;
        double wH = world.ymax - world.ymin;
        if (wW <= 0 || wH <= 0) return;

        double sx = contentW / wW;
        double sy = contentH / wH;
        double mmScale = Math.min(sx, sy);
        double mmOx = pad + (contentW - wW * mmScale) / 2.0;
        double mmOy = pad + (contentH - wH * mmScale) / 2.0;

        // helper transforms (local to minimap)
        java.util.function.DoubleUnaryOperator mmx = x -> mmOx + (x - world.xmin) * mmScale;
        java.util.function.DoubleUnaryOperator mmy = y -> minimap.getHeight() - (mmOy + (y - world.ymin) * mmScale); // flip Y

        // draw sites (or captured)
        g.setFill(Color.web("#444444"));
        g.setGlobalAlpha(0.85);
        if (miniSites != null) {
            for (Point p : miniSites) {
                double mx = mmx.applyAsDouble(p.x());
                double my = mmy.applyAsDouble(p.y());
                g.fillRect(mx - 1, my - 1, 2, 2);
            }
        }
        g.setGlobalAlpha(1.0);

        // optionally draw thin edges for current frame (semi-transparent)
        if (currentFrame != null && currentFrame.edges != null) {
            g.setStroke(Color.web("#2f80ed", 0.35));
            g.setLineWidth(0.6);
            for (asi.voronoi.Line ln : currentFrame.edges) {
                var b = ln.getBeginP().orElse(null);
                var e = ln.getEndP().orElse(null);
                if (b != null && e != null) {
                    g.strokeLine(mmx.applyAsDouble(b.x()), mmy.applyAsDouble(b.y()), mmx.applyAsDouble(e.x()), mmy.applyAsDouble(e.y()));
                } else if (b != null) {
                    var d = ln.getDir();
                    double x2 = b.x() + d.x() * 2000, y2 = b.y() + d.y() * 2000;
                    g.strokeLine(mmx.applyAsDouble(b.x()), mmy.applyAsDouble(b.y()), mmx.applyAsDouble(x2), mmy.applyAsDouble(y2));
                } else if (e != null) {
                    var d = ln.getDir();
                    double x1 = e.x() - d.x() * 2000, y1 = e.y() - d.y() * 2000;
                    g.strokeLine(mmx.applyAsDouble(x1), mmy.applyAsDouble(y1), mmx.applyAsDouble(e.x()), mmy.applyAsDouble(e.y()));
                } else {
                    var m = ln.getMidP();
                    var d = ln.getDir();
                    double x1 = m.x() - d.x() * 2000, y1 = m.y() - d.y() * 2000;
                    double x2 = m.x() + d.x() * 2000, y2 = m.y() + d.y() * 2000;
                    g.strokeLine(mmx.applyAsDouble(x1), mmy.applyAsDouble(y1), mmx.applyAsDouble(x2), mmy.applyAsDouble(y2));
                }
            }
        }

        // view rect (orange)
        double vx1 = mmx.applyAsDouble(zpc.getViewXmin());
        double vy1 = mmy.applyAsDouble(zpc.getViewYmax()); // top-left (note Y flip)
        double vx2 = mmx.applyAsDouble(zpc.getViewXmax());
        double vy2 = mmy.applyAsDouble(zpc.getViewYmin()); // bottom-right

        double rw = Math.abs(vx2 - vx1), rh = Math.abs(vy2 - vy1);
        double rx = Math.min(vx1, vx2), ry = Math.min(vy1, vy2);

        g.setGlobalAlpha(0.15);
        g.setFill(Color.ORANGE);
        g.fillRect(rx, ry, rw, rh);
        g.setGlobalAlpha(1.0);
        g.setStroke(Color.ORANGE);
        g.setLineWidth(1.4);
        g.strokeRect(rx + 0.5, ry + 0.5, Math.max(0, rw - 1), Math.max(0, rh - 1));
    }
}
