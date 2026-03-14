package com.example.cinema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class CinemaApplication {
    public static void main(String[] args) {
        // Force keystore from project when tls profile (overrides C:\Users\...\.keystore)
        String profiles = System.getProperty("spring.profiles.active", "")
                + "," + System.getProperty("spring-boot.run.profiles", "");
        if (profiles.contains("tls")) {
            Path keystore = Paths.get(System.getProperty("user.dir"), "certs", "cinema-keystore.p12");
            if (keystore.toFile().exists()) {
                String path = keystore.toAbsolutePath().toString();
                String pathUri = keystore.toUri().toString();
                System.setProperty("server.ssl.key-store", pathUri);
                System.setProperty("server.ssl.key-store-password", "changeit");
                System.setProperty("server.ssl.key-store-type", "PKCS12");
                System.setProperty("server.ssl.key-alias", "cinema-server");
                System.setProperty("server.ssl.key-password", "changeit");
                // Also set Java defaults (Tomcat may read these)
                System.setProperty("javax.net.ssl.keyStore", path);
                System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
                System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
            }
        }
        SpringApplication.run(CinemaApplication.class, args);
    }
}
