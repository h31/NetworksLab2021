package com.example.delservice.repository;

import com.example.delservice.model.Goods;
import org.springframework.data.repository.CrudRepository;

public interface GoodsRepository extends CrudRepository<Goods, Long> {

    Goods findByName(String name);

    boolean existsByName(String name);
}
