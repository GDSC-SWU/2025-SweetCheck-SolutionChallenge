package me.hakyuwon.sweetCheck.dto.meal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.hakyuwon.sweetCheck.enums.MealType;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class SugarResponse {
    private Date mealTime;
    private MealType mealType;
    private double sugar;
}
