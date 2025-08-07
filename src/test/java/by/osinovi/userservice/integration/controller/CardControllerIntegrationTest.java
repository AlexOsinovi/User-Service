package by.osinovi.userservice.integration.controller;

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

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createCard_ShouldReturnCreatedCard() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("John");
        userRequest.setSurname("Doe");
        userRequest.setEmail("john.doe@example.com");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        String userResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserResponseDto createdUser = objectMapper.readValue(userResponse, UserResponseDto.class);

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

        String userResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserResponseDto createdUser = objectMapper.readValue(userResponse, UserResponseDto.class);

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

        mockMvc.perform(post("/api/cards/user/{userId}", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCardById_ShouldReturnCard() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Alice");
        userRequest.setSurname("Johnson");
        userRequest.setEmail("alice.johnson@example.com");
        userRequest.setBirthDate(LocalDate.of(1992, 3, 10));

        String userResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserResponseDto createdUser = objectMapper.readValue(userResponse, UserResponseDto.class);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("1111111111111111");
        cardRequest.setHolder("ALICE JOHNSON");
        cardRequest.setExpirationDate(LocalDate.of(2026, 6, 30));

        String cardResponse = mockMvc.perform(post("/api/cards/user/{userId}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        CardResponseDto createdCard = objectMapper.readValue(cardResponse, CardResponseDto.class);

        mockMvc.perform(get("/api/cards/{id}", createdCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdCard.getId()))
                .andExpect(jsonPath("$.number").value("1111111111111111"))
                .andExpect(jsonPath("$.holder").value("ALICE JOHNSON"))
                .andExpect(jsonPath("$.expirationDate").value("2026-06-30"))
                .andExpect(jsonPath("$.userId").value(createdUser.getId()));
    }

    @Test
    void getCardById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", "999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCardsByUserId_ShouldReturnCards() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Bob");
        userRequest.setSurname("Brown");
        userRequest.setEmail("bob.brown@example.com");
        userRequest.setBirthDate(LocalDate.of(1988, 7, 22));

        String userResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserResponseDto createdUser = objectMapper.readValue(userResponse, UserResponseDto.class);

        CardRequestDto card1 = new CardRequestDto();
        card1.setNumber("2222222222222222");
        card1.setHolder("BOB BROWN");
        card1.setExpirationDate(LocalDate.of(2025, 8, 15));

        CardRequestDto card2 = new CardRequestDto();
        card2.setNumber("3333333333333333");
        card2.setHolder("BOB BROWN");
        card2.setExpirationDate(LocalDate.of(2027, 3, 20));

        mockMvc.perform(post("/api/cards/user/{userId}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(card1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/cards/user/{userId}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(card2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/cards/user/{userId}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(createdUser.getId()))
                .andExpect(jsonPath("$[1].userId").value(createdUser.getId()));
    }

    @Test
    void getCardsByUserId_WithNonExistentUser_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/cards/user/{userId}", "999"))
                .andExpect(status().isNotFound());

    }

    @Test
    void updateCard_ShouldReturnUpdatedCard() throws Exception {
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setName("Charlie");
        userRequest.setSurname("Wilson");
        userRequest.setEmail("charlie.wilson@example.com");
        userRequest.setBirthDate(LocalDate.of(1995, 11, 8));

        String userResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserResponseDto createdUser = objectMapper.readValue(userResponse, UserResponseDto.class);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("4444444444444444");
        cardRequest.setHolder("CHARLIE WILSON");
        cardRequest.setExpirationDate(LocalDate.of(2026, 1, 10));

        String cardResponse = mockMvc.perform(post("/api/cards/user/{userId}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        CardResponseDto createdCard = objectMapper.readValue(cardResponse, CardResponseDto.class);

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

        String userResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserResponseDto createdUser = objectMapper.readValue(userResponse, UserResponseDto.class);

        CardRequestDto updateRequest = new CardRequestDto();
        updateRequest.setNumber("6666666666666666");
        updateRequest.setHolder("DAVID MILLER");
        updateRequest.setExpirationDate(LocalDate.of(2027, 9, 30));

        mockMvc.perform(put("/api/cards/{id}/user/{userId}", "999", createdUser.getId())
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

        String userResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserResponseDto createdUser = objectMapper.readValue(userResponse, UserResponseDto.class);

        CardRequestDto cardRequest = new CardRequestDto();
        cardRequest.setNumber("7777777777777777");
        cardRequest.setHolder("EVE DAVIS");
        cardRequest.setExpirationDate(LocalDate.of(2026, 12, 31));

        String cardResponse = mockMvc.perform(post("/api/cards/user/{userId}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        CardResponseDto createdCard = objectMapper.readValue(cardResponse, CardResponseDto.class);

        mockMvc.perform(delete("/api/cards/{id}", createdCard.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cards/{id}", createdCard.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCard_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/cards/{id}", "999"))
                .andExpect(status().isNotFound());
    }

} 