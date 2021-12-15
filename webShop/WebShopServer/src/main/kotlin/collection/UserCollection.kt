package collection

import model.User
import org.litote.kmongo.coroutine.CoroutineCollection

class UserCollection: CollectionFactory<User>() {
    override val collection: CoroutineCollection<User>
        get() = getCollect()
}