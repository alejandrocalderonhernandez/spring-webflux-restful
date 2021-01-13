package com.alejandro.webflux.repocitory;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.alejandro.webflux.document.ProductDocument;

import reactor.core.publisher.Mono;

public interface ProductRepocitory extends ReactiveMongoRepository<ProductDocument, String> {
	
	public Mono<ProductDocument> findByName(String name);

}
