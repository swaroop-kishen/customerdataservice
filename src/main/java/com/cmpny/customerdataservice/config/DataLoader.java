package com.cmpny.customerdataservice.config;

import com.cmpny.customerdataservice.model.Customer;
import com.cmpny.customerdataservice.model.CustomerRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * Utility component to populate some initial data to the database
 * from the resource file data.json
 */
@Component
@Slf4j
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CustomerRepository repository;

    @Override
    public void run(String... args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<List<Customer>> typeReference = new TypeReference<>() {};
        InputStream inputStream = TypeReference.class.getResourceAsStream("/data.json");
        try {
            List<Customer> customers = objectMapper.readValue(inputStream,typeReference);
            customers.forEach(customer -> repository.save(customer));
            log.info("Customer data initialized!");
        } catch (Exception ex) {
            log.error("Unable to persist customers", ex);
        }
    }
}
