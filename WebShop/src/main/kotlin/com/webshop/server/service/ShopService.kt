package com.webshop.server.service

import com.webshop.server.model.GoodsModel
import com.webshop.server.repository.ShopRepository
import com.webshop.server.util.REPO_NOT_FOUND
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ShopService {

    @Autowired
    private val repository: ShopRepository? = null

    fun getAllGoods(): List<GoodsModel> {
        requireNotNull(repository) { REPO_NOT_FOUND }
        return repository.findAll()
    }

    fun buyGoods(id: Int, count: Int): Boolean {
        requireNotNull(repository) { REPO_NOT_FOUND }
        return if (repository.findById(id).get().count!! - count >= 0) {
            repository.buyGoods(id, count)
            true
        } else false
    }

    fun addGoods(goodsModel: GoodsModel) {
        requireNotNull(repository) { REPO_NOT_FOUND }
        repository.save(goodsModel)
    }
}
