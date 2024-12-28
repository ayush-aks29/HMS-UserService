package com.userService.service;

import com.userService.entities.User;

import java.util.List;

public interface UserService {

    //create
    User saveUser(User user);

    //get all
    List<User> getAllUsers();

    //get by id
    User getUser(String userId);

    //update
    User updateUser(String userId, User user);

    //delete
    User deleteUser(String userId);

}
