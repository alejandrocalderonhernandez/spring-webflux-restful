package com.alejandro.webflux;

import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.alejandro.webflux.document.ProductDocument;
import com.alejandro.webflux.service.ProductService;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SpringWebfluxRestfulApplicationTests {
	
	private WebTestClient client;
	private ProductService service;
	
	@Autowired
	public SpringWebfluxRestfulApplicationTests(WebTestClient client, ProductService service) {
		this.client = client;
		this.service = service;
	}

	private static final String BASE_URL;
	private static final String ID_PATH;
	private static final String ID_PLACEHONDER;
	private static final String TEST_PRODUCT_NAME;
	private static final Double TEST_PRODUCT_PRICE;
	private static final ProductDocument PRODUCT_TO_INSERT;
	private static final ProductDocument PRODUCT_TO_UPDATE;
	
	static {
		BASE_URL="/api/v2/product";
		ID_PATH = "/{id}";
		ID_PLACEHONDER = "id";
		TEST_PRODUCT_NAME="Choclet";
		TEST_PRODUCT_PRICE= 4.90;
		PRODUCT_TO_INSERT = new ProductDocument("HappyPath", 55.55);
		PRODUCT_TO_UPDATE = new ProductDocument("chocletUpdate", 67.67);
	}

	@Test
	void toListTest() {
		this.client
			.get()
			.uri(BASE_URL)
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(ProductDocument.class)
			.consumeWith(res -> {
				List<ProductDocument> products = res.getResponseBody();
				Assertions.assertThat(products.size()==5).isTrue();
			});
	}
	
	@Test
	void findByIdTest() {
		ProductDocument choclet = this.service.findByName(TEST_PRODUCT_NAME).block();
		 this.client
		  .get()
		  .uri(BASE_URL + ID_PATH, Collections.singletonMap(ID_PLACEHONDER, choclet.getId()))
		  .exchange()
		  .expectStatus().isOk()
		  .expectBody()
		  .jsonPath("$.id").isNotEmpty()
		  .jsonPath("$.name").isEqualTo(TEST_PRODUCT_NAME)
		  .jsonPath("$.price").isEqualTo(TEST_PRODUCT_PRICE);
	}
	
	@Test
	void createTest() {
		 this.client
		 	.post()
		 	.uri(BASE_URL)
		 	.body(Mono.just(PRODUCT_TO_INSERT), ProductDocument.class)
		 	.exchange()
		 	.expectStatus().isCreated()
		 	.expectBody(ProductDocument.class)
		 	.consumeWith(res -> {
		 		ProductDocument product = res.getResponseBody();
		 		Assertions.assertThat(product.getId()).isNotEmpty();
		 		Assertions.assertThat(product.getName()).isEqualTo(PRODUCT_TO_INSERT.getName());
				Assertions.assertThat(product.getPrice()).isEqualTo(PRODUCT_TO_INSERT.getPrice());
		 	}); 	
	}
	
	@Test
	void editTest() {
		ProductDocument choclet = this.service.findByName(TEST_PRODUCT_NAME).block();
		 this.client
		 	.put()
			.uri(BASE_URL + ID_PATH, Collections.singletonMap(ID_PLACEHONDER, choclet.getId()))
		 	.body(Mono.just(PRODUCT_TO_UPDATE), ProductDocument.class)
		 	.exchange()
		 	.expectStatus().isCreated()
		 	.expectBody(ProductDocument.class)
		 	.consumeWith(res -> {
		 		ProductDocument product = res.getResponseBody();
		 		Assertions.assertThat(product.getName()).isEqualTo(PRODUCT_TO_UPDATE.getName());
				Assertions.assertThat(product.getPrice()).isEqualTo(PRODUCT_TO_UPDATE.getPrice());
		 	});	
	}
	
	@Test
	void deleteTest() {
		ProductDocument cokies = this.service.findByName("Cockies").block();
		this.client
			.delete()
			.uri(BASE_URL + ID_PATH, Collections.singletonMap(ID_PLACEHONDER, cokies.getId()))
			.exchange()
			.expectStatus().isNoContent();
	}

}
