package com.example.cinema.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.core.Ordered;
import org.springframework.boot.web.server.SslStoreProvider;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

/**
 * Forces keystore from project certs folder when tls profile is active.
 * Bypasses property resolution that can pick up C:\Users\...\.keystore.
 */
@Configuration
@Profile("tls")
public class TlsConfig {

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatSslStoreCustomizer(
            @Value("${server.ssl.key-store-password:changeit}") String keyStorePassword) {
        Path keystorePath = Paths.get(System.getProperty("user.dir"), "certs", "cinema-keystore.p12");
        if (!Files.exists(keystorePath)) {
            throw new IllegalStateException("Keystore not found: " + keystorePath);
        }
        char[] password = keyStorePassword.toCharArray();
        KeyStore keyStore;
        try (InputStream is = Files.newInputStream(keystorePath)) {
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(is, password);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load keystore from " + keystorePath, e);
        }
        KeyStore store = keyStore;
        return factory -> factory.setSslStoreProvider(new SslStoreProvider() {
            @Override
            public KeyStore getKeyStore() {
                return store;
            }

            @Override
            public KeyStore getTrustStore() {
                return null;
            }
        });
    }
}
