package de.adorsys.datasafe.rest.impl.config;

import de.adorsys.datasafe.rest.impl.security.SecurityConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.*;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

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
                .securitySchemes(Collections.singletonList(
                        new ApiKey(
                                "JWT",
                                SecurityConstants.TOKEN_HEADER,
                                SecurityConstants.TOKEN_HEADER)
                        )
                )
                .securityContexts(Collections.singletonList((
                        SecurityContext.builder()
                                .securityReferences(
                                        Collections.singletonList(SecurityReference.builder()
                                                .reference("JWT")
                                                .scopes(new AuthorizationScope[0])
                                                .build()
                                        )
                                )
                                .build())
                ));
    }
}
