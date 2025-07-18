package me.hakyuwon.sweetCheck.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data // 👉 getter, setter, toString 다 포함!
@AllArgsConstructor
public class LoginResponse {
    private String uid;
    private String email;
    private String name;
    private String profileImage;
}
