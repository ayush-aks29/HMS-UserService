package com.userService.service.impl;

import com.userService.entities.Hotel;
import com.userService.entities.Rating;
import com.userService.entities.User;
import com.userService.exceptions.ResourceNotFoundException;
import com.userService.repositories.UserRepository;
import com.userService.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;


    @Override
    public User saveUser(User user) {
        String randomUserId = UUID.randomUUID().toString();
        user.setUserId(randomUserId);
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            // Fetch ratings for the user
            Rating[] ratingsArr = restTemplate.getForObject("http://RATING-SERVICE/api/v1/ratings/getRatingByUserId/" + user.getUserId(), Rating[].class);
            List<Rating> ratingsByUser = Arrays.stream(ratingsArr).collect(Collectors.toList());

            // Fetch hotel details for each rating
            ratingsByUser.stream().map(rating -> {
                ResponseEntity<Hotel> hotelResponse = restTemplate.getForEntity("http://HOTEL-SERVICE/api/v1/hotels/getHotel/" + rating.getHotelId(), Hotel.class);
                Hotel hotel = hotelResponse.getBody();
                rating.setHotel(hotel);
                return rating;
            }).collect(Collectors.toList());

            // Set ratings to user
            user.setRatings(ratingsByUser);
        }
        return users;
    }


    @Override
    public User getUser(String userId) {
        User user = userRepository.findById(userId).
                orElseThrow(()->new
                        ResourceNotFoundException("User not found with Id: "+userId+" !!"));

        //fetch rating of the given user
        Rating[] ratingArr  = restTemplate.getForObject("http://RATING-SERVICE/api/v1/ratings/getRatingByUserId/"+userId, Rating[].class);

        List<Rating> ratingByUser;
        ratingByUser = Arrays.stream(ratingArr).toList();

        //get details of hotel also
        ratingByUser.stream().map(rating -> {
            ResponseEntity<Hotel> hotelList = restTemplate.getForEntity("http://HOTEL-SERVICE/api/v1/hotels/getHotel/"+rating.getHotelId(), Hotel.class);
            Hotel hotel = hotelList.getBody();

            //set hotel to rating:
            rating.setHotel(hotel);

            return rating;
            }).collect(Collectors.toList());

        user.setRatings(ratingByUser);
        return user;
    }

    @Override
    public User updateUser(String userId, User user) {
        User existingUser = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        return userRepository.save(existingUser);
    }

    @Override
    public User deleteUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
        return user;
    }
}
