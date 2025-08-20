package by.osinovi.userservice.integration.cache;

import by.osinovi.userservice.config.cache.UserCacheManager;
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
    void createUser_ShouldCacheUser() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Test");
        userRequest.setSurname("User");
        userRequest.setEmail("test@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        mockMvc.perform(get("/api/users/{id}", createdUser.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test"));

        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNotNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNotNull();
    }

    @Test
    void updateUser_ShouldEvictOldEmailCache() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Evict");
        userRequest.setSurname("Test");
        userRequest.setEmail("test@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        UserRequestDto updateRequest = new UserRequestDto();
        updateRequest.setName("Evict");
        updateRequest.setSurname("Testaa");
        updateRequest.setEmail("evict.updated@example.com");
        updateRequest.setBirthDate(LocalDate.of(1992, 1, 1));

        String updatedResponse = mockMvc.perform(put("/api/users/{id}", createdUser.getId())
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Evict"))
                .andExpect(jsonPath("$.surname").value("Testaa"))
                .andExpect(jsonPath("$.email").value("evict.updated@example.com"))
                .andExpect(jsonPath("$.birthDate").value("1992-01-01"))
                .andReturn().getResponse().getContentAsString();

        UserResponseDto updatedUser = objectMapper.readValue(updatedResponse, UserResponseDto.class);

        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNotNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNull();
        assertThat(userCacheManager.getUserByEmail(updatedUser.getEmail())).isNotNull();
    }

    @Test
    void deleteUser_ShouldEvictCache() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Delete");
        userRequest.setSurname("Test");
        userRequest.setEmail("test@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        mockMvc.perform(get("/api/users/{id}", createdUser.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Delete"));

        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNotNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNotNull();

        mockMvc.perform(delete("/api/users/{id}", createdUser.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNoContent());

        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNull();
    }
}