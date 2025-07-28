package by.osinovi.userservice.service.impl;

import by.osinovi.userservice.dto.user.UserRequestDto;
import by.osinovi.userservice.dto.user.UserResponseDto;
import by.osinovi.userservice.entity.User;
import by.osinovi.userservice.exception.UserNotFoundException;
import by.osinovi.userservice.mapper.UserMapper;
import by.osinovi.userservice.repository.UserRepository;
import by.osinovi.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        User user = userMapper.toEntity(userRequestDto);
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponseDto getUserById(String id) {
        User user = userRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        return userMapper.toDto(user);
    }

    @Override
    @Cacheable(value = "usersList", key = "#ids")
    public List<UserResponseDto> getUsersByIds(List<String> ids) {
        List<Long> longIds = ids.stream().map(Long::valueOf).collect(Collectors.toList());
        List<User> users = userRepository.findUserByIdIn(longIds);

        if (users.isEmpty()) {
            throw new UserNotFoundException("No users found with IDs " + String.join(", ", ids));
        }

        return users.stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "users", key = "#email")
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#id")
    public UserResponseDto updateUser(String id, UserRequestDto userRequestDto) {
        User existingUser = userRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        User updatedUser = userMapper.toEntity(userRequestDto);
        existingUser.setName(updatedUser.getName());
        existingUser.setSurname(updatedUser.getSurname());
        existingUser.setBirthDate(updatedUser.getBirthDate());
        existingUser.setEmail(updatedUser.getEmail());
        userRepository.save(existingUser);
        return userMapper.toDto(existingUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(String id) {
        User user = userRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        userRepository.delete(user);
    }
}