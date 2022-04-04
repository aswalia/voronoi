package asi.voronoi.tree;

import asi.voronoi.DCEL;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class VTree {
    private DCEL info;
    private VTree lft, rgt;

    @Override
    public String toString() {
        return "" + info;
    }

    public void toFile() throws Exception {
        info.toFile();
    }
    
    public DCEL getInfo() {
        return info;
    }

    public void writeTree(String filename) throws IOException {
        try (FileWriter fw = new FileWriter(filename); 
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(this.toString());
        }
    }

    public void buildStructure(BinaryTree b) {
        if (b == null) {
            info = null;
        } else if (b.isLeaf()) {
            info = new DCEL(b.p);  
        } else if (b.lft == null) {
            try {
                info = new DCEL(b.p);
                rgt = new VTree();
                rgt.buildStructure(b.rgt);
                info = info.merge(rgt.info);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
        } else if (b.rgt == null) {
            try {
                info = new DCEL(b.p);
                lft = new VTree();
                lft.buildStructure(b.lft);
                info = info.merge(lft.info);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }            
        } else {
            try {
                info = new DCEL(b.p);
                lft = new VTree();
                lft.buildStructure(b.lft);
                info = info.merge(lft.info);
                rgt = new VTree();
                rgt.buildStructure(b.rgt);
                info = info.merge(rgt.info);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }            
        }
    }
}
