package by.osinovi.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardRequestDto {
    @NotBlank(message = "Card number is required")
    @Size(max = 32, message = "Card number must not exceed 32 characters")
    private String number;

    @NotBlank(message = "Holder is required")
    @Size(max = 128, message = "Holder must not exceed 128 characters")
    private String holder;

    @NotBlank(message = "Expiration date is required")
    private LocalDate expirationDate;
}