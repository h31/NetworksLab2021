# Калькулятор
## Login
Request
```
POST /login

{
    "login": "login",
    "password": "password"
}
```
OK Response
```
200
"Authorization was successful"
```
Error Response

```
401
"Authorization failed"
```

## Logout
Request
```
GET /logout
```
OK Response
```
200
"Logout was successful"
```
Error Response
```
401
```

## sum, sub, mul, div

Request
```
Post /fast/sum
Post /fast/sub
Post /fast/mul
Post /fast/div
{
    "operand1": 2,
    "operand2": 1
}
```
OK Response
```
200
{
    "operand1": 2,
    "operand2": 1,
    "result": 3
}
```

## fact, sqrt

Request
```
Post /slow/fact
Post /slow/sqrt
{
    "operand1": 2
}
```
OK Response
```
200
{
    "operand1": 2,
    "result:: 2
}
```