package ru.javabegin.springboot.jwt.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.javabegin.springboot.jwt.entity.Activity;
import ru.javabegin.springboot.jwt.entity.Role;
import ru.javabegin.springboot.jwt.entity.User;
import ru.javabegin.springboot.jwt.exception.RoleNotFoundException;
import ru.javabegin.springboot.jwt.exception.UserAlreadyActivatedException;
import ru.javabegin.springboot.jwt.exception.UserOrEmailExistsException;
import ru.javabegin.springboot.jwt.objects.JsonException;
import ru.javabegin.springboot.jwt.service.UserDetailsImpl;
import ru.javabegin.springboot.jwt.service.UserService;
import ru.javabegin.springboot.jwt.utils.CookieUtils;
import ru.javabegin.springboot.jwt.utils.JwtUtils;

import javax.validation.Valid;
import java.util.UUID;

import static ru.javabegin.springboot.jwt.service.UserService.DEFAULT_ROLE;

@RestController
@RequestMapping("/auth")
@Log
public class AuthController {

    private UserService userService; // сервис для работы с пользователями
    private PasswordEncoder encoder; // кодировщик паролей (или любых данных), создает односторонний хеш
    private AuthenticationManager authenticationManager; // стандартный встроенный менеджер Spring, проверяет логин-пароль
    private JwtUtils jwtUtils; // класс-утилита для работы с jwt
    private CookieUtils cookieUtils; // класс-утилита для работы с куками




    @Autowired
    public AuthController(UserService userService, PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils, CookieUtils cookieUtils) {
        this.userService = userService;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.cookieUtils = cookieUtils;

    }



    @PostMapping("/test-no-auth")
    public String testNoAuth() {
        return "OK-no-auth";
    }

    @PostMapping("/test-with-auth")
    @PreAuthorize("USER")
    public String testWithAuth() {
        return "OK-with-auth";
    }



    // регистрация нового пользователя
    // этот метод всем будет доступен для вызова (не будем его "защищать" с помощью токенов, т.к. это не требуется по задаче)
    @PutMapping("/register")
    public ResponseEntity register(@Valid @RequestBody User user) { // здесь параметр user используется, чтобы передать все данные пользователя для регистрации

        // если имя или email уже существуют в БД - не даем создать пользователя
        if (userService.userExists(user.getUsername(), user.getEmail())) {
            throw new UserOrEmailExistsException("User or email already exists"); // возвращаем клиенту ошибку
        }


        // присваиваем дефолтную роль новому пользователю

        Role userRole = userService.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new RoleNotFoundException("Default Role USER not found.")); // если в БД нет такой роли - выбрасываем исключение
        user.getRoles().add(userRole); // добавить роль USER для создаваемого пользователя

        /* нам не нужно создавать новый объект Role, а нужно получать его из БД (т.к. все роли уже созданы в таблицах)
           Объект роли нужно всегда получать из БД перед регистрацией пользователя, т.к. все должно работать динамически (если роли изменили в БД, мы считаем обновленные данные)
          Т.е. вариант загрузки всех ролей в начале работы приложения - не подойдет, т.к. данные в БД могут измениться, а нас останется неактуальные роли
        */



        // зашифровать пароль (алгоритм BCrypt)
        user.setPassword(encoder.encode(user.getPassword())); // генерим хеш пароля на основе переданного текста


        // сохранить в БД активность пользователя
        Activity activity = new Activity();
        activity.setUser(user);
        activity.setUuid(UUID.randomUUID().toString()); // уникальный UUID - нужен для активации пользователя

        userService.register(user, activity);

        return ResponseEntity.ok().build(); // просто отправляем статус 200-ОК (без каких-либо данных) - значит регистрация выполнилась успешно
    }


    // активация пользователя (чтобы мог авторизоваться и работать дальше с приложением)
    // этот метод всем будет доступен для вызова (не будем его "защищать" с помощью токенов, т.к. это не требуется по задаче)
    @PostMapping("/activate-account")
    public ResponseEntity<Boolean> activateUser(@RequestBody String uuid) { // true - успешно активирован

        // проверяем UUID пользователя, которого хотим активировать
        Activity activity = userService.findActivityByUuid(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("Activity Not Found with uuid: " + uuid));

        // если пользователь уже был ранее активирован
        if (activity.isActivated())
            throw new UserAlreadyActivatedException("User already activated");

        // возвращает кол-во обновленных записей (в нашем случае должна быть 1)
        int updatedCount = userService.activate(uuid); // активируем пользователя

        return ResponseEntity.ok(updatedCount == 1); // 1 - значит запись обновилась успешно, 0 - что-то пошло не так
    }



    // залогиниться по паролю-пользователю
    // этот метод всем будет доступен для вызова (не будем его "защищать" с помощью токенов, т.к. это не требуется по задаче)
    @PostMapping("/login")
    public ResponseEntity<User> login(@Valid @RequestBody User user) { // здесь параметр user используется как контейнер, чтобы передать логин-пароль

        // проверяем логин-пароль
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        // добавляем в Spring-контейнер информацию об авторизации (чтобы Spring понимал, что пользователь успешно вошел и мог использовать его роли и другие параметры)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // UserDetailsImpl - спец. объект, который хранится в Spring контейнере и содержит данные пользователя
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // активирован пользователь или нет (проверяем только после того, как пользователь успешно залогинился)
        if (!userDetails.isActivated()) {
            throw new DisabledException("User disabled"); // клиенту отправится ошибка о том, что пользователь не активирован
        }


        // если мы дошло до этой строки, значит пользователь успешно залогинился

        // пароль зануляем до формирования jwt
        userDetails.getUser().setPassword(null); // пароль нужен только один раз для аутентификации - поэтому можем его занулить, чтобы больше нигде не "засветился"


        // после каждого успешного входа генерируется новый jwt, чтобы следующие запросы на backend авторизовывать автоматически
        String jwt = jwtUtils.createAccessToken(userDetails.getUser());


        // создаем кук со значением jwt (браузер будет отправлять его автоматически на backend при каждом запросе)
        // обратите внимание на флаги безопасности в методе создания кука
        HttpCookie cookie = cookieUtils.createJwtCookie(jwt); // server-side cookie

        HttpHeaders responseHeaders = new HttpHeaders(); // объект для добавления заголовков в response
        responseHeaders.add(HttpHeaders.SET_COOKIE, cookie.toString()); // добавляем server-side cookie в заголовок (header)


        // отправляем клиенту данные пользователя (и jwt-кук в заголовке Set-Cookie)
        return ResponseEntity.ok().headers(responseHeaders).body(userDetails.getUser());


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

        BadCredentialsException - неверный логин-пароль (или любые другие данные пользователя, которые он использовал для аутентификации)
        UserOrEmailExistsException - пользователь или email уже существуют
        DataIntegrityViolationException - ошибка уникальности в БД

        Эти типы ошибок можно будет считывать на клиенте и обрабатывать как нужно (например, показать текст ошибки)

*/

        // отправляем название класса ошибки (чтобы правильно обработать ошибку на клиенте)
        return new ResponseEntity(new JsonException(ex.getClass().getSimpleName()), HttpStatus.BAD_REQUEST); // Spring автоматически конвертирует объект JsonException в JSON

    }

}
