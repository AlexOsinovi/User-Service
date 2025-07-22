package by.osinovi.userservice.mapper;

import by.osinovi.userservice.dto.CardRequestDto;
import by.osinovi.userservice.dto.CardResponseDto;
import by.osinovi.userservice.entity.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {

    Card toEntity(CardRequestDto dto);

    CardResponseDto toDto(Card entity);
}