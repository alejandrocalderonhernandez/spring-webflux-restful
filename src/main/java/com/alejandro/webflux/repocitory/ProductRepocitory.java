package com.alejandro.webflux.repocitory;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.alejandro.webflux.document.ProductDocument;

public interface ProductRepocitory extends ReactiveMongoRepository<ProductDocument, String>{

}
