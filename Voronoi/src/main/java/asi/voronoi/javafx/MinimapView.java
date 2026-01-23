package asi.voronoi.javafx;

import asi.voronoi.Point;
import asi.voronoi.anim.StoryboardRecorder;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

/**
 * Manages the minimap overlay Canvas for displaying an overview of the entire diagram.
 * Handles layout, drawing, and mouse interactions for the minimap.
 */
class MinimapView {
    private final Canvas canvas;
    private boolean enabled = true;

    enum Position {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
    private Position position = Position.BOTTOM_RIGHT;
    private double width = 220;
    private double height = 160;
    private final double pad = 6; // inner padding

    // Computed for drawing (world->minimap transform)
    private double mmScale = 1.0;
    private double mmOx = 0.0;
    private double mmOy = 0.0;

    // Drag state
    private boolean draggingRect = false;
    private double dragStartX = 0;
    private double dragStartY = 0;
    private double viewStartXmin;
    private double viewStartXmax;
    private double viewStartYmin;
    private double viewStartYmax;

    // Callbacks for interactions
    private MinimapInteractionHandler interactionHandler;

    MinimapView() {
        canvas = new Canvas(width, height);
        canvas.setPickOnBounds(false);
        installInteractions();
    }

    /**
     * Gets the canvas node to add to the scene graph.
     */
    Canvas getCanvas() {
        return canvas;
    }

    /**
     * Sets whether the minimap is enabled/visible.
     */
    void setEnabled(boolean enabled) {
        this.enabled = enabled;
        canvas.setVisible(enabled);
    }

    /**
     * Sets the minimap position.
     */
    void setPosition(Position pos) {
        this.position = pos;
    }

    /**
     * Gets the current position.
     */
    Position getPosition() {
        return position;
    }

    /**
     * Sets the minimap size in pixels.
     */
    void setSize(double w, double h) {
        this.width = w;
        this.height = h;
        canvas.setWidth(w);
        canvas.setHeight(h);
    }

    /**
     * Lays out the minimap based on parent dimensions and current position.
     */
    void layoutMinimap(double parentW, double parentH) {
        canvas.setVisible(enabled);
        if (!enabled) {
            return;
        }
        double margin = 10;
        switch (position) {
            case TOP_LEFT -> {
                canvas.setLayoutX(margin);
                canvas.setLayoutY(margin);
            }
            case TOP_RIGHT -> {
                canvas.setLayoutX(parentW - width - margin);
                canvas.setLayoutY(margin);
            }
            case BOTTOM_LEFT -> {
                canvas.setLayoutX(margin);
                canvas.setLayoutY(parentH - height - margin);
            }
            case BOTTOM_RIGHT -> {
                canvas.setLayoutX(parentW - width - margin);
                canvas.setLayoutY(parentH - height - margin);
            }
        }
        // Ensure size is correct
        if (canvas.getWidth() != width) {
            canvas.setWidth(width);
        }
        if (canvas.getHeight() != height) {
            canvas.setHeight(height);
        }
    }

    /**
     * Draws the minimap showing sites, edges, and current view rectangle.
     */
    void drawMinimap(List<Point> sites, ZoomPanController zoomPan, StoryboardRecorder.Frame currentFrame) {
        if (!enabled) {
            return;
        }
        WorldBounds world = zoomPan.getWorldBounds();
        if (world == null) {
            return;
        }

        GraphicsContext g = canvas.getGraphicsContext2D();
        
        // Background and border
        g.setFill(Color.rgb(250, 250, 250, 0.92));
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setStroke(Color.GRAY);
        g.setLineWidth(1);
        g.strokeRect(0.5, 0.5, canvas.getWidth() - 1, canvas.getHeight() - 1);

        // Fit world to minimap with letterboxing
        double contentW = canvas.getWidth() - 2 * pad;
        double contentH = canvas.getHeight() - 2 * pad;
        double wW = world.xmax - world.xmin;
        double wH = world.ymax - world.ymin;
        if (wW <= 0 || wH <= 0) {
            return;
        }
        double sx = contentW / wW;
        double sy = contentH / wH;
        mmScale = Math.min(sx, sy);
        mmOx = pad + (contentW - wW * mmScale) / 2.0;
        mmOy = pad + (contentH - wH * mmScale) / 2.0;

        // Draw sites
        g.setFill(Color.web("#444444"));
        g.setGlobalAlpha(0.85);
        for (Point p : sites) {
            double mx = mmx(p.x(), world);
            double my = mmy(p.y(), world);
            g.fillRect(mx - 1, my - 1, 2, 2);
        }
        g.setGlobalAlpha(1.0);

        // Draw edges (light, for overview)
        if (currentFrame != null && currentFrame.edges != null) {
            g.setStroke(Color.web("#2f80ed", 0.35));
            g.setLineWidth(0.6);
            for (asi.voronoi.Line ln : currentFrame.edges) {
                Point b = ln.getBeginP().orElse(null);
                Point e = ln.getEndP().orElse(null);
                if (b != null && e != null) {
                    g.strokeLine(mmx(b.x(), world), mmy(b.y(), world), 
                                mmx(e.x(), world), mmy(e.y(), world));
                } else if (b != null) {
                    Point d = ln.getDir();
                    double x2 = b.x() + d.x() * 2000;
                    double y2 = b.y() + d.y() * 2000;
                    g.strokeLine(mmx(b.x(), world), mmy(b.y(), world), 
                                mmx(x2, world), mmy(y2, world));
                } else if (e != null) {
                    Point d = ln.getDir();
                    double x1 = e.x() - d.x() * 2000;
                    double y1 = e.y() - d.y() * 2000;
                    g.strokeLine(mmx(x1, world), mmy(y1, world), 
                                mmx(e.x(), world), mmy(e.y(), world));
                } else {
                    Point m = ln.getMidP();
                    Point d = ln.getDir();
                    double x1 = m.x() - d.x() * 2000;
                    double y1 = m.y() - d.y() * 2000;
                    double x2 = m.x() + d.x() * 2000;
                    double y2 = m.y() + d.y() * 2000;
                    g.strokeLine(mmx(x1, world), mmy(y1, world), 
                                mmx(x2, world), mmy(y2, world));
                }
            }
        }

        // Draw view rectangle
        double vx1 = mmx(zoomPan.getViewXmin(), world);
        double vy1 = mmy(zoomPan.getViewYmax(), world); // top-left (Y invert)
        double vx2 = mmx(zoomPan.getViewXmax(), world);
        double vy2 = mmy(zoomPan.getViewYmin(), world); // bottom-right
        double rw = Math.abs(vx2 - vx1);
        double rh = Math.abs(vy2 - vy1);
        double rx = Math.min(vx1, vx2);
        double ry = Math.min(vy1, vy2);
        g.setGlobalAlpha(0.15);
        g.setFill(Color.ORANGE);
        g.fillRect(rx, ry, rw, rh);
        g.setGlobalAlpha(1.0);
        g.setStroke(Color.ORANGE);
        g.setLineWidth(1.4);
        g.strokeRect(rx + 0.5, ry + 0.5, Math.max(0, rw - 1), Math.max(0, rh - 1));
    }

    /**
     * Sets the interaction handler for minimap events.
     */
    void setInteractionHandler(MinimapInteractionHandler handler) {
        this.interactionHandler = handler;
    }

    private void installInteractions() {
        canvas.setOnMousePressed(ev -> {
            if (!enabled || interactionHandler == null) {
                return;
            }
            WPoint pt = minimapToWorld(ev.getX(), ev.getY());
            if (pt == null) {
                return;
            }
            // Check if inside view rect => start dragging
            if (interactionHandler.isInsideViewRect(pt.x, pt.y)) {
                draggingRect = true;
                dragStartX = ev.getX();
                dragStartY = ev.getY();
                viewStartXmin = interactionHandler.getViewXmin();
                viewStartXmax = interactionHandler.getViewXmax();
                viewStartYmin = interactionHandler.getViewYmin();
                viewStartYmax = interactionHandler.getViewYmax();
            } else {
                // Click: center view on point
                interactionHandler.centerViewOn(pt.x, pt.y);
            }
        });

        canvas.setOnMouseDragged(ev -> {
            if (!enabled || !draggingRect || interactionHandler == null) {
                return;
            }
            WPoint p0 = minimapToWorld(dragStartX, dragStartY);
            WPoint p1 = minimapToWorld(ev.getX(), ev.getY());
            if (p0 == null || p1 == null) {
                return;
            }
            double dx = p1.x - p0.x;
            double dy = p1.y - p0.y;
            interactionHandler.setViewBounds(
                viewStartXmin + dx, viewStartXmax + dx,
                viewStartYmin + dy, viewStartYmax + dy
            );
        });

        canvas.setOnMouseReleased(ev -> draggingRect = false);

        canvas.setOnScroll(ev -> {
            if (!enabled || interactionHandler == null) {
                return;
            }
            WPoint pt = minimapToWorld(ev.getX(), ev.getY());
            if (pt == null) {
                return;
            }
            double factor = (ev.getDeltaY() > 0) ? 1.2 : (1 / 1.2);
            interactionHandler.zoomAtWorldPoint(factor, pt.x, pt.y);
            ev.consume();
        });
    }

    private double mmx(double x, WorldBounds world) {
        return mmOx + (x - world.xmin) * mmScale;
    }

    private double mmy(double y, WorldBounds world) {
        // Flip Y in minimap: (0,0) top-left
        return canvas.getHeight() - (mmOy + (y - world.ymin) * mmScale);
    }

    private static class WPoint {
        double x;
        double y;
        WPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private WPoint minimapToWorld(double mx, double my) {
        ZoomPanController zoomPan = interactionHandler != null ? interactionHandler.getZoomPanController() : null;
        if (zoomPan == null) {
            return null;
        }
        WorldBounds world = zoomPan.getWorldBounds();
        if (world == null) {
            return null;
        }
        // Check if within content (including letterbox)
        double contentLeft = mmOx;
        double contentTop = mmOy;
        double contentRight = canvas.getWidth() - mmOx;
        double contentBottom = canvas.getHeight() - mmOy;
        if (mx < contentLeft || mx > contentRight || 
            my < (canvas.getHeight() - contentBottom) || 
            my > (canvas.getHeight() - contentTop)) {
            return null;
        }
        double wx = world.xmin + (mx - mmOx) / mmScale;
        double wy = world.ymin + ((canvas.getHeight() - my) - mmOy) / mmScale;
        return new WPoint(wx, wy);
    }

    /**
     * Interface for minimap to communicate with the main view.
     */
    interface MinimapInteractionHandler {
        boolean isInsideViewRect(double wx, double wy);
        void centerViewOn(double wx, double wy);
        void setViewBounds(double xmin, double xmax, double ymin, double ymax);
        void zoomAtWorldPoint(double factor, double wx, double wy);
        double getViewXmin();
        double getViewXmax();
        double getViewYmin();
        double getViewYmax();
        ZoomPanController getZoomPanController();
    }
}
