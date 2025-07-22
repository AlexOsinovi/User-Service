package by.osinovi.userservice.service.impl;

import by.osinovi.userservice.dto.CardRequestDto;
import by.osinovi.userservice.dto.CardResponseDto;
import by.osinovi.userservice.entity.Card;
import by.osinovi.userservice.entity.User;
import by.osinovi.userservice.mapper.CardMapper;
import by.osinovi.userservice.repository.CardRepository;
import by.osinovi.userservice.repository.UserRepository;
import by.osinovi.userservice.service.CardService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createCard(String userId, CardRequestDto cardRequestDto) {
        User user = userRepository.findById(Integer.valueOf(userId))
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Card card = cardMapper.toEntity(cardRequestDto);
        card.setUser(user);
        cardRepository.save(card);
    }

    @Override
    public CardResponseDto getCardById(String id) {
        Card card = cardRepository.findCardById(Integer.valueOf(id))
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + id));
        return cardMapper.toDto(card);
    }

    @Override
    public List<CardResponseDto> getCardsByUserId(String userId) {
        List<Card> cards = cardRepository.findCardsByUserId(Integer.valueOf(userId));
        return cards.stream().map(cardMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CardResponseDto> getCardsByIds(List<String> ids) {
        List<Integer> intIds = ids.stream().map(Integer::valueOf).collect(Collectors.toList());
        List<Card> cards = cardRepository.findCardByIdIn(intIds);
        return cards.stream().map(cardMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateCard(String id, String userId, CardRequestDto cardRequestDto) {
        User user = userRepository.findById(Integer.valueOf(userId))
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Card existingCard = cardRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + id));
        Card updatedCard = cardMapper.toEntity(cardRequestDto);
        existingCard.setNumber(updatedCard.getNumber());
        existingCard.setHolder(updatedCard.getHolder());
        existingCard.setExpirationDate(updatedCard.getExpirationDate());
        existingCard.setUser(user);
        cardRepository.save(existingCard);
    }

    @Override
    @Transactional
    public void deleteCard(String id) {
        Card card = cardRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new EntityNotFoundException("Card not found with number: " + id));
        cardRepository.delete(card);
    }
}