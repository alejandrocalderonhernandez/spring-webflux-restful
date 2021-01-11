package com.alejandro.webflux.resource;

import java.io.File;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.alejandro.webflux.document.ProductDocument;
import com.alejandro.webflux.service.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductResource {

	private ProductService service;
	private Validator validator;
	
	@Autowired
	public ProductResource(ProductService service, Validator validator) {
		this.service = service;
		this.validator = validator;
	}

	private static final Logger log = LoggerFactory.getLogger(ProductResource.class);


	private static final String ID_PARAM = "id";

	private static final String CREATED_URI = "api/v2/product/";
	
	private static final String PATH = "/home/alejandro/Projects/Spring/project-reactor/spring-webflux-restful/uploads/";
 

	public Mono<ServerResponse> toList(ServerRequest request) {
		log.info("reuqest to v2");
		 return ServerResponse
				 .ok()
				 .contentType(MediaType.APPLICATION_JSON)
			     .body(this.service.findAll(), ProductDocument.class);
	}
	
	public Mono<ServerResponse> findById(ServerRequest request) {
		String id = request.pathVariable(ID_PARAM);
		return this.service.findById(id).flatMap(p -> {
			return ServerResponse
					.ok()
					.body(BodyInserters.fromValue(p))
					.switchIfEmpty(ServerResponse.notFound().build());
		});
	}
	
	public Mono<ServerResponse> create(ServerRequest request) {
		Mono<ProductDocument> product = request.bodyToMono(ProductDocument.class);
		return product.flatMap(p -> {
			Errors errors = new BeanPropertyBindingResult(p, p.getClass().getName());
			validator.validate(p, errors);
			if(errors.hasErrors()) {
				return Flux.fromIterable(errors.getFieldErrors())
					.map(e -> e.getField() + e.getDefaultMessage())
					.collectList()
					.flatMap(l -> ServerResponse.badRequest().body(BodyInserters.fromValue(l)));
				
			} else {
				p.setDate(LocalDate.now());
				return this.service.save(p).flatMap(pdb -> ServerResponse.created(URI.create(CREATED_URI + pdb.getId()))
	                      .body(BodyInserters.fromValue(pdb)));
			}
		});
	}
	
	public Mono<ServerResponse> update(ServerRequest request) {
		Mono<ProductDocument> productReq = request.bodyToMono(ProductDocument.class);
		String id = request.pathVariable(ID_PARAM);
		Mono<ProductDocument> productdb = this.service.findById(id);
		return productdb.zipWith(productReq, (db, req) -> {
			db.setName(((ProductDocument) req).getName());
			db.setPrice(((ProductDocument) req).getPrice());
			return db;
		}).flatMap(p -> ServerResponse.created(URI.create(CREATED_URI + p.getId()))
				                       .body(this.service.save(p), ProductDocument.class));
	}
	
	public Mono<ServerResponse> delete(ServerRequest request) {
		String id = request.pathVariable(ID_PARAM);
		Mono<ProductDocument> product = this.service.findById(id);
		return 
			product.flatMap(p -> this.service.delete(p.getId()).then(ServerResponse.noContent().build()))
			                                                   .switchIfEmpty(ServerResponse.notFound().build());
	}
	
	public Mono<ServerResponse> uploadImg(ServerRequest request) {
		String id = request.pathVariable(ID_PARAM);
		return request.multipartData()
				.map(mp -> mp.toSingleValueMap().get("img"))
				.cast(FilePart.class)
				.flatMap(f -> this.service.findById(id).flatMap(p -> {
					p.setPhoto(UUID.randomUUID().toString() + "-" + f.filename()
					.replace(" ", "")
					.replace(":", "")
					.replace("/", ""));
					return f.transferTo(new File(PATH + p.getPhoto())).then(this.service.save(p));
				})).flatMap(p -> ServerResponse.created(URI.create(CREATED_URI + p.getId()))
						                       .body(BodyInserters.fromValue(p)))
				.switchIfEmpty(ServerResponse.notFound().build());
	}
}
