# Describe

### Models

    GoodsModel(
        id: Int
        name: String
        count: Int
    )

### ROLES

    ADMIN:
    -login: admin
    -password: admin
    USER:
    -login: user
    -password: user

### EndPoints

##### Admin's:

-> Add new product to database, this endpoint need request body like this: {"id":6,"name":"prod1","count":12}

    /goods/add - POST METHOD

#### User's

-> Get all products

     /goods/ - GET METHOD

-> Buy any goods, this endpoint need request param like this: id=6

    /goods/buy - POST METHOD

### Example curl

    User's

curl http://localhost:8080/goods/ -u user:user | json_pp  
curl -X POST http://localhost:8080/goods/buyGoods -u user:user -d 'id=6'

    Admin's

curl -X POST -H 'Content-Type: application/json' -d '{"id":6,"name":"prod1","count":
12}' 'http://localhost:8080/goods/admin/addGoods' -u admin:admin
