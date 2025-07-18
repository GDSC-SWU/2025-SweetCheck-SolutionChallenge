package me.hakyuwon.sweetCheck.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenRequest {
    private String idToken; // ✅ 이름 통일!
}
