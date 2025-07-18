package me.hakyuwon.sweetCheck.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        log.info("Initializing Firebase.");

        InputStream serviceAccount = getClass().getClassLoader()
                .getResourceAsStream("sweet-check-firebase-adminsdk-fbsvc-c98e76606d.json");

        if (serviceAccount == null) {
            log.error("❌ Firebase JSON 파일 못 찾음!");
            throw new RuntimeException("Firebase service account JSON 파일 못 찾음");
        } else {
            log.info("✅ Firebase JSON 파일 찾음!");
        }


        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp app;
        if (FirebaseApp.getApps().isEmpty()) {
            app = FirebaseApp.initializeApp(options);
            log.info("FirebaseApp initialized: " + app.getName());
        } else {
            app = FirebaseApp.getInstance();
            log.info("FirebaseApp already initialized: " + app.getName());
        }
        return app;
    }

    @Bean
    public FirebaseAuth getFirebaseAuth() throws IOException {
        return FirebaseAuth.getInstance(firebaseApp());
    }

    @Bean
    public Firestore getFirestore() throws IOException {
        return FirestoreClient.getFirestore();
    }
}
