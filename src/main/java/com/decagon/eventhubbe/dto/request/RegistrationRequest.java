package com.decagon.eventhubbe.dto.request;

import lombok.*;

// refactor this class to a Reg request class and create a registration response
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String password;
}
