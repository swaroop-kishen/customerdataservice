package com.cmpny.customerdataservice.model;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface to interact with DB using JPA
 * to save and load Customer information
 */
public interface CustomerRepository extends CrudRepository<Customer, UUID> {

    /**
     * Utility method to retrieve record based on email address as its one
     * of the other unique fields besides customer id
     * @param email email to lookup customer info
     * @return Customer object
     */
    Optional<Customer> findByEmailAddress(String email);
}
