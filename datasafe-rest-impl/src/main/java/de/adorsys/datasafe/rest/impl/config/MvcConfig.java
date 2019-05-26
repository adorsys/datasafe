package de.adorsys.datasafe.rest.impl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class MvcConfig extends WebMvcConfigurationSupport {

    @Bean
    @Override
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();
        handlerMapping.setPathMatcher(new ExtendedMatcher());
        return handlerMapping;
    }

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
