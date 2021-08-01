package ru.javabegin.springboot.jwt.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// POJO класс для передачи ошибки клиенту в формате JSON
@Getter
@Setter
@AllArgsConstructor
public class JsonException {

    private String exception; // тип ошибки


}
