package by.osinovi.userservice.integration.controller;

import by.osinovi.userservice.dto.card.CardRequestDto;
import by.osinovi.userservice.dto.card.CardResponseDto;
import by.osinovi.userservice.dto.user.UserRequestDto;
import by.osinovi.userservice.dto.user.UserResponseDto;
import by.osinovi.userservice.integration.config.BaseIntegrationTest;
import by.osinovi.userservice.repository.CardRepository;
import by.osinovi.userservice.repository.UserRepository;
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
class CardControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        cardRepository.deleteAll();
        userRepository.deleteAll();
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
    void createCard_ShouldReturnCreatedCard() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("John");
        userRequest.setSurname("Doe");
        userRequest.setEmail("john.doe@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("1234567890123456");
        cardRequest.setHolder("JOHN DOE");
        cardRequest.setExpirationDate(LocalDate.of(2025, 12, 31));

        mockMvc.perform(post("/api/cards/user/{userId}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.number").value("1234567890123456"))
                .andExpect(jsonPath("$.holder").value("JOHN DOE"))
                .andExpect(jsonPath("$.expirationDate").value("2025-12-31"))
                .andExpect(jsonPath("$.userId").value(createdUser.getId()));
    }

    @Test
    void createCard_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Jane");
        userRequest.setSurname("Smith");
        userRequest.setEmail("jane.smith@example.com");
        userRequest.setBirthDate(LocalDate.of(1985, 5, 15));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("123");
        cardRequest.setHolder("jane smith");
        cardRequest.setExpirationDate(LocalDate.of(2025, 12, 31));

        mockMvc.perform(post("/api/cards/user/{userId}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCard_WithNonExistentUser_ShouldReturnNotFound() throws Exception {
        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("1234567890123456");
        cardRequest.setHolder("JOHN DOE");
        cardRequest.setExpirationDate(LocalDate.of(2025, 12, 31));

        mockMvc.perform(post("/api/cards/user/{userId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCardById_ShouldReturnCard() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Alic");
        userRequest.setSurname("Johnson");
        userRequest.setEmail("alice.johnson@example.com");
        userRequest.setBirthDate(LocalDate.of(1992, 3, 10));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("1234567800123456");
        cardRequest.setHolder("ALIC JOHNSON");
        cardRequest.setExpirationDate(LocalDate.of(2025, 12, 31));

        CardResponseDto createdCard = createCard(createdUser.getId(), cardRequest);

        mockMvc.perform(get("/api/cards/{id}", createdCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdCard.getId()))
                .andExpect(jsonPath("$.number").value("1234567800123456"))
                .andExpect(jsonPath("$.holder").value("ALIC JOHNSON"))
                .andExpect(jsonPath("$.expirationDate").value("2025-12-31"))
                .andExpect(jsonPath("$.userId").value(createdUser.getId()));
    }

    @Test
    void getCardsByUserId_WithNonExistentUser_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/cards/user/{userId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCard_ShouldReturnUpdatedCard() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Charlie");
        userRequest.setSurname("Wilson");
        userRequest.setEmail("charlie.wilson@example.com");
        userRequest.setBirthDate(LocalDate.of(1995, 11, 8));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("4444444444444444");
        cardRequest.setHolder("CHARLIE WILSON");
        cardRequest.setExpirationDate(LocalDate.of(2026, 1, 10));

        CardResponseDto createdCard = createCard(createdUser.getId(), cardRequest);

        CardRequestDto updateRequest = new CardRequestDto();
        updateRequest.setNumber("5555555555555555");
        updateRequest.setHolder("CHARLIE WILSON");
        updateRequest.setExpirationDate(LocalDate.of(2029, 5, 15));

        mockMvc.perform(put("/api/cards/{id}/user/{userId}", createdCard.getId(), createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdCard.getId()))
                .andExpect(jsonPath("$.number").value("5555555555555555"))
                .andExpect(jsonPath("$.holder").value("CHARLIE WILSON"))
                .andExpect(jsonPath("$.expirationDate").value("2029-05-15"))
                .andExpect(jsonPath("$.userId").value(createdUser.getId()));
    }

    @Test
    void updateCard_WithNonExistentCard_ShouldReturnNotFound() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("David");
        userRequest.setSurname("Miller");
        userRequest.setEmail("david.miller@example.com");
        userRequest.setBirthDate(LocalDate.of(1991, 4, 12));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto updateRequest = new CardRequestDto();
        updateRequest.setNumber("6666666666666666");
        updateRequest.setHolder("DAVID MILLER");
        updateRequest.setExpirationDate(LocalDate.of(2027, 9, 30));

        mockMvc.perform(put("/api/cards/{id}/user/{userId}", 999L, createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCard_ShouldReturnNoContent() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Eve");
        userRequest.setSurname("Davis");
        userRequest.setEmail("eve.davis@example.com");
        userRequest.setBirthDate(LocalDate.of(1993, 9, 25));

        UserResponseDto createdUser = createUser(userRequest);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("7777777777777777");
        cardRequest.setHolder("EVE DAVIS");
        cardRequest.setExpirationDate(LocalDate.of(2026, 12, 31));

        CardResponseDto createdCard = createCard(createdUser.getId(), cardRequest);

        mockMvc.perform(delete("/api/cards/{id}", createdCard.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cards/{id}", createdCard.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCard_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/cards/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}