package ru.vladislav117.variant.error;

/**
 * Ошибка, возникающая при неподдерживаемом типе устанавливаемого значения.
 */
public class VariantSetValueError extends VariantError {
    /**
     * Создание ошибки, возникающей при неподдерживаемом типе устанавливаемого значения.
     *
     * @param value Значение
     */
    public VariantSetValueError(Object value) {
        super("Variant does not support type " + value.getClass().getSimpleName());
    }
}
