package com.webshop.server.controller

import com.webshop.server.model.GoodsModel
import com.webshop.server.service.ShopService
import com.webshop.server.util.SRV_NOT_FOUND
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
        requireNotNull(shopService) { SRV_NOT_FOUND }
        return shopService.getAllGoods()
    }

    @PostMapping("/admin/add", produces = ["application/json"])
    fun addGoods(@RequestBody goodsModel: GoodsModel): HttpStatus {
        requireNotNull(shopService) { SRV_NOT_FOUND }
        shopService.addGoods(goodsModel)
        return HttpStatus.OK

    }

    @PostMapping("/buy", produces = ["application/json"])
    fun buyGoods(@RequestParam id: Int, @RequestParam count: Int): HttpStatus {
        requireNotNull(shopService) { SRV_NOT_FOUND }
        return if (shopService.buyGoods(id, count)) HttpStatus.OK
        else return HttpStatus.PAYMENT_REQUIRED
    }
}
