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
import by.osinovi.userservice.config.cache.CardCacheManager;
import by.osinovi.userservice.config.cache.UserCacheManager;
import by.osinovi.userservice.service.CardService;
import lombok.RequiredArgsConstructor;
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
    private final CardCacheManager cardCacheManager;
    private final UserCacheManager userCacheManager;

    @Override
    @Transactional
    public CardResponseDto createCard(String userId, CardRequestDto cardRequestDto) {
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));

        if (cardRepository.existsByNumber(cardRequestDto.getNumber())) {
            throw new InvalidInputException("Card with number " + cardRequestDto.getNumber() + " already exists");
        }

        String fullName = user.getName() + " " + user.getSurname();
        String holder = cardRequestDto.getHolder().trim();
        if (!holder.equalsIgnoreCase(fullName)) {
            throw new InvalidInputException("Holder must match the user's full name: " + fullName);
        }

        Card card = cardMapper.toEntity(cardRequestDto);
        card.setUser(user);
        cardRepository.save(card);
        CardResponseDto response = cardMapper.toDto(card);
        cardCacheManager.cacheCard(String.valueOf(card.getId()), response);
        userCacheManager.evictUser(userId, user.getEmail());
        return response;
    }

    @Override
    public CardResponseDto getCardById(String id) {
        CardResponseDto cached = cardCacheManager.getCard(id);
        if (cached != null) {
            return cached;
        }
        Card card = cardRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new CardNotFoundException("Card with id " + id + " not found"));
        CardResponseDto response = cardMapper.toDto(card);
        cardCacheManager.cacheCard(id, response);
        return response;
    }

    @Override
    public List<CardResponseDto> getCardsByUserId(String userId) {
        List<Card> cards = cardRepository.findCardsByUserId(Long.valueOf(userId));
        if (cards.isEmpty()) {
            throw new CardNotFoundException("No cards found for userId " + userId);
        }
        return cards.stream().map(cardMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CardResponseDto updateCard(String id, String userId, CardRequestDto cardRequestDto) {
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
        Card existingCard = cardRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new CardNotFoundException("Card with id " + id + " not found"));

        if (!existingCard.getNumber().equals(cardRequestDto.getNumber()) &&
                cardRepository.existsByNumber(cardRequestDto.getNumber())) {
            throw new IllegalArgumentException("Card with number " + cardRequestDto.getNumber() + " already exists");
        }

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
        CardResponseDto response = cardMapper.toDto(existingCard);
        cardCacheManager.cacheCard(id, response);
        userCacheManager.evictUser(userId, user.getEmail());
        return response;
    }

    @Override
    @Transactional
    public void deleteCard(String id) {
        Card card = cardRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new CardNotFoundException("Card with id " + id + " not found"));
        cardCacheManager.evictCard(id);
        userCacheManager.evictUser(String.valueOf(card.getUser().getId()), String.valueOf(card.getUser().getEmail()));
        cardRepository.delete(card);
    }

}