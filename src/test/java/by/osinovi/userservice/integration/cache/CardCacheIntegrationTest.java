package by.osinovi.userservice.integration.cache;

import by.osinovi.userservice.config.CardCacheManager;
import by.osinovi.userservice.config.UserCacheManager;
import by.osinovi.userservice.dto.card.CardRequestDto;
import by.osinovi.userservice.dto.card.CardResponseDto;
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
class CardCacheIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CardCacheManager cardCacheManager;

    @Autowired
    private UserCacheManager userCacheManager;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        cardCacheManager.clearAll();
        userCacheManager.clearAll();
    }

    private UserResponseDto createUser(UserRequestDto userRequest) throws Exception {
        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, UserResponseDto.class);
    }

    private CardResponseDto createCard(Long userId, CardRequestDto cardRequest) throws Exception {
        String response = mockMvc.perform(post("/api/cards/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, CardResponseDto.class);
    }

    @Test
    void cacheCard_ShouldCacheResultAndEvictUser() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Cache");
        userRequest.setSurname("Card");
        userRequest.setEmail("cache.card@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("1234567890123456");
        cardRequest.setHolder("CACHE CARD");
        cardRequest.setExpirationDate(LocalDate.of(2025, 12, 31));

        CardResponseDto createdCard = createCard(createdUser.getId(), cardRequest);

        assertThat(cardCacheManager.getCard(String.valueOf(createdCard.getId()))).isNotNull();
        assertThat(userCacheManager.getUserById(String.valueOf(createdUser.getId()))).isNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNull();
    }

    @Test
    void getCardById_ShouldCacheResult() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Getcache");
        userRequest.setSurname("Card");
        userRequest.setEmail("get.cache.card@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("1234560000000456");
        cardRequest.setHolder("GETCACHE CARD");
        cardRequest.setExpirationDate(LocalDate.of(2025, 12, 31));

        CardResponseDto createdCard = createCard(createdUser.getId(), cardRequest);

        cardCacheManager.clearAll();
        userCacheManager.clearAll();

        mockMvc.perform(get("/api/cards/{id}", createdCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdCard.getId()));

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
        cardRequest.setNumber("1111222233334444");
        cardRequest.setHolder("EVICT CARD");
        cardRequest.setExpirationDate(LocalDate.of(2026, 6, 30));

        CardResponseDto createdCard = createCard(createdUser.getId(), cardRequest);

        mockMvc.perform(get("/api/cards/{id}", createdCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("1111222233334444"));

        assertThat(cardCacheManager.getCard(String.valueOf(createdCard.getId()))).isNotNull();

        CardRequestDto updateRequest = new CardRequestDto();
        updateRequest.setNumber("5555666677778888");
        updateRequest.setHolder("EVICT CARD");
        updateRequest.setExpirationDate(LocalDate.of(2028, 1, 1));

        mockMvc.perform(put("/api/cards/{id}/user/{userId}", createdCard.getId(), createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdCard.getId()))
                .andExpect(jsonPath("$.number").value("5555666677778888"))
                .andExpect(jsonPath("$.holder").value("EVICT CARD"))
                .andExpect(jsonPath("$.expirationDate").value("2028-01-01"))
                .andExpect(jsonPath("$.userId").value(createdUser.getId()));

        assertThat(cardCacheManager.getCard(String.valueOf(createdCard.getId()))).isNotNull();
        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNull();
    }

    @Test
    void deleteCard_ShouldEvictCache() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Delete");
        userRequest.setSurname("Card");
        userRequest.setEmail("delete.card@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("9999888877776666");
        cardRequest.setHolder("DELETE CARD");
        cardRequest.setExpirationDate(LocalDate.of(2027, 7, 7));

        CardResponseDto createdCard = createCard(createdUser.getId(), cardRequest);

        mockMvc.perform(get("/api/cards/{id}", createdCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("9999888877776666"));

        mockMvc.perform(get("/api/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()));

        assertThat(cardCacheManager.getCard(String.valueOf(createdCard.getId()))).isNotNull();
        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNotNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNotNull();

        mockMvc.perform(delete("/api/cards/{id}", createdCard.getId()))
                .andExpect(status().isNoContent());

        assertThat(cardCacheManager.getCard(String.valueOf(createdCard.getId()))).isNull();
        assertThat(userCacheManager.getUserById(createdUser.getId().toString())).isNull();
        assertThat(userCacheManager.getUserByEmail(createdUser.getEmail())).isNull();
    }
}