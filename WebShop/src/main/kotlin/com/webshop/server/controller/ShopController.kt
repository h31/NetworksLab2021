package com.webshop.server.controller

import com.webshop.server.model.GoodsModel
import com.webshop.server.service.ShopService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("goods")
class ShopController {

    @Autowired
    private val shopService: ShopService? = null

    @GetMapping("/all")
    fun getAllGoods(): List<GoodsModel> {
        if (shopService == null) throw ClassNotFoundException("ShopService was not found")
        else return shopService.getAllGoods()
    }

    @PostMapping("/admin/add", produces = ["application/json"])
    fun addGoods(@RequestBody goodsModel: GoodsModel): HttpStatus {
        if (shopService == null) throw ClassNotFoundException("ShopService was not found")
        else {
            shopService.addGoods(goodsModel)
            return HttpStatus.OK
        }
    }

    @PostMapping("/buy", produces = ["application/json"])
    fun buyGoods(@RequestParam id: Int, @RequestParam count: Int): HttpStatus {
        if (shopService == null) throw ClassNotFoundException("ShopService was not found")
        return if (shopService.buyGoods(id, count)) HttpStatus.OK
        else return HttpStatus.PAYMENT_REQUIRED
    }
}
