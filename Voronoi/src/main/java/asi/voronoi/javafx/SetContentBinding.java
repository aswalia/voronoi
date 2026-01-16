/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asi.voronoi.javafx;

import java.util.Collection;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

/**
 *
 * @author asi
 * @param <T>
 */
public class SetContentBinding<T> {

    private final ObservableSet<T> source;
    private final Collection<? super T> target;
    private final SetChangeListener<T> listener;

    public SetContentBinding(ObservableSet<T> source, Collection<? super T> target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException();
        }
        this.source = source;
        this.target = target;
        target.clear();
        target.addAll(source);
        this.listener = c -> {
            if (c.wasAdded()) {
                target.add(c.getElementAdded());
            } else {
                target.remove(c.getElementRemoved());
            }
        };
        source.addListener(this.listener);
    }

    /**
     * dispose the binding
     */
    public void unbind() {
        source.removeListener(listener);
    }

}
