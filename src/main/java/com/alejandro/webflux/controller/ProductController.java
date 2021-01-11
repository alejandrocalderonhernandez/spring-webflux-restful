package com.alejandro.webflux.controller;

import java.io.File;
import java.net.URI;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.alejandro.webflux.document.ProductDocument;
import com.alejandro.webflux.service.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "api/v1/product")
public class ProductController {

	@Autowired
	ProductService service;
	
	@Value("/home/alejandro/Projects/Spring/project-reactor/spring-webflux-restful/uploads/")
	private String path; 
	
	private static final Logger log = LoggerFactory.getLogger(ProductController.class);

	@GetMapping
	public Mono<ResponseEntity<Flux<ProductDocument>>> toList() {
		log.info("reuqest to v1");
		return Mono.just(ResponseEntity.ok(this.service.findAll()));
	}
	
	@GetMapping(path = "/{id}")
	public Mono<ResponseEntity<ProductDocument>> findById(@PathVariable String id) {
		return this.service.findById(id).map(p -> ResponseEntity.ok(p))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@PostMapping
	public Mono<ResponseEntity<Map<String, Object>>> create(@Valid @RequestBody Mono<ProductDocument> monoProduct) {
		Map<String, Object> response = new HashMap<>();
		return 
			monoProduct.flatMap(product -> {
				product.setDate(LocalDate.now());
				URI uri = URI.create("api/v1/product/" + product.getId());
				return this.service.save(product).map(p -> { 
					response.put("product", p);
					return ResponseEntity.created(uri).body(response);
				});
			})
		   .onErrorResume(t -> {
			   return Mono.just(t).cast(WebExchangeBindException.class)
					   .flatMap(ex -> Mono.just(ex.getFieldErrors()))
					   .flatMapMany(Flux::fromIterable)
					   .map(e -> e.getField() + e.getDefaultMessage())
					   .collectList()
					   .flatMap(list -> {
						   response.put("errors", list);
						   return Mono.just(ResponseEntity.badRequest().body(response));
			  });
				 
		   });


	}
	
	@PutMapping
	public Mono<ResponseEntity<ProductDocument>> update(@RequestBody ProductDocument product) {
		String id = product.getId();
		if(id == null) {
			return Mono.just(ResponseEntity.badRequest().build());
		}
		return this.service.findById(id).flatMap(p -> {
			p.setName(product.getName());
			p.setPrice(product.getPrice());
			return this.service.save(p);
		}).map(pUpdated -> ResponseEntity.ok(pUpdated));
	}
	
	@DeleteMapping(path = "/{id}")
	public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
		return this.service.findById(id).flatMap(p -> {
			return this.service.delete(id).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
		}).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}
	
	@PostMapping(path = "upload/img/{id}")
	public Mono<ResponseEntity<ProductDocument>> uploadImg(@PathVariable String id, @RequestPart FilePart img) {
		return this.service.findById(id)
				.flatMap(p -> {
					p.setPhoto(UUID.randomUUID().toString() + "-" + img.filename()
					  .replace(" ", "")
					  .replace(":", "")
					  .replace("/", ""));
					return img.transferTo(new File(this.path + p.getPhoto())).then(this.service.save(p));
				})
				.map(p -> ResponseEntity.ok(p))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
}
