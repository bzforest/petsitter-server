package com.company.pet_sitter_server.address.util;

/**
 * ตัดคำนำหน้าทางปกครองไทยที่มักติดมากับข้อมูลจาก geocoder/OSM
 * เพื่อเก็บค่าใน DB ในรูปแบบสั้น (เช่น "ห้วยขวาง" แทน "เขตห้วยขวาง")
 */
public final class ThaiAddressNormalizer {

    private ThaiAddressNormalizer() {}

    /** ค่าจังหวัด / อำเภอ / ตำบลแบบแบน หลัง reconcile กรุงเทพและสลับฟิลด์จาก geocoder */
    public static final class FlatThaiAddress {
        public final String province;
        public final String district;
        public final String subDistrict;

        public FlatThaiAddress(String province, String district, String subDistrict) {
            this.province = province;
            this.district = district;
            this.subDistrict = subDistrict;
        }
    }

    private static String trimToEmpty(String s) {
        if (s == null) {
            return "";
        }
        return s.trim();
    }

    private static boolean isBangkokMetroLabel(String raw) {
        String t = trimToEmpty(raw);
        if (t.isEmpty()) {
            return false;
        }
        return t.contains("กรุงเทพ") || t.equalsIgnoreCase("bangkok");
    }

    /**
     * จัดระเบียบฟิลด์ที่ flat สำหรับกรุงเทพมหานคร: geocoder รุ่นเก่ามักสลับจังหวัดกับเขต
     * หรือใส่คำว่า "เขต..." ไว้ในช่องตำบล/แขวง — ฟังก์ชันนี้แก้ลำดับและตัดซ้ำก่อน normalize
     */
    public static FlatThaiAddress reconcileBangkokFlatFields(String province, String district, String subDistrict) {
        String p = trimToEmpty(province);
        String d = trimToEmpty(district);
        String s = trimToEmpty(subDistrict);

        if (p.isEmpty() && isBangkokMetroLabel(d)) {
            p = "กรุงเทพมหานคร";
        }
        if (!s.isEmpty() && s.startsWith("เขต")) {
            d = s;
            s = "";
        }
        if (isBangkokMetroLabel(d) && isBangkokMetroLabel(p)) {
            d = "";
        }

        return new FlatThaiAddress(
                p.isEmpty() ? null : p,
                d.isEmpty() ? null : d,
                s.isEmpty() ? null : s);
    }

    public static String normalizeProvince(String raw) {
        return stripFirstPrefix(raw, "จังหวัด");
    }

    public static String normalizeDistrict(String raw) {
        String t = stripFirstPrefix(raw, "เขต");
        t = stripFirstPrefix(t, "อำเภอ");
        return emptyToNull(t);
    }

    public static String normalizeSubDistrict(String raw) {
        String t = stripFirstPrefix(raw, "แขวง");
        t = stripFirstPrefix(t, "ตำบล");
        return emptyToNull(t);
    }

    private static String stripFirstPrefix(String raw, String prefix) {
        if (raw == null) return null;
        String t = raw.trim();
        if (t.isEmpty()) return null;
        if (t.startsWith(prefix)) {
            t = t.substring(prefix.length()).trim();
        }
        return t.isEmpty() ? null : t;
    }

    private static String emptyToNull(String t) {
        if (t == null || t.isEmpty()) return null;
        return t;
    }
}
