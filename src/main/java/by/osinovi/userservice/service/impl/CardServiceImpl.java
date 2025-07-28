package by.osinovi.userservice.service.impl;

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
import by.osinovi.userservice.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CardResponseDto createCard(String userId, CardRequestDto cardRequestDto) {
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));

        String fullName = user.getName() + " " + user.getSurname();
        String holder = cardRequestDto.getHolder().trim();
        if (!holder.equalsIgnoreCase(fullName)) {
            throw new InvalidInputException("Holder must match the user's full name: " + fullName);
        }

        Card card = cardMapper.toEntity(cardRequestDto);
        card.setUser(user);
        cardRepository.save(card);
        return cardMapper.toDto(card);
    }

    @Override
    @Cacheable(value = "cards", key = "#id")
    public CardResponseDto getCardById(String id) {
        Card card = cardRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new CardNotFoundException("Карта с id " + id + " не найдена"));
        return cardMapper.toDto(card);
    }

    @Override
    @Cacheable(value = "cardsList", key = "#userId")
    public List<CardResponseDto> getCardsByUserId(String userId) {
        List<Card> cards = cardRepository.findCardsByUserId(Long.valueOf(userId));
        if (cards.isEmpty()) {
            throw new CardNotFoundException("Нет ни одной карты по userId " + userId);
        }
        return cards.stream().map(cardMapper::toDto).collect(Collectors.toList());
    }



    @Transactional
    @CachePut(value = "cards", key = "#id")
    public CardResponseDto updateCard(String id, String userId, CardRequestDto cardRequestDto) {
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        Card existingCard = cardRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new CardNotFoundException("Карта с id " + id + " не найдена"));

        String fullName = user.getName().toUpperCase() + " " + user.getSurname().toUpperCase();
        String holder = cardRequestDto.getHolder().trim();
        if (!holder.equalsIgnoreCase(fullName)) {
            throw new InvalidInputException("Holder must match the user's full name: " + fullName);
        }

        Card updatedCard = cardMapper.toEntity(cardRequestDto);
        existingCard.setNumber(updatedCard.getNumber());
        existingCard.setHolder(updatedCard.getHolder());
        existingCard.setExpirationDate(updatedCard.getExpirationDate());
        existingCard.setUser(user);
        cardRepository.save(existingCard);
        return cardMapper.toDto(existingCard);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cards", key = "#id")
    public void deleteCard(String id) {
        Card card = cardRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new CardNotFoundException("Карта с id " + id + " не найдена"));
        cardRepository.delete(card);
    }
}