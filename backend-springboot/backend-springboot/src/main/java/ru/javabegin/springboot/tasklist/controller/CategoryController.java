package ru.javabegin.springboot.tasklist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.javabegin.springboot.tasklist.entity.Category;
import ru.javabegin.springboot.tasklist.service.CategoryService;
import ru.javabegin.springboot.tasklist.util.MyLogger;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/all")
    public List<Category> findAll(String email) {

        MyLogger.debugMethodName("CategoryController: findAll(email) ---------------------------------------------------------- ");

        return categoryService.findAll("test@gmail.com");
    }
}
