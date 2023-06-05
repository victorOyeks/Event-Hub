package com.decagon.eventhubbe.domain.repository;

import com.decagon.eventhubbe.domain.entities.JwtToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JwtTokenRepository extends MongoRepository<JwtToken,String> {
    JwtToken findByAccessToken(String accessToken);

    JwtToken findByRefreshToken(String refreshToken);

}
