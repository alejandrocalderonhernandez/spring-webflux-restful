package com.alejandro.webflux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.alejandro.webflux.document.ProductDocument;
import com.alejandro.webflux.repocitory.ProductRepocitory;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringWebfluxRestfulApplication implements CommandLineRunner {
	
	@Autowired
	ProductRepocitory repocitory;
	
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;
	
	private static final String COLLECTION_NAME = "productDocument";

	public static void main(String[] args) {
		SpringApplication.run(SpringWebfluxRestfulApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		this.mongoTemplate.dropCollection(COLLECTION_NAME).subscribe();
		
		Flux<ProductDocument> flux = Flux.just(
				new ProductDocument("Cockies", 2.30),
				new ProductDocument("Juice", 3.90),
				new ProductDocument("Milk", 7.10),
				new ProductDocument("Choclet", 4.90),
				new ProductDocument("Candy", 3.30));
		
		flux
			.flatMap(product -> this.repocitory.save(product))
			.subscribe(productSave -> System.out.println("Saved: " + productSave.getName()));
		
	}
}
