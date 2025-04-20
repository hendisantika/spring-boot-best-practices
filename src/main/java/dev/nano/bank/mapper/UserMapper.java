package dev.nano.bank.mapper;

import dev.nano.bank.domain.User;
import dev.nano.bank.dto.UserDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setGender(user.getGender());
        userDto.setLastName(user.getLastname());
        userDto.setFirstName(user.getFirstname());
        userDto.setBirthdate(user.getBirthdate());
        userDto.setRole(user.getRole());
        return userDto;
    }

    public List<UserDto> toListDto(List<User> users) {
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            userDtos.add(toDto(user));
        }
        return userDtos;
    }
}
