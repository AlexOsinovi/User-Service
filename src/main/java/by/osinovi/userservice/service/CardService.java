package by.osinovi.userservice.service;

import by.osinovi.userservice.dto.CardRequestDto;
import by.osinovi.userservice.dto.CardResponseDto;

import java.util.List;

public interface CardService {
    void createCard(String userId, CardRequestDto cardRequestDto);

    CardResponseDto getCardById(String id);

    List<CardResponseDto> getCardsByUserId(String id);

    List<CardResponseDto> getCardsByIds(List<String> ids);

    void updateCard(String id, String userId, CardRequestDto cardRequestDto);

    void deleteCard(String id);
}
