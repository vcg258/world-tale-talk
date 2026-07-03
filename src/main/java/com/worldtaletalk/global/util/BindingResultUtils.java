package com.worldtaletalk.global.util;

import lombok.experimental.UtilityClass;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@UtilityClass
public final class BindingResultUtils {

    /**
     * bindingResult의 null방지 유틸 메서드
     *
     * @param bindingResult 리다이렉트시 전달할 메세지
     * @param fallbackMessage null일경우 보낼 메세지
     * @return 리다이렉트시 전달할 메세지 (null일경우 폴백 메세지)
     */
    public static String getFirstErrorMessage(BindingResult bindingResult, String fallbackMessage) {
        FieldError fieldError = bindingResult.getFieldError();
        return fieldError != null ? fieldError.getDefaultMessage() : fallbackMessage;
    }
}
