package asi.voronoi;

public class Line {

    public void setStartToEnd(Point s, Point e) {
        beginP = s;
        endP = e;
    }

    public void setMidAndDir(Point m, Point d) {
        midP = m;
        dir = d;
    }

    public void setBeginP(Point p) {
        beginP = p;
    }

    public void setEndP(Point p) {
        endP = p;
    }

    public void setMidP(Point p) {
        midP = p;
    }

    public void setDir(Point p) {
        dir = p;
    }

    public Point getMidP() {
        return midP;
    }

    public Point getDir() {
        return dir;
    }

    public java.util.Optional<Point> getBeginP() {
        return java.util.Optional.ofNullable(beginP);
    }

    public java.util.Optional<Point> getEndP() {
        return java.util.Optional.ofNullable(endP);
    }

    private Point endP;
    private Point beginP;
    private Point midP;
    private Point dir;

}
