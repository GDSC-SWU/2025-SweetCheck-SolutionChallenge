package me.hakyuwon.sweetCheck.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import me.hakyuwon.sweetCheck.dto.LoginResponse;
import me.hakyuwon.sweetCheck.dto.TokenRequest;
import org.springframework.stereotype.Service;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import me.hakyuwon.sweetCheck.dto.ProfileRequest;
import me.hakyuwon.sweetCheck.dto.WeeklyReportResponse;
import me.hakyuwon.sweetCheck.enums.ErrorCode;
import me.hakyuwon.sweetCheck.enums.Gender;
import me.hakyuwon.sweetCheck.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    public LoginResponse login(TokenRequest tokenRequest) throws IOException {
        try {
            String idToken = tokenRequest.getIdToken();

            String CLIENT_ID = "983762013559-1uq109l17mvci4peipqo0ua1stf3dd3t.apps.googleusercontent.com";

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                throw new RuntimeException("Invalid ID token: verification failed");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();

            String uid = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            // Firestore에 유저 정보 저장
            saveOrUpdateUser(uid, email, name, picture);

            return new LoginResponse(uid, email, name, picture);

        } catch (Exception e) {
            throw new RuntimeException("Google ID token verification failed: " + e.getMessage(), e);
        }
    }

    @Autowired
    private final Firestore firestore;

    // first log in
    public String saveOrUpdateUser(String uid, String email, String name, String picture) {
        try {
            log.info("🔥 [saveOrUpdateUser] called with uid={}, email={}, name={}", uid, email, name); // ✅ 추가

            DocumentReference docRef = firestore.collection("users").document(uid);

            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("name", name);
            userData.put("profileImage", picture);

            DocumentSnapshot snapshot = docRef.get().get();
            if (!snapshot.exists()) {
                userData.put("createdAt", new Date());
                log.info("🆕 New user. Setting createdAt.");
            } else {
                log.info("📝 Existing user. Updating data.");
            }

            docRef.set(userData, SetOptions.merge()).get();  // 동기 처리
            log.info("✅ User saved or updated successfully in Firestore");

            return uid;

        } catch (Exception e) {
            log.error("❌ Error saving user to Firestore", e);
            throw new RuntimeException("Failed to save or update user");
        }
    }

    // save individually for users profile
    public void saveUserProfile(ProfileRequest profileRequest) {
        try {
            DocumentReference docRef = firestore.collection("user_profiles").document(profileRequest.getUid());

            Map<String, Object> userProfileData = new HashMap<>();
            userProfileData.put("gender", profileRequest.getGender());
            userProfileData.put("height", profileRequest.getHeight());//위조 가능성
            userProfileData.put("weight", profileRequest.getWeight());
            userProfileData.put("age", profileRequest.getAge());

            // 'user_profiles' 컬렉션에 추가 정보 저장
            docRef.set(userProfileData, SetOptions.merge()).get();
            log.info("User profile saved or updated.");
        } catch (Exception e) {
            log.error("Error saving user profile", e);
            throw new RuntimeException("Failed to save or update user profile");
        }
    }

    /* log in and verify token
    public LoginResponse login(TokenRequest tokenRequest) {
        try {
            String idToken = tokenRequest.getIdToken();
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String name = (String) decodedToken.getClaims().get("name");
            String picture = (String) decodedToken.getClaims().get("picture");

            return new LoginResponse(uid, email, name, picture);

        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Invalid Firebase ID token");
        }
    }*/

    // delete user
    public void deleteUser(String uid) throws FirebaseAuthException {
        FirebaseAuth.getInstance().deleteUser(uid);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public WeeklyReportResponse getWeeklySugarStats(String userId) {
        try {
            // get user information at Firestore
            DocumentReference userRef = firestore.collection("user_profiles").document(userId.trim());
            DocumentSnapshot userSnapshot = userRef.get().get();

            if (!userSnapshot.exists()) {
                log.warn("User not found: {}", userId);
                throw new CustomException(ErrorCode.USER_NOT_FOUND);
            }

            // change in to User object
            String genderStr = userSnapshot.getString("gender");
            Long age = userSnapshot.getLong("age");

            if (genderStr == null || age == null) {
                log.warn("User data incomplete: {}", userId);
                throw new CustomException(ErrorCode.INVALID_PARAMETER);
            }

            Gender gender = Gender.valueOf(genderStr.toUpperCase()); // considering lower case like "female"
            double thisWeekAvg = roundToTwoDecimals(getWeeklyAverage(userId, LocalDate.now().minusDays(6), LocalDate.now()));
            double lastWeekAvg = roundToTwoDecimals(getWeeklyAverage(userId, LocalDate.now().minusDays(13), LocalDate.now().minusDays(7)));
            double peopleAvg = roundToTwoDecimals(getPeopleAverage(gender, age.intValue()));

            return new WeeklyReportResponse(peopleAvg, lastWeekAvg, thisWeekAvg);

        }catch (CustomException e) {
            throw e;
        }catch (Exception e) {
            log.error("Error retrieving weekly sugar stats for user: {}", userId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // get weekly average sugar
    public double getWeeklyAverage(String userId, LocalDate start, LocalDate end) {
        List<Double> dailySugars = getSugarValuesBetween(userId, start, end); // 일별 total_sugar 합계 가져오기
        if (dailySugars.isEmpty()) return 0.0;
        return dailySugars.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    // get sugar values between week to week
    public List<Double> getSugarValuesBetween(String userId, LocalDate start, LocalDate end) {
        List<Double> sugarList = new ArrayList<>();

        // set range Firestore Timestamp
        LocalDateTime startDateTime = start.atStartOfDay(); // 00:00
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay(); // next day 00:00

        Timestamp startTimestamp = Timestamp.ofTimeSecondsAndNanos(startDateTime.toEpochSecond(ZoneOffset.UTC), 0);
        Timestamp endTimestamp = Timestamp.ofTimeSecondsAndNanos(endDateTime.toEpochSecond(ZoneOffset.UTC), 0);

        CollectionReference mealsRef = firestore.collection("users").document(userId).collection("meals");

        try {
            ApiFuture<QuerySnapshot> query = mealsRef
                    .whereGreaterThanOrEqualTo("mealDateTime", startTimestamp)
                    .whereLessThan("mealDateTime", endTimestamp)
                    .get();

            for (DocumentSnapshot doc : query.get().getDocuments()) {
                Double sugar = doc.getDouble("totalSugar");
                if (sugar != null) sugarList.add(sugar);
            }
        } catch (Exception e) {
            log.error("Error querying meals between {} and {}: {}", start, end, e.getMessage());
        }

        return sugarList;
    }

    // average from same age, gender
    public double getPeopleAverage (Gender gender,int age) {
        if (gender == Gender.MALE) {
            if (age < 30) return 70.0;
            else if (age < 50) return 65.0;
            else return 60.0;
        } else {
            if (age < 30) return 65.0;
            else if (age < 50) return 55.0;
            else return 45.0;
        }
    }
}


