package ru.vladislav117.variant.error;

import ru.vladislav117.variant.VariantType;

import java.util.Arrays;

/**
 * Ошибка, возникающая при несоответствии типов.
 */
public class VariantTypeError extends VariantError {
    /**
     * Создание ошибки, возникающей при несоответствии типов.
     *
     * @param actualType   Тип объекта
     * @param requiredType Требуемый тип объекта
     */
    public VariantTypeError(VariantType actualType, VariantType requiredType) {
        super("Value type is " + actualType + ", but " + requiredType + " is required");
    }

    /**
     * Создание ошибки, возникающей при несоответствии типов.
     *
     * @param actualType    Тип объекта
     * @param requiredTypes Требуемые типы объекта
     */
    public VariantTypeError(VariantType actualType, VariantType... requiredTypes) {
        super("Value type is " + actualType + ", but " + Arrays.toString(requiredTypes) + " is required");
    }
}
