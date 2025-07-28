package by.osinovi.userservice.service;


import by.osinovi.userservice.dto.UserRequestDto;
import by.osinovi.userservice.dto.UserResponseDto;

import java.util.List;

public interface UserService {
    UserResponseDto createUser(UserRequestDto userRequestDto);

    UserResponseDto getUserById(String id);

    List<UserResponseDto> getUsersByIds(List<String> ids);

    UserResponseDto getUserByEmail(String email);

    UserResponseDto updateUser(String id, UserRequestDto userRequestDto);

    void deleteUser(String id);
}
