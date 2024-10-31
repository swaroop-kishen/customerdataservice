package com.cmpny.customerdataservice.exception;

/**
 * Used to wrap exceptions that happen during operations in customer data service layer
 */
public class CustomerDataServiceException extends RuntimeException{

    public CustomerDataServiceException(Exception ex) {
        super(ex);
    }
}
