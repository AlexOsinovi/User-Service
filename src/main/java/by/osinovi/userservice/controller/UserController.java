package by.osinovi.userservice.controller;

import by.osinovi.userservice.dto.UserRequestDto;
import by.osinovi.userservice.dto.UserResponseDto;
import by.osinovi.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        userService.createUser(userRequestDto);
        UserResponseDto userResponseDto = userService.getUserByEmail(userRequestDto.getEmail());
        return new ResponseEntity<>(userResponseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable String id) {
        UserResponseDto userResponseDto = userService.getUserById(id);
        return new ResponseEntity<>(userResponseDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getUsersByIds(@RequestParam List<String> ids) {
        List<UserResponseDto> users = userService.getUsersByIds(ids);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String email) {
        UserResponseDto userResponseDto = userService.getUserByEmail(email);
        return new ResponseEntity<>(userResponseDto, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable String id, @Valid @RequestBody UserRequestDto userRequestDto) {
        userService.updateUser(id, userRequestDto);
        UserResponseDto userResponseDto = userService.getUserById(id);
        return new ResponseEntity<>(userResponseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}