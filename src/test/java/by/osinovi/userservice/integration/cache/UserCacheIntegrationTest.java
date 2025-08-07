package by.osinovi.userservice.integration.cache;

import by.osinovi.userservice.config.UserCacheManager;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWebMvc
class UserCacheIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserCacheManager userCacheManager;


    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        userCacheManager.clearAll();
    }

    @Test
    void createUser_ShouldCacheResult() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Cache");
        userRequest.setSurname("Test");
        userRequest.setEmail("create.cache@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserResponseDto createdUser = objectMapper.readValue(response, UserResponseDto.class);

        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNotNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNotNull();
    }

    @Test
    void getUserById_ShouldCacheResult() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("GetCache");
        userRequest.setSurname("Test");
        userRequest.setEmail("cache.test@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserResponseDto createdUser = objectMapper.readValue(response, UserResponseDto.class);

        userCacheManager.clearAll();

        mockMvc.perform(get("/api/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()));

        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNotNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNotNull();
    }

    @Test
    void getUserByEmail_ShouldCacheResult() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Email");
        userRequest.setSurname("Cache");
        userRequest.setEmail("email.cache@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserResponseDto createdUser = objectMapper.readValue(response, UserResponseDto.class);

        userCacheManager.clearAll();

        mockMvc.perform(get("/api/users/email/{email}", "email.cache@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Email"))
                .andExpect(jsonPath("$.surname").value("Cache"));

        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNotNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNotNull();
    }

    @Test
    void updateUser_ShouldUpdateCache() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Evict");
        userRequest.setSurname("Test");
        userRequest.setEmail("evict.test@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserResponseDto createdUser = objectMapper.readValue(response, UserResponseDto.class);

        mockMvc.perform(get("/api/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Evict"));

        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNotNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNotNull();

        UserRequestDto updateRequest = new UserRequestDto();
        updateRequest.setName("Evict Updated");
        updateRequest.setSurname("Test Updated");
        updateRequest.setEmail("evict.updated@example.com");
        updateRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        String updatedResponse = mockMvc.perform(put("/api/users/{id}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Evict Updated"))
                .andReturn().getResponse().getContentAsString();
        ;

        UserResponseDto updatedUser = objectMapper.readValue(updatedResponse, UserResponseDto.class);
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNull();
        assertThat(userCacheManager.getUserByEmail(updatedUser.getEmail())).isNotNull();
    }

    @Test
    void deleteUser_ShouldEvictCache() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Delete");
        userRequest.setSurname("Test");
        userRequest.setEmail("delete.test@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserResponseDto createdUser = objectMapper.readValue(response, UserResponseDto.class);

        mockMvc.perform(get("/api/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Delete"));

        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNotNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNotNull();

        mockMvc.perform(delete("/api/users/{id}", createdUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNull();
    }

} 