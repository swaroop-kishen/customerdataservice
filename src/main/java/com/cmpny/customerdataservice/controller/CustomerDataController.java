package com.cmpny.customerdataservice.controller;

import com.cmpny.customerdataservice.exception.CustomerDataNotFoundException;
import com.cmpny.customerdataservice.exception.CustomerDataServiceException;
import com.cmpny.customerdataservice.exception.CustomerEmailExistsException;
import com.cmpny.customerdataservice.model.Customer;
import com.cmpny.customerdataservice.service.CustomerDataService;
import com.cmpny.customerdataservice.validator.CustomerRequestValidator;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * REST controller to map various request operations
 * that operate on customer information workflow
 */
@Slf4j
@RestController
public class CustomerDataController {

    @Autowired
    private CustomerDataService customerDataService;

    /**
     * GET customer information based on customer ID
     * @param customerId customer id to look up customer information
     * @return customer data if found
     */
    @Timed("GET.customer.byId")
    @Counted("GET.customer.byId")
    @GetMapping("/customer")
    public Customer getCustomer(@RequestParam(value = "id") UUID customerId) {
            return customerDataService.findCustomerById(customerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer Not Found"));
    }

    /**
     * GET customer information based on customer's email address
     * @param email customer email to look up customer information
     * @return customer data if found
     */
    @Timed("GET.customer.byEmail")
    @Counted("GET.customer.byEmail")
    @GetMapping("/customerByEmail")
    public Customer getCustomerByEmail(@RequestParam(value = "email") String email) {
        try {
            CustomerRequestValidator.validateCustomerEmail(email);
            return customerDataService.findCustomerByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer Not Found"));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email provided");
        }
    }

    /**
     * GET all available customer information [most probably use would be to get data dump or perform high level data
     * debugging]
     * @return list of current customers
     */
    @Timed("GET.customers")
    @Counted("GET.customers")
    @GetMapping("/customers")
    public List<Customer> getCustomers() {
        return customerDataService.fetchCustomerList();
    }

    /**
     * POST - used to update customer attribute(s), takes in the whole customer object as request input
     * and writes updates them to database post validation
     *
     * [Typically would be invoked in some type of account edit workflow where the customer edits
     * one or more entries and the whole object is passed to the operation and the data is persisted post validation]
     *
     * @param customer customer object to be updated
     * @return updated customer object
     */
    @Timed("POST.customer")
    @Counted("POST.customer")
    @PostMapping("/customer")
    @ResponseStatus(HttpStatus.OK)
    public Customer updateCustomer(@RequestBody Customer customer) {
        try {
            CustomerRequestValidator.validateCustomer(customer, false);
           return customerDataService.updateCustomer(customer);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid arguments provided to update operation", ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid arguments provided");
        } catch (CustomerDataNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer id not found");
        } catch (CustomerEmailExistsException ex) {
            log.error("Exception while updating customer data with customer Id {}, email: {} already exists",
                    customer.getId(), customer.getEmailAddress(), ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer email already exists");
        } catch (CustomerDataServiceException ex) {
            log.error("Exception while updating customer data with customer Id {}", customer.getId(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while trying to update customer");
        }
    }

    /**
     * PUT - used to create a new customer entry to the database, takes all the required information (sans customer Id)
     * and creates a new entry in database post validation
     *
     * [Typically used in some kind of new customer creation workflow, where a new customer might be signing up for an
     * account]
     * @param customer new customer information to be created in database
     */
    @Timed("PUT.customer")
    @Counted("PUT.customer")
    @PutMapping("/customer")
    @ResponseStatus(HttpStatus.OK)
    public void createCustomer(@RequestBody Customer customer) {
        try {
            CustomerRequestValidator.validateCustomer(customer, true);
            customerDataService.saveCustomer(customer);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid arguments provided");
        } catch (CustomerEmailExistsException ex) {
            log.error("Exception while creating new customer, email: {} already exists", customer.getEmailAddress());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer email already exists");
        } catch (CustomerDataServiceException ex) {
            log.error("Exception while trying to create customer with data {} ", customer, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while trying to create customer");
        }
    }

    /**
     * DELETE - used to delete a customer information based on customer ID,
     * [typically used in some kind of account deletion workflow]
     * @param customerId
     */
    @Timed("DELETE.customer")
    @Counted("DELETE.customer")
    @DeleteMapping("/customer")
    @ResponseStatus(HttpStatus.OK)
    public void deleteCustomer(@RequestParam(value = "id") UUID customerId) {
            customerDataService.deleteCustomerById(customerId);
    }

}
