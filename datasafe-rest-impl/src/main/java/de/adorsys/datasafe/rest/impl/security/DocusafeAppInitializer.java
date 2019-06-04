package de.adorsys.datasafe.rest.impl.security;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class DocusafeAppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.addFilter("JwtAuthorizationFilter", new DelegatingFilterProxy("JwtAuthorizationFilter"))
                .addMappingForUrlPatterns(null, true, "/*");
    }
}
