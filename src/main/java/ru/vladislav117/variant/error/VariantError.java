package ru.vladislav117.variant.error;

/**
 * Ошибка, связанная с Variant.
 */
public class VariantError extends Error {
    /**
     * Создание ошибки, связанной с Variant.
     *
     * @param message Сообщение об ошибке
     */
    public VariantError(String message) {
        super(message);
    }
}
