package ru.javabegin.springboot.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.javabegin.springboot.jwt.entity.User;

import java.util.Optional;

// работа с пользователем

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	// используем обертку Optional - контейнер, который хранит значение или null - позволяет избежать ошибки NullPointerException
	Optional<User> findByUsername(String username); // поиск по username

	// используем обертку Optional - контейнер, который хранит значение или null - позволяет избежать ошибки NullPointerException
	Optional<User> findByEmail(String email); // поиск по email

	// обновление пароля
	@Modifying // если запрос изменяет данные - желательно добавлять эту аннотацию
	@Transactional // если запрос изменяет данные - желательно добавлять эту аннотацию
	@Query("UPDATE User u SET u.password = :password WHERE u.username=:username") // обновление записи с нужным UUID
	int updatePassword(@Param("password") String password, @Param("username") String username); // возвращает int (сколько записей обновил) - в данном случае всегда должен возвращать 1




}
