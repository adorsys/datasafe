package de.adorsys.datasafe.rest.impl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.*;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static springfox.documentation.builders.RequestHandlerSelectors.*;

/**
 * Swagger2 UI for REST api.
 */
@EnableSwagger2
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .apis(basePackage("de.adorsys"))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(
                        Stream.of(new ParameterBuilder()
                                .name("contentType")
                                .description("type of content ex. application/json")
                                .modelRef(new ModelRef("string"))
                                .parameterType("header")
                                .required(false)
                                .build()).collect(Collectors.toList()))
                .globalOperationParameters(
                        Stream.of(new ParameterBuilder()
                                .name("token")
                                .description("access key (bearer token)")
                                .modelRef(new ModelRef("string"))
                                .parameterType("header")
                                .required(true)
                                .build()).collect(Collectors.toList()));
    }
}
