package by.osinovi.userservice.integration.controller;

import by.osinovi.userservice.dto.user.UserRequestDto;
import by.osinovi.userservice.dto.user.UserResponseDto;
import by.osinovi.userservice.integration.config.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
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
class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private UserResponseDto createUser(UserRequestDto userRequest) throws Exception {
        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, UserResponseDto.class);
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("John");
        userRequest.setSurname("Doe");
        userRequest.setEmail("john.doe@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.birthDate").value("1990-01-01"));
    }

    @Test
    void createUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("");
        userRequest.setSurname("Doe");
        userRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Jane");
        userRequest.setSurname("Smith");
        userRequest.setEmail("jane.smith@example.com");
        userRequest.setBirthDate(LocalDate.of(1985, 5, 15));

        UserResponseDto createdUser = createUser(userRequest);

        mockMvc.perform(get("/api/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.surname").value("Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));
    }

    @Test
    void getUserById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", "999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUsersByIds_ShouldReturnUsers() throws Exception {
        UserRequestDto user1 = new UserRequestDto();
        user1.setName("Alice");
        user1.setSurname("Johnson");
        user1.setEmail("alice.johnson@example.com");
        user1.setBirthDate(LocalDate.of(1992, 3, 10));

        UserRequestDto user2 = new UserRequestDto();
        user2.setName("Bob");
        user2.setSurname("Brown");
        user2.setEmail("bob.brown@example.com");
        user2.setBirthDate(LocalDate.of(1988, 7, 22));

        UserResponseDto createdUser1 = createUser(user1);
        UserResponseDto createdUser2 = createUser(user2);

        mockMvc.perform(get("/api/users")
                        .param("ids", createdUser1.getId().toString(), createdUser2.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(createdUser1.getId()))
                .andExpect(jsonPath("$[1].id").value(createdUser2.getId()));
    }

    @Test
    void getUserByEmail_ShouldReturnUser() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Charlie");
        userRequest.setSurname("Wilson");
        userRequest.setEmail("charlie.wilson@example.com");
        userRequest.setBirthDate(LocalDate.of(1995, 11, 8));

        createUser(userRequest);

        mockMvc.perform(get("/api/users/email/{email}", "charlie.wilson@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Charlie"))
                .andExpect(jsonPath("$.surname").value("Wilson"))
                .andExpect(jsonPath("$.email").value("charlie.wilson@example.com"));
    }

    @Test
    void getUserByEmail_WithNonExistentEmail_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/email/{email}", "nonexistent@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("David");
        userRequest.setSurname("Miller");
        userRequest.setEmail("david.miller@example.com");
        userRequest.setBirthDate(LocalDate.of(1991, 4, 12));

        UserResponseDto createdUser = createUser(userRequest);;

        UserRequestDto updateRequest = new UserRequestDto();
        updateRequest.setName("David Updated");
        updateRequest.setSurname("Miller Updated");
        updateRequest.setEmail("david.updated@example.com");
        updateRequest.setBirthDate(LocalDate.of(1991, 4, 12));

        mockMvc.perform(put("/api/users/{id}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.name").value("David Updated"))
                .andExpect(jsonPath("$.surname").value("Miller Updated"))
                .andExpect(jsonPath("$.email").value("david.updated@example.com"));
    }

    @Test
    void updateUser_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UserRequestDto updateRequest = new UserRequestDto();
        updateRequest.setName("Test");
        updateRequest.setSurname("User");
        updateRequest.setEmail("test@example.com");

        mockMvc.perform(put("/api/users/{id}", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Eve");
        userRequest.setSurname("Davis");
        userRequest.setEmail("eve.davis@example.com");
        userRequest.setBirthDate(LocalDate.of(1993, 9, 25));



        UserResponseDto createdUser = createUser(userRequest);

        mockMvc.perform(delete("/api/users/{id}", createdUser.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", createdUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", "999"))
                .andExpect(status().isNotFound());
    }

}