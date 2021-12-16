package com.webshop.server.repository

import com.webshop.server.model.GoodsModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface ShopRepository: JpaRepository<GoodsModel, Int> {

    @Modifying
    @Transactional
    @Query(
        value = "update goods as g set g.count = g.count - 1 where g.id = ?1",
        nativeQuery = true
    )
    fun buyGoods(@Param("id_prod") id: Int)
}
