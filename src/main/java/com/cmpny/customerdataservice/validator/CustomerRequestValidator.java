package com.cmpny.customerdataservice.validator;

import com.cmpny.customerdataservice.model.Customer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.Objects;

/**
 * Utility class to validate requests to various operations in the
 * Customer Data controller
 */
public class CustomerRequestValidator {

    private static boolean hasDigits(String name) {
        if (name != null) {
            return !StringUtils.getDigits(name).isEmpty();
        }
        return  false;
    }

    public static void validateCustomer(Customer customer, boolean isCreate) {
        if (Objects.isNull(customer.getId()) && !isCreate) {
            throw new IllegalArgumentException("Invalid customer Id provided");
        }

        if (Objects.isNull(customer.getFirstName()) || "".equalsIgnoreCase(customer.getFirstName()) || hasDigits(customer.getFirstName())) {
            throw new IllegalArgumentException("Invalid first name provided");
        }

        if (Objects.isNull(customer.getLastName()) || "".equalsIgnoreCase(customer.getLastName()) || hasDigits(customer.getLastName())) {
            throw new IllegalArgumentException("Invalid last name provided");
        }

        validateCustomerEmail(customer.getEmailAddress());

        if (Objects.isNull(customer.getPhoneNumber()) || "".equalsIgnoreCase(customer.getPhoneNumber())) {
            // TODO: add better validation for phone number
            throw new IllegalArgumentException("Invalid phone number provided");
        }
    }

    public static void validateCustomerEmail(String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            throw new IllegalArgumentException("Invalid email address provided");
        }
    }
}
