package lch.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// 관리자 계정 생성용
public class BCryptGen {
    public static void main(String[] args) {
        String raw = (args.length > 0) ? args[0] : "실제 비밀번호로 사용할 값 넣기";
        System.out.println(new BCryptPasswordEncoder().encode(raw));
    }
}
