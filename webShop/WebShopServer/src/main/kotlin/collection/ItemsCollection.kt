package collection

import com.mongodb.client.result.UpdateResult
import model.Item
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class ItemsCollection: CollectionFactory<Item>() {
    override val collection: CoroutineCollection<Item>
        get() = getCollect()

    suspend fun updateItem(item: Item, amount: Int): UpdateResult =
        collection.updateOne(Item::name eq item.name, setValue(Item::amount, amount))
}