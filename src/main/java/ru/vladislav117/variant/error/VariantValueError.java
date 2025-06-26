package ru.vladislav117.variant.error;

import ru.vladislav117.variant.VariantType;

/**
 * Ошибка, возникающая при несоответствии типов значений.
 */
public class VariantValueError extends VariantError {
    /**
     * Создание ошибки, возникающей при несоответствии типов значений.
     *
     * @param actualType   Тип значения
     * @param requiredType Требуемый тип значения
     */
    public VariantValueError(VariantType actualType, String requiredType) {
        super("Object type is " + actualType + ", but it was converted to " + requiredType);
    }
}
