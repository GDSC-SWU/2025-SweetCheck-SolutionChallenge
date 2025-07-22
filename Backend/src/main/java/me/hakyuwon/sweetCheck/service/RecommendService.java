package me.hakyuwon.sweetCheck.service;

import lombok.RequiredArgsConstructor;
import me.hakyuwon.sweetCheck.dto.meal.DailyMealResponse;
import me.hakyuwon.sweetCheck.dto.RecommendResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RecommendService {
    private final VisionService visionService;
    private final MealService mealService;
    private final GeminiService geminiService;

    private static final int LOW_SUGAR_STANDARD = 25;

    public RecommendResponse recommendMenu(String userId, MultipartFile image) throws Exception {

        List<String> menus = visionService.menuFromImage(image);
        if (menus.isEmpty()) {
            throw new IllegalArgumentException("메뉴를 추출할 수 없습니다.");
        }
        LocalDate today = LocalDate.now();

        DailyMealResponse response = mealService.getDailyMeals(userId, today);
        double todaySugar = response.getDailyTotalSugar();

        // setting conditions
        String condition = todaySugar < LOW_SUGAR_STANDARD
                ? "오늘 섭취한 당이 적으니 당이 조금 높은 음료를 추천해줘."
                : "오늘 섭취한 당이 많으니 당이 적은 음료를 추천해줘.";

        // write prompts to Gemini
        String prompt = makePrompt(menus, condition);

        // get recommendation of Gemini
        String recommendedMenu = geminiService.recommendMenu(prompt);

        return new RecommendResponse(recommendedMenu);
    }

    private String makePrompt(List<String> menus, String condition) {
        StringBuilder sb = new StringBuilder();
        sb.append("다음 메뉴 중에서 ").append(condition).append("\n");
        for (String menu : menus) {
            sb.append("- ").append(menu).append("\n");
        }
        sb.append("한 가지만 추천해줘. 추천 이유는 짧게 덧붙여줘.");
        return sb.toString();
    }
}
