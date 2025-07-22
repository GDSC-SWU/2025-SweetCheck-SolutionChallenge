package me.hakyuwon.sweetCheck.dto.meal;

import lombok.Getter;
import lombok.Setter;
import me.hakyuwon.sweetCheck.domain.MealItem;

import java.util.List;

@Getter
@Setter
public class MealRequest {
    private String userId;
    private String imageUrl;
    private String mealDateTime;
    private String mealType;
    private double totalSugar;
    private List<MealItem> items;
}
