package de.adorsys.datasafe.rest.impl.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom configuration of Spring MVC that allows using PathVariables that contain slash. For example, when
 * doing request path matching mapping {@code /documents/{path:.*}} will be converted to {@code /documents/**}.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MvcConfig extends WebMvcConfigurationSupport {

    private final DatasafeProperties datasafeProperties;

    /**
     * Register customized request matcher that maps /{path:.*} to /** and extracts variables properly.
     */
    @Bean
    @Override
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();
        handlerMapping.setPathMatcher(new ExtendedMatcher());
        return handlerMapping;
    }

    /**
     * Register static resources - frontend UI.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (StringUtils.isEmpty(datasafeProperties.getStaticResources())) {
            return;
        }

        log.info("Serving static resources from {} as /static/**", datasafeProperties.getStaticResources());
        registry
                .addResourceHandler("/static/**")
                .addResourceLocations(datasafeProperties.getStaticResources());
    }

    /**
     * Spring MVC uses URL-ending extension to deduce stream format, we override that to use endpoint output format.
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorPathExtension(false)
                .favorParameter(false)
                .ignoreAcceptHeader(false)
                .defaultContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    protected void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    public static class ExtendedMatcher extends AntPathMatcher {

        private final Pattern expandedUriPattern = Pattern.compile("(\\{(\\w+):\\.\\*})");

        @Override
        protected boolean doMatch(String pattern, String path, boolean fullMatch, Map<String, String> uriTemplateVariables) {
            // /documents/{path:.*} is converted to /documents/**
            String expandedPattern = expandedUriPattern.matcher(pattern).replaceAll("**");
            return super.doMatch(expandedPattern, path, fullMatch, uriTemplateVariables);
        }

        @Override
        public Map<String, String> extractUriTemplateVariables(String pattern, String path) {
            Matcher patternMatcher = expandedUriPattern.matcher(pattern);
            if (!patternMatcher.find()) {
                return super.extractUriTemplateVariables(pattern, path);
            }

            String expandedPattern = expandedUriPattern.matcher(pattern).replaceAll("(.+)");
            Matcher pathMatcher = Pattern.compile(expandedPattern).matcher(path);
            if (!pathMatcher.find()) {
                return super.extractUriTemplateVariables(pattern, path);
            }

            Map<String, String> variables = new LinkedHashMap<>();
            variables.put(patternMatcher.group(2), pathMatcher.group(1));
            return variables;
        }
    }
}
