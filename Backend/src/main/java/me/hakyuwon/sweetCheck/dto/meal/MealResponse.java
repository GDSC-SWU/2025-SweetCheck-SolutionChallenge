package me.hakyuwon.sweetCheck.dto.meal;

import lombok.Getter;
import lombok.Setter;
import me.hakyuwon.sweetCheck.domain.MealItem;

import java.util.List;

@Getter
@Setter
public class MealResponse {
    private String mealType;
    private String imageUrl;
    private double totalSugar;
    private List<MealItem> mealItems;
}
