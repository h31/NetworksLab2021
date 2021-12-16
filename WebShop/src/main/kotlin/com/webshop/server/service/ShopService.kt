package com.webshop.server.service

import com.webshop.server.model.GoodsModel
import com.webshop.server.repository.ShopRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ShopService {

    @Autowired
    private val repository: ShopRepository? = null

    fun getAllGoods(): List<GoodsModel> {
        if (repository == null) throw Exception("Repository was not found")
        return repository.findAll()
    }

    fun buyGoods(id: Int): Boolean {
        if (null == repository) throw Exception("Repository was not found")
        return if (repository.findById(id).get().count!! > 0) {
            repository.buyGoods(id)
            true
        } else false
    }

    fun addGoods(goodsModel: GoodsModel) {
        if (repository == null) throw Exception("Repository was not found")
        repository.save(goodsModel)
    }
}
