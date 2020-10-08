package asi.voronoi;

import java.io.*;

public class Serializer implements Serializable {
    public static void store(String filename, Object obj) throws IOException {
        FileOutputStream out = new FileOutputStream(filename);
        try (ObjectOutputStream s = new ObjectOutputStream(out)) {
            s.writeObject(obj);
            s.flush();
        }
    }

    public static Object fetch(String filename) throws IOException, ClassNotFoundException {
        FileInputStream in = new FileInputStream(filename);
        try (ObjectInputStream s = new ObjectInputStream(in)) {
            return s.readObject();
        }
    }
}
