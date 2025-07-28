package by.osinovi.userservice.mapper;

import by.osinovi.userservice.dto.user.UserRequestDto;
import by.osinovi.userservice.dto.user.UserResponseDto;
import by.osinovi.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,uses = CardMapper.class)
public interface UserMapper {

    User toEntity(UserRequestDto dto);

    UserResponseDto toDto(User entity);
}
