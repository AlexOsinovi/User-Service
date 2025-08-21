package by.osinovi.userservice.integration.controller;

import by.osinovi.userservice.dto.card.CardRequestDto;
import by.osinovi.userservice.dto.card.CardResponseDto;
import by.osinovi.userservice.dto.user.UserRequestDto;
import by.osinovi.userservice.dto.user.UserResponseDto;
import by.osinovi.userservice.integration.config.BaseIntegrationTest;
import by.osinovi.userservice.integration.util.JwtUtilTest;
import by.osinovi.userservice.repository.CardRepository;
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
class CardControllerIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

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

        cardRepository.deleteAll();
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

    private CardResponseDto createCard(Long userId, CardRequestDto cardRequest) throws Exception {
        String response = mockMvc.perform(post("/api/cards/user/{userId}", userId)
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, CardResponseDto.class);
    }

    @Test
    void createCard_ShouldReturnCreated() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Test");
        userRequest.setSurname("User");
        userRequest.setEmail("test@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("1234567890123456");
        cardRequest.setHolder("TEST USER");
        cardRequest.setExpirationDate(LocalDate.of(2025, 1, 1));

        mockMvc.perform(post("/api/cards/user/{userId}", createdUser.getId())
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value("1234567890123456"))
                .andExpect(jsonPath("$.holder").value("TEST USER"))
                .andExpect(jsonPath("$.expirationDate").value("2025-01-01"));
    }

    @Test
    void getCardById_ShouldReturnOk() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Test");
        userRequest.setSurname("User");
        userRequest.setEmail("test_user@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("1234567890123456");
        cardRequest.setHolder("TEST USER");
        cardRequest.setExpirationDate(LocalDate.of(2025, 1, 1));

        CardResponseDto createdCard = createCard(createdUser.getId(), cardRequest);

        mockMvc.perform(get("/api/cards/{id}", createdCard.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("1234567890123456"))
                .andExpect(jsonPath("$.holder").value("TEST USER"))
                .andExpect(jsonPath("$.expirationDate").value("2025-01-01"));
    }

    @Test
    void updateCard_ShouldReturnOk() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Test");
        userRequest.setSurname("User");
        userRequest.setEmail("test@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("1234567890123456");
        cardRequest.setHolder("TEST USER");
        cardRequest.setExpirationDate(LocalDate.of(2025, 1, 1));

        CardResponseDto createdCard = createCard(createdUser.getId(), cardRequest);

        CardRequestDto updateRequest = new CardRequestDto();
        updateRequest.setNumber("9876543210987654");
        updateRequest.setHolder("TEST USER");
        updateRequest.setExpirationDate(LocalDate.of(2026, 1, 1));

        mockMvc.perform(put("/api/cards/{id}/user/{userId}", createdCard.getId(),createdUser.getId())
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("9876543210987654"))
                .andExpect(jsonPath("$.holder").value("TEST USER"))
                .andExpect(jsonPath("$.expirationDate").value("2026-01-01"));
    }

    @Test
    void deleteCard_ShouldReturnNoContent() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Eve");
        userRequest.setSurname("Davis");
        userRequest.setEmail("test@example.com");
        userRequest.setBirthDate(LocalDate.of(1993, 9, 25));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("7777777777777777");
        cardRequest.setHolder("EVE DAVIS");
        cardRequest.setExpirationDate(LocalDate.of(2026, 12, 31));

        CardResponseDto createdCard = createCard(createdUser.getId(), cardRequest);

        mockMvc.perform(delete("/api/cards/{id}", createdCard.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cards/{id}", createdCard.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCard_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/cards/{id}", 999L)
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCard_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", 999L)
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCardWithoutToken_ShouldReturnUnauthorized() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Frank");
        userRequest.setSurname("White");
        userRequest.setEmail("test_Frank@example.com");
        userRequest.setBirthDate(LocalDate.of(1988, 7, 20));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("8888888888888888");
        cardRequest.setHolder("FRANK WHITE");
        cardRequest.setExpirationDate(LocalDate.of(2025, 8, 20));

        mockMvc.perform(post("/api/cards/user/{userId}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isUnauthorized());
    }
}