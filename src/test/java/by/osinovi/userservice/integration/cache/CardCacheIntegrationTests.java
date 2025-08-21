package by.osinovi.userservice.integration.cache;

import by.osinovi.userservice.config.cache.CardCacheManager;
import by.osinovi.userservice.config.cache.UserCacheManager;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWebMvc
class CardCacheIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CardCacheManager cardCacheManager;

    @Autowired
    private UserCacheManager userCacheManager;

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

    private CardResponseDto createCard(UserResponseDto user, CardRequestDto cardRequest) throws Exception {
        String response = mockMvc.perform(post("/api/cards/user/{userId}", user.getId())
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, CardResponseDto.class);
    }

    @Test
    void createUser_ShouldReturnOk() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Test");
        userRequest.setSurname("User");
        userRequest.setEmail("CUSRO@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("CUSRO@example.com"))
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.surname").value("User"))
                .andExpect(jsonPath("$.birthDate").value("1990-01-01"));
    }

    @Test
    void createCard_ShouldCacheCard() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Cache");
        userRequest.setSurname("Card");
        userRequest.setEmail("CC@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("1234567890123456");
        cardRequest.setHolder("CACHE CARD");
        cardRequest.setExpirationDate(LocalDate.of(2025, 1, 1));

        CardResponseDto createdCard = createCard(createdUser, cardRequest);

        mockMvc.perform(get("/api/cards/{id}", createdCard.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("1234567890123456"))
                .andExpect(jsonPath("$.holder").value("CACHE CARD"))
                .andExpect(jsonPath("$.expirationDate").value("2025-01-01"));

        assertThat(cardCacheManager.getCard(String.valueOf(createdCard.getId()))).isNotNull();
    }

    @Test
    void updateCard_ShouldEvictCache() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Evict");
        userRequest.setSurname("Card");
        userRequest.setEmail("evict.card@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("5555666677778888");
        cardRequest.setHolder("EVICT CARD");
        cardRequest.setExpirationDate(LocalDate.of(2028, 1, 1));

        CardResponseDto createdCard = createCard(createdUser, cardRequest);

        mockMvc.perform(get("/api/cards/{id}", createdCard.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("5555666677778888"));

        assertThat(cardCacheManager.getCard(String.valueOf(createdCard.getId()))).isNotNull();

        CardRequestDto updateRequest = new CardRequestDto();
        updateRequest.setNumber("8888777766665555");
        updateRequest.setHolder("EVICT CARD");
        updateRequest.setExpirationDate(LocalDate.of(2029, 1, 1));

        mockMvc.perform(put("/api/cards/{id}/user/{userId}", createdCard.getId(),createdUser.getId())
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("8888777766665555"))
                .andExpect(jsonPath("$.holder").value("EVICT CARD"))
                .andExpect(jsonPath("$.expirationDate").value("2029-01-01"));

        assertThat(cardCacheManager.getCard(String.valueOf(createdCard.getId()))).isNotNull();
        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNull();

    }

    @Test
    void deleteCard_ShouldEvictCache() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Delete");
        userRequest.setSurname("Card");
        userRequest.setEmail("delc@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("9999888877776666");
        cardRequest.setHolder("DELETE CARD");
        cardRequest.setExpirationDate(LocalDate.of(2027, 7, 7));

        CardResponseDto createdCard = createCard(createdUser, cardRequest);

        mockMvc.perform(get("/api/cards/{id}", createdCard.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("9999888877776666"));

        mockMvc.perform(get("/api/users/{id}", createdUser.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId().toString()));

        assertThat(cardCacheManager.getCard(String.valueOf(createdCard.getId()))).isNotNull();
        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNotNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNotNull();

        mockMvc.perform(delete("/api/cards/{id}", createdCard.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNoContent());

        assertThat(cardCacheManager.getCard(String.valueOf(createdCard.getId()))).isNull();
        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNull();
    }
}