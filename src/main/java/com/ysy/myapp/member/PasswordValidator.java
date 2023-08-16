package com.ysy.myapp.member;

public class PasswordValidator {
    public boolean validatePassword(String inputPassword, String password) {
        // 입력한 패스워드와 저장된 패스워드를 비교하여 일치 여부를 반환
        return inputPassword.equals(password);
    }
}
