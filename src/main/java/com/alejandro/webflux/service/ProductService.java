package com.alejandro.webflux.service;

import com.alejandro.webflux.document.ProductDocument;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

	public Flux<ProductDocument> findAll();
	public Mono<ProductDocument> findById(String id);
	public Mono<ProductDocument> save(ProductDocument productDocument);
	public Mono<Void> delete(String id);
	public Mono<ProductDocument> findByName(String name);
}
