package ru.vladislav117.variant.error;

/**
 * Ошибка, возникающая при отсутствии дочернего объекта по заданному индексу.
 */
public class VariantChildIndexError extends VariantError {
    /**
     * Создание ошибки, возникающей при отсутствии дочернего объекта по заданному индексу.
     *
     * @param index Указанный индекс
     * @param size  Размер списка
     */
    public VariantChildIndexError(int index, int size) {
        super("Object size is " + size + ", but index " + index + " was given");
    }
}
