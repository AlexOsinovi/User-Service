package by.osinovi.userservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CardResponseDto {
    private Integer id;
    private String number;
    private String holder;
    private LocalDate expirationDate;
    private Integer userId;
}