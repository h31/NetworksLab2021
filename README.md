# OnlineRoulette API
## Requests 
### Path "/auth"
````
Post "/login"
request: 
    body: Json {name: String, password: String, admin: Bool}
response:
    - HttpStatusCode.OK
        header: ("Authorization" to Bearer Token)
    - HttpStatusCode.NotFound
        body: "Your credentials are not correct or user does not exist"
    - HttpStatusCode.Forbidden
        body: "User is already logged in"
    
````
````
Delete "/logout"
request:
    header: ("Authorization" to Bearer Token)
response: 
    - HttpStatusCode.OK
    - HttpStatusCode.Bad Request
        body: "Your token is missing or invalid"
    - HttpStatusCode.Unauthorized
        body: "User is not logged in"
````

### Path "/game"
````
Get "/info"
request:
    header: ("Authorization" to Bearer Token)
response: 
    - HttpStatusCode.OK
        body: Json array [{bet: Int, type: String, number: Int}]
    - HttpStatusCode.Bad Request
        body: "Your token is missing or invalid"
    - HttpStatusCode.Unauthorized
        body: "User is not logged in"
    
````
````
Post "/bet"
request: 
    header: ("Authorization" to Bearer Token)
    body: Json {bet: Int, type: String, number: Int}
Limits:
    * bet > 0
    * type in ["odd", "even", "number"] 
    * if type = "number" when number in 0..36
response: 
    - HttpStatusCode.OK
    - HttpStatusCode.Bad Request
        body: "Your token is missing or invalid"
    - HttpStatusCode.Unauthorized
        body: "User is not logged in"
````
````
Put "/gamble"
Access only for user with admin = true
request:
    header: ("Authorization" to Bearer Token)
response: 
    - HttpStatusCode.OK
        body: Json {result: Int}
    - HttpStatusCode.Bad Request
        body: "Your token is missing or invalid"
    - HttpStatusCode.Unauthorized
        body: "User is not logged in"
````
````
Get "/result"
request:
    header: ("Authorization" to Bearer Token)
response:
    - HttpStatusCode.OK
        body: Json {result: Int, win: Int}
    - HttpStatusCode.Bad Request
        body: "Your token is missing or invalid"
    - HttpStatusCode.Unauthorized
        body: "User is not logged in"
````