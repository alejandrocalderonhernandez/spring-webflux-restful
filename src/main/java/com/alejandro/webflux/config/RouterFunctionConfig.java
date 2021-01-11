package com.alejandro.webflux.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.alejandro.webflux.resource.ProductResource;

@Configuration
public class RouterFunctionConfig {

	@Bean
	public RouterFunction<ServerResponse> routes(ProductResource resource) {
		return 
			RouterFunctions
				.route(RequestPredicates.GET("api/v2/product"), resource::toList)
				.andRoute(RequestPredicates.GET("api/v2/product/{id}"), resource::findById)
				.andRoute(RequestPredicates.POST("api/v2/product"), resource::create)
		        .andRoute(RequestPredicates.DELETE("api/v2/product/{id}"), resource::delete)
		        .andRoute(RequestPredicates.POST("/api/v2/product/upload/img/{id}"), resource::uploadImg);
	}
}
