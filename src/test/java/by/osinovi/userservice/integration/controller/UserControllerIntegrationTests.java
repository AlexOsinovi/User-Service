package by.osinovi.userservice.integration.controller;

import by.osinovi.userservice.dto.user.UserRequestDto;
import by.osinovi.userservice.dto.user.UserResponseDto;
import by.osinovi.userservice.integration.config.BaseIntegrationTest;
import by.osinovi.userservice.integration.util.JwtUtilTest;
import by.osinovi.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWebMvc
class UserControllerIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtilTest jwtUtilTest;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private String testToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        userRepository.deleteAll();

        testToken = jwtUtilTest.generateTestToken("test@example.com");
    }

    private UserResponseDto createUser(UserRequestDto userRequest) throws Exception {
        String response = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, UserResponseDto.class);
    }

    @Test
    void createUser_ShouldReturnCreated() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Test");
        userRequest.setSurname("User");
        userRequest.setEmail("test@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.surname").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.birthDate").value("1990-01-01"));
    }

    @Test
    void getUserById_ShouldReturnOk() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Test");
        userRequest.setSurname("User");
        userRequest.setEmail("test@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        mockMvc.perform(get("/api/users/{id}", createdUser.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.surname").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.birthDate").value("1990-01-01"));
    }

    @Test
    void updateUser_ShouldReturnOk() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Test");
        userRequest.setSurname("User");
        userRequest.setEmail("test@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        UserRequestDto updateRequest = new UserRequestDto();
        updateRequest.setName("Updated");
        updateRequest.setSurname("User");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setBirthDate(LocalDate.of(1991, 1, 1));

        mockMvc.perform(put("/api/users/{id}", createdUser.getId())
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.surname").value("User"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.birthDate").value("1991-01-01"));
    }

    @Test
    void updateUser_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UserRequestDto updateRequest = new UserRequestDto();
        updateRequest.setName("Test");
        updateRequest.setSurname("User");
        updateRequest.setEmail("test@example.com");
        updateRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(put("/api/users/{id}", 999L)
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Eve");
        userRequest.setSurname("Davis");
        userRequest.setEmail("test@example.com");
        userRequest.setBirthDate(LocalDate.of(1993, 9, 25));

        UserResponseDto createdUser = createUser(userRequest);

        mockMvc.perform(delete("/api/users/{id}", createdUser.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", createdUser.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 999L)
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L)
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUserWithoutToken_ShouldReturnUnauthorized() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Frank");
        userRequest.setSurname("White");
        userRequest.setEmail("test_DDDD@example.com");
        userRequest.setBirthDate(LocalDate.of(1988, 7, 20));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isUnauthorized());
    }
}