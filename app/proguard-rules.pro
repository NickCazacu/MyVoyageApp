# Правила R8/ProGuard для release-сборки.
# Room и Compose поставляют свои consumer-правила, поэтому здесь обычно пусто.
# Оставляем имена enum-констант (используются в Room TypeConverters и в when-блоках).
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
