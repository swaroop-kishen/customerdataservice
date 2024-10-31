package com.cmpny.customerdataservice.exception;

/**
 * Used to indicate that the email provided (either during update or create) is
 * already being used in some other account
 */
public class CustomerEmailExistsException extends RuntimeException { }
