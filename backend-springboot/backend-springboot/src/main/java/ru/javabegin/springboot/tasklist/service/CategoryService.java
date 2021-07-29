package ru.javabegin.springboot.tasklist.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.javabegin.springboot.tasklist.entity.Category;
import ru.javabegin.springboot.tasklist.repository.CategoryRepository;

import java.util.List;

@Service
public class CategoryService {

    private CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll(String email) {
        return categoryRepository.findByUserEmailOrderByTitleAsc(email);
    }

    public Category add(Category category) {
        return categoryRepository.save(category); // метод save обновляет или создает новый объект, если его не было
    }

    public Category update(Category category) {
        return categoryRepository.save(category); // метод save обновляет или создает новый объект, если его не было
    }

    public void delete(Long id) {
        categoryRepository.deleteById(id);
    } // удаляем 1 объект по id

    // поиск категорий пользователя по названию
    public List<Category> findByTitle(String text, String email) {
        return categoryRepository.findByTitle(text, email);
    }

    // находим 1 объект по id
    public Category findById(Long id) {
        return categoryRepository.findById(id).get(); // т.к. возвращается Optional - можно получить объект методом get()
    }
}
