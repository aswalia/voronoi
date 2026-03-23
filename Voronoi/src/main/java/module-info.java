module asi.voronoi {
    requires javafx.controls;
    requires javafx.swing;
    requires java.desktop;
    requires java.sql;
    requires org.apache.logging.log4j;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.web;

    exports asi.voronoi;
    exports asi.voronoi.tree;
    exports asi.voronoi.javafx;
}
