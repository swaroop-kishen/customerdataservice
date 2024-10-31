package com.cmpny.customerdataservice.service;

import com.cmpny.customerdataservice.exception.CustomerDataNotFoundException;
import com.cmpny.customerdataservice.exception.CustomerDataServiceException;
import com.cmpny.customerdataservice.exception.CustomerEmailExistsException;
import com.cmpny.customerdataservice.model.Customer;
import com.cmpny.customerdataservice.model.CustomerRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
public class CustomerDataServiceTest {
    @Mock
    CustomerRepository customerRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    MeterRegistry meterRegistry;

    @InjectMocks
    CustomerDataServiceImpl customerDataService;

    private final Customer customer = Customer.builder()
            .firstName("firstName")
            .lastName("lastName")
            .emailAddress("email@email.com")
            .id(UUID.randomUUID())
            .phoneNumber("4255252233")
            .build();

    private final List<Customer> customers = List.of(customer);

    @BeforeEach
    public void setUp() {
        var meterRegistry = new SimpleMeterRegistry();
        Metrics.addRegistry(meterRegistry);
    }

    @Test
    public void fetchCustomers_worksAsExpected() throws Exception {
        Mockito.when(customerRepository.findAll()).thenReturn(customers);
        List<Customer> cstrs = customerDataService.fetchCustomerList();
        assertEquals(cstrs, customers);
    }

    @Test
    public void findCustomerByEmail_worksAsExpected() throws Exception {
        Mockito.when(customerRepository.findByEmailAddress(any(String.class))).thenReturn(Optional.of(customer));
        Optional<Customer> cstr = customerDataService.findCustomerByEmail("email@email.com");
        assertEquals(cstr, Optional.of(customer));
    }

    @Test
    public void findCustomerById_worksAsExpected() throws Exception {
        Mockito.when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(customer));
        Optional<Customer> cstr = customerDataService.findCustomerById(UUID.randomUUID());
        assertEquals(cstr, Optional.of(customer));
    }

    @Test
    public void saveCustomer_worksAsExpected() throws Exception {
        Mockito.when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        customerDataService.saveCustomer(customer);
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    public void saveCustomer_ThrowsEmailExistsException() throws Exception {
        Mockito.when(customerRepository.save(any(Customer.class))).thenThrow(DataIntegrityViolationException.class);
        assertThrows(CustomerEmailExistsException.class, () -> customerDataService.saveCustomer(customer));
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    public void saveCustomer_ThrowsCustomerDataServiceException() throws Exception {
        Mockito.when(customerRepository.save(any(Customer.class))).thenThrow(RuntimeException.class);
        assertThrows(CustomerDataServiceException.class, () -> customerDataService.saveCustomer(customer));
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    public void updateCustomer_worksAsExpected() throws Exception {
        Mockito.when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(customer));
        Mockito.when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        customerDataService.updateCustomer(customer);
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    public void updateCustomer_ThrowsCustomerNotFoundException() throws Exception {
        Mockito.when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(CustomerDataNotFoundException.class, () -> customerDataService.updateCustomer(customer));
        verify(customerRepository, times(0)).save(customer);
    }

    @Test
    public void updateCustomer_ThrowsEmailExistsException() throws Exception {
        Mockito.when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(customer));
        Mockito.when(customerRepository.save(any(Customer.class))).thenThrow(DataIntegrityViolationException.class);

        assertThrows(CustomerEmailExistsException.class, () -> customerDataService.updateCustomer(customer));
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    public void updateCustomer_ThrowsCustomerDataServiceException() throws Exception {
        Mockito.when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(customer));
        Mockito.when(customerRepository.save(any(Customer.class))).thenThrow(RuntimeException.class);

        assertThrows(CustomerDataServiceException.class, () -> customerDataService.updateCustomer(customer));
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    public void deleteCustomer_worksAsExpected() throws Exception {
        UUID id = UUID.randomUUID();
        customerDataService.deleteCustomerById(id);
        verify(customerRepository, times(1)).deleteById(id);
    }
}
