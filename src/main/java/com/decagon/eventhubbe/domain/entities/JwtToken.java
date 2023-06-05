package com.decagon.eventhubbe.domain.entities;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "jwt_token")
public class JwtToken {
    @Id
    private String id;
    @Indexed(unique = true)
    private String accessToken;
    @Indexed(unique = true)
    private String refreshToken;
    @DBRef
    private AppUser appUser;

    private Date generatedAt;
    private Date expiresAt;
    private Date refreshedAt;
}
