package asi.voronoi.javafx;

import asi.voronoi.Point;
import asi.voronoi.anim.StoryboardRecorder;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Minimap overlay canvas that shows a bird's-eye view of the world with current viewport indicator.
 * Supports interactive dragging and clicking for navigation.
 */
public class MinimapView extends Canvas {
    private WorldBounds worldBounds;
    private ZoomPanController zoomPan;
    private List<Point> sites = List.of();
    private StoryboardRecorder.Frame currentFrame;
    
    private boolean enabled = true;
    private MinimapPos position = MinimapPos.BOTTOM_RIGHT;
    private final double miniPad = 6;
    
    // Transform state (world -> minimap)
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
    private Consumer<WorldPoint> onCenterRequest;
    private BiConsumer<WorldPoint, Double> onZoomRequest;
    private Runnable onRedraw;

    public enum MinimapPos {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public MinimapView() {
        super(220, 160);
        setPickOnBounds(false);
        installInteractions();
    }

    /**
     * Set world bounds for minimap rendering.
     */
    public void setWorldBounds(WorldBounds bounds) {
        this.worldBounds = bounds;
    }

    /**
     * Set zoom/pan controller for view rect rendering.
     */
    public void setZoomPanController(ZoomPanController zoomPan) {
        this.zoomPan = zoomPan;
    }

    /**
     * Set sites to display in minimap.
     */
    public void setSites(List<Point> sites) {
        this.sites = sites != null ? sites : List.of();
    }

    /**
     * Set current frame for edge rendering in minimap.
     */
    public void setCurrentFrame(StoryboardRecorder.Frame frame) {
        this.currentFrame = frame;
    }

    /**
     * Enable or disable minimap rendering.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setVisible(enabled);
    }

    /**
     * Check if minimap is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set minimap corner position.
     */
    public void setPosition(MinimapPos pos) {
        this.position = pos;
    }

    /**
     * Get current minimap position.
     */
    public MinimapPos getPosition() {
        return position;
    }

    /**
     * Set callback for when user clicks outside view rect to center.
     */
    public void setOnCenterRequest(Consumer<WorldPoint> callback) {
        this.onCenterRequest = callback;
    }

    /**
     * Set callback for zoom events.
     */
    public void setOnZoomRequest(BiConsumer<WorldPoint, Double> callback) {
        this.onZoomRequest = callback;
    }

    /**
     * Set callback for redraw events.
     */
    public void setOnRedraw(Runnable callback) {
        this.onRedraw = callback;
    }

    /**
     * Layout minimap within parent pane with margin.
     */
    public void layoutInPane(Pane parent, double margin) {
        if (!enabled) {
            setVisible(false);
            return;
        }
        setVisible(true);
        
        double parentW = parent.getWidth();
        double parentH = parent.getHeight();
        
        switch (position) {
            case TOP_LEFT -> {
                setLayoutX(margin);
                setLayoutY(margin);
            }
            case TOP_RIGHT -> {
                setLayoutX(parentW - getWidth() - margin);
                setLayoutY(margin);
            }
            case BOTTOM_LEFT -> {
                setLayoutX(margin);
                setLayoutY(parentH - getHeight() - margin);
            }
            case BOTTOM_RIGHT -> {
                setLayoutX(parentW - getWidth() - margin);
                setLayoutY(parentH - getHeight() - margin);
            }
        }
    }

    /**
     * Draw the minimap.
     */
    public void draw() {
        if (!enabled || worldBounds == null || zoomPan == null) {
            return;
        }
        
        GraphicsContext g = getGraphicsContext2D();
        
        // Background and border
        g.setFill(Color.rgb(250, 250, 250, 0.92));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setStroke(Color.GRAY);
        g.setLineWidth(1);
        g.strokeRect(0.5, 0.5, getWidth() - 1, getHeight() - 1);
        
        // World fit to minimap with letterboxing
        double contentW = getWidth() - 2 * miniPad;
        double contentH = getHeight() - 2 * miniPad;
        double wW = worldBounds.width();
        double wH = worldBounds.height();
        
        if (wW <= 0 || wH <= 0) {
            return;
        }
        
        double sx = contentW / wW;
        double sy = contentH / wH;
        mmScale = Math.min(sx, sy);
        mmOx = miniPad + (contentW - wW * mmScale) / 2.0;
        mmOy = miniPad + (contentH - wH * mmScale) / 2.0;
        
        // Draw sites
        g.setFill(Color.web("#444444"));
        g.setGlobalAlpha(0.85);
        for (Point p : sites) {
            double mx = mmx(p.x());
            double my = mmy(p.y());
            g.fillRect(mx - 1, my - 1, 2, 2);
        }
        g.setGlobalAlpha(1.0);
        
        // Draw edges from current frame (optional, thin and low alpha for performance)
        if (currentFrame != null && currentFrame.edges != null) {
            g.setStroke(Color.web("#2f80ed", 0.35));
            g.setLineWidth(0.6);
            for (asi.voronoi.Line ln : currentFrame.edges) {
                Point b = ln.getBeginP().orElse(null);
                Point e = ln.getEndP().orElse(null);
                if (b != null && e != null) {
                    g.strokeLine(mmx(b.x()), mmy(b.y()), mmx(e.x()), mmy(e.y()));
                } else if (b != null) {
                    Point d = ln.getDir();
                    double x2 = b.x() + d.x() * 2000;
                    double y2 = b.y() + d.y() * 2000;
                    g.strokeLine(mmx(b.x()), mmy(b.y()), mmx(x2), mmy(y2));
                } else if (e != null) {
                    Point d = ln.getDir();
                    double x1 = e.x() - d.x() * 2000;
                    double y1 = e.y() - d.y() * 2000;
                    g.strokeLine(mmx(x1), mmy(y1), mmx(e.x()), mmy(e.y()));
                } else {
                    Point m = ln.getMidP();
                    Point d = ln.getDir();
                    double x1 = m.x() - d.x() * 2000;
                    double y1 = m.y() - d.y() * 2000;
                    double x2 = m.x() + d.x() * 2000;
                    double y2 = m.y() + d.y() * 2000;
                    g.strokeLine(mmx(x1), mmy(y1), mmx(x2), mmy(y2));
                }
            }
        }
        
        // Draw view rect (orange)
        double vx1 = mmx(zoomPan.getViewXmin());
        double vy1 = mmy(zoomPan.getViewYmax()); // top-left (Y invert)
        double vx2 = mmx(zoomPan.getViewXmax());
        double vy2 = mmy(zoomPan.getViewYmin()); // bottom-right
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
     * Convert world X to minimap X.
     */
    private double mmx(double x) {
        return mmOx + (x - worldBounds.xmin()) * mmScale;
    }

    /**
     * Convert world Y to minimap Y (Y-flipped).
     */
    private double mmy(double y) {
        return getHeight() - (mmOy + (y - worldBounds.ymin()) * mmScale);
    }

    /**
     * Convert minimap coordinates to world coordinates.
     */
    private WorldPoint minimapToWorld(double mx, double my) {
        // Check if within content area (including letterbox)
        double contentLeft = mmOx;
        double contentTop = mmOy;
        double contentRight = getWidth() - mmOx;
        double contentBottom = getHeight() - mmOy;
        
        if (mx < contentLeft || mx > contentRight || 
            my < (getHeight() - contentBottom) || my > (getHeight() - contentTop)) {
            return null;
        }
        
        double wx = worldBounds.xmin() + (mx - mmOx) / mmScale;
        double wy = worldBounds.ymin() + ((getHeight() - my) - mmOy) / mmScale;
        return new WorldPoint(wx, wy);
    }

    private void installInteractions() {
        setOnMousePressed(ev -> {
            if (!enabled) {
                return;
            }
            WorldPoint pt = minimapToWorld(ev.getX(), ev.getY());
            if (pt == null) {
                return;
            }
            
            // Check if press is inside view rect => start dragging rectangle
            if (zoomPan != null && zoomPan.isInsideViewRect(pt.x, pt.y)) {
                draggingRect = true;
                dragStartX = ev.getX();
                dragStartY = ev.getY();
                viewStartXmin = zoomPan.getViewXmin();
                viewStartXmax = zoomPan.getViewXmax();
                viewStartYmin = zoomPan.getViewYmin();
                viewStartYmax = zoomPan.getViewYmax();
            } else {
                // Click outside: center view on point
                if (onCenterRequest != null) {
                    onCenterRequest.accept(pt);
                }
            }
        });
        
        setOnMouseDragged(ev -> {
            if (!enabled || !draggingRect) {
                return;
            }
            WorldPoint p0 = minimapToWorld(dragStartX, dragStartY);
            WorldPoint p1 = minimapToWorld(ev.getX(), ev.getY());
            if (p0 == null || p1 == null) {
                return;
            }
            
            double dx = p1.x - p0.x;
            double dy = p1.y - p0.y;
            
            // Pan the view by the delta
            if (zoomPan != null) {
                zoomPan.panByScreen(dx / (zoomPan.getViewXmax() - zoomPan.getViewXmin()) * 
                    (zoomPan.sx(zoomPan.getViewXmax()) - zoomPan.sx(zoomPan.getViewXmin())),
                    -dy / (zoomPan.getViewYmax() - zoomPan.getViewYmin()) * 
                    (zoomPan.sy(zoomPan.getViewYmin()) - zoomPan.sy(zoomPan.getViewYmax())));
            }
        });
        
        setOnMouseReleased(ev -> {
            draggingRect = false;
        });
        
        // Scroll wheel zoom in minimap
        setOnScroll(ev -> {
            if (!enabled) {
                return;
            }
            WorldPoint pt = minimapToWorld(ev.getX(), ev.getY());
            if (pt == null) {
                return;
            }
            double factor = (ev.getDeltaY() > 0) ? 1.2 : (1 / 1.2);
            if (onZoomRequest != null) {
                onZoomRequest.accept(pt, factor);
            }
            ev.consume();
        });
    }

    /**
     * Simple world point holder.
     */
    public static class WorldPoint {
        public final double x;
        public final double y;
        
        public WorldPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
