package me.hakyuwon.sweetCheck.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import me.hakyuwon.sweetCheck.dto.LoginResponse;
import me.hakyuwon.sweetCheck.dto.TokenRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
public class UserController {

    // ✅ 안드로이드에서 사용하는 Web Client ID를 여기에!
    private static final String CLIENT_ID = "983762013559-1uq109l17mvci4peipqo0ua1stf3dd3t.apps.googleusercontent.com";

    @PostMapping("/api/users/login")
    public ResponseEntity<LoginResponse> login(@RequestBody TokenRequest tokenRequest) {
        System.out.println("🔵 컨트롤러 들어옴!");
        System.out.println("📌 받은 TokenRequest: " + tokenRequest);

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(tokenRequest.getIdToken());

            if (idToken == null) {
                System.out.println("❌ ID Token 검증 실패");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String uid = payload.getSubject(); // 사용자 고유 ID
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            System.out.println("✅ ID Token 검증 성공: UID = " + uid);

            LoginResponse response = new LoginResponse(uid, email, name, picture);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Exception 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
