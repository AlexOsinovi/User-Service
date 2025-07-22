package by.osinovi.userservice.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserResponseDto {
    private Integer id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private List<CardResponseDto> cards;
}
