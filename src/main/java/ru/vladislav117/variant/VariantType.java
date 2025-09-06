package ru.vladislav117.variant;

/**
 * Тип объекта.
 */
public enum VariantType {
    /**
     * Пусто.
     */
    NULL("null"),
    /**
     * Логическое значение.
     */
    BOOLEAN("boolean"),
    /**
     * Число.
     */
    NUMBER("number"),
    /**
     * Строка.
     */
    STRING("string"),
    /**
     * Список.
     */
    LIST("list"),
    /**
     * Таблица.
     */
    MAP("map");

    /**
     * Название типа.
     */
    final String name;

    /**
     * Создание типа.
     *
     * @param name Название типа
     */
    VariantType(String name) {
        this.name = name;
    }

    /**
     * Получение название.
     *
     * @return Название.
     */
    public String getName() {
        return name;
    }
}
