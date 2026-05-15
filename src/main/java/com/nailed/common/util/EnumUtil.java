//package com.nailed.common.util;
//
//import com.nailed.common.exception.CustomException;
//import com.nailed.common.exception.ErrorCode;
//
//public class EnumUtil {
//
//    public static <T extends Enum<T>> T parse(Class<T> clazz, String value, ErrorCode errorCode) {
//        try {
//            return Enum.valueOf(clazz, value);
//        } catch (IllegalArgumentException e) {
//            throw new CustomException(errorCode);
//        }
//    }
//}
