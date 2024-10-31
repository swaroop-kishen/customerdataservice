package com.cmpny.customerdataservice.service;

import com.cmpny.customerdataservice.exception.CustomerDataNotFoundException;
import com.cmpny.customerdataservice.exception.CustomerDataServiceException;
import com.cmpny.customerdataservice.exception.CustomerEmailExistsException;
import com.cmpny.customerdataservice.model.Customer;
import com.cmpny.customerdataservice.model.CustomerRepository;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class CustomerDataServiceImpl implements CustomerDataService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    MeterRegistry meterRegistry;

    @Override
    @Counted("customerdataservice.savecustomer")
    @Timed("customerdataservice.savecustomer")
    public void saveCustomer(Customer customer) {
        try {
            customerRepository.save(customer);
        } catch (DataIntegrityViolationException ex) {
            // Right now we only have constraint on email so using exception to check conflicts, if we have more constraints,
            // we can add validations before we make the call
            meterRegistry.counter("customerdataservice.savecustomer.emailexistsexception").increment();
            throw new CustomerEmailExistsException();
        } catch (Exception ex) {
            meterRegistry.counter("customerdataservice.savecustomer.exception").increment();
            throw new CustomerDataServiceException(ex);
        }
    }

    @Override
    @Counted("customerdataservice.fetchcustomers")
    @Timed("customerdataservice.fetchcustomers")
    public List<Customer> fetchCustomerList() {
        return (List<Customer>) customerRepository.findAll();
    }

    @Override
    @Counted("customerdataservice.findcustomer.byemail")
    @Timed("customerdataservice.findcustomer.byemail")
    public Optional<Customer> findCustomerByEmail(String email) {
        return customerRepository.findByEmailAddress(email);
    }

    @Override
    @Counted("customerdataservice.findcustomer.byid")
    @Timed("customerdataservice.findcustomer.byid")
    public Optional<Customer> findCustomerById(UUID id) {
        return customerRepository.findById(id);
    }

    @Override
    @Counted("customerdataservice.updatecustomer")
    @Timed("customerdataservice.updatecustomer")
    public Customer updateCustomer(Customer customer) {
        Optional<Customer> customerFromDB = customerRepository.findById(customer.getId());

        if(customerFromDB.isPresent()) {
            Customer customerToUpdate = customerFromDB.get();
            customerToUpdate.setFirstName(customer.getFirstName());
            customerToUpdate.setMiddleName(customer.getMiddleName());
            customerToUpdate.setLastName(customer.getLastName());
            customerToUpdate.setEmailAddress(customer.getEmailAddress());
            customerToUpdate.setPhoneNumber(customer.getPhoneNumber());

            try {
                return customerRepository.save(customerToUpdate);
            } catch (DataIntegrityViolationException ex) {
                // Right now we only have constraint on email so using exception to check conflicts, if we have more constraints,
                // we can add validations before we make the call
                meterRegistry.counter("customerdataservice.updatecustomer.emailexistsexception").increment();
                throw new CustomerEmailExistsException();
            }
            catch (Exception ex) {
                meterRegistry.counter("customerdataservice.updatecustomer.exception").increment();
                throw new CustomerDataServiceException(ex);
            }

        } else {
            log.error("Customer with id {} not found", customer.getId());
            meterRegistry.counter("customerdataservice.updatecustomer.customernotfoundexception").increment();
            throw new CustomerDataNotFoundException();
        }

    }

    @Override
    @Counted("customerdataservice.deletecustomer")
    @Timed("customerdataservice.deletecustomer")
    public void deleteCustomerById(UUID customerId) {
            customerRepository.deleteById(customerId);
    }
}
