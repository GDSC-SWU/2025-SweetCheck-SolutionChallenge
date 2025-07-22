package me.hakyuwon.sweetCheck.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import me.hakyuwon.sweetCheck.domain.Meal;
import me.hakyuwon.sweetCheck.domain.MealItem;
import me.hakyuwon.sweetCheck.dto.meal.DailyMealResponse;
import me.hakyuwon.sweetCheck.dto.meal.SugarResponse;
import me.hakyuwon.sweetCheck.dto.meal.MealRequest;
import me.hakyuwon.sweetCheck.enums.MealStatus;
import me.hakyuwon.sweetCheck.enums.MealType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
public class MealService {
    private final Firestore firestore = FirestoreClient.getFirestore();

    public String saveMealDraft(MealRequest request, MealType mealType) {
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(request.getMealDateTime());
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

            Meal meal = new Meal();
            meal.setImageUrl(request.getImageUrl());
            meal.setMealDateTime(date);
            meal.setMealType(mealType);
            meal.setTotalSugar(request.getTotalSugar());
            meal.setStatus(MealStatus.DRAFT); // (임시저장)

            DocumentReference mealRef = firestore.collection("users")
                    .document(request.getUserId())
                    .collection("meals")
                    .document();

            mealRef.set(meal).get();

            // save mealItem
            for (MealItem item : request.getItems()) {
                mealRef.collection("mealItems").add(item).get();
            }
            String mealId = mealRef.getId();
            log.info("Meal and items draft saved for user: {}", request.getUserId());
            return mealId; // 수정을 위한 mealId 반환

        } catch (Exception e) {
            log.error("Error saving meal draft", e);
        }
        return null;
    }

    // update menu
    public void updateMealDraft(String userId, String mealId, MealRequest request) {
        try {
            DocumentReference mealRef = firestore.collection("users")
                    .document(userId)
                    .collection("meals")
                    .document(mealId);

            mealRef.update(
                    "mealType", MealType.valueOf(request.getMealType()),
                    "totalSugar", request.getTotalSugar()
            ).get();

            // delete existing mealItems and save new ones
            CollectionReference itemsRef = mealRef.collection("mealItems");
            ApiFuture<QuerySnapshot> future = itemsRef.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                doc.getReference().delete();
            }

            for (MealItem item : request.getItems()) {
                itemsRef.add(item).get();
            }

            log.info("Meal draft updated for user: {}", userId);

        } catch (Exception e) {
            log.error("Error updating meal draft", e);
        }
    }

    // confirm meal
    public void confirmMeal(String userId, String mealId) {
        try {
            DocumentReference mealRef = firestore.collection("users")
                    .document(userId)
                    .collection("meals")
                    .document(mealId);

            mealRef.update("status", MealStatus.CONFIRMED).get();

            log.info("Meal confirmed for user: {}", userId);

        } catch (Exception e) {
            log.error("Error confirming meal", e);
        }
    }

    // get daily meals
    public DailyMealResponse getDailyMeals(String userId, LocalDate date) {
        DailyMealResponse response = new DailyMealResponse();
        List<Meal> meals = new ArrayList<>();

        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            Instant startInstant = startOfDay.atZone(ZoneId.systemDefault()).toInstant();
            Instant endInstant = endOfDay.atZone(ZoneId.systemDefault()).toInstant();

            Query query = firestore.collection("users")
                    .document(userId)
                    .collection("meals")
                    .whereGreaterThanOrEqualTo("mealDateTime", startInstant)
                    .whereLessThanOrEqualTo("mealDateTime", endInstant);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            double dailyTotalSugar = 0.0;

            for (QueryDocumentSnapshot document : documents) {
                Meal meal = document.toObject(Meal.class);
                meals.add(meal);

                dailyTotalSugar += meal.getTotalSugar();
            }

            response.setDate(date.toString());
            response.setDailyTotalSugar(dailyTotalSugar);
            response.setMeals(meals);

        } catch (Exception e) {
            log.error("Error retrieving daily meals for userId: {}, date: {}", userId, date, e);
        }
        return response;
    }

    // analyze today's sugar
    public Map<String, Object> getDailySugar(String userId, LocalDate date) {
        List<SugarResponse> responses = new ArrayList<>();
        String message = "Good sugar management today. Keep it up!";

        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            Instant startInstant = startOfDay.atZone(ZoneId.systemDefault()).toInstant();
            Instant endInstant = endOfDay.atZone(ZoneId.systemDefault()).toInstant();

            Query query = firestore.collection("users")
                    .document(userId)
                    .collection("meals")
                    .whereGreaterThanOrEqualTo("mealDateTime", startInstant)
                    .whereLessThanOrEqualTo("mealDateTime", endInstant);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                Meal meal = document.toObject(Meal.class);

                double sugar = meal.getTotalSugar();
                MealType mealType = meal.getMealType();

                SugarResponse response = new SugarResponse(
                        meal.getMealDateTime(),
                        mealType,
                        sugar
                );
                responses.add(response);

                // confirm messages
                if (sugar >= 25) {
                    message = "Lot of sugar intake at " + mealType + ". Eating more than the right standard.";
                    // if exceed 25g
                    break;
                }
            }

        } catch (Exception e) {
            log.error("Failed to get daily sugar data for userId: {}, date: {}", userId, date, e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", responses);
        result.put("message", message);

        return result;
    }
}

