package me.hakyuwon.sweetCheck.domain;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.hakyuwon.sweetCheck.enums.Gender;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String name;
    private String email;
    private String nickname;
    private Date createdAt;
    private String imageUrl;
    private Gender gender;
    private Integer height;
    private Integer weight;
    private Integer age;
}
