package com.cmpny.customerdataservice.service;

import com.cmpny.customerdataservice.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface to perform data related operations on customer database
 */
public interface CustomerDataService {

    /**
     * Creates a new entry in databased, based on the passed customer information
     * @param customer customer to create in database
     */
    void saveCustomer(Customer customer);

    /**
     * Fetches the current list of customers in the database
     * @return List of customers
     */
    List<Customer> fetchCustomerList();

    /**
     * Finds a customer based on given customer's email address
     * @param email email to lookup.
     * @return customer object if found.
     */
    Optional<Customer> findCustomerByEmail(String email);

    /**
     * Finds a customer based on customer Id
     * @param id customer id to lookup
     * @return customer object if found.
     */
    Optional<Customer> findCustomerById(UUID id);

    /**
     * Update an existing customer entry in the database with the passed customer information
     * @param customer customer object information
     * @return  updated customer object
     */
    Customer updateCustomer(Customer customer);

    /**
     * Delete customer entry from databased based on customer Id
     * @param customerId
     */
    void deleteCustomerById(UUID customerId);
}
