package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryCode {

    // 맨즈웨어
    MENS_TOP("상의", "맨즈웨어 > 상의"),
    MENS_BOTTOM("하의", "맨즈웨어 > 하의"),
    MENS_OUTER("아우터", "맨즈웨어 > 아우터"),
    MENS_SHOES("신발", "맨즈웨어 > 신발"),
    MENS_SUIT("슈트/정장", "맨즈웨어 > 슈트/정장"),

    // 우먼즈웨어
    WOMENS_TOP("상의", "우먼즈웨어 > 상의"),
    WOMENS_BOTTOM("하의", "우먼즈웨어 > 하의"),
    WOMENS_OUTER("아우터", "우먼즈웨어 > 아우터"),
    WOMENS_SHOES("신발", "우먼즈웨어 > 신발"),
    WOMENS_DRESS("원피스/스커트", "우먼즈웨어 > 원피스/스커트"),

    // 럭셔리
    LUXURY_BAG("가방", "럭셔리 > 가방"),
    LUXURY_WALLET("지갑/소품", "럭셔리 > 지갑/소품"),
    LUXURY_SHOES("신발", "럭셔리 > 신발"),
    LUXURY_CLOTHES("의류", "럭셔리 > 의류"),
    LUXURY_WATCH("시계", "럭셔리 > 시계"),

    // 악세서리
    ACC_BAG("가방", "악세서리 > 가방"),
    ACC_HAT("모자", "악세서리 > 모자"),
    ACC_JEWELRY("주얼리", "악세서리 > 주얼리"),
    ACC_WATCH("시계", "악세서리 > 시계"),
    ACC_BELT("벨트", "악세서리 > 벨트"),
    ACC_SUNGLASS("선글라스/안경", "악세서리 > 선글라스/안경"),

    // 라이프
    LIFE_BEAUTY("뷰티/화장품", "라이프 > 뷰티/화장품"),
    LIFE_INTERIOR("인테리어/소품", "라이프 > 인테리어/소품"),
    LIFE_SPORTS("스포츠/레저", "라이프 > 스포츠/레저"),
    LIFE_BOOK("도서/음반", "라이프 > 도서/음반"),
    LIFE_FOOD("식품/건강", "라이프 > 식품/건강"),

    // IT/테크
    IT_LAPTOP("노트북", "IT/테크 > 노트북"),
    IT_PHONE("스마트폰", "IT/테크 > 스마트폰"),
    IT_TABLET("태블릿", "IT/테크 > 태블릿"),
    IT_PERIPHERAL("주변기기", "IT/테크 > 주변기기"),
    IT_AUDIO("음향기기", "IT/테크 > 음향기기"),
    IT_CAMERA("카메라", "IT/테크 > 카메라"),
    IT_GAME("게임기기", "IT/테크 > 게임기기");

    private final String label;
    private final String description;
}
