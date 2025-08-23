package by.osinovi.userservice.mapper;

import by.osinovi.userservice.dto.card.CardRequestDto;
import by.osinovi.userservice.dto.card.CardResponseDto;
import by.osinovi.userservice.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {

    Card toEntity(CardRequestDto dto);

    @Mapping(source = "user.id", target = "userId")
    CardResponseDto toDto(Card entity);
}