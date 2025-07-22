package by.osinovi.userservice.service;


import by.osinovi.userservice.dto.UserRequestDto;
import by.osinovi.userservice.dto.UserResponseDto;
import by.osinovi.userservice.entity.User;

import java.util.List;

public interface UserService {
    void createUser(UserRequestDto userRequestDto);

    UserResponseDto getUserById(String id);

    List<UserResponseDto> getUsersByIds(List<String> ids);

    UserResponseDto getUserByEmail(String email);

    void updateUser(String id, UserRequestDto userRequestDto);

    void deleteUser(String id);
}
