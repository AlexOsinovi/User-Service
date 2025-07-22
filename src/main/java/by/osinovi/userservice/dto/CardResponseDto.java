package by.osinovi.userservice.dto;

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
    private Integer id;
    private String number;
    private String holder;
    private LocalDate expirationDate;
    private Integer userId;
}