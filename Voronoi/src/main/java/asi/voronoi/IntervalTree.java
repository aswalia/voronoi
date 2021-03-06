package asi.voronoi;

import java.io.*;
import java.util.StringTokenizer;

public class IntervalTree implements java.io.Serializable, ModelObject {
    private static String iToken;
    private static BufferedReader br;
    private static StringTokenizer st;

    protected Object info;
    protected IntervalTree lft, rgt;

    public IntervalTree newNode() {
        return new IntervalTree();
    }

    public boolean isLeaf() {
        return (lft == null) && (rgt == null);
    }

    public void buildTree(String filename) throws IOException {
        FileReader fr;
        fr = new FileReader(filename);
        br = new BufferedReader(fr);
        buildTreeFromFile();
    }

    public Object getInfo() {
        return info;
    }

    public void buildTree(BinaryTree b) {
        if (b.isLeaf()) {
            info = b.getP();
        } else if (b.lft() == null) {
            lft = newNode();
            lft.info = b.getP();
            rgt = newNode();
            rgt.buildTree(b.rgt());
        } else if (b.rgt() == null) {
            rgt = newNode();
            rgt.info = b.getP();
            lft = newNode();
            lft.buildTree(b.lft());
        } else if (b.getP().isLess(Point.average(b.min(), b.max()))) {
            lft = newNode();
            lft.rgt = newNode();
            lft.rgt.info = b.getP();
            lft.lft = newNode();
            lft.lft.buildTree(b.lft());
            rgt = newNode();
            rgt.buildTree(b.rgt());
        } else {
            rgt = newNode();
            rgt.lft = newNode();
            rgt.lft.info = b.getP();
            rgt.rgt = newNode();
            rgt.rgt.buildTree(b.rgt());
            lft = newNode();
            lft.buildTree(b.lft());
        }
    }

    public void buildStructure() {
        System.out.println("Empty build");
    }

    public IntervalTree lft() {
        return lft;
    }

    public IntervalTree rgt() {
        return rgt;
    }

    public void writeTree(String filename) throws IOException {
        try (FileWriter fw = new FileWriter(filename); 
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(this.toString());
        }
    }

    @Override
    public double maxX() {
        double ret;
        if (isLeaf()) {
            ret = ((Point) info).x();
        } else {
            ret = rgt.maxX();
        }
        return ret;
    }

    @Override
    public double maxY() {
        double ret = 0, y;
        if (isLeaf()) {
            if ((y = ((Point) info).y()) > ret) {
                ret = y;
            }
        } else {
            double retr, retl;
            retl = lft.maxY();
            retr = rgt.maxY();
            ret = (retl > retr) ? retl : retr;
        }
        return ret;
    }

    @Override
    public double minX() {
        double ret;
        if (isLeaf()) {
            ret = ((Point) info).x();
        } else {
            ret = lft.minX();
        }
        return ret;
    }

    @Override
    public double minY() {
        double ret = Integer.MAX_VALUE, y;
        if (isLeaf()) {
            if ((y = ((Point) info).y()) < ret) {
                ret = y;
            }
        } else {
            double retr, retl;
            retl = lft.minY();
            retr = rgt.minY();
            ret = (retl < retr) ? retl : retr;
        }
        return ret;
    }

    public static IntervalTree fetch(String filename) throws java.io.IOException, ClassNotFoundException {
        return (IntervalTree) Serializer.fetch(filename);
    }

    @Override
    public String toString() {
        String ret = "";
        if (lft.isLeaf() && rgt.isLeaf()) {
            ret += "node-point:" + lft.info + " " + rgt.info + "\n"; 
        } else if (lft.isLeaf()) {
            ret += "lft-point:" + lft.info + "\n";
            ret += rgt;
        } else if (rgt.isLeaf()) {
            ret += "rgt-point:" + rgt.info + "\n";
            ret += lft;            
        } else {
            ret += "node\n";
            ret += lft;
            ret += rgt;
        }
        return ret;
    }

    private void getNextToken() throws IOException  {
        String s, delim = " " + ":" + "(" + ")" + "," + "\t\n\r\f";        
        s = br.readLine();
        if (s != null) {
            st = new StringTokenizer(s, delim);
            iToken = st.nextToken();
        }        
    }
    
    private IntervalTree parsePoint() {
        IntervalTree node = newNode();
        double x, y;
        iToken = st.nextToken(); // x-coordinat
        x = Double.parseDouble(iToken);
        iToken = st.nextToken(); // y-coordinat
        y = Double.parseDouble(iToken);
        node.info = new Point(x, y);
        return node;
    }
    
    private void handleNode() throws IOException  {
        lft = newNode();
        rgt = newNode();
        lft.buildTreeFromFile();
        rgt.buildTreeFromFile();
    }
    
    private void handleLftPoint() throws IOException {
        lft = parsePoint();
        rgt = newNode();
        rgt.buildTreeFromFile();
    }
    
    private void handleRgtPoint() throws IOException {
        rgt = parsePoint();
        lft = newNode();
        lft.buildTreeFromFile();
    }
    
    private void handleNodePoint() {
        lft = parsePoint();
        rgt = parsePoint();
    }
    
    private void buildTreeFromFile() throws IOException {       
        getNextToken();                
        switch(iToken) {
            case "node" : handleNode();
                          break;
            case "lft-point" : handleLftPoint();
                               break;
            case "rgt-point" : handleRgtPoint();
                               break;
            case "node-point" : handleNodePoint();
                                break;
            default : throw new RuntimeException("Invalid Tree description");
        } 
    }
}
