import { Component, OnInit } from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  form: FormGroup; // форма с введенными пользователем данными


  // внедрение всех нужных объектов
  constructor(
    private formBuilder: FormBuilder // для создание формы
  ) {
  }

  ngOnInit(): void {
    // инициализация формы с нужными полями, начальными значениями и валидаторами
    this.form = this.formBuilder.group({
      username: ['', Validators.required], // Validators.required - проверка на обязательность заполнения
      password: ['', [Validators.required]],
    });

  }

  // быстрый доступ на компоненты формы (для сокращения кода, чтобы каждый раз не писать this.form.get('') )
  get usernameField(): AbstractControl {
    return this.form.get('username');
  }

  get passwordField(): AbstractControl {
    return this.form.get('password');
  }


  // попытка отправки данных формы аутентификации
  submitForm(): void {

    console.log(this.form); // пока просто считываем введенные данные
    console.log(this.usernameField.value); // пока просто считываем введенные данные
    console.log(this.passwordField.value); // пока просто считываем введенные данные

  }

}
