# HTTP-сервер для выполнения вычислений (калькулятор)

## Основные возможности

После подключения и успешной авторизации пользователь может выполнять операции сложения, вычитания, деления, умножения,
извлечения квадратного корня и расчет факториала для числа или чисел.

## Алгоритм работы с сервером

Сначала клиент проходит регистрацию, выполняя POST-запрос, далее клиент проходит аутентификацию и производит вычисления
посредством GET-запросов.

## Список запросов

### POST

* /register?username={username}&password={password} - запрос для регистрации, в котором клиент передает имя пользователя
  и пароль
* /login - запрос для аутентификации, в котором клиент передает имя пользователя и пароль
* /result?id=operation_id - получение от сервера результатов медленной операции

### GET

* /fast/sum?args=[] - быстрая сумма чисел, переданных в виде одномерного массива
* /fast/sub?args=[] - быстрая разность чисел, переданных в виде одномерного массива
* /fast/mul?args=[] - быстрое произведение чисел, переданных в виде одномерного массива
* /fast/div?args=[] - быстрое последовательное частное чисел, переданных в виде одномерного массива (в этом запросе
  запрещается деление на ноль)
* /slow/sqrt?args=[] - медленный квадратный корень для каждого числа, переданного в виде одномерного массива
* /slow/fact?args=[] - медленный факториал для каждого числа, переданного в виде одномерного массива (в этом запросе
  запрещается передача отрицательных чисел)

## Примеры запросов

### POST

* /register?username=valid_username&password=valid_password

```Status code: 200```

```json
{
  "message": "Successful registration"
}
```

* /register?username=invalid_username&password=valid_password

```Status code: 403 Username already used```

* /login Authentication: (valid_username, valid_password)

```Status code: 200```

```json
{
  "message": "Hello, valid_username!"
}
```

* /login Authentication: (invalid_username, invalid_password)

```Status code: 404 User with these credentials doesn't exist```

* /result?id=operation_id

```Status code: 425 Not ready yet```

* /result?id=operation_id

```Status code: 200```

 ```json
 {
  "result": [
    "(6.123233995736766e-17+1j)",
    7.483314773547883,
    2.0
  ]
}
 ```

* /result?id=operation_id

```Status code: 400 An attempt to calculate the factorial for an unsuitable operand has been stopped```

* /result?id=operation_id

```Status code: 200```

```json
{
  "result": [
    710998587804863451854045647463724949736497978881168458687447040000000000000,
    1,
    24
  ]
}
```

### GET

* /fast/sum?args=[-1, 4, 56]

```Status code: 200```

 ```json
{
  "result": [
    59.0
  ]
}
 ```

* /fast/sub?args=[56, 4, -1]

```Status code: 200```

 ```json
{
  "result": [
    53.0
  ]
}
 ```

* /fast/mul?args=[-1, 4, 56]

```Status code: 200```

 ```json
 {
  "result": [
    -224.0
  ]
}
 ```

* /fast/div?args=[56, -4]

```Status code: 200```

 ```json
 {
  "result": [
    -14.0
  ]
}
 ```

* /fast/div?args=[56, 0]

```Status code: 400 An attempt to divide by zero has been stopped```

* /fast/sum?args=[-1, "четыре", 56]

```Status code: 400 Incorrect operand data```

* /slow/sqrt?args=[-1, 56, 4]

```Status code: 200```

 ```json
  {
  "id": "operation_id",
  "message": "Accepted for processing"
}
```

* /slow/fact?args=[56, -1, 4]`

```Status code: 200```

```json
{
  "id": "operation_id",
  "message": "Accepted for processing"
}
```

* /slow/fact?args=[56, 1, 4]

```Status code: 200```

```json
{
  "id": "operation_id",
  "message": "Accepted for processing"
}

```