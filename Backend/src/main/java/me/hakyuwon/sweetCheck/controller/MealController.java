package me.hakyuwon.sweetCheck.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.hakyuwon.sweetCheck.dto.meal.DailyMealResponse;
import me.hakyuwon.sweetCheck.dto.meal.MealRequest;
import me.hakyuwon.sweetCheck.service.AnalyzeService;
import me.hakyuwon.sweetCheck.service.MealService;
import me.hakyuwon.sweetCheck.util.SecurityUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MealController {
    private final MealService mealService;
    private final AnalyzeService analyzeService;

    /* use fast api to analyze meals (replaced to use at front)
    @PostMapping("/api/analyze-meals")
    public ResponseEntity<?> analyzeMealDay(
            @RequestParam("morning") MultipartFile morning,
            @RequestParam("lunch") MultipartFile lunch,
            @RequestParam("dinner") MultipartFile dinner,
            @RequestParam("snack") MultipartFile snack,
            @RequestParam(value="mealDate",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate mealDate,
            @RequestParam("userId") String userId
    ) {
        if (mealDate == null) {
            mealDate = LocalDate.now();
        }

        try {
            Map<String, MultipartFile> images = Map.of(
                    "morning", morning,
                    "lunch", lunch,
                    "dinner", dinner,
                    "snack", snack
            );

            Map<String, Resource> resources = new HashMap<>();
            for (Map.Entry<String, MultipartFile> entry : images.entrySet()) {
                String filename = entry.getKey() + ".jpg";
                ByteArrayResource resource = new ByteArrayResource(entry.getValue().getBytes()) {
                    @Override
                    public String getFilename() {
                        return filename;
                    }
                };
                resources.put(entry.getKey(), resource);
            }

            // FastAPI 분석 호출
            Map<String, Object> analysisResult = analyzeService.analyzeDay(resources).block();

            // meals 추출
            Map<String, Object> meals = (Map<String, Object>) analysisResult.get("meals");
            for (Map.Entry<String, Object> entry : meals.entrySet()) {
                String mealKey = entry.getKey();
                Object mealDataObj = entry.getValue();
                if (!(mealDataObj instanceof Map)) continue;

                try {
                    Optional<MealType> optionalMealType = MealType.fromKey(mealKey);
                    if (optionalMealType.isEmpty()) {
                        log.warn("알 수 없는 식사 유형 key: {}", mealKey);
                        continue; // skip this meal
                    }
                    MealType mealType = optionalMealType.get(); // 매핑 메서드 필요

                    Map<String, Object> mealData = (Map<String, Object>) mealDataObj;
                    MealRequest mealRequest = new MealRequest();
                    mealRequest.setUserId(userId);
                    mealRequest.setMealDateTime(mealDate.atTime(8, 0).toString());
                    mealRequest.setMealType(mealType.name());
                    mealRequest.setImageUrl("https://dummy.url/" + mealKey); // TODO: 이미지 저장 후 URL 적용
                    mealRequest.setTotalSugar((Double) mealData.get("total_sugar"));

                    List<MealItem> items = new ArrayList<>();
                    Map<String, Object> foodSugarData = (Map<String, Object>) mealData.get("food_sugar_data");
                    for (Map.Entry<String, Object> foodEntry : foodSugarData.entrySet()) {
                        MealItem item = new MealItem();
                        item.setName(foodEntry.getKey());
                        Object sugar = foodEntry.getValue();
                        if (sugar instanceof Number) {
                            item.setSugarPer100(((Number) sugar).doubleValue());
                        } else {
                            item.setSugarPer100(0.0); // 또는 로깅만 하고 continue
                        }
                        items.add(item);
                    }
                    mealRequest.setItems(items);

                    mealService.saveMealDraft(mealRequest, mealType);

                } catch (IllegalArgumentException e) {
                    log.warn("알 수 없는 식사 유형 key: {}", mealKey);
                }
            }

            return ResponseEntity.ok(analysisResult);

        } catch (Exception e) {
            log.error("식사 분석 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("분석 실패: " + e.getMessage());
        }
    }*/

    // modify today's menu
    @PutMapping("/api/meals/{mealId}")
    public ResponseEntity<String> updateMealDraft(@PathVariable String mealId,
                                                @RequestBody MealRequest request) {
        mealService.updateMealDraft(request.getUserId(), mealId, request);
        return ResponseEntity.ok("Meal updated successfully");
    }

    // confirm menu
    @PostMapping("/api/meals/{mealId}/confirm")
    public ResponseEntity<String> confirmMeal(@PathVariable String mealId) {
        String userId = SecurityUtil.getCurrentUserId();
        mealService.confirmMeal(userId, mealId);
        return ResponseEntity.ok("Meal confirmed successfully");
    }

    // get daily meals
    @GetMapping("/api/meals/{date}")
    public ResponseEntity<DailyMealResponse> getDailyMeals(@PathVariable String date
    ) {
        String userId = SecurityUtil.getCurrentUserId();
        LocalDate localDate = LocalDate.parse(date);
        DailyMealResponse response = mealService.getDailyMeals(userId, localDate);
        return ResponseEntity.ok(response);
    }

    // get today's sugar result
    @GetMapping("/api/meals/result")
    public ResponseEntity<Map<String, Object>> getDailySugar(@RequestParam String userId, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        {
            if (date == null) {
                date = LocalDate.now();
            }
            Map<String, Object> result = mealService.getDailySugar(userId, date);
            return ResponseEntity.ok(result);
        }
    }
}

