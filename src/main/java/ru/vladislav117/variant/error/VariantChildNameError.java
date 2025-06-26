package ru.vladislav117.variant.error;

/**
 * Ошибка, возникающая при отсутствии дочернего объекта по заданному имени.
 */
public class VariantChildNameError extends VariantError {
    /**
     * Создание ошибки, возникающей при отсутствии дочернего объекта по заданному имени.
     *
     * @param childName Указанное имя
     */
    public VariantChildNameError(String childName) {
        super("Object does not have child with name \"" + childName + "\"");
    }
}
