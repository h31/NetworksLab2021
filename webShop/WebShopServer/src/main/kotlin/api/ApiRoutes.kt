package api

import collection.ItemsCollection
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import model.Item
import org.litote.kmongo.eq

fun Route.itemRouting(db: ItemsCollection) {
    authenticate("validate") {
        route("/item") {
            get {
                val items = db.getAll()
                if (items.isNotEmpty()) return@get call.respond(items)
                call.respond(
                    status = HttpStatusCode.NoContent,
                    message = "No items found")
            }
            post {
                val item = call.receive<Item>()
                if (item.name.isBlank()) return@post call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Item name is blank.")
                if (item.amount < 0) return@post call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Amount must be equal or greater then zero", )
                if (item.price <= 0) return@post call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Price must be greater then zero")
                if (db.add(item)) return@post call.respond(
                    status = HttpStatusCode.OK,
                    message = "Item added")
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Something went wrong! Maybe this product already exist")
            }
            put("buy") {
                val parameters = call.receiveParameters()
                val name = parameters["name"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val amount = parameters["amount"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                var newAmount: Int = amount.toIntOrNull() ?: return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Amount not a number")
                val item = db.getOne(Item::name eq name) ?: return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Item not found")
                if (item.amount < newAmount) return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Too big amount")
                newAmount = item.amount - newAmount
                db.updateItem(item,newAmount)
                call.respond(
                    status = HttpStatusCode.OK,
                    message = "Amount of item: \"${item.name}\" was changed " +
                            "from \"${item.amount}\" to \"${newAmount}\"")
            }
            put("add") {
                val parameters = call.receiveParameters()
                val name = parameters["name"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val amount = parameters["amount"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                var newAmount: Int = amount.toIntOrNull() ?: return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Amount not a number")
                val item = db.getOne(Item::name eq name) ?: return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Item not found")
                newAmount += item.amount
                db.updateItem(item, newAmount)
                call.respond(
                    status = HttpStatusCode.OK,
                    message = "Amount of item: \"${item.name}\" was changed " +
                            "from \"${item.amount}\" to \"${newAmount}\"")
            }
        }
    }
}