/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asi.voronoi.javafx;

import asi.voronoi.PointSet;

/**
 *
 * @author asi
 */
public class PointSetInteractor {

    private final PointSetModel model;
//    private final CustomerBroker broker = new CustomerBroker();

    public PointSetInteractor(PointSetModel model) {
        this.model = model;
    }

    /*    public void saveCustomer() {
        String customerName = model.getCustomerName();
        String account = model.getAccountNumber();
        int result = broker.saveCustomer(createCustomerFromModel());
        System.out.println("Saving account: " + account + " Name: " + customerName + " Result: " + result);
    }

     PointSet createCustomerFromModel() {
        Customer customer = new Customer();
        customer.setAccountNumber(model.getAccountNumber());
        customer.setName(model.getCustomerName());
        return customer;
    }
     */
}
