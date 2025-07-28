package by.osinovi.userservice.service;

import by.osinovi.userservice.dto.card.CardRequestDto;
import by.osinovi.userservice.dto.card.CardResponseDto;

import java.util.List;

public interface CardService {
    CardResponseDto createCard(String userId, CardRequestDto cardRequestDto);

    CardResponseDto getCardById(String id);

    List<CardResponseDto> getCardsByUserId(String id);

    CardResponseDto updateCard(String id, String userId, CardRequestDto cardRequestDto);

    void deleteCard(String id);
}
