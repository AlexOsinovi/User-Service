package by.osinovi.userservice.service;

import by.osinovi.userservice.entity.Card;
import by.osinovi.userservice.entity.User;

import java.util.List;

public interface CardService {
    void createCard(Card card);

    Card getCardById(String id);

    List<Card> getCardsByUserId(String id);

    List<Card> getCardsByIds(List<String> ids);

    void updateCard(String id, Card card);

    void deleteCard(String cardNumber);
}
