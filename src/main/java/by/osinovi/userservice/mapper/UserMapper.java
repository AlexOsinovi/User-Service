package by.osinovi.userservice.mapper;

import by.osinovi.userservice.dto.UserRequestDto;
import by.osinovi.userservice.dto.UserResponseDto;
import by.osinovi.userservice.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",uses = CardMapper.class)
public interface UserMapper {

    User toEntity(UserRequestDto dto);

    UserResponseDto toDto(User entity);
}
