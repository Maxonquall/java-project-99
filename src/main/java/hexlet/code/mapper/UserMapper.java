package hexlet.code.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.model.User;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)

public abstract class UserMapper {
    @Autowired
    private BCryptPasswordEncoder encoder;

    @Mapping(target = "passwordDigest", source = "password")
    public abstract User map(UserCreateDTO model);


    public abstract User map(UserUpdateDTO model);

    @Mapping(target = "username", source = "email")
    @Mapping(target = "password", ignore = true)
    public abstract UserDTO map(User model);

    public abstract void update(UserUpdateDTO update, @MappingTarget User destination);

    @BeforeMapping
    public void encryptPassword(UserCreateDTO data) {
        var password = data.getPassword();
        data.setPassword(encoder.encode(password));
    }

//    @AfterMapping
//    public void encryptPassword(UserCreateDTO data, @MappingTarget User user) {
//        var rawPassword = user.getRawPassword();
//        if (rawPassword != null) {
//            user.setPasswordDigest(encoder.encode(rawPassword));
//            user.setRawPassword(null); // очистка временного поля
//        }
//    }
//
}