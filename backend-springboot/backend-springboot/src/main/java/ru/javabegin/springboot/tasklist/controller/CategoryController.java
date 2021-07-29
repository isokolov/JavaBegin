package ru.javabegin.springboot.tasklist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.javabegin.springboot.tasklist.entity.Category;
import ru.javabegin.springboot.tasklist.search.CategorySearchValues;
import ru.javabegin.springboot.tasklist.service.CategoryService;
import ru.javabegin.springboot.tasklist.util.MyLogger;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // для получения данных используем тип запроса POST, позволяет отправлять и получать значения в body - более безопасно, по сравнению с GET
    @PostMapping("/all")
    public ResponseEntity<List<Category>> findAll(@RequestBody String email){ // пока в body будет передаваться только значение email

        MyLogger.debugMethodName("CategoryController: findAll(email) ---------------------------------------------------------- ");

        return ResponseEntity.ok(categoryService.findAll(email));
    }

    /* {
      "title": "тест",
      "user": {
        "id": 35
      }
    } */
    @PutMapping("/add")
    public ResponseEntity<Category> add(@RequestBody Category category){ // в category передается объект для вставки в БД


        MyLogger.debugMethodName("CategoryController: add(category) ---------------------------------------------------------- ");


        // проверка на обязательные параметры
        if (category.getId() != null && category.getId() != 0) {
            // id создается автоматически в БД (autoincrement), поэтому его передавать не нужно, иначе может быть конфликт уникальности значения
            return new ResponseEntity("redundant param: id MUST be null", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение title
        if (category.getTitle() == null || category.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title", HttpStatus.NOT_ACCEPTABLE);
        }

        // получаем созданный в БД объект уже с ID и передаем его клиенту обратно

//        return new ResponseEntity(categoryService.add(category), HttpStatus.OK);
        return ResponseEntity.ok(categoryService.add(category));

    }

    // для обновления используем тип запроса PATCH , позволяет передавать значение в body, а не в адресной строке (как в GET)
    @PatchMapping("/update")
    public ResponseEntity update(@RequestBody Category category) { // в category передается объект для обновления в БД

        MyLogger.debugMethodName("CategoryController: update(category) ---------------------------------------------------------- ");


        // проверка на обязательные параметры - id должен быть заполнен, т.к. это обновление существующего объекта
        if (category.getId() == null || category.getId() == 0) {
            return new ResponseEntity("missed param: id", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение title (обязательно должен быть заполнен)
        if (category.getTitle() == null || category.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title", HttpStatus.NOT_ACCEPTABLE);
        }

        // save работает как на добавление, так и на обновление
        categoryService.update(category);

        return new ResponseEntity(HttpStatus.OK); // просто отправляем статус 200 (операция прошла успешно), без объекта, т.к. нет смысла его обратно передавать
    }



    // для обновления используем тип запроса DELETE , позволяет передавать значение в body, а не в адресной строке (как в GET)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity delete(@PathVariable Long id) { // id объекта, который хотим удалить

        MyLogger.debugMethodName("CategoryController: delete(id) ---------------------------------------------------------- ");

        // проверка на обязательные параметры - id должен быть заполнен, т.к. это обновление существующего объекта
        if (id == null || id == 0) {
            return new ResponseEntity("missed param: id", HttpStatus.NOT_ACCEPTABLE);
        }

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            categoryService.delete(id);
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return new ResponseEntity("id=" + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return new ResponseEntity(HttpStatus.OK); // просто отправляем статус 200 без объектов (операция прошла успешно)
    }

    // поиск по любым параметрам CategorySearchValues
    @PostMapping("/search")
    public ResponseEntity<List<Category>> search(@RequestBody CategorySearchValues categorySearchValues) {

        MyLogger.debugMethodName("CategoryController: search() ---------------------------------------------------------- ");

        // поиск категорий пользователя по названию
        List<Category> list = categoryService.findByTitle(categorySearchValues.getTitle(), categorySearchValues.getEmail());

        return ResponseEntity.ok(list);
    }

    // поиск 1 объекта по id
    @PostMapping("/id")
    public ResponseEntity<Category> findById(@RequestBody Long id) {

        MyLogger.debugMethodName("CategoryController: findById() ---------------------------------------------------------- ");


        Category category = null;

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            category = categoryService.findById(id);
        } catch (NoSuchElementException e) { // если объект не будет найден
            e.printStackTrace();
            return new ResponseEntity("id=" + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return ResponseEntity.ok(category);
    }

}
