package by.osinovi.userservice.mapper;

import by.osinovi.userservice.dto.CardRequestDto;
import by.osinovi.userservice.dto.CardResponseDto;
import by.osinovi.userservice.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface CardMapper {

    Card toEntity(CardRequestDto dto);

    @Mapping(target = "userId", expression = "java(entity.getUser().getId())")
    CardResponseDto toDto(Card entity);
}