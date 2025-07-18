package me.hakyuwon.sweetCheck.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import me.hakyuwon.sweetCheck.dto.LoginResponse;
import me.hakyuwon.sweetCheck.dto.ProfileRequest;
import me.hakyuwon.sweetCheck.dto.TokenRequest;
import me.hakyuwon.sweetCheck.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ✅ 안드로이드에서 사용하는 Web Client ID
    private static final String CLIENT_ID = "983762013559-1uq109l17mvci4peipqo0ua1stf3dd3t.apps.googleusercontent.com";

    /**
     * 🔐 로그인 처리: 안드로이드에서 ID 토큰을 보내면 검증하고 사용자 정보 반환
     */
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

    /**
     * 🔐 프로필 저장: 인증된 사용자만 허용, 토큰과 uid 비교로 위조 방지
     */
    @PostMapping("/api/users/profile")
    public ResponseEntity<Void> saveProfile(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody ProfileRequest request
    ) {
        try {
            // ✅ 헤더 체크 및 토큰 추출
            if (!bearerToken.startsWith("Bearer ")) {
                System.out.println("❌ Authorization 헤더 형식 오류");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String idToken = bearerToken.substring(7); // "Bearer " 제거

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                System.out.println("❌ ID 토큰 검증 실패");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String uidFromToken = googleIdToken.getPayload().getSubject();

            if (!uidFromToken.equals(request.getUid())) {
                System.out.println("❌ 요청 uid와 토큰 uid 불일치: 위조 가능성");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            userService.saveUserProfile(request);
            System.out.println("✅ 프로필 저장 완료: " + request.getUid());
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            System.out.println("❌ 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
