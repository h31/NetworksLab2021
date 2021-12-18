import ch.qos.logback.classic.db.names.TableName
import models.Question
import models.Test
import models.User
import org.ktorm.database.Database
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

import java.sql.SQLException
import kotlin.system.exitProcess

fun connect(): Database? {
    return try {
        Database.connect("jdbc:postgresql://localhost:5432/postgres", user = "intellijIdea", password = "1234")
    }
    catch (e: SQLException) {
        println("can not connect to DB. Exiting...")
        e.printStackTrace()
        null
    }
}

fun getTestFromEntry(entry: QueryRowSet): Test {
    val id = entry.getInt("id")
    val name = entry.getString("name")!!
    val desc = entry.getString("description")!!
    return Test(id, name, desc)
}

fun getQuestionFromEntry(entry: QueryRowSet): Question {
    val id = entry.getInt("id")
    val testId = entry.getInt("testid")
    val value = entry.getInt("value")
    val questionText = entry.getString("questiontext")!!
    val answersList = listOf(
        entry.getString("var1")!!,
        entry.getString("var2")!!,
        entry.getString("var3")!!,
        entry.getString("var4")!!)
    val answer = entry.getInt("answer")
    return Question(id, testId, value, questionText, answersList, answer)
}

fun getUserFromEntry(entry: QueryRowSet): User {
    val id = entry.getInt("id")
    val login = entry.getString("login")!!
    val lastTestId = entry.getInt("lasttestid")
    val lastResult = entry.getInt("lastresult")
    return User(id, login, lastTestId, lastResult)
}

object TestTable: Table<Nothing>(schema = "testapp", tableName = "tests") {
    val id = int("id").primaryKey()
    val name = varchar("name")
    val description = varchar("description")
}

object QuestionTable: Table<Nothing>(schema = "testapp", tableName = "questions") {
    val id = int("id").primaryKey()
    val testId = int("testid")
    val value = int("value")
    val questionText = varchar("questiontext")
    val var1 = int("var1")
    val var2 = int("var2")
    val var3 = int("var3")
    val var4 = int("var4")
    val answer = int("answer")
}

object UserTable: Table<Nothing>(schema = "testapp", tableName = "users") {
    val id = int("id").primaryKey()
    val login = varchar("login")
    val passwordHash = varchar("passwordhash")
    val lastTestId = int("lasttestid")
    val lastResult = int("lastresult")
}
