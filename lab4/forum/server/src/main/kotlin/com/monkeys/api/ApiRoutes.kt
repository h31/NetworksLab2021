import com.monkeys.controller.UserController
import com.monkeys.models.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.api(controller: UserController) {
    route("/forum") {
        authenticate("validate") {
            route("/request") {

                get("/hierarchy") {
                    val principal = call.authentication.principal<AuthModel>()
                    if (principal != null) {
                        val res = controller.getHierarchy()
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = OkHierarchy(response = res)
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.Forbidden,
                            message = "No token, please signIn"
                        )
                    }

                }

                get("/logout") {
                    val principal = call.authentication.principal<AuthModel>()
                    if (principal != null) {
                        val res = controller.logout()
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = "You have successfully logged out"
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = "Something went wrong. Try again"
                        )
                    }
                }

                get("/active-users") {
                    val principal = call.authentication.principal<AuthModel>()
                    if (principal != null) {
                        val res = controller.getActiveUsers()
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = OKActivityUsers(res)
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = "Something went wrong. Try again"
                        )
                    }
                }

                get("/message") {
                    val principal = call.authentication.principal<AuthModel>()
                    val msg = call.receive<MessageModel>()
                    if (principal != null && controller.putNewMessage(principal, msg)) {
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = "Success"
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = "Something went wrong. Try again"
                        )
                    }
                }

                get("/message-list") {
                    val principal = call.authentication.principal<AuthModel>()
                    val subTheme = call.receive<ThemeModel>()
                    if (principal != null) {
                        val res = controller.getMessages(subTheme)
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = OkListOfMessage(res)
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = "Something went wrong. Try again"
                        )
                    }
                }
            }
        }
    }
}