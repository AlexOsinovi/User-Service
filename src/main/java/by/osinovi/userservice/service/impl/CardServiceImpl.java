package by.osinovi.userservice.service.impl;

import by.osinovi.userservice.dto.CardRequestDto;
import by.osinovi.userservice.dto.CardResponseDto;
import by.osinovi.userservice.entity.Card;
import by.osinovi.userservice.repository.CardRepository;
import by.osinovi.userservice.service.CardService;
import by.osinovi.userservice.mapper.CardMapper;
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

    @Override
    @Transactional
    public void createCard(CardRequestDto cardRequestDto) {
        Card card = cardMapper.toEntity(cardRequestDto);
        cardRepository.save(card);
    }

    @Override
    public CardResponseDto getCardById(String id) {
        Card card = cardRepository.findById(Integer.valueOf(id))
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
    public void updateCard(String id, CardRequestDto cardRequestDto) {
        Card existingCard = cardRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + id));
        Card updatedCard = cardMapper.toEntity(cardRequestDto);
        existingCard.setNumber(updatedCard.getNumber());
        existingCard.setHolder(updatedCard.getHolder());
        existingCard.setExpirationDate(updatedCard.getExpirationDate());
        cardRepository.save(existingCard);
    }

    @Override
    @Transactional
    public void deleteCard(String cardNumber) {
        Card card = cardRepository.findById(Integer.valueOf(cardNumber))
                .orElseThrow(() -> new EntityNotFoundException("Card not found with number: " + cardNumber));
        cardRepository.delete(card);
    }
}