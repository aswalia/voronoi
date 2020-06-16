package asi.voronoi;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

public class DrawingBoard extends JFrame {
    private static Color bg = Color.white;
    private static Color fg = Color.blue;

    private boolean isGeometry = true;
    private Image myOffScreenImage;
    private Graphics myOffScreenGraphics;
    DrawObject dObj;
    JPanel p;
    JScrollBar horizontalMove, horizontalZoom;
    int zoomIn = 0;

    private class ScrollEvent implements AdjustmentListener {
        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            double s, tx = 0, ty = 0;
            if (e.getAdjustable().getOrientation() == Adjustable.HORIZONTAL) {
                // one of the horirontal scrollbars
                if (e.getSource().equals(horizontalZoom)) {
                    // the top scrollbar
//                    if (e.getValue() > zoomIn) {
//                        // scrolling to the right
//                        s = 1;
//                    } else {
//                        s = -1;
//                    }
                    s = e.getValue();
                    dObj.setScale(s);
                } else {
                    // the bottom scrollbar
                    tx = e.getValue();
                    dObj.setTranslation(tx, ty);
                }
            } else {
                // the vertical scrollbar
                ty = e.getValue();
                dObj.setTranslation(tx, ty);
            }
            repaint();
        }
    }

    public DrawingBoard(DrawObject dObj) {
        this.dObj = dObj;
        p = new JPanel();
        setTitle("DrawingBoard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().add("Center", p);
        horizontalMove = new JScrollBar(JScrollBar.HORIZONTAL, 0, 10, -200, 200);
        JScrollBar verticalMove = new JScrollBar(JScrollBar.VERTICAL, 0, 10, -200, 200);
        getContentPane().add("South", horizontalMove);
        getContentPane().add("East", verticalMove);
        horizontalZoom = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, -100, 100);
        getContentPane().add("North", horizontalZoom);
        horizontalMove.addAdjustmentListener(new ScrollEvent());
        verticalMove.addAdjustmentListener(new ScrollEvent());
        horizontalZoom.addAdjustmentListener(new ScrollEvent());
        setBackground(bg);
        setForeground(fg);
        pack();
        setSize(new Dimension(700, 700));
        setVisible(true);
    }

    public void setGeometry(boolean geo) {
        isGeometry = geo;
    }

    public boolean getGeometry() {
        return isGeometry;
    }

    @Override
    public void update(Graphics g) {
        myOffScreenImage = createImage(getWidth(), getHeight());
        myOffScreenGraphics = myOffScreenImage.getGraphics();
        myOffScreenGraphics.clearRect(0, 0, getWidth(), getHeight());
        paint(myOffScreenGraphics);
        g.drawImage(myOffScreenImage, 0, 0, p);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (dObj != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Dimension d = getSize();
            GraphicData gd = new GraphicData(g2, d);
            if (isGeometry) {
                setTitle("DrawingBoard - Geometric View - " + dObj.objectTitle());
                dObj.drawGeometry(gd);
            } else {
                setTitle("DrawingBoard - Object Representation View - " + dObj.objectTitle());
                dObj.drawRepresentation(gd);
            }
        }
    }
}