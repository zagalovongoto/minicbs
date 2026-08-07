package org.gimuemoa.minicbs.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionTimeoutConfig implements ServletContextInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // FORCE l'expiration de la session Tomcat à 1 minute (60 secondes) au niveau du conteneur de servlets
        servletContext.setSessionTimeout(5);
    }
}
