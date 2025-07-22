package by.osinovi.userservice.mapper;

import by.osinovi.userservice.dto.UserRequestDto;
import by.osinovi.userservice.dto.UserResponseDto;
import by.osinovi.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "cards", ignore = true)
    User toEntity(UserRequestDto dto);

    UserResponseDto toDto(User entity);
}
