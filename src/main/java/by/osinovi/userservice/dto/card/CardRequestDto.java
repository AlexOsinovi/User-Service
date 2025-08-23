package by.osinovi.userservice.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "\\d{16}", message = "Card number must contain 16 digits")
    private String number;

    @NotBlank(message = "Holder is required")
    @Pattern(regexp = "^[A-Z]{1,32}\\s[A-Z]{1,32}$", message = "The holder must contain only uppercase letters and follow the pattern: NAME SURNAME")
    private String holder;

    @NotNull(message = "Expiration date is required")
    private LocalDate expirationDate;
}