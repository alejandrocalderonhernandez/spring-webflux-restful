package com.alejandro.webflux.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alejandro.webflux.document.ProductDocument;
import com.alejandro.webflux.repocitory.ProductRepocitory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService {
	
	@Autowired
	private ProductRepocitory repocitory;

	@Override
	public Flux<ProductDocument> findAll() {
		return this.repocitory.findAll();
	}

	@Override
	public Mono<ProductDocument> findById(String id) {
		return this.repocitory.findById(id);
	}

	@Override
	public Mono<ProductDocument> save(ProductDocument productDocument) {
		return this.repocitory.save(productDocument);
	}

	@Override
	public Mono<Void> delete(String id) {
		return this.repocitory.deleteById(id);
	}

}
