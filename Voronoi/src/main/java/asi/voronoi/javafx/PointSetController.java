/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asi.voronoi.javafx;

import javafx.concurrent.Task;
import javafx.scene.layout.Region;
import javafx.util.Builder;

/**
 *
 * @author asi
 */
public class PointSetController {

    private final Builder<Region> viewBuilder;
    private final PointSetInteractor interactor;

    public PointSetController() {
        PointSetModel model = new PointSetModel();
        interactor = new PointSetInteractor(model);
        viewBuilder = new PointSetViewBuilder(model, this::getPointSet);
    }

    private void getPointSet(Runnable postTaskGuiActions) {
        Task<Void> getTask = new Task<>() {
            @Override
            protected Void call() {
//                interactor.saveCustomer();
                return null;
            }
        };
        getTask.setOnSucceeded(evt -> postTaskGuiActions.run());
        Thread getThread = new Thread(getTask);
        getThread.start();
    }

    public Region getView() {
        return viewBuilder.build();
    }

}
