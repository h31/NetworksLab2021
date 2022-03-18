import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import spark.Spark;

import java.io.*;
import java.util.HashMap;

public class Main {
    static void serialize(Serializable obj, String filename) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
            oos.writeObject(obj);
            oos.close();
        }
        catch (IOException e) {
            System.out.println("Не удалось записать файл");
        }
    }
    static <T> T deserialize(String filename) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
            Object obj = ois.readObject();
            ois.close();
            return (T) obj;
        }
        catch (IOException|ClassNotFoundException e) {
            System.out.println("Не удалось прочитать файл");
            return null;
        }
    }

    public static void main(String[] args) {
        String productsFileName = "products.dat";

        HashMap<Integer, Product> products;
        if (new File(productsFileName).isFile()) {
            products = deserialize(productsFileName);
            if (products == null) return;
        }
        else {
            products = new HashMap<>();
            products.put(1, new Product("Хлеб", 36.5, 20));
            products.put(2, new Product("Молоко", 84.9, 25));
            serialize(products, productsFileName);
        }

        HashMap<String, String> admins = new HashMap<>();
        admins.put("admin", "pass");

        Gson gson = new Gson();

        Spark.get("/product", (request, response) -> {
            response.header("Content-Type", "application/json");
            return new Response("success", products);
        }, gson::toJson);
        Spark.get("/product/:id", (request, response) -> {
            response.header("Content-Type", "application/json");
            int id = Integer.parseInt(request.params(":id"));
            Product p = products.get(id);
            if (p == null) {
                response.status(404);
                return new Response("error", "Товар с таким id не найден");
            }
            return new Response("success", p);
        }, gson::toJson);
        Spark.post("/product", "application/json", (request, response) -> {
            response.header("Content-Type", "application/json");
            JsonObject jsonObject = JsonParser.parseString(request.body()).getAsJsonObject();
            try {
                String login = jsonObject.get("login").getAsString();
                String password = jsonObject.get("password").getAsString();
                if (!admins.get(login).equals(password)) {
                    throw new RuntimeException();
                }
            }
            catch (RuntimeException e) {
                response.status(400);
                return new Response("error", "Логин/пароль указаны неверно");
            }
            String name = jsonObject.get("name").getAsString();
            double price = jsonObject.get("price").getAsDouble();
            int count = jsonObject.get("count").getAsInt();
            Product p = new Product(name, price, count);
            products.put(products.size() + 1, p);
            serialize(products, productsFileName);
            return new Response("success", "ok");
        }, gson::toJson);
        Spark.put("/product/:id", "application/json", (request, response) -> {
            response.header("Content-Type", "application/json");
            int id = Integer.parseInt(request.params(":id"));
            JsonObject jsonObject = JsonParser.parseString(request.body()).getAsJsonObject();
            try {
                String login = jsonObject.get("login").getAsString();
                String password = jsonObject.get("password").getAsString();
                if (!admins.get(login).equals(password)) {
                    throw new RuntimeException();
                }
            }
            catch (RuntimeException e) {
                response.status(400);
                return new Response("error", "Логин/пароль указаны неверно");
            }
            Product p = products.get(id);
            int count = jsonObject.get("add").getAsInt();
            p.add(count);
            serialize(products, productsFileName);
            return new Response("success", p);
        }, gson::toJson);
        Spark.post("/order", "application/json", (request, response) -> {
            response.header("Content-Type", "application/json");
            JsonObject jsonObject = JsonParser.parseString(request.body()).getAsJsonObject();
            int id = jsonObject.get("id").getAsInt();
            int count = jsonObject.get("count").getAsInt();
            Product p = products.get(id);
            if (p == null) {
                response.status(404);
                return new Response("error", "Указан несуществующий товар");
            }
            if (p.getCount() < count) {
                response.status(400);
                return new Response("error", "Недостаточно товара в наличии. В заказе " + count + ", в наличии " + p.getCount());
            }
            p.buy(count);
            return new Response("success", "Вы успешно купили " + count + " единиц " + p.getName() + " на сумму " + p.getPrice() * count);
        }, gson::toJson);
    }
}

/*
POST /product                           (Добавить товар)
< { name, price, count, login, password }
> ok/error

POST /order                             (Покупка товара)
< { id, count }
> success/error

PUT /product/:id                        (Учет кол-ва товаров)
< { add, login, password }
> ok/error

GET /product/:id                        (Данные об одном товаре)
> данные о товаре

GET /product                            (Данные обо всех товарах)
> данные обо всех товарах

*/
