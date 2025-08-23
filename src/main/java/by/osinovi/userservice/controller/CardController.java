package by.osinovi.userservice.controller;

import by.osinovi.userservice.dto.card.CardRequestDto;
import by.osinovi.userservice.dto.card.CardResponseDto;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(userId, cardRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDto> getCardById(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.OK).body(cardService.getCardById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardResponseDto>> getCardsByUserId(@PathVariable String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(cardService.getCardsByUserId(userId));
    }

    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<CardResponseDto> updateCard(@PathVariable String id, @PathVariable String userId, @Valid @RequestBody CardRequestDto cardRequestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(cardService.updateCard(id, userId, cardRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable String id) {
        cardService.deleteCard(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}