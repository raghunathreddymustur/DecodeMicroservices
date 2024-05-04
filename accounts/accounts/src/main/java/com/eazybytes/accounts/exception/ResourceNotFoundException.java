package com.eazybytes.accounts.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(value=HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends  RuntimeException{

    public ResourceNotFoundException(String resource, String field, String fieldValue)
    {
        super(String.format("%s the following resource is not found with given detials %s : %s",resource,field,fieldValue));
    }
}
