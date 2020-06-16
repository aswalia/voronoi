/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asi.voronoi;

/**
 *
 * @author asi
 */
public class PointPair {
    private final Point lft;
    private final Point rgt;
    
    public PointPair(Point l, Point r) {
        lft = l;
        rgt = r;
    }
    
    public Point getLft() {
        return lft;
    }
    
    public Point getRgt() {
        return rgt;
    }
}
