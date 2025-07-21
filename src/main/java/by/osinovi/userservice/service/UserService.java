package by.osinovi.userservice.service;


import by.osinovi.userservice.entity.User;

import java.util.List;

public interface UserService {
    void createUser(User user);

    User getUserById(String id);

    List<User> getUsersByIds(List<String> ids);

    User getUserByEmail(String email);

    void updateUser(String id, User user);

    void deleteUser(String id);
}
