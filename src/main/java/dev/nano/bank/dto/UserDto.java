package dev.nano.bank.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter @Getter
public class UserDto {
    private Long id;
    private String username;
    private String gender;
    private String lastName;
    private String firstName;
    private Date birthdate;
    private String role;
}
