package by.osinovi.userservice.service.impl;

import by.osinovi.userservice.dto.user.UserRequestDto;
import by.osinovi.userservice.dto.user.UserResponseDto;
import by.osinovi.userservice.entity.User;
import by.osinovi.userservice.exception.InvalidInputException;
import by.osinovi.userservice.exception.UserNotFoundException;
import by.osinovi.userservice.mapper.UserMapper;
import by.osinovi.userservice.repository.UserRepository;
import by.osinovi.userservice.config.UserCacheManager;
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
    private final UserCacheManager userCacheManager;

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        if (userRepository.findUserByEmail(userRequestDto.getEmail()).isPresent()) {
            throw new InvalidInputException("Email " + userRequestDto.getEmail() + " already exists");
        }

        User user = userMapper.toEntity(userRequestDto);
        userRepository.save(user);
        UserResponseDto response = userMapper.toDto(user);
        userCacheManager.cacheUser(String.valueOf(user.getId()), user.getEmail(), response);
        return response;
    }

    @Override
    public UserResponseDto getUserById(String id) {
        UserResponseDto cached = userCacheManager.getUserById(id);
        if (cached != null) {
            return cached;
        }
        User user = userRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        UserResponseDto response = userMapper.toDto(user);
        userCacheManager.cacheUser(id, user.getEmail(), response);
        return response;
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        UserResponseDto cached = userCacheManager.getUserByEmail(email);
        if (cached != null) {
            return cached;
        }
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
        UserResponseDto response = userMapper.toDto(user);
        userCacheManager.cacheUser(String.valueOf(user.getId()), email, response);
        return response;
    }

    @Override
    public List<UserResponseDto> getUsersByIds(List<String> ids) {
        List<Long> longIds = ids.stream().map(Long::valueOf).toList();
        List<User> users = userRepository.findUserByIdIn(longIds);
        if (users.isEmpty()) {
            throw new UserNotFoundException("No users found with IDs " + String.join(", ", ids));
        }
        return users.stream().map(userMapper::toDto).toList();
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(String id, UserRequestDto userRequestDto) {
        User existingUser = userRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

        String newEmail = userRequestDto.getEmail();
        if (!newEmail.equals(existingUser.getEmail()) && userRepository.findUserByEmail(newEmail).isPresent()) {
            throw new InvalidInputException("Email " + newEmail + " already exists");
        }

        String oldEmail = existingUser.getEmail();
        User updatedUser = userMapper.toEntity(userRequestDto);
        existingUser.setName(updatedUser.getName());
        existingUser.setSurname(updatedUser.getSurname());
        existingUser.setBirthDate(updatedUser.getBirthDate());
        existingUser.setEmail(updatedUser.getEmail());
        userRepository.save(existingUser);
        UserResponseDto response = userMapper.toDto(existingUser);
        userCacheManager.cacheUser(id, existingUser.getEmail(), response);
        if (!oldEmail.equals(existingUser.getEmail())) {
            userCacheManager.evictUser(id, oldEmail);
            userCacheManager.cacheUser(id,null, response);
        }
        return response;
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        userCacheManager.evictUser(id, user.getEmail());
        userRepository.delete(user);
    }

}