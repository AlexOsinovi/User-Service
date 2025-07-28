package by.osinovi.userservice.dto.card;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardResponseDto {
    private Long id;
    private String number;
    private String holder;
    private LocalDate expirationDate;
    private Integer userId;
}