package com.cmpny.customerdataservice.controller;

import com.cmpny.customerdataservice.exception.CustomerDataNotFoundException;
import com.cmpny.customerdataservice.exception.CustomerDataServiceException;
import com.cmpny.customerdataservice.exception.CustomerEmailExistsException;
import com.cmpny.customerdataservice.model.Customer;
import com.cmpny.customerdataservice.service.CustomerDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.CoreMatchers.is;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CustomerDataController.class)
public class CustomerDataControllerTest {

    @MockBean
    CustomerDataService customerDataService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer customer = Customer.builder()
            .firstName("firstName")
            .lastName("lastName")
            .emailAddress("email@email.com")
            .id(UUID.randomUUID())
            .phoneNumber("4255252233")
            .build();

    private List<Customer> customers = List.of(customer);

    @BeforeEach
    void setUp() {
        Mockito.when(customerDataService.fetchCustomerList()).thenReturn(customers);
        Mockito.when(customerDataService.findCustomerById(any(UUID.class))).thenReturn(Optional.of(customer));
        Mockito.when(customerDataService.findCustomerByEmail(any(String.class))).thenReturn(Optional.of(customer));
        Mockito.when(customerDataService.updateCustomer(any(Customer.class))).thenReturn(customer);
    }

    @Test
    public void get_Customers_WorksAsExpected() throws Exception{
        ResultActions response = mockMvc.perform(get("/customers"));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(customers.size())));
    }

    @Test
    public void get_Customer_ById_WorksAsExpected() throws Exception {
        ResultActions response = mockMvc.perform(
                get("/customer")
                        .param("id", UUID.randomUUID().toString())
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(customer.getFirstName())));
    }

    @Test
    public void get_Customer_ByEmail_WorksAsExpected() throws Exception{
        ResultActions response = mockMvc.perform(
                get("/customerByEmail")
                        .param("email", "email@email.com")
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(customer.getFirstName())));
    }

    @Test
    public void get_Customer_ByEmail_ReturnsBadRequest() throws Exception{
        ResultActions response = mockMvc.perform(
                get("/customerByEmail")
                        .param("email", "email@email")
        );

        response.andExpect(status().isBadRequest());
    }

    @Test
    public void update_customer_worksAsExpected() throws Exception {
        ResultActions response = mockMvc.perform(
                post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(customer.getFirstName())));
    }

    @Test
    public void update_customer_ReturnsBadRequest_ForInvalidRequestArguments() throws Exception {

        Customer customer = Customer.builder()
                .firstName("firstName")
                .lastName("lastName")
                .id(UUID.randomUUID())
                .phoneNumber("4255252233")
                .build();

        ResultActions response = mockMvc.perform(
                post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))
        );

        response.andExpect(status().isBadRequest());
    }

    @Test
    public void update_customer_ReturnsBadRequest_ForCustomerNotFound() throws Exception {

        Mockito.when(customerDataService.updateCustomer(any(Customer.class)))
                .thenThrow(CustomerDataNotFoundException.class);

        ResultActions response = mockMvc.perform(
                post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))
        );

        response.andExpect(status().isBadRequest());
    }

    @Test
    public void update_customer_ReturnsBadRequest_ForCustomerEmailAlreadyPresent() throws Exception {

        Mockito.when(customerDataService.updateCustomer(any(Customer.class)))
                .thenThrow(CustomerEmailExistsException.class);

        ResultActions response = mockMvc.perform(
                post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))
        );

        response.andExpect(status().isBadRequest());
    }

    @Test
    public void update_customer_ReturnsInternalServerError_ForCustomerDataServiceException() throws Exception {

        Mockito.when(customerDataService.updateCustomer(any(Customer.class)))
                .thenThrow(CustomerDataServiceException.class);

        ResultActions response = mockMvc.perform(
                post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))
        );

        response.andExpect(status().is5xxServerError());
    }

    @Test
    public void put_customer_worksAsExpected() throws Exception {

        Customer customer = Customer.builder()
                .firstName("firstName")
                .lastName("lastName")
                .emailAddress("email@email.com")
                .phoneNumber("4255252233")
                .build();

        ResultActions response = mockMvc.perform(
                put("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))
        );

        response.andExpect(status().isOk());
    }

    @Test
    public void put_customer_ReturnsBadRequest_ForInvalidRequestArguments() throws Exception {

        Customer customer = Customer.builder()
                .firstName("firstName")
                .lastName("lastName")
                .phoneNumber("4255252233")
                .build();

        ResultActions response = mockMvc.perform(
                put("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))
        );

        response.andExpect(status().isBadRequest());
    }


    @Test
    public void put_customer_ReturnsBadRequest_ForCustomerEmailAlreadyPresent() throws Exception {

        Mockito.doThrow(CustomerEmailExistsException.class)
                .when(customerDataService).saveCustomer(any(Customer.class));

        ResultActions response = mockMvc.perform(
                put("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))
        );

        response.andExpect(status().isBadRequest());
    }

    @Test
    public void put_customer_ReturnsInternalServerError_ForCustomerDataServiceException() throws Exception {

        Mockito.doThrow(CustomerDataServiceException.class)
                .when(customerDataService).saveCustomer(any(Customer.class));

        ResultActions response = mockMvc.perform(
                put("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))
        );

        response.andExpect(status().is5xxServerError());
    }

    @Test
    public void delete_customer_worksAsExpected() throws Exception {
        ResultActions response = mockMvc.perform(
                delete("/customer")
                .param("id", UUID.randomUUID().toString())
        );

        response.andExpect(status().isOk());
    }
}
