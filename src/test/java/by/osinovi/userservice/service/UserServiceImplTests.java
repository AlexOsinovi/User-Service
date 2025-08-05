package by.osinovi.userservice.service;

import by.osinovi.userservice.dto.user.UserRequestDto;
import by.osinovi.userservice.dto.user.UserResponseDto;
import by.osinovi.userservice.entity.User;
import by.osinovi.userservice.exception.UserNotFoundException;
import by.osinovi.userservice.mapper.UserMapper;
import by.osinovi.userservice.repository.UserRepository;
import by.osinovi.userservice.config.UserCacheManager;
import by.osinovi.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserCacheManager userCacheManager;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setSurname("Doe");
        user.setEmail("john.doe@example.com");
        user.setBirthDate(LocalDate.of(1990, 1, 1));

        userRequestDto = new UserRequestDto();
        userRequestDto.setName("John");
        userRequestDto.setSurname("Doe");
        userRequestDto.setEmail("john.doe@example.com");
        userRequestDto.setBirthDate(LocalDate.of(1990, 1, 1));

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setName("John");
        userResponseDto.setSurname("Doe");
        userResponseDto.setEmail("john.doe@example.com");
        userResponseDto.setBirthDate(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createUser_Success() {
        when(userMapper.toEntity(userRequestDto)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.createUser(userRequestDto);

        assertNotNull(result);
        assertEquals(userResponseDto, result);
        verify(userRepository).save(user);
        verify(userCacheManager).cacheUser("1", user.getEmail(), userResponseDto);
    }

    @Test
    void getUserById_CacheHit_Success() {
        when(userCacheManager.getUserById("1")).thenReturn(userResponseDto);

        UserResponseDto result = userService.getUserById("1");

        assertNotNull(result);
        assertEquals(userResponseDto, result);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void getUserById_CacheMiss_Success() {
        when(userCacheManager.getUserById("1")).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.getUserById("1");

        assertNotNull(result);
        assertEquals(userResponseDto, result);
        verify(userCacheManager).cacheUser("1", user.getEmail(), userResponseDto);
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userCacheManager.getUserById("1")).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById("1"));
    }

    @Test
    void getUserByEmail_CacheHit_Success() {
        when(userCacheManager.getUserByEmail("john.doe@example.com")).thenReturn(userResponseDto);

        UserResponseDto result = userService.getUserByEmail("john.doe@example.com");

        assertNotNull(result);
        assertEquals(userResponseDto, result);
        verify(userRepository, never()).findUserByEmail(anyString());
    }

    @Test
    void getUserByEmail_CacheMiss_Success() {
        when(userCacheManager.getUserByEmail("john.doe@example.com")).thenReturn(null);
        when(userRepository.findUserByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.getUserByEmail("john.doe@example.com");

        assertNotNull(result);
        assertEquals(userResponseDto, result);
        verify(userCacheManager).cacheUser("1", user.getEmail(), userResponseDto);
    }

    @Test
    void getUserByEmail_NotFound_ThrowsException() {
        when(userCacheManager.getUserByEmail("john.doe@example.com")).thenReturn(null);
        when(userRepository.findUserByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("john.doe@example.com"));
    }

    @Test
    void getUsersByIds_Success() {
        when(userRepository.findUserByIdIn(List.of(1L))).thenReturn(List.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        List<UserResponseDto> result = userService.getUsersByIds(List.of("1"));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userResponseDto, result.get(0));
    }

    @Test
    void getUsersByIds_NoUsers_ThrowsException() {
        when(userRepository.findUserByIdIn(List.of(1L))).thenReturn(Collections.emptyList());

        assertThrows(UserNotFoundException.class, () -> userService.getUsersByIds(List.of("1")));
    }

    @Test
    void updateUser_Success_SameEmail() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toEntity(userRequestDto)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.updateUser("1", userRequestDto);

        assertNotNull(result);
        assertEquals(userResponseDto, result);
        verify(userRepository).save(user);
        verify(userCacheManager).cacheUser("1", user.getEmail(), userResponseDto);
        verify(userCacheManager, never()).evictUser(anyString(), anyString());
    }

    @Test
    void updateUser_Success_DifferentEmail() {
        String newEmail = "new.john.doe@example.com";
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("John");
        updatedUser.setSurname("Doe");
        updatedUser.setEmail(newEmail);
        updatedUser.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto updatedResponseDto = new UserResponseDto();
        updatedResponseDto.setId(1L);
        updatedResponseDto.setName("John");
        updatedResponseDto.setSurname("Doe");
        updatedResponseDto.setEmail(newEmail);
        updatedResponseDto.setBirthDate(LocalDate.of(1990, 1, 1));

        userRequestDto.setEmail(newEmail);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findUserByEmail(newEmail)).thenReturn(Optional.empty());
        when(userMapper.toEntity(userRequestDto)).thenReturn(updatedUser);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(updatedResponseDto);

        UserResponseDto result = userService.updateUser("1", userRequestDto);

        assertNotNull(result);
        assertEquals(updatedResponseDto, result);
        verify(userRepository).save(user);
        verify(userCacheManager).cacheUser("1", newEmail, updatedResponseDto);
        verify(userCacheManager).evictUser("1", "john.doe@example.com");
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser("1", userRequestDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser("1");

        verify(userRepository).delete(user);
        verify(userCacheManager).evictUser("1", user.getEmail());
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser("1"));
        verify(userRepository, never()).delete(any());
    }
}