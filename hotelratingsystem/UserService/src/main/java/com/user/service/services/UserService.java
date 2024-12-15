package com.user.service.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.service.entities.Hotel;
import com.user.service.entities.Rating;
import com.user.service.entities.User;
import com.user.service.exceptions.ResourceNotFoundException;
import com.user.service.feign.HotelService;
import com.user.service.reposiitory.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HotelService hotelService;

    private Logger logger = LoggerFactory.getLogger(UserService.class);

    public User saveUser(User user) {
        user.setUserId(UUID.randomUUID().toString());
        return userRepository.save(user);
    }

    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    public User getUser(String userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with given id not found : " + userId));
        String url = "http://RATING-SERVICE/ratings/users/" + userId;
        List<Rating> ratings = new ObjectMapper().convertValue(restTemplate.getForEntity(url, List.class).getBody(), new TypeReference<List<Rating>>() {});

        List<Rating> finalRating = ratings.stream().map(rating -> {
                    Hotel hotel = hotelService.getHotel(rating.getHotelId());
                    //Hotel hotel = restTemplate.getForEntity("http://HOTEL-SERVICE/hotels/" + rating.getHotelId(), Hotel.class).getBody();
                    //logger.info("response status code: {} ", hotel);
                    rating.setHotel(hotel);
                    return rating;
                })
                .collect(Collectors.toList());

        user.setRatings(finalRating);
        return user;
    }
}
