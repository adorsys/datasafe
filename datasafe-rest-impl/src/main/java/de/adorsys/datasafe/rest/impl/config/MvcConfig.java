package de.adorsys.datasafe.rest.impl.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MvcConfig extends WebMvcConfigurationSupport {

    private final DatasafeProperties datasafeProperties;

    /**
     * Register static resources - frontend UI.
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        if (!StringUtils.hasLength(datasafeProperties.getStaticResources())) {
            return;
        }

        log.info("Serving static resources from {} as /static/**", datasafeProperties.getStaticResources());
        registry
                .addResourceHandler("/static/**")
                .addResourceLocations(datasafeProperties.getStaticResources());
    }
}
