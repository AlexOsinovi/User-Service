package by.osinovi.userservice.service.impl;

import by.osinovi.userservice.dto.UserRequestDto;
import by.osinovi.userservice.dto.UserResponseDto;
import by.osinovi.userservice.entity.User;
import by.osinovi.userservice.exception.UserNotFoundException;
import by.osinovi.userservice.mapper.UserMapper;
import by.osinovi.userservice.repository.UserRepository;
import by.osinovi.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void createUser(UserRequestDto userRequestDto) {
        User user = userMapper.toEntity(userRequestDto);
        userRepository.save(user);
    }

    @Override
    public UserResponseDto getUserById(String id) {
        User user = userRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не найден"));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserResponseDto> getUsersByIds(List<String> ids) {
        List<Integer> intIds = ids.stream().map(Integer::valueOf).collect(Collectors.toList());
        List<User> users = userRepository.findUserByIdIn(intIds);
        if (users.isEmpty()) {
            throw new UserNotFoundException("Ни один пользователь с ID " + String.join(", ", ids) + " не найден");
        }
        return users.stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с email " + email + " не найден"));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void updateUser(String id, UserRequestDto userRequestDto) {
        User existingUser = userRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не найден"));
        User updatedUser = userMapper.toEntity(userRequestDto);
        existingUser.setName(updatedUser.getName());
        existingUser.setSurname(updatedUser.getSurname());
        existingUser.setBirthDate(updatedUser.getBirthDate());
        existingUser.setEmail(updatedUser.getEmail());
        userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не найден"));
        userRepository.delete(user);
    }
}