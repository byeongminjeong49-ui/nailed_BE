package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryCode {

    // ───────────── 맨즈웨어 > 상의 ─────────────
    MENS_TOP_TSHIRT("티셔츠", "맨즈웨어 > 상의 > 티셔츠"),
    MENS_TOP_HOODIE("후드티", "맨즈웨어 > 상의 > 후드티"),
    MENS_TOP_KNIT("니트", "맨즈웨어 > 상의 > 니트"),
    MENS_TOP_SWEATSHIRT("맨투맨", "맨즈웨어 > 상의 > 맨투맨"),
    MENS_TOP_SHIRT("셔츠", "맨즈웨어 > 상의 > 셔츠"),
    MENS_TOP_CARDIGAN("가디건", "맨즈웨어 > 상의 > 가디건"),

    // ───────────── 맨즈웨어 > 아우터 ─────────────
    MENS_OUTER_JERSEY("저지", "맨즈웨어 > 아우터 > 저지"),
    MENS_OUTER_WINDBREAKER("바람막이", "맨즈웨어 > 아우터 > 바람막이"),
    MENS_OUTER_JACKET("자켓", "맨즈웨어 > 아우터 > 자켓"),
    MENS_OUTER_HOODIE_ZIP("후드집업", "맨즈웨어 > 아우터 > 후드집업"),
    MENS_OUTER_BLOUSON("볼버/블루종", "맨즈웨어 > 아우터 > 볼버/블루종"),
    MENS_OUTER_COAT("코트", "맨즈웨어 > 아우터 > 코트"),
    MENS_OUTER_PADDING("패딩", "맨즈웨어 > 아우터 > 패딩"),

    // ───────────── 맨즈웨어 > 하의 ─────────────
    MENS_BOTTOM_SHORTS("반바지", "맨즈웨어 > 하의 > 반바지"),
    MENS_BOTTOM_DENIM("데님팬츠", "맨즈웨어 > 하의 > 데님팬츠"),
    MENS_BOTTOM_CARGO("카고팬츠", "맨즈웨어 > 하의 > 카고팬츠"),
    MENS_BOTTOM_SWEAT("스웨트팬츠", "맨즈웨어 > 하의 > 스웨트팬츠"),
    MENS_BOTTOM_SLACKS("슬랙스", "맨즈웨어 > 하의 > 슬랙스"),

    // ───────────── 맨즈웨어 > 신발 ─────────────
    MENS_SHOES_SNEAKERS("스니커즈", "맨즈웨어 > 신발 > 스니커즈"),
    MENS_SHOES_BOOTS("부츠", "맨즈웨어 > 신발 > 부츠"),
    MENS_SHOES_LOAFER("구두/로퍼", "맨즈웨어 > 신발 > 구두/로퍼"),
    MENS_SHOES_SANDAL("샌들/슬리퍼", "맨즈웨어 > 신발 > 샌들/슬리퍼"),

    // ───────────── 맨즈웨어 > 가방 ─────────────
    MENS_BAG_BACKPACK("백팩", "맨즈웨어 > 가방 > 백팩"),
    MENS_BAG_CROSSBODY("크로스백", "맨즈웨어 > 가방 > 크로스백"),
    MENS_BAG_SHOULDER("숄더백", "맨즈웨어 > 가방 > 숄더백"),
    MENS_BAG_TOTE("토트백", "맨즈웨어 > 가방 > 토트백"),

    // ───────────── 맨즈웨어 > 모자 ─────────────
    MENS_CAP_CAP("캡", "맨즈웨어 > 모자 > 캡"),
    MENS_CAP_BEANIE("비니", "맨즈웨어 > 모자 > 비니"),

    // ───────────── 우먼즈웨어 > 상의 ─────────────
    WOMENS_TOP_TSHIRT("티셔츠", "우먼즈웨어 > 상의 > 티셔츠"),
    WOMENS_TOP_HOODIE("후드티", "우먼즈웨어 > 상의 > 후드티"),
    WOMENS_TOP_KNIT("니트", "우먼즈웨어 > 상의 > 니트"),
    WOMENS_TOP_SWEATSHIRT("맨투맨", "우먼즈웨어 > 상의 > 맨투맨"),
    WOMENS_TOP_SHIRT("셔츠", "우먼즈웨어 > 상의 > 셔츠"),
    WOMENS_TOP_CARDIGAN("가디건", "우먼즈웨어 > 상의 > 가디건"),

    // ───────────── 우먼즈웨어 > 아우터 ─────────────
    WOMENS_OUTER_JERSEY("저지", "우먼즈웨어 > 아우터 > 저지"),
    WOMENS_OUTER_WINDBREAKER("바람막이", "우먼즈웨어 > 아우터 > 바람막이"),
    WOMENS_OUTER_JACKET("자켓", "우먼즈웨어 > 아우터 > 자켓"),
    WOMENS_OUTER_HOODIE_ZIP("후드집업", "우먼즈웨어 > 아우터 > 후드집업"),
    WOMENS_OUTER_COAT("코트", "우먼즈웨어 > 아우터 > 코트"),
    WOMENS_OUTER_PADDING("패딩", "우먼즈웨어 > 아우터 > 패딩"),

    // ───────────── 우먼즈웨어 > 하의 ─────────────
    WOMENS_BOTTOM_SHORTS("반바지", "우먼즈웨어 > 하의 > 반바지"),
    WOMENS_BOTTOM_DENIM("데님팬츠", "우먼즈웨어 > 하의 > 데님팬츠"),
    WOMENS_BOTTOM_CARGO("카고팬츠", "우먼즈웨어 > 하의 > 카고팬츠"),
    WOMENS_BOTTOM_SWEAT("스웨트팬츠", "우먼즈웨어 > 하의 > 스웨트팬츠"),
    WOMENS_BOTTOM_SLACKS("슬랙스", "우먼즈웨어 > 하의 > 슬랙스"),

    // ───────────── 우먼즈웨어 > 신발 ─────────────
    WOMENS_SHOES_SNEAKERS("스니커즈", "우먼즈웨어 > 신발 > 스니커즈"),
    WOMENS_SHOES_BOOTS("부츠", "우먼즈웨어 > 신발 > 부츠"),
    WOMENS_SHOES_LOAFER("구두/로퍼", "우먼즈웨어 > 신발 > 구두/로퍼"),
    WOMENS_SHOES_SANDAL("샌들/슬리퍼", "우먼즈웨어 > 신발 > 샌들/슬리퍼"),

    // ───────────── 우먼즈웨어 > 가방 ─────────────
    WOMENS_BAG_BACKPACK("백팩", "우먼즈웨어 > 가방 > 백팩"),
    WOMENS_BAG_CROSSBODY("크로스백", "우먼즈웨어 > 가방 > 크로스백"),
    WOMENS_BAG_SHOULDER("숄더백", "우먼즈웨어 > 가방 > 숄더백"),
    WOMENS_BAG_TOTE("토트백", "우먼즈웨어 > 가방 > 토트백"),

    // ───────────── 우먼즈웨어 > 모자 ─────────────
    WOMENS_CAP_CAP("캡", "우먼즈웨어 > 모자 > 캡"),
    WOMENS_CAP_BEANIE("비니", "우먼즈웨어 > 모자 > 비니"),

    // ───────────── 우먼즈웨어 > 치마 ─────────────
    WOMENS_SKIRT_LONG("롱 스커트", "우먼즈웨어 > 치마 > 롱 스커트"),
    WOMENS_SKIRT_MINI("미니 스커트", "우먼즈웨어 > 치마 > 미니 스커트"),

    // ───────────── 우먼즈웨어 > 원피스 ─────────────
    WOMENS_DRESS_LONG("롱 원피스", "우먼즈웨어 > 원피스 > 롱 원피스"),
    WOMENS_DRESS_MINI("미니 원피스", "우먼즈웨어 > 원피스 > 미니 원피스"),

    // ───────────── 럭셔리 > 브랜드 ─────────────
    LUXURY_GOYARD("고야드", "럭셔리 > 고야드"),
    LUXURY_GUCCI("구찌", "럭셔리 > 구찌"),
    LUXURY_HERMES("에르메스", "럭셔리 > 에르메스"),
    LUXURY_BOTTEGA("보테가베네타", "럭셔리 > 보테가베네타"),
    LUXURY_PRADA("프라다", "럭셔리 > 프라다"),
    LUXURY_BURBERRY("버버리", "럭셔리 > 버버리"),
    LUXURY_DIOR("크리스찬디올", "럭셔리 > 크리스찬디올"),
    LUXURY_FERRAGAMO("페라가모", "럭셔리 > 페라가모"),
    LUXURY_CHANEL("샤넬", "럭셔리 > 샤넬"),
    LUXURY_LV("루이비통", "럭셔리 > 루이비통"),
    LUXURY_MONTBLANC("몽블랑", "럭셔리 > 몽블랑"),
    LUXURY_CHROME_HEARTS("크롬하츠", "럭셔리 > 크롬하츠"),

    // ───────────── 액세서리 > 패션잡화 ─────────────
    ACC_SUNGLASS("선글라스", "액세서리 > 패션잡화 > 선글라스"),
    ACC_WALLET("지갑", "액세서리 > 패션잡화 > 지갑"),
    ACC_BELT("벨트", "액세서리 > 패션잡화 > 벨트"),
    ACC_KEYRING("키링", "액세서리 > 패션잡화 > 키링"),

    // ───────────── 액세서리 > 주얼리 ─────────────
    ACC_JEWELRY_NECKLACE("목걸이", "액세서리 > 주얼리 > 목걸이"),
    ACC_JEWELRY_RING("반지", "액세서리 > 주얼리 > 반지"),
    ACC_JEWELRY_WATCH("시계", "액세서리 > 주얼리 > 시계"),
    ACC_JEWELRY_BRACELET("팔찌", "액세서리 > 주얼리 > 팔찌"),

    // ───────────── IT/테크 > 디바이스 ─────────────
    IT_CAMERA("카메라", "IT/테크 > 디바이스 > 카메라"),
    IT_PHONE("핸드폰", "IT/테크 > 디바이스 > 핸드폰"),
    IT_LAPTOP("노트북", "IT/테크 > 디바이스 > 노트북"),
    IT_TABLET("태블릿", "IT/테크 > 디바이스 > 태블릿");

    private final String label;
    private final String description;
}