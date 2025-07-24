package by.osinovi.userservice.controller;

import by.osinovi.userservice.dto.CardRequestDto;
import by.osinovi.userservice.dto.CardResponseDto;
import by.osinovi.userservice.exception.CardNotFoundException;
import by.osinovi.userservice.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<CardResponseDto> createCard(@PathVariable String userId, @Valid @RequestBody CardRequestDto cardRequestDto) {
        cardService.createCard(userId, cardRequestDto);
        List<CardResponseDto> cards = cardService.getCardsByUserId(userId);
        CardResponseDto createdCard = cards.stream()
                .filter(card -> card.getNumber().equals(cardRequestDto.getNumber()))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException("Не удалось получить созданную карту"));
        return new ResponseEntity<>(createdCard, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDto> getCardById(@PathVariable String id) {
        CardResponseDto cardResponseDto = cardService.getCardById(id);
        return new ResponseEntity<>(cardResponseDto, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardResponseDto>> getCardsByUserId(@PathVariable String userId) {
        List<CardResponseDto> cards = cardService.getCardsByUserId(userId);
        return new ResponseEntity<>(cards, HttpStatus.OK);
    }

    @GetMapping("/bulk")
    public ResponseEntity<List<CardResponseDto>> getCardsByIds(@RequestParam List<String> ids) {
        List<CardResponseDto> cards = cardService.getCardsByIds(ids);
        return new ResponseEntity<>(cards, HttpStatus.OK);
    }

    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<CardResponseDto> updateCard(@PathVariable String id, @PathVariable String userId, @Valid @RequestBody CardRequestDto cardRequestDto) {
        cardService.updateCard(id, userId, cardRequestDto);
        CardResponseDto cardResponseDto = cardService.getCardById(id);
        return new ResponseEntity<>(cardResponseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable String id) {
        cardService.deleteCard(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}