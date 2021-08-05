package ru.javabegin.springboot.jwt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.javabegin.springboot.jwt.entity.Activity;
import ru.javabegin.springboot.jwt.entity.Role;
import ru.javabegin.springboot.jwt.entity.User;
import ru.javabegin.springboot.jwt.repository.ActivityRepository;
import ru.javabegin.springboot.jwt.repository.RoleRepository;
import ru.javabegin.springboot.jwt.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    public static final String DEFAULT_ROLE = "USER"; // такая роль должна быть обязательно в таблице БД


    private UserRepository userRepository; // работа с пользователями
    private RoleRepository roleRepository; // работа с ролями
    private ActivityRepository activityRepository; // работа с активностями


    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, ActivityRepository activityRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.activityRepository = activityRepository;
    }


    public void register(User user, Activity activity) {

        // сохраняем данные в БД - если будет ошибка - никакие данные в БД не попадут, произойдет Rollback (откат транзакции) - благодаря @Transactional
        userRepository.save(user);
        activityRepository.save(activity); // почему мы отдельно сохр. activity - потому это новый пользователь и у него еще нет соттв. записи в Activity
    }

//    public void save(User user) {
//        userRepository.save(user);
//    }



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

    // получаем из БД объект роли
    public Optional<Role> findByName(String role) {
        return roleRepository.findByName(role);
    }

    public Activity saveActivity(Activity activity){
        return activityRepository.save(activity);
    }

    public Optional<Activity> findActivityByUuid(String uuid){
        return activityRepository.findByUuid(uuid);
    }

    // true сконвертируется в 1, т.к. указали @Type(type = "org.hibernate.type.NumericBooleanType") в классе Activity
    public int activate(String uuid){
        return activityRepository.changeActivated(uuid, true);
    }

    // false сконвертируется в 0, т.к. указали @Type(type = "org.hibernate.type.NumericBooleanType") в классе Activity
    public int deactivate(String uuid){
        return activityRepository.changeActivated(uuid, false);
    }


    public int updatePassword(String password, String username){
        return userRepository.updatePassword(password, username);
    }
}
