import com.monkeys.controller.UserController
import com.monkeys.models.AuthModel
import com.monkeys.models.OkHierarchy
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
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
                            message = OkHierarchy(res))
                    } else {
                        call.respond(
                            status = HttpStatusCode.Forbidden,
                            message = "No token, please signIn")
                    }

                }

                post("") {

                }
            }
        }
    }
}