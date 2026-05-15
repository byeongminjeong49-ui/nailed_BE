//package com.nailed.common.exception;
//
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//
//@Getter
//@RequiredArgsConstructor
//public enum ErrorCode {
//
//    // ─────────────────────────────────────────────────────────────────
//    // C : Common
//    // ─────────────────────────────────────────────────────────────────
//    INVALID_INPUT_VALUE                 (HttpStatus.BAD_REQUEST,            "C001", "입력값이 올바르지 않습니다."),
//    METHOD_NOT_ALLOWED                  (HttpStatus.METHOD_NOT_ALLOWED,     "C002", "지원하지 않는 HTTP 메서드입니다."),
//    INTERNAL_SERVER_ERROR               (HttpStatus.INTERNAL_SERVER_ERROR,  "C003", "서버 내부 오류가 발생했습니다."),
//    NOT_FOUND                           (HttpStatus.NOT_FOUND,              "C004", "요청한 리소스를 찾을 수 없습니다."),
//    UNAUTHORIZED                        (HttpStatus.UNAUTHORIZED,           "C005", "인증이 필요합니다."),
//    FORBIDDEN                           (HttpStatus.FORBIDDEN,              "C006", "접근 권한이 없습니다."),
//    INVALID_JSON                        (HttpStatus.BAD_REQUEST,            "C007", "JSON 형식이 올바르지 않습니다."),
//    MISSING_PARAMETER                   (HttpStatus.BAD_REQUEST,            "C008", "필수 파라미터가 누락되었습니다."),
//    UNSUPPORTED_MEDIA_TYPE              (HttpStatus.UNSUPPORTED_MEDIA_TYPE, "C009", "지원하지 않는 미디어 타입입니다."),
//
//    // ─────────────────────────────────────────────────────────────────
//    // M : Member
//    // ─────────────────────────────────────────────────────────────────
//    MEMBER_NOT_FOUND                    (HttpStatus.NOT_FOUND,              "M001", "존재하지 않는 회원입니다."),
//    MEMBER_ALREADY_EXISTS               (HttpStatus.CONFLICT,               "M002", "이미 가입된 회원입니다."),
//    MEMBER_WITHDRAWN                    (HttpStatus.BAD_REQUEST,            "M003", "탈퇴한 회원입니다."),
//    MEMBER_SUSPENDED                    (HttpStatus.FORBIDDEN,              "M004", "일시 정지된 계정입니다. 관리자에게 문의하세요."),
//    MEMBER_BANNED                       (HttpStatus.FORBIDDEN,              "M005", "영구 차단된 계정입니다."),
//    INVALID_TOKEN                       (HttpStatus.UNAUTHORIZED,           "M007", "유효하지 않은 토큰입니다."),
//    TOKEN_EXPIRED                       (HttpStatus.UNAUTHORIZED,           "M008", "만료된 토큰입니다. 다시 로그인해주세요."),
//    INVALID_LOGIN                       (HttpStatus.UNAUTHORIZED,           "M010", "이메일 또는 비밀번호가 올바르지 않습니다."),
//    PHONE_VERIFICATION_REQUIRED         (HttpStatus.BAD_REQUEST,            "M011", "휴대폰 번호 인증이 필요합니다."),
//    INVALID_PHONE_VERIFICATION_CODE     (HttpStatus.BAD_REQUEST,            "M012", "휴대폰 인증번호가 올바르지 않거나 만료되었습니다."),
//    EMAIL_LOGIN_VERIFICATION_REQUIRED   (HttpStatus.BAD_REQUEST,            "M013", "이메일 로그인 인증이 필요합니다."),
//    INVALID_EMAIL_LOGIN_VERIFICATION_CODE(HttpStatus.BAD_REQUEST,           "M014", "이메일 로그인 인증번호가 올바르지 않거나 만료되었습니다."),
//    EMAIL_SEND_FAILED                   (HttpStatus.INTERNAL_SERVER_ERROR,  "M015", "이메일 발송에 실패했습니다."),
//
//    // ─────────────────────────────────────────────────────────────────
//    // P : Product
//    // ─────────────────────────────────────────────────────────────────
//    PRODUCT_NOT_FOUND                   (HttpStatus.NOT_FOUND,              "P001", "존재하지 않는 상품입니다."),
//    PRODUCT_ALREADY_SOLD                (HttpStatus.BAD_REQUEST,            "P002", "이미 판매 완료된 상품입니다."),
//    PRODUCT_DELETED                     (HttpStatus.BAD_REQUEST,            "P003", "삭제된 상품입니다."),
//    PRODUCT_UNAUTHORIZED                (HttpStatus.FORBIDDEN,              "P004", "해당 상품에 대한 권한이 없습니다."),
//    CATEGORY_NOT_FOUND                  (HttpStatus.NOT_FOUND,              "P005", "존재하지 않는 카테고리입니다."),
//    PRODUCT_LOCK_CONFLICT               (HttpStatus.CONFLICT,               "P006", "상품 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
//
//    // ─────────────────────────────────────────────────────────────────
//    // O : Order / Payment
//    // ─────────────────────────────────────────────────────────────────
//    ORDER_NOT_FOUND                     (HttpStatus.NOT_FOUND,              "O001", "존재하지 않는 주문입니다."),
//    ORDER_INVALID_STATUS                (HttpStatus.BAD_REQUEST,            "O002", "현재 주문 상태에서 불가능한 요청입니다."),
//    ORDER_UNAUTHORIZED                  (HttpStatus.FORBIDDEN,              "O003", "해당 주문에 대한 권한이 없습니다."),
//    SELF_ORDER_NOT_ALLOWED              (HttpStatus.BAD_REQUEST,            "O004", "본인 상품은 구매할 수 없습니다."),
//    PAYMENT_NOT_FOUND                   (HttpStatus.NOT_FOUND,              "O005", "존재하지 않는 결제 정보입니다."),
//    PAYMENT_FAILED                      (HttpStatus.BAD_REQUEST,            "O006", "결제에 실패했습니다."),
//    PAYMENT_ALREADY_COMPLETED           (HttpStatus.BAD_REQUEST,            "O007", "이미 완료된 결제입니다."),
//    PAYMENT_AMOUNT_MISMATCH             (HttpStatus.BAD_REQUEST,            "O008", "결제 금액이 일치하지 않습니다."),
//    CANCEL_REQUEST_NOT_FOUND            (HttpStatus.NOT_FOUND,              "O010", "존재하지 않는 취소 요청입니다."),
//    CANCEL_ALREADY_REQUESTED            (HttpStatus.CONFLICT,               "O011", "이미 취소 요청이 접수된 주문입니다."),
//    PAYMENT_REFUND_FAILED               (HttpStatus.BAD_REQUEST,            "O012", "결제 환불에 실패했습니다."),
//
//    // ─────────────────────────────────────────────────────────────────
//    // D : Delivery
//    // ─────────────────────────────────────────────────────────────────
//    DELIVERY_NOT_FOUND                  (HttpStatus.NOT_FOUND,              "D001", "존재하지 않는 배송 정보입니다."),
//    DELIVERY_INVALID_STATUS             (HttpStatus.BAD_REQUEST,            "D002", "현재 배송 상태에서 불가능한 요청입니다."),
//    INVALID_COURIER_CODE                (HttpStatus.BAD_REQUEST,            "D003", "지원하지 않는 택배사 코드입니다."),
//    TRACKING_NUMBER_ALREADY_EXISTS      (HttpStatus.CONFLICT,               "D004", "이미 운송장이 입력된 주문입니다."),
//    DELIVERY_SYSTEM_ERROR               (HttpStatus.INTERNAL_SERVER_ERROR,  "D005", "배송 추적 시스템 오류가 발생했습니다."),
//
//    // ─────────────────────────────────────────────────────────────────
//    // R : Report
//    // ─────────────────────────────────────────────────────────────────
//    REPORT_NOT_FOUND                    (HttpStatus.NOT_FOUND,              "R001", "존재하지 않는 신고입니다."),
//    REPORT_ALREADY_EXISTS               (HttpStatus.CONFLICT,               "R002", "이미 신고한 대상입니다."),
//    SELF_REPORT_NOT_ALLOWED             (HttpStatus.BAD_REQUEST,            "R003", "본인은 신고할 수 없습니다."),
//
//    // ─────────────────────────────────────────────────────────────────
//    // V : Review
//    // ─────────────────────────────────────────────────────────────────
//    REVIEW_NOT_FOUND                    (HttpStatus.NOT_FOUND,              "V001", "존재하지 않는 리뷰입니다."),
//    REVIEW_ALREADY_EXISTS               (HttpStatus.CONFLICT,               "V002", "이미 리뷰를 작성한 주문입니다."),
//
//    // ─────────────────────────────────────────────────────────────────
//    // W : Wishlist
//    // ─────────────────────────────────────────────────────────────────
//    WISHLIST_ALREADY_EXISTS             (HttpStatus.CONFLICT,               "W001", "이미 찜한 상품입니다."),
//    WISHLIST_NOT_FOUND                  (HttpStatus.NOT_FOUND,              "W002", "찜 목록에 없는 상품입니다.");
//
//    private final HttpStatus httpStatus;
//    private final String code;
//    private final String message;
//}