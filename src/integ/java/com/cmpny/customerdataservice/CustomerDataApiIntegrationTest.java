package com.cmpny.customerdataservice;

import com.cmpny.customerdataservice.controller.CustomerDataController;
import com.cmpny.customerdataservice.exception.CustomerDataNotFoundException;
import com.cmpny.customerdataservice.exception.CustomerEmailExistsException;
import com.cmpny.customerdataservice.model.Customer;
import com.cmpny.customerdataservice.model.CustomerRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerDataApiIntegrationTest {

    @Autowired
    CustomerDataController controller;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ObjectMapper objectMapper;

    static List<Customer> customers;

    @Autowired
    private MockMvc mockMvc;

    private List<Customer> getCurrentCustomers() throws Exception {
        MvcResult mvcResult =  mockMvc.perform(get("/customers")
                .accept(MediaType.APPLICATION_JSON)).andReturn();

        return Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Customer[].class));
    }

    @Test
    @Order(1)
    void springContext_Init_AsExpected() throws Exception {
        assertThat(controller).isNotNull();
    }

    @Test
    @Order(2)
    void getCustomers_ReturnsResponse_AsExpected() throws Exception {

        ResultActions response = mockMvc.perform(get("/customers"));
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(3)));
    }

    @Test
    @Order(3)
    public void get_Customer_ById_WorksAsExpected() throws Exception {
        Customer firstCustomer = getCurrentCustomers().getFirst();

        ResultActions response = mockMvc.perform(
                get("/customer")
                        .param("id", firstCustomer.getId().toString())
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(firstCustomer.getFirstName())));
    }

    @Test
    @Order(4)
    public void get_Customer_ByEmail_WorksAsExpected() throws Exception{
        Customer firstCustomer = getCurrentCustomers().getFirst();

        ResultActions response = mockMvc.perform(
                get("/customerByEmail")
                        .param("email", firstCustomer.getEmailAddress())
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(firstCustomer.getFirstName())));
    }

    @Test
    @Order(5)
    public void get_Customer_ByEmail_ReturnsBadRequest() throws Exception{
        ResultActions response = mockMvc.perform(
                get("/customerByEmail")
                        .param("email", "email@email")
        );

        response.andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    public void update_customer_worksAsExpected() throws Exception {
        Customer firstCustomer = getCurrentCustomers().getFirst();

        firstCustomer.setFirstName("FirstName");

        ResultActions response = mockMvc.perform(
                post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstCustomer))
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(firstCustomer.getFirstName())));

    }

    @Test
    @Order(7)
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
    @Order(8)
    public void update_customer_ReturnsBadRequest_ForCustomerNotFound() throws Exception {

        Customer customer = Customer.builder()
                .firstName("firstName")
                .lastName("lastName")
                .phoneNumber("email@email.com")
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
    @Order(9)
    public void update_customer_ReturnsBadRequest_ForCustomerEmailAlreadyPresent() throws Exception {

        List<Customer> customers = getCurrentCustomers();

        Customer sCustomer = customers.get(1);
        sCustomer.setEmailAddress(customers.get(0).getEmailAddress());

        ResultActions response = mockMvc.perform(
                post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sCustomer))
        );

        response.andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    public void put_customer_worksAsExpected() throws Exception {

        Customer customer = Customer.builder()
                .firstName("firstName")
                .lastName("lastName")
                .emailAddress("email1@email.com")
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
    @Order(11)
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
    @Order(12)
    public void put_customer_ReturnsBadRequest_ForCustomerEmailAlreadyPresent() throws Exception {

        List<Customer> customers = getCurrentCustomers();

        Customer sCustomer = customers.get(1);
        sCustomer.setEmailAddress(customers.get(0).getEmailAddress());

        ResultActions response = mockMvc.perform(
                put("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sCustomer))
        );

        response.andExpect(status().isBadRequest());
    }

    @Test
    @Order(13)
    public void delete_customer_worksAsExpected() throws Exception {

        Customer firstCustomer = getCurrentCustomers().getFirst();

        ResultActions response = mockMvc.perform(
                delete("/customer")
                        .param("id", firstCustomer.getId().toString())
        );

        response.andExpect(status().isOk());
    }
}
