package ru.javabegin.springboot.jwt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.javabegin.springboot.jwt.entity.User;
import ru.javabegin.springboot.jwt.repository.UserRepository;

import javax.transaction.Transactional;

@Service
@Transactional
public class UserService {

    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User save(User user){
        return userRepository.save(user);
    }

    // проверка, существует ли пользователь в БД (email и username должны быть уникальными в таблице)
    public boolean userExists(String username, String email) {

        if (userRepository.existsByUsername(username)) {
            return true; // если запись в БД существует
        }

        if (userRepository.existsByEmail(email)) {
            return true; // если запись в БД существует
        }

        return false;
    }
}
