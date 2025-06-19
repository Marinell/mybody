package com.fitconnect.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.io.InputStream;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class FirebaseInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirebaseInitializer.class);

    @ConfigProperty(name = "firebase.service-account-file")
    String serviceAccountFile;

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("Initializing Firebase Admin SDK...");
        try (InputStream serviceAccount = getClass().getResourceAsStream(serviceAccountFile)) {
            if (serviceAccount == null) {
                throw new RuntimeException("Firebase service account file not found: " + serviceAccountFile + ". Please ensure it's in the classpath, e.g., src/main/resources/");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                LOGGER.info("Firebase Admin SDK initialized successfully.");
            } else {
                LOGGER.info("Firebase Admin SDK already initialized.");
            }

        } catch (Exception e) {
            LOGGER.error("Error initializing Firebase Admin SDK", e);
            // Depending on the application's needs, you might want to rethrow or handle differently
            // For now, we'll let the application start but log a severe error.
        }
    }
}
