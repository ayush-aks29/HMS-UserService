package com.userService.exceptions;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(){
        super("User not found!!");
    }

    public ResourceNotFoundException(String message){
        super(message);
    }

}
