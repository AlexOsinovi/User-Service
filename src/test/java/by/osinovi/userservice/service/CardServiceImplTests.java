package by.osinovi.userservice.service;

import by.osinovi.userservice.dto.card.CardRequestDto;
import by.osinovi.userservice.dto.card.CardResponseDto;
import by.osinovi.userservice.entity.Card;
import by.osinovi.userservice.entity.User;
import by.osinovi.userservice.exception.CardNotFoundException;
import by.osinovi.userservice.exception.InvalidInputException;
import by.osinovi.userservice.exception.UserNotFoundException;
import by.osinovi.userservice.mapper.CardMapper;
import by.osinovi.userservice.repository.CardRepository;
import by.osinovi.userservice.repository.UserRepository;
import by.osinovi.userservice.config.CardCacheManager;
import by.osinovi.userservice.config.UserCacheManager;
import by.osinovi.userservice.service.impl.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTests {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardCacheManager cardCacheManager;

    @Mock
    private UserCacheManager userCacheManager;

    @InjectMocks
    private CardServiceImpl cardService;

    private User user;
    private Card card;
    private CardRequestDto cardRequestDto;
    private CardResponseDto cardResponseDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("JOHN");
        user.setSurname("DOE");
        user.setEmail("john.doe@example.com");

        card = new Card();
        card.setId(1L);
        card.setNumber("1234567890123456");
        card.setHolder("JOHN DOE");
        card.setExpirationDate(LocalDate.of(2025, 12, 31));
        card.setUser(user);

        cardRequestDto = new CardRequestDto();
        cardRequestDto.setNumber("1234567890123456");
        cardRequestDto.setHolder("JOHN DOE");
        cardRequestDto.setExpirationDate(LocalDate.of(2025, 12, 31));

        cardResponseDto = new CardResponseDto();
        cardResponseDto.setId(1L);
        cardResponseDto.setNumber("1234567890123456");
        cardResponseDto.setHolder("JOHN DOE");
        cardResponseDto.setExpirationDate(LocalDate.of(2025, 12, 31));
        cardResponseDto.setUserId(1);
    }

    @Test
    void createCard_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.existsByNumber(cardRequestDto.getNumber())).thenReturn(false);
        when(cardMapper.toEntity(cardRequestDto)).thenReturn(card);
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardResponseDto);

        CardResponseDto result = cardService.createCard("1", cardRequestDto);

        assertNotNull(result);
        assertEquals(cardResponseDto, result);
        verify(cardRepository).save(card);
        verify(cardCacheManager).cacheCard("1", cardResponseDto);
        verify(userCacheManager).evictUser("1", user.getEmail());
    }

    @Test
    void createCard_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> cardService.createCard("1", cardRequestDto));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCard_CardNumberExists_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.existsByNumber(cardRequestDto.getNumber())).thenReturn(true);

        assertThrows(InvalidInputException.class, () -> cardService.createCard("1", cardRequestDto));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCard_InvalidHolder_ThrowsException() {
        cardRequestDto.setHolder("JANE DOE");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.existsByNumber(cardRequestDto.getNumber())).thenReturn(false);

        assertThrows(InvalidInputException.class, () -> cardService.createCard("1", cardRequestDto));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void getCardById_CacheHit_Success() {
        when(cardCacheManager.getCard("1")).thenReturn(cardResponseDto);

        CardResponseDto result = cardService.getCardById("1");

        assertNotNull(result);
        assertEquals(cardResponseDto, result);
        verify(cardRepository, never()).findById(anyLong());
    }

    @Test
    void getCardById_CacheMiss_Success() {
        when(cardCacheManager.getCard("1")).thenReturn(null);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardResponseDto);

        CardResponseDto result = cardService.getCardById("1");

        assertNotNull(result);
        assertEquals(cardResponseDto, result);
        verify(cardCacheManager).cacheCard("1", cardResponseDto);
    }

    @Test
    void getCardById_NotFound_ThrowsException() {
        when(cardCacheManager.getCard("1")).thenReturn(null);
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getCardById("1"));
    }

    @Test
    void getCardsByUserId_Success() {
        when(cardRepository.findCardsByUserId(1L)).thenReturn(List.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardResponseDto);

        List<CardResponseDto> result = cardService.getCardsByUserId("1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(cardResponseDto, result.get(0));
    }

    @Test
    void getCardsByUserId_NoCards_ThrowsException() {
        when(cardRepository.findCardsByUserId(1L)).thenReturn(Collections.emptyList());

        assertThrows(CardNotFoundException.class, () -> cardService.getCardsByUserId("1"));
    }

    @Test
    void updateCard_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardMapper.toEntity(cardRequestDto)).thenReturn(card);
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardResponseDto);

        CardResponseDto result = cardService.updateCard("1", "1", cardRequestDto);

        assertNotNull(result);
        assertEquals(cardResponseDto, result);
        verify(cardRepository).save(card);
        verify(cardCacheManager).cacheCard("1", cardResponseDto);
        verify(userCacheManager).evictUser("1", user.getEmail());
    }

    @Test
    void updateCard_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> cardService.updateCard("1", "1", cardRequestDto));
    }

    @Test
    void updateCard_CardNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.updateCard("1", "1", cardRequestDto));
    }

    @Test
    void updateCard_DuplicateCardNumber_ThrowsException() {
        cardRequestDto.setNumber("9876543210987654");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.existsByNumber(cardRequestDto.getNumber())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> cardService.updateCard("1", "1", cardRequestDto));
    }

    @Test
    void updateCard_InvalidHolder_ThrowsException() {
        cardRequestDto.setHolder("JANE DOE");
        cardRequestDto.setNumber("1234567890123456");
        cardRequestDto.setExpirationDate(LocalDate.of(2025, 12, 31));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(InvalidInputException.class, () -> cardService.updateCard("1", "1", cardRequestDto));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void deleteCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.deleteCard("1");

        verify(cardRepository).delete(card);
        verify(cardCacheManager).evictCard("1");
        verify(userCacheManager).evictUser("1", user.getEmail());
    }

    @Test
    void deleteCard_NotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard("1"));
        verify(cardRepository, never()).delete(any());
    }
}