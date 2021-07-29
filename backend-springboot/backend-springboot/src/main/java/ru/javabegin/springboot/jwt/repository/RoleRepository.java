package ru.javabegin.springboot.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.javabegin.springboot.jwt.entity.Role;

import java.util.Optional;

// работа с ролями пользователя

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name); // поиск роли по названию
}
