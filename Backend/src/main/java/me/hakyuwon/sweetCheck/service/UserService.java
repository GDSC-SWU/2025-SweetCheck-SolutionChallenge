package me.hakyuwon.sweetCheck.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.hakyuwon.sweetCheck.domain.User;
import me.hakyuwon.sweetCheck.dto.LoginResponse;
import me.hakyuwon.sweetCheck.dto.ProfileRequest;
import me.hakyuwon.sweetCheck.dto.TokenRequest;
import me.hakyuwon.sweetCheck.dto.WeeklyReportResponse;
import me.hakyuwon.sweetCheck.enums.ErrorCode;
import me.hakyuwon.sweetCheck.enums.Gender;
import me.hakyuwon.sweetCheck.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    @Autowired
    private final Firestore firestore;

    // 처음 구글 로그인 시 구글 정보 저장
    public String saveOrUpdateUser(String uid, String email, String name, String picture) {
        try {
            DocumentReference docRef = firestore.collection("users").document(uid);

            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("name", name);
            userData.put("profileImage", picture);

            DocumentSnapshot snapshot = docRef.get().get();
            if (!snapshot.exists()) {
                userData.put("createdAt", new Date());
            }

            docRef.set(userData, SetOptions.merge()).get();  // 동기 처리
            log.info("User saved or updated.");
            return uid;

        } catch (Exception e) {
            log.error("Error saving user", e);
            throw new RuntimeException("Failed to save or update user");
        }
    }

    // 사용자 프로필 입력 정보 따로 저장
    public void saveUserProfile(ProfileRequest profileRequest) {
        try {
            DocumentReference docRef = firestore.collection("user_profiles").document(profileRequest.getUid());

            Map<String, Object> userProfileData = new HashMap<>();
            userProfileData.put("gender", profileRequest.getGender());
            userProfileData.put("height", profileRequest.getHeight());
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

    // 로그인, 토큰 검증
    public LoginResponse login(TokenRequest tokenRequest) {
        try {
            String idToken = tokenRequest.getToken();
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String name = (String) decodedToken.getClaims().get("name");
            String picture = (String) decodedToken.getClaims().get("picture");

            return new LoginResponse(uid, email, name, picture);

        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Invalid Firebase ID token");
        }
    }

    // 사용자 탈퇴
    public void deleteUser(String uid) throws FirebaseAuthException {
        FirebaseAuth.getInstance().deleteUser(uid);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public WeeklyReportResponse getWeeklySugarStats(String userId) {
        try {
            // Firestore에서 사용자 정보 가져오기
            DocumentReference userRef = firestore.collection("user_profiles").document(userId.trim());
            DocumentSnapshot userSnapshot = userRef.get().get();

            if (!userSnapshot.exists()) {
                log.warn("User not found: {}", userId);
                throw new CustomException(ErrorCode.USER_NOT_FOUND);
            }

            // User 객체로 변환 (필요한 필드만 추출)
            String genderStr = userSnapshot.getString("gender");
            Long age = userSnapshot.getLong("age");

            if (genderStr == null || age == null) {
                log.warn("User data incomplete: {}", userId);
                throw new CustomException(ErrorCode.INVALID_PARAMETER);
            }

            Gender gender = Gender.valueOf(genderStr.toUpperCase()); // 혹시 "female"처럼 소문자일 수도 있으니 upper 처리
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

    // 일주일 당 평균
    public double getWeeklyAverage(String userId, LocalDate start, LocalDate end) {
        List<Double> dailySugars = getSugarValuesBetween(userId, start, end); // 일별 total_sugar 합계 가져오기
        if (dailySugars.isEmpty()) return 0.0;
        return dailySugars.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    // 일주일 당 섭취량 파이어베이스에서 가져오는 로직
    public List<Double> getSugarValuesBetween(String userId, LocalDate start, LocalDate end) {
        List<Double> sugarList = new ArrayList<>();

        // Firestore Timestamp 범위 설정
        LocalDateTime startDateTime = start.atStartOfDay(); // 00:00
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay(); // 다음날 00:00

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

    // 연령, 성별 기준 평균 당 섭취량 반환
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
