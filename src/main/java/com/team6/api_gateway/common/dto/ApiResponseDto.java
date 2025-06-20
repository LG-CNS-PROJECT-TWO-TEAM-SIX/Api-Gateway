package com.team6.api_gateway.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApiResponseDto<T> {
    private String code;
    private String message;
    private T data;

    private ApiResponseDto(String code, String message){
        this.code = code;
        this.message = message;
    }

    private ApiResponseDto(String code, String message, T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }
    public static <T> ApiResponseDto<T> createOk(T data){
        return new ApiResponseDto<>("Ok","요청이 성공하였습니다.",data);
    }
    public static ApiResponseDto<String> defaultOk(){
        return ApiResponseDto.createOk(null);
    }
    public static ApiResponseDto<String> createError(String code, String message) {
        return new ApiResponseDto<>(code, message);
    }
}

