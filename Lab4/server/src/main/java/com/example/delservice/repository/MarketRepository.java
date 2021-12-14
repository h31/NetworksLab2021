package com.example.delservice.repository;

import com.example.delservice.model.Market;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketRepository extends CrudRepository<Market, Long> {

}
