package ru.javabegin.springboot.jwt.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.javabegin.springboot.jwt.entity.User;
import ru.javabegin.springboot.jwt.exception.UserOrEmailExistsException;
import ru.javabegin.springboot.jwt.objects.JsonException;
import ru.javabegin.springboot.jwt.service.UserService;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@Log
public class AuthController {

    private UserService userService; // сервис для работы с пользователями
    private PasswordEncoder encoder; // кодировщик паролей (или любых данных), создает односторонний хеш

    @Autowired
    public AuthController(UserService userService, PasswordEncoder encoder) {
        this.userService = userService;
        this.encoder = encoder;
    }

    // регистрация нового пользователя
    @PutMapping("/register")
    public ResponseEntity register(@Valid @RequestBody User user) { // здесь параметр user используется, чтобы передать все данные пользователя для регистрации

        // если имя или email уже существуют в БД - не даем создать пользователя
        if (userService.userExists(user.getUsername(), user.getEmail())) {
            throw new UserOrEmailExistsException("User or email already exists"); // возвращаем клиенту ошибку
        }
        // зашифровать пароль (алгоритм BCrypt)
        user.setPassword(encoder.encode(user.getPassword())); // генерим хеш пароля на основе переданного текста

        userService.save(user); // сохранить пользователя в БД

        return ResponseEntity.ok().build(); // просто отправляем статус 200-ОК (без каких-либо данных) - значит регистрация выполнилась успешно
    }

    /*

    Метод перехватывает все ошибки в контроллере (неверный логин-пароль и пр.)

    Даже без этого метода все ошибки будут отправляться клиенту, просто здесь это можно кастомизировать, например отправить JSON в нужном формате

    Можно настроить, какие типа ошибок отправлять в явном виде, а какие нет (чтобы не давать лишнюю информацию злоумышленникам)

    */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonException> handleExceptions(Exception ex) {

/*

        DisabledException (наш созданный класс) - не активирован
        UserAlreadyActivatedException - пользователь уже активирован (пытается неск. раз активировать)
        UsernameNotFoundException - username или email не найден в базе

        BadCredentialsException - неверный логин-пароль
        UserOrEmailExistsException - пользователь или email уже существуют
        DataIntegrityViolationException - ошибка уникальности в БД

        Эти типы ошибок можно будет считывать на клиенте и обрабатывать как нужно (например, показать текст ошибки)

*/

        // отправляем название класса ошибки (чтобы правильно обработать ошибку на клиенте)
        return new ResponseEntity(new JsonException(ex.getClass().getSimpleName()), HttpStatus.BAD_REQUEST); // Spring автоматически конвертирует объект JsonException в JSON

    }
}
