package collection

import DatabaseSingleton.Companion.getDb
import com.mongodb.MongoWriteException
import org.bson.conversions.Bson
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineCollection

abstract class CollectionFactory<T : Any> {
    inline fun <reified T : Any> getCollect() : CoroutineCollection<T> {
        return getDb().getCollection()
    }

    abstract val collection: CoroutineCollection<T>

    suspend fun add(member: T): Boolean =
        try {
            collection.insertOne(member)
            true
        } catch (ex: MongoWriteException) {
            println(ex.error)
            false
        }

    suspend fun getAll(): List<T> =
        collection.find().toList()

    suspend fun getOne(vararg filters: Bson): T? =
        collection.findOne(and(*filters))

    suspend fun getOne(filter: Bson): T? =
        collection.findOne(filter)

    suspend fun delete(vararg filters: Bson): Boolean =
        !collection.deleteOne(and(*filters)).wasAcknowledged()
}