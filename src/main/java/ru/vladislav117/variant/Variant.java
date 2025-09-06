package ru.vladislav117.variant;

import com.google.gson.*;
import org.jetbrains.annotations.Nullable;
import ru.vladislav117.variant.error.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Объект, который может иметь значения различных типов, таких как
 * логическое значение, число, строка, список или таблица.
 */
public class Variant {
    static Gson gson = new Gson();
    protected VariantType type = VariantType.NULL;
    protected Object object = null;

    /**
     * Создание объекта.
     * Если тип устанавливаемого значения не поддерживается, будет вызвано исключение.
     *
     * @param value Значение объекта
     * @throws VariantSetValueError Если тип устанавливаемого значения не поддерживается, будет вызвано исключение.
     * @see Variant
     */
    public Variant(@Nullable Object value) {
        set(value);
    }

    /**
     * Создание объекта из json-элемента.
     *
     * @param json Json-элемент
     * @return Объект из json-элемента
     * @see Variant#fromJsonString(String)
     */
    public static Variant fromJson(JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();
            if (primitive.isBoolean()) return new Variant(primitive.getAsBoolean());
            if (primitive.isNumber()) return new Variant(primitive.getAsNumber().doubleValue());
            if (primitive.isString()) return new Variant(primitive.getAsString());
        }
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            ArrayList<Variant> list = new ArrayList<>();
            for (JsonElement entry : array) {
                list.add(fromJson(entry));
            }
            return new Variant(list);
        }
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            HashMap<String, Variant> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                map.put(entry.getKey(), fromJson(entry.getValue()));
            }
            return new Variant(map);
        }
        return new Variant(null);
    }

    /**
     * Создание объекта из json-строки.
     *
     * @param json Json-строка
     * @return Объект из json-строки
     * @see Variant#fromJson(JsonElement)
     */
    public static Variant fromJsonString(String json) {
        return fromJson(gson.fromJson(json, JsonElement.class));
    }

    /**
     * Получение типа объекта
     *
     * @return Тип объекта.
     */
    public VariantType getType() {
        return type;
    }

    /**
     * Получение размера объекта: длины строки, размера списка или размера таблицы.
     * Если этот объект не является строкой, списком или таблицей, будет вызвано исключение.
     *
     * @return Длина строки, размер списка или размер таблицы.
     * @throws VariantTypeError Если этот объект не является строкой, списком или таблицей, будет вызвано исключение.
     */
    public int getSize() {
        if (type == VariantType.STRING) return ((String) object).length();
        if (type == VariantType.LIST) return ((List<?>) object).size();
        if (type == VariantType.MAP) return ((Map<?, ?>) object).size();
        throw new VariantTypeError(type, VariantType.STRING, VariantType.LIST, VariantType.MAP);
    }

    /**
     * Получение имён дочерних объектов этой таблицы.
     * Изменение набора имён повлечёт за собой изменение таблицы.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @return Имена дочерних объектов.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public Set<String> getChildrenNames() {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        return ((Map<String, Variant>) object).keySet();
    }

    /**
     * Проверка типа Variant.
     *
     * @param type Тип для сравнения
     * @return Равенство типов.
     */
    public boolean is(VariantType type) {
        return this.type == type;
    }

    /**
     * Проверка типа дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param type Тип для сравнения
     * @return Равенство типов.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean is(int childIndex, VariantType type) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        return ((List<Variant>) object).get(childIndex).type == type;
    }

    /**
     * Проверка типа дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @param type Тип для сравнения
     * @return Равенство типов.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean is(String childName, VariantType type) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) throw new VariantChildNameError(childName);
        return ((Map<String, Variant>) object).get(childName).type == type;
    }

    /**
     * Проверка наличия дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Наличие дочернего объекта.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public boolean contains(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        return childIndex > 0 && childIndex < ((List<?>) object).size();
    }

    /**
     * Проверка наличия дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Наличие дочернего объекта.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public boolean contains(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        return ((Map<?, ?>) object).containsKey(childName);
    }

    /**
     * Получение дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Дочерний объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public Variant get(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        return ((List<Variant>) object).get(childIndex);
    }

    /**
     * Получение дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено null.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Дочерний объект или null.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public @Nullable Variant getOrNull(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) return null;
        return ((List<Variant>) object).get(childIndex);
    }

    /**
     * Получение дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено значение по умолчанию.
     *
     * @param childIndex Индекс дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Дочерний объект или null.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public @Nullable Variant getOrDefault(int childIndex, Variant defaultValue) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) return defaultValue;
        return ((List<Variant>) object).get(childIndex);
    }

    /**
     * Получение дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Дочерний объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public Variant get(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) throw new VariantChildNameError(childName);
        return ((Map<String, Variant>) object).get(childName);
    }

    /**
     * Получение дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено null.
     *
     * @param childName Имя дочернего объекта
     * @return Дочерний объект или null.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public @Nullable Variant getOrNull(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) return null;
        return ((Map<String, Variant>) object).get(childName);
    }

    /**
     * Получение дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено значение по умолчанию.
     *
     * @param childName Имя дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Дочерний объект или null.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public @Nullable Variant getOrDefault(String childName, Variant defaultValue) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) return defaultValue;
        return ((Map<String, Variant>) object).get(childName);
    }

    /**
     * Установка значения объекта.
     * Если тип устанавливаемого значения не поддерживается, будет вызвано исключение.
     *
     * @param value Значение объекта
     * @return Этот же объект.
     * @throws VariantSetValueError Если тип устанавливаемого значения не поддерживается, будет вызвано исключение.
     */
    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public Variant set(Object value) {
        if (type == VariantType.LIST) ((List<?>) object).clear();
        if (type == VariantType.MAP) ((Map<?, ?>) object).clear();
        type = VariantType.NULL;
        object = null;
        if (value == null) {
            return this;
        }
        if (value instanceof Boolean) {
            type = VariantType.BOOLEAN;
            object = value;
            return this;
        }
        if (value instanceof Byte) {
            type = VariantType.NUMBER;
            object = ((Byte) value).doubleValue();
            return this;
        }
        if (value instanceof Short) {
            type = VariantType.NUMBER;
            object = ((Short) value).doubleValue();
            return this;
        }
        if (value instanceof Integer) {
            type = VariantType.NUMBER;
            object = ((Integer) value).doubleValue();
            return this;
        }
        if (value instanceof Long) {
            type = VariantType.NUMBER;
            object = ((Long) value).doubleValue();
            return this;
        }
        if (value instanceof Float) {
            type = VariantType.NUMBER;
            object = ((Float) value).doubleValue();
            return this;
        }
        if (value instanceof Double) {
            type = VariantType.NUMBER;
            object = value;
            return this;
        }
        if (value instanceof Character) {
            type = VariantType.STRING;
            object = ((Character) value).toString();
            return this;
        }
        if (value instanceof String) {
            type = VariantType.STRING;
            object = value;
            return this;
        }
        if (value instanceof List<?>) {
            type = VariantType.LIST;
            object = new ArrayList<Variant>(((List<?>) value).size());
            for (Object innerObject : ((List<?>) value)) {
                ((List<Variant>) object).add(new Variant(innerObject));
            }
            return this;
        }
        if (value instanceof Map<?, ?>) {
            type = VariantType.MAP;
            object = new HashMap<String, Variant>(((Map<?, ?>) value).size());
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                ((Map<String, Variant>) object).put(entry.getKey().toString(), new Variant(entry.getValue()));
            }
            return this;
        }
        if (value instanceof Variant) {
            set(((Variant) value).object);
            return this;
        }
        throw new VariantSetValueError(value);
    }

    /**
     * Установка дочернего объекта по индексу.
     * Если тип устанавливаемого значения не поддерживается, будет вызвано исключение.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение объекта
     * @return Этот же объект.
     * @throws VariantSetValueError Если тип устанавливаемого значения не поддерживается, будет вызвано исключение.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public Variant set(int childIndex, Object value) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        ((List<Variant>) object).set(childIndex, new Variant(value));
        return this;
    }

    /**
     * Вставка дочернего объекта в список.
     * Если индекс меньше 0, объект будет добавлен в начало списка.
     * Если индекс больше или равен длине списка, объект будет добавлен в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public Variant insert(int childIndex, Object value) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0) {
            ((List<Variant>) object).add(0, new Variant(value));
            return this;
        }
        if (childIndex >= ((List<?>) object).size()) {
            ((List<Variant>) object).add(new Variant(value));
            return this;
        }
        ((List<Variant>) object).add(childIndex, new Variant(value));
        return this;
    }

    /**
     * Добавление дочернего объекта в список.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public Variant add(Object value) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        ((List<Variant>) object).add(new Variant(value));
        return this;
    }

    /**
     * Добавление дочернего объекта в начало списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public Variant addFirst(Object value) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        ((List<Variant>) object).add(0, new Variant(value));
        return this;
    }

    /**
     * Добавление дочернего объекта в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public Variant addLast(Object value) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        ((List<Variant>) object).add(new Variant(value));
        return this;
    }

    /**
     * Установка дочернего объекта по имени.
     * Если тип устанавливаемого значения не поддерживается, будет вызвано исключение.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @param value Значение объекта
     * @return Этот же объект.
     * @throws VariantSetValueError Если тип устанавливаемого значения не поддерживается, будет вызвано исключение.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public Variant set(String childName, Object value) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        ((Map<String, Variant>) object).put(childName, new Variant(value));
        return this;
    }

    /**
     * Удаление дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant remove(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        ((List<?>) object).remove(childIndex);
        return this;
    }

    /**
     * Удаление дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant remove(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) throw new VariantChildNameError(childName);
        ((Map<?, ?>) object).remove(childName);
        return this;
    }

    /**
     * Удаление всех дочерних объектов, подходящих условию.
     * Если этот объект не списком или таблицей, будет вызвано исключение.
     *
     * @param filter Фильтр дочерних объектов
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не списком или таблицей, будет вызвано исключение.
     */
    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public Variant removeIf(Predicate<Variant> filter) {
        if (type == VariantType.LIST) {
            ((List<Variant>) object).removeIf(filter);
            return this;
        }
        if (type == VariantType.MAP) {
            ((Map<String, Variant>) object).entrySet().removeIf(entry -> filter.test(entry.getValue()));
            return this;
        }
        throw new VariantTypeError(type,VariantType.LIST, VariantType.MAP);
    }

    /**
     * Обработка каждого дочернего объекта.
     * Если этот объект не является списком или таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик дочернего объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком или таблицей, будет вызвано исключение.
     */
    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public Variant forEach(Consumer<Variant> handler) {
        if (type == VariantType.LIST) {
            ((List<Variant>) object).forEach(handler);
            return this;
        }
        if (type == VariantType.MAP) {
            ((Map<String, Variant>) object).forEach((key, value) -> handler.accept(value));
            return this;
        }
        throw new VariantTypeError(type, VariantType.LIST, VariantType.MAP);
    }

    /**
     * Обработка каждого дочернего объекта в списке.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param handler Обработчик дочернего объекта и его индекса
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public Variant forEachInList(BiConsumer<Integer, Variant> handler) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        int childIndex = 0;
        for (Variant child : ((List<Variant>) object)) {
            handler.accept(childIndex, child);
            childIndex++;
        }
        return this;
    }

    /**
     * Обработка каждого дочернего объекта в таблице.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик дочернего объекта и его имени
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public Variant forEachInMap(BiConsumer<String, Variant> handler) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        ((Map<String, Variant>) object).forEach(handler);
        return this;
    }

    /**
     * Преобразование объекта в json.
     *
     * @return Json-объект
     */
    @SuppressWarnings("unchecked")
    public JsonElement toJson() {
        if (type == VariantType.NULL) return JsonNull.INSTANCE;
        if (type == VariantType.BOOLEAN) return new JsonPrimitive((Boolean) object);
        if (type == VariantType.NUMBER) return new JsonPrimitive((Double) object);
        if (type == VariantType.STRING) return new JsonPrimitive((String) object);
        if (type == VariantType.LIST) {
            JsonArray array = new JsonArray();
            for (Variant child : (List<Variant>) object) {
                array.add(child.toJson());
            }
            return array;
        }
        if (type == VariantType.MAP) {
            JsonObject object = new JsonObject();
            for (Map.Entry<String, Variant> entry : ((Map<String, Variant>) this.object).entrySet()) {
                object.add(entry.getKey(), entry.getValue().toJson());
            }
            return object;
        }
        return null;
    }

    /**
     * Преобразование объекта в json-строку.
     *
     * @return Json-строка
     */
    public String toJsonString() {
        return toJson().toString();
    }

    /**
     * Преобразование объекта в строку.
     *
     * @return Строка
     */
    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        if (type == VariantType.NULL) return "null";
        if (type == VariantType.BOOLEAN) return ((Boolean) object) ? "true" : "false";
        if (type == VariantType.NUMBER) {
            if (((Double) object).longValue() == (Double) object) return Long.toString(((Double) object).longValue());
            return ((Double) object).toString();
        }
        if (type == VariantType.STRING) return "\"" + object + "\"";
        if (type == VariantType.LIST) {
            StringBuilder string = new StringBuilder("[");
            for (Variant child : (List<Variant>) object) {
                string.append(child.toString()).append(", ");
            }
            if (!((List<?>) object).isEmpty()) {
                string.delete(string.length() - 2, string.length());
            }
            string.append("]");
            return string.toString();
        }
        if (type == VariantType.MAP) {
            StringBuilder string = new StringBuilder("{");
            for (Map.Entry<String, Variant> entry : ((Map<String, Variant>) this.object).entrySet()) {
                string.append(entry.getKey()).append("=").append(entry.getValue().toString()).append(", ");
            }
            if (!((Map<?, ?>) object).isEmpty()) {
                string.delete(string.length() - 2, string.length());
            }
            string.append("}");
            return string.toString();
        }
        return null;
    }

    /**
     * Получение хеша объекта.
     *
     * @return Хеш.
     */
    @Override
    public int hashCode() {
        return object.hashCode();
    }

    /**
     * Сравнение объектов.
     *
     * @param obj Объект для сравнения
     * @return Равенство объектов.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (!(obj instanceof Variant)) return false;
        Variant other = (Variant) obj;
        if (type != other.type) return false;
        if (type == VariantType.NULL) return true;
        if (type == VariantType.BOOLEAN) return object == other.object;
        if (type == VariantType.NUMBER) return object == other.object;
        if (type == VariantType.STRING) return object.equals(other.object);
        if (type == VariantType.LIST) {
            List<Variant> list = ((List<Variant>) object);
            List<Variant> otherList = ((List<Variant>) other.object);
            if (list.size() != otherList.size()) return false;
            for (int index = 0; index < list.size(); index++) {
                if (!list.get(index).equals(otherList.get(index))) return false;
            }
            return true;
        }
        if (type == VariantType.MAP) {
            Map<String, Variant> map = ((Map<String, Variant>) object);
            Map<String, Variant> otherMap = ((Map<String, Variant>) other.object);
            if (map.size() != otherMap.size()) return false;
            for (Map.Entry<String, Variant> entry : map.entrySet()) {
                if (!entry.getValue().equals(otherMap.get(entry.getKey()))) return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Создание объекта со значением типа Boolean.
     *
     * @param value Значение объекта
     * @return Объект со значением типа Boolean.
     */
    public static Variant newBoolean(boolean value) {
        return new Variant(value);
    }

    /**
     * Создание объекта со значением типа Boolean по умолчанию.
     *
     * @return Объект со значением типа Boolean по умолчанию.
     */
    public static Variant newBoolean() {
        return new Variant(false);
    }

    /**
     * Проверка, является ли значение этого объекта Boolean.
     *
     * @return Является ли значение этого объекта Boolean.
     */
    public boolean isBoolean() {
        return type == VariantType.BOOLEAN;
    }

    /**
     * Проверка типа значения дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isBoolean(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        return ((List<Variant>) object).get(childIndex).isBoolean();
    }

    /**
     * Проверка типа значения дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isBoolean(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) throw new VariantChildNameError(childName);
        return ((Map<String, Variant>) object).get(childName).isBoolean();
    }

    /**
     * Преобразование значения объекта к типу Boolean.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @return Значение объекта с типом Boolean.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean asBoolean() {
        if (!isBoolean()) throw new VariantValueError(type, "boolean");
        return (Boolean) object;
    }

    /**
     * Преобразование значения объекта к типу Boolean.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @return Значение объекта с типом Boolean или null.
     */
    @SuppressWarnings("unchecked")
    public @Nullable Boolean asBooleanOrNull() {
        if (!isBoolean()) return null;
        return (Boolean) object;
    }

    /**
     * Преобразование значения объекта к типу Boolean.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param defaultValue Значение по умолчанию
     * @return Значение объекта с типом Boolean или значение по умолчанию.
     */
    @SuppressWarnings("unchecked")
    public Boolean asBooleanOrDefault(Boolean defaultValue) {
        if (!isBoolean()) return defaultValue;
        return (Boolean) object;
    }

    /**
     * Преобразование значения дочернего объекта к типу Boolean.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Boolean.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public boolean getBoolean(int childIndex) {
        return get(childIndex).asBoolean();
    }

    /**
     * Преобразование значения дочернего объекта к типу Boolean.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Boolean или null.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public @Nullable Boolean getBooleanOrNull(int childIndex) {
        Variant child = getOrNull(childIndex);
        if (child == null) return null;
        return child.asBooleanOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Boolean.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childIndex Индекс дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Boolean или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Boolean getBooleanOrDefault(int childIndex, Boolean defaultValue) {
        Variant child = getOrNull(childIndex);
        if (child == null) return defaultValue;
        return child.asBooleanOrDefault(defaultValue);
    }

    /**
     * Преобразование значения дочернего объекта к типу Boolean.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Boolean.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public boolean getBoolean(String childName) {
        return get(childName).asBoolean();
    }

    /**
     * Преобразование значения дочернего объекта к типу Boolean.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Boolean или null.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public @Nullable Boolean getBooleanOrNull(String childName) {
        Variant child = getOrNull(childName);
        if (child == null) return null;
        return child.asBooleanOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Boolean.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childName Имя дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Boolean или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Boolean getBooleanOrDefault(String childName, Boolean defaultValue) {
        Variant child = getOrNull(childName);
        if (child == null) return defaultValue;
        return child.asBooleanOrDefault(defaultValue);
    }

    /**
     * Установка значения объекта с типом Boolean.
     *
     * @param value Значение объекта с типом Boolean
     * @return Этот же объект.
     */
    public Variant setBoolean(boolean value) {
        set(value);
        return this;
    }

    /**
     * Установка дочернего объекта со значением с типом Boolean.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Boolean
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    public Variant setBoolean(int childIndex, boolean value) {
        set(childIndex, value);
        return this;
    }

    /**
     * Вставка дочернего объекта со значением с типом Boolean.
     * Если индекс меньше 0, объект будет добавлен в начало списка.
     * Если индекс больше или равен длине списка, объект будет добавлен в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Boolean
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant insertBoolean(int childIndex, boolean value) {
        insert(childIndex, value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Boolean.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Boolean
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addBoolean(boolean value) {
        add(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Boolean в начало списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Boolean
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addFirstBoolean(boolean value) {
        addFirst(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Boolean в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Boolean
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addLastBoolean(boolean value) {
        addLast(value);
        return this;
    }

    /**
     * Установка значения дочернего объекта с типом Boolean.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @param value Значение дочернего объекта с типом Boolean
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Variant setBoolean(String childName, boolean value) {
        set(childName, value);
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Boolean.
     * Если этот объект не является списком или таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком или таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachBoolean(Consumer<Boolean> handler) {
        forEach((child) -> { if (child.isBoolean()) handler.accept(child.asBoolean()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Boolean в списке.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его индекса
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachBooleanInList(BiConsumer<Integer, Boolean> handler) {
        forEachInList((childIndex, child) -> { if (child.isBoolean()) handler.accept(childIndex, child.asBoolean()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Boolean в таблице.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его имени
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachBooleanInMap(BiConsumer<String, Boolean> handler) {
        forEachInMap((childName, child) -> { if (child.isBoolean()) handler.accept(childName, child.asBoolean()); });
        return this;
    }

    /**
     * Создание объекта со значением типа Byte.
     *
     * @param value Значение объекта
     * @return Объект со значением типа Byte.
     */
    public static Variant newByte(byte value) {
        return new Variant(value);
    }

    /**
     * Создание объекта со значением типа Byte по умолчанию.
     *
     * @return Объект со значением типа Byte по умолчанию.
     */
    public static Variant newByte() {
        return new Variant(0);
    }

    /**
     * Проверка, является ли значение этого объекта Byte.
     *
     * @return Является ли значение этого объекта Byte.
     */
    public boolean isByte() {
        return type == VariantType.NUMBER && ((Double) object).byteValue() == (Double) object;
    }

    /**
     * Проверка типа значения дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isByte(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        return ((List<Variant>) object).get(childIndex).isByte();
    }

    /**
     * Проверка типа значения дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isByte(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) throw new VariantChildNameError(childName);
        return ((Map<String, Variant>) object).get(childName).isByte();
    }

    /**
     * Преобразование значения объекта к типу Byte.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @return Значение объекта с типом Byte.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public byte asByte() {
        if (!isByte()) throw new VariantValueError(type, "byte");
        return ((Double) object).byteValue();
    }

    /**
     * Преобразование значения объекта к типу Byte.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @return Значение объекта с типом Byte или null.
     */
    @SuppressWarnings("unchecked")
    public @Nullable Byte asByteOrNull() {
        if (!isByte()) return null;
        return ((Double) object).byteValue();
    }

    /**
     * Преобразование значения объекта к типу Byte.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param defaultValue Значение по умолчанию
     * @return Значение объекта с типом Byte или значение по умолчанию.
     */
    @SuppressWarnings("unchecked")
    public Byte asByteOrDefault(Byte defaultValue) {
        if (!isByte()) return defaultValue;
        return ((Double) object).byteValue();
    }

    /**
     * Преобразование значения дочернего объекта к типу Byte.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Byte.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public byte getByte(int childIndex) {
        return get(childIndex).asByte();
    }

    /**
     * Преобразование значения дочернего объекта к типу Byte.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Byte или null.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public @Nullable Byte getByteOrNull(int childIndex) {
        Variant child = getOrNull(childIndex);
        if (child == null) return null;
        return child.asByteOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Byte.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childIndex Индекс дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Byte или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Byte getByteOrDefault(int childIndex, Byte defaultValue) {
        Variant child = getOrNull(childIndex);
        if (child == null) return defaultValue;
        return child.asByteOrDefault(defaultValue);
    }

    /**
     * Преобразование значения дочернего объекта к типу Byte.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Byte.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public byte getByte(String childName) {
        return get(childName).asByte();
    }

    /**
     * Преобразование значения дочернего объекта к типу Byte.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Byte или null.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public @Nullable Byte getByteOrNull(String childName) {
        Variant child = getOrNull(childName);
        if (child == null) return null;
        return child.asByteOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Byte.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childName Имя дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Byte или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Byte getByteOrDefault(String childName, Byte defaultValue) {
        Variant child = getOrNull(childName);
        if (child == null) return defaultValue;
        return child.asByteOrDefault(defaultValue);
    }

    /**
     * Установка значения объекта с типом Byte.
     *
     * @param value Значение объекта с типом Byte
     * @return Этот же объект.
     */
    public Variant setByte(byte value) {
        set(value);
        return this;
    }

    /**
     * Установка дочернего объекта со значением с типом Byte.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Byte
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    public Variant setByte(int childIndex, byte value) {
        set(childIndex, value);
        return this;
    }

    /**
     * Вставка дочернего объекта со значением с типом Byte.
     * Если индекс меньше 0, объект будет добавлен в начало списка.
     * Если индекс больше или равен длине списка, объект будет добавлен в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Byte
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant insertByte(int childIndex, byte value) {
        insert(childIndex, value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Byte.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Byte
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addByte(byte value) {
        add(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Byte в начало списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Byte
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addFirstByte(byte value) {
        addFirst(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Byte в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Byte
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addLastByte(byte value) {
        addLast(value);
        return this;
    }

    /**
     * Установка значения дочернего объекта с типом Byte.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @param value Значение дочернего объекта с типом Byte
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Variant setByte(String childName, byte value) {
        set(childName, value);
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Byte.
     * Если этот объект не является списком или таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком или таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachByte(Consumer<Byte> handler) {
        forEach((child) -> { if (child.isByte()) handler.accept(child.asByte()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Byte в списке.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его индекса
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachByteInList(BiConsumer<Integer, Byte> handler) {
        forEachInList((childIndex, child) -> { if (child.isByte()) handler.accept(childIndex, child.asByte()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Byte в таблице.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его имени
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachByteInMap(BiConsumer<String, Byte> handler) {
        forEachInMap((childName, child) -> { if (child.isByte()) handler.accept(childName, child.asByte()); });
        return this;
    }

    /**
     * Создание объекта со значением типа Short.
     *
     * @param value Значение объекта
     * @return Объект со значением типа Short.
     */
    public static Variant newShort(short value) {
        return new Variant(value);
    }

    /**
     * Создание объекта со значением типа Short по умолчанию.
     *
     * @return Объект со значением типа Short по умолчанию.
     */
    public static Variant newShort() {
        return new Variant(0);
    }

    /**
     * Проверка, является ли значение этого объекта Short.
     *
     * @return Является ли значение этого объекта Short.
     */
    public boolean isShort() {
        return type == VariantType.NUMBER && ((Double) object).shortValue() == (Double) object;
    }

    /**
     * Проверка типа значения дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isShort(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        return ((List<Variant>) object).get(childIndex).isShort();
    }

    /**
     * Проверка типа значения дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isShort(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) throw new VariantChildNameError(childName);
        return ((Map<String, Variant>) object).get(childName).isShort();
    }

    /**
     * Преобразование значения объекта к типу Short.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @return Значение объекта с типом Short.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public short asShort() {
        if (!isShort()) throw new VariantValueError(type, "short");
        return ((Double) object).shortValue();
    }

    /**
     * Преобразование значения объекта к типу Short.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @return Значение объекта с типом Short или null.
     */
    @SuppressWarnings("unchecked")
    public @Nullable Short asShortOrNull() {
        if (!isShort()) return null;
        return ((Double) object).shortValue();
    }

    /**
     * Преобразование значения объекта к типу Short.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param defaultValue Значение по умолчанию
     * @return Значение объекта с типом Short или значение по умолчанию.
     */
    @SuppressWarnings("unchecked")
    public Short asShortOrDefault(Short defaultValue) {
        if (!isShort()) return defaultValue;
        return ((Double) object).shortValue();
    }

    /**
     * Преобразование значения дочернего объекта к типу Short.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Short.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public short getShort(int childIndex) {
        return get(childIndex).asShort();
    }

    /**
     * Преобразование значения дочернего объекта к типу Short.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Short или null.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public @Nullable Short getShortOrNull(int childIndex) {
        Variant child = getOrNull(childIndex);
        if (child == null) return null;
        return child.asShortOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Short.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childIndex Индекс дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Short или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Short getShortOrDefault(int childIndex, Short defaultValue) {
        Variant child = getOrNull(childIndex);
        if (child == null) return defaultValue;
        return child.asShortOrDefault(defaultValue);
    }

    /**
     * Преобразование значения дочернего объекта к типу Short.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Short.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public short getShort(String childName) {
        return get(childName).asShort();
    }

    /**
     * Преобразование значения дочернего объекта к типу Short.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Short или null.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public @Nullable Short getShortOrNull(String childName) {
        Variant child = getOrNull(childName);
        if (child == null) return null;
        return child.asShortOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Short.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childName Имя дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Short или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Short getShortOrDefault(String childName, Short defaultValue) {
        Variant child = getOrNull(childName);
        if (child == null) return defaultValue;
        return child.asShortOrDefault(defaultValue);
    }

    /**
     * Установка значения объекта с типом Short.
     *
     * @param value Значение объекта с типом Short
     * @return Этот же объект.
     */
    public Variant setShort(short value) {
        set(value);
        return this;
    }

    /**
     * Установка дочернего объекта со значением с типом Short.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Short
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    public Variant setShort(int childIndex, short value) {
        set(childIndex, value);
        return this;
    }

    /**
     * Вставка дочернего объекта со значением с типом Short.
     * Если индекс меньше 0, объект будет добавлен в начало списка.
     * Если индекс больше или равен длине списка, объект будет добавлен в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Short
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant insertShort(int childIndex, short value) {
        insert(childIndex, value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Short.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Short
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addShort(short value) {
        add(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Short в начало списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Short
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addFirstShort(short value) {
        addFirst(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Short в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Short
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addLastShort(short value) {
        addLast(value);
        return this;
    }

    /**
     * Установка значения дочернего объекта с типом Short.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @param value Значение дочернего объекта с типом Short
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Variant setShort(String childName, short value) {
        set(childName, value);
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Short.
     * Если этот объект не является списком или таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком или таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachShort(Consumer<Short> handler) {
        forEach((child) -> { if (child.isShort()) handler.accept(child.asShort()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Short в списке.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его индекса
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachShortInList(BiConsumer<Integer, Short> handler) {
        forEachInList((childIndex, child) -> { if (child.isShort()) handler.accept(childIndex, child.asShort()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Short в таблице.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его имени
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachShortInMap(BiConsumer<String, Short> handler) {
        forEachInMap((childName, child) -> { if (child.isShort()) handler.accept(childName, child.asShort()); });
        return this;
    }

    /**
     * Создание объекта со значением типа Integer.
     *
     * @param value Значение объекта
     * @return Объект со значением типа Integer.
     */
    public static Variant newInteger(int value) {
        return new Variant(value);
    }

    /**
     * Создание объекта со значением типа Integer по умолчанию.
     *
     * @return Объект со значением типа Integer по умолчанию.
     */
    public static Variant newInteger() {
        return new Variant(0);
    }

    /**
     * Проверка, является ли значение этого объекта Integer.
     *
     * @return Является ли значение этого объекта Integer.
     */
    public boolean isInteger() {
        return type == VariantType.NUMBER && ((Double) object).intValue() == (Double) object;
    }

    /**
     * Проверка типа значения дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isInteger(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        return ((List<Variant>) object).get(childIndex).isInteger();
    }

    /**
     * Проверка типа значения дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isInteger(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) throw new VariantChildNameError(childName);
        return ((Map<String, Variant>) object).get(childName).isInteger();
    }

    /**
     * Преобразование значения объекта к типу Integer.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @return Значение объекта с типом Integer.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public int asInteger() {
        if (!isInteger()) throw new VariantValueError(type, "int");
        return ((Double) object).intValue();
    }

    /**
     * Преобразование значения объекта к типу Integer.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @return Значение объекта с типом Integer или null.
     */
    @SuppressWarnings("unchecked")
    public @Nullable Integer asIntegerOrNull() {
        if (!isInteger()) return null;
        return ((Double) object).intValue();
    }

    /**
     * Преобразование значения объекта к типу Integer.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param defaultValue Значение по умолчанию
     * @return Значение объекта с типом Integer или значение по умолчанию.
     */
    @SuppressWarnings("unchecked")
    public Integer asIntegerOrDefault(Integer defaultValue) {
        if (!isInteger()) return defaultValue;
        return ((Double) object).intValue();
    }

    /**
     * Преобразование значения дочернего объекта к типу Integer.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Integer.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public int getInteger(int childIndex) {
        return get(childIndex).asInteger();
    }

    /**
     * Преобразование значения дочернего объекта к типу Integer.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Integer или null.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public @Nullable Integer getIntegerOrNull(int childIndex) {
        Variant child = getOrNull(childIndex);
        if (child == null) return null;
        return child.asIntegerOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Integer.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childIndex Индекс дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Integer или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Integer getIntegerOrDefault(int childIndex, Integer defaultValue) {
        Variant child = getOrNull(childIndex);
        if (child == null) return defaultValue;
        return child.asIntegerOrDefault(defaultValue);
    }

    /**
     * Преобразование значения дочернего объекта к типу Integer.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Integer.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public int getInteger(String childName) {
        return get(childName).asInteger();
    }

    /**
     * Преобразование значения дочернего объекта к типу Integer.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Integer или null.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public @Nullable Integer getIntegerOrNull(String childName) {
        Variant child = getOrNull(childName);
        if (child == null) return null;
        return child.asIntegerOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Integer.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childName Имя дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Integer или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Integer getIntegerOrDefault(String childName, Integer defaultValue) {
        Variant child = getOrNull(childName);
        if (child == null) return defaultValue;
        return child.asIntegerOrDefault(defaultValue);
    }

    /**
     * Установка значения объекта с типом Integer.
     *
     * @param value Значение объекта с типом Integer
     * @return Этот же объект.
     */
    public Variant setInteger(int value) {
        set(value);
        return this;
    }

    /**
     * Установка дочернего объекта со значением с типом Integer.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Integer
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    public Variant setInteger(int childIndex, int value) {
        set(childIndex, value);
        return this;
    }

    /**
     * Вставка дочернего объекта со значением с типом Integer.
     * Если индекс меньше 0, объект будет добавлен в начало списка.
     * Если индекс больше или равен длине списка, объект будет добавлен в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Integer
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant insertInteger(int childIndex, int value) {
        insert(childIndex, value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Integer.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Integer
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addInteger(int value) {
        add(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Integer в начало списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Integer
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addFirstInteger(int value) {
        addFirst(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Integer в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Integer
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addLastInteger(int value) {
        addLast(value);
        return this;
    }

    /**
     * Установка значения дочернего объекта с типом Integer.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @param value Значение дочернего объекта с типом Integer
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Variant setInteger(String childName, int value) {
        set(childName, value);
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Integer.
     * Если этот объект не является списком или таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком или таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachInteger(Consumer<Integer> handler) {
        forEach((child) -> { if (child.isInteger()) handler.accept(child.asInteger()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Integer в списке.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его индекса
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachIntegerInList(BiConsumer<Integer, Integer> handler) {
        forEachInList((childIndex, child) -> { if (child.isInteger()) handler.accept(childIndex, child.asInteger()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Integer в таблице.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его имени
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachIntegerInMap(BiConsumer<String, Integer> handler) {
        forEachInMap((childName, child) -> { if (child.isInteger()) handler.accept(childName, child.asInteger()); });
        return this;
    }

    /**
     * Создание объекта со значением типа Long.
     *
     * @param value Значение объекта
     * @return Объект со значением типа Long.
     */
    public static Variant newLong(long value) {
        return new Variant(value);
    }

    /**
     * Создание объекта со значением типа Long по умолчанию.
     *
     * @return Объект со значением типа Long по умолчанию.
     */
    public static Variant newLong() {
        return new Variant(0);
    }

    /**
     * Проверка, является ли значение этого объекта Long.
     *
     * @return Является ли значение этого объекта Long.
     */
    public boolean isLong() {
        return type == VariantType.NUMBER && ((Double) object).longValue() == (Double) object;
    }

    /**
     * Проверка типа значения дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isLong(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        return ((List<Variant>) object).get(childIndex).isLong();
    }

    /**
     * Проверка типа значения дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isLong(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) throw new VariantChildNameError(childName);
        return ((Map<String, Variant>) object).get(childName).isLong();
    }

    /**
     * Преобразование значения объекта к типу Long.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @return Значение объекта с типом Long.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public long asLong() {
        if (!isLong()) throw new VariantValueError(type, "long");
        return ((Double) object).longValue();
    }

    /**
     * Преобразование значения объекта к типу Long.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @return Значение объекта с типом Long или null.
     */
    @SuppressWarnings("unchecked")
    public @Nullable Long asLongOrNull() {
        if (!isLong()) return null;
        return ((Double) object).longValue();
    }

    /**
     * Преобразование значения объекта к типу Long.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param defaultValue Значение по умолчанию
     * @return Значение объекта с типом Long или значение по умолчанию.
     */
    @SuppressWarnings("unchecked")
    public Long asLongOrDefault(Long defaultValue) {
        if (!isLong()) return defaultValue;
        return ((Double) object).longValue();
    }

    /**
     * Преобразование значения дочернего объекта к типу Long.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Long.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public long getLong(int childIndex) {
        return get(childIndex).asLong();
    }

    /**
     * Преобразование значения дочернего объекта к типу Long.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Long или null.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public @Nullable Long getLongOrNull(int childIndex) {
        Variant child = getOrNull(childIndex);
        if (child == null) return null;
        return child.asLongOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Long.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childIndex Индекс дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Long или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Long getLongOrDefault(int childIndex, Long defaultValue) {
        Variant child = getOrNull(childIndex);
        if (child == null) return defaultValue;
        return child.asLongOrDefault(defaultValue);
    }

    /**
     * Преобразование значения дочернего объекта к типу Long.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Long.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public long getLong(String childName) {
        return get(childName).asLong();
    }

    /**
     * Преобразование значения дочернего объекта к типу Long.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Long или null.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public @Nullable Long getLongOrNull(String childName) {
        Variant child = getOrNull(childName);
        if (child == null) return null;
        return child.asLongOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Long.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childName Имя дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Long или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Long getLongOrDefault(String childName, Long defaultValue) {
        Variant child = getOrNull(childName);
        if (child == null) return defaultValue;
        return child.asLongOrDefault(defaultValue);
    }

    /**
     * Установка значения объекта с типом Long.
     *
     * @param value Значение объекта с типом Long
     * @return Этот же объект.
     */
    public Variant setLong(long value) {
        set(value);
        return this;
    }

    /**
     * Установка дочернего объекта со значением с типом Long.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Long
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    public Variant setLong(int childIndex, long value) {
        set(childIndex, value);
        return this;
    }

    /**
     * Вставка дочернего объекта со значением с типом Long.
     * Если индекс меньше 0, объект будет добавлен в начало списка.
     * Если индекс больше или равен длине списка, объект будет добавлен в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Long
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant insertLong(int childIndex, long value) {
        insert(childIndex, value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Long.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Long
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addLong(long value) {
        add(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Long в начало списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Long
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addFirstLong(long value) {
        addFirst(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Long в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Long
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addLastLong(long value) {
        addLast(value);
        return this;
    }

    /**
     * Установка значения дочернего объекта с типом Long.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @param value Значение дочернего объекта с типом Long
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Variant setLong(String childName, long value) {
        set(childName, value);
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Long.
     * Если этот объект не является списком или таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком или таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachLong(Consumer<Long> handler) {
        forEach((child) -> { if (child.isLong()) handler.accept(child.asLong()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Long в списке.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его индекса
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachLongInList(BiConsumer<Integer, Long> handler) {
        forEachInList((childIndex, child) -> { if (child.isLong()) handler.accept(childIndex, child.asLong()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Long в таблице.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его имени
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachLongInMap(BiConsumer<String, Long> handler) {
        forEachInMap((childName, child) -> { if (child.isLong()) handler.accept(childName, child.asLong()); });
        return this;
    }

    /**
     * Создание объекта со значением типа Float.
     *
     * @param value Значение объекта
     * @return Объект со значением типа Float.
     */
    public static Variant newFloat(float value) {
        return new Variant(value);
    }

    /**
     * Создание объекта со значением типа Float по умолчанию.
     *
     * @return Объект со значением типа Float по умолчанию.
     */
    public static Variant newFloat() {
        return new Variant(0);
    }

    /**
     * Проверка, является ли значение этого объекта Float.
     *
     * @return Является ли значение этого объекта Float.
     */
    public boolean isFloat() {
        return type == VariantType.NUMBER && ((Double) object).floatValue() == (Double) object;
    }

    /**
     * Проверка типа значения дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isFloat(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        return ((List<Variant>) object).get(childIndex).isFloat();
    }

    /**
     * Проверка типа значения дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isFloat(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) throw new VariantChildNameError(childName);
        return ((Map<String, Variant>) object).get(childName).isFloat();
    }

    /**
     * Преобразование значения объекта к типу Float.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @return Значение объекта с типом Float.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public float asFloat() {
        if (!isFloat()) throw new VariantValueError(type, "float");
        return ((Double) object).floatValue();
    }

    /**
     * Преобразование значения объекта к типу Float.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @return Значение объекта с типом Float или null.
     */
    @SuppressWarnings("unchecked")
    public @Nullable Float asFloatOrNull() {
        if (!isFloat()) return null;
        return ((Double) object).floatValue();
    }

    /**
     * Преобразование значения объекта к типу Float.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param defaultValue Значение по умолчанию
     * @return Значение объекта с типом Float или значение по умолчанию.
     */
    @SuppressWarnings("unchecked")
    public Float asFloatOrDefault(Float defaultValue) {
        if (!isFloat()) return defaultValue;
        return ((Double) object).floatValue();
    }

    /**
     * Преобразование значения дочернего объекта к типу Float.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Float.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public float getFloat(int childIndex) {
        return get(childIndex).asFloat();
    }

    /**
     * Преобразование значения дочернего объекта к типу Float.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Float или null.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public @Nullable Float getFloatOrNull(int childIndex) {
        Variant child = getOrNull(childIndex);
        if (child == null) return null;
        return child.asFloatOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Float.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childIndex Индекс дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Float или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Float getFloatOrDefault(int childIndex, Float defaultValue) {
        Variant child = getOrNull(childIndex);
        if (child == null) return defaultValue;
        return child.asFloatOrDefault(defaultValue);
    }

    /**
     * Преобразование значения дочернего объекта к типу Float.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Float.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public float getFloat(String childName) {
        return get(childName).asFloat();
    }

    /**
     * Преобразование значения дочернего объекта к типу Float.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Float или null.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public @Nullable Float getFloatOrNull(String childName) {
        Variant child = getOrNull(childName);
        if (child == null) return null;
        return child.asFloatOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Float.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childName Имя дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Float или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Float getFloatOrDefault(String childName, Float defaultValue) {
        Variant child = getOrNull(childName);
        if (child == null) return defaultValue;
        return child.asFloatOrDefault(defaultValue);
    }

    /**
     * Установка значения объекта с типом Float.
     *
     * @param value Значение объекта с типом Float
     * @return Этот же объект.
     */
    public Variant setFloat(float value) {
        set(value);
        return this;
    }

    /**
     * Установка дочернего объекта со значением с типом Float.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Float
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    public Variant setFloat(int childIndex, float value) {
        set(childIndex, value);
        return this;
    }

    /**
     * Вставка дочернего объекта со значением с типом Float.
     * Если индекс меньше 0, объект будет добавлен в начало списка.
     * Если индекс больше или равен длине списка, объект будет добавлен в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Float
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant insertFloat(int childIndex, float value) {
        insert(childIndex, value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Float.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Float
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addFloat(float value) {
        add(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Float в начало списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Float
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addFirstFloat(float value) {
        addFirst(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Float в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Float
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addLastFloat(float value) {
        addLast(value);
        return this;
    }

    /**
     * Установка значения дочернего объекта с типом Float.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @param value Значение дочернего объекта с типом Float
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Variant setFloat(String childName, float value) {
        set(childName, value);
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Float.
     * Если этот объект не является списком или таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком или таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachFloat(Consumer<Float> handler) {
        forEach((child) -> { if (child.isFloat()) handler.accept(child.asFloat()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Float в списке.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его индекса
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachFloatInList(BiConsumer<Integer, Float> handler) {
        forEachInList((childIndex, child) -> { if (child.isFloat()) handler.accept(childIndex, child.asFloat()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Float в таблице.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его имени
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachFloatInMap(BiConsumer<String, Float> handler) {
        forEachInMap((childName, child) -> { if (child.isFloat()) handler.accept(childName, child.asFloat()); });
        return this;
    }

    /**
     * Создание объекта со значением типа Double.
     *
     * @param value Значение объекта
     * @return Объект со значением типа Double.
     */
    public static Variant newDouble(double value) {
        return new Variant(value);
    }

    /**
     * Создание объекта со значением типа Double по умолчанию.
     *
     * @return Объект со значением типа Double по умолчанию.
     */
    public static Variant newDouble() {
        return new Variant(0);
    }

    /**
     * Проверка, является ли значение этого объекта Double.
     *
     * @return Является ли значение этого объекта Double.
     */
    public boolean isDouble() {
        return type == VariantType.NUMBER;
    }

    /**
     * Проверка типа значения дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isDouble(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        return ((List<Variant>) object).get(childIndex).isDouble();
    }

    /**
     * Проверка типа значения дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isDouble(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) throw new VariantChildNameError(childName);
        return ((Map<String, Variant>) object).get(childName).isDouble();
    }

    /**
     * Преобразование значения объекта к типу Double.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @return Значение объекта с типом Double.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public double asDouble() {
        if (!isDouble()) throw new VariantValueError(type, "double");
        return (Double) object;
    }

    /**
     * Преобразование значения объекта к типу Double.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @return Значение объекта с типом Double или null.
     */
    @SuppressWarnings("unchecked")
    public @Nullable Double asDoubleOrNull() {
        if (!isDouble()) return null;
        return (Double) object;
    }

    /**
     * Преобразование значения объекта к типу Double.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param defaultValue Значение по умолчанию
     * @return Значение объекта с типом Double или значение по умолчанию.
     */
    @SuppressWarnings("unchecked")
    public Double asDoubleOrDefault(Double defaultValue) {
        if (!isDouble()) return defaultValue;
        return (Double) object;
    }

    /**
     * Преобразование значения дочернего объекта к типу Double.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Double.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public double getDouble(int childIndex) {
        return get(childIndex).asDouble();
    }

    /**
     * Преобразование значения дочернего объекта к типу Double.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Double или null.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public @Nullable Double getDoubleOrNull(int childIndex) {
        Variant child = getOrNull(childIndex);
        if (child == null) return null;
        return child.asDoubleOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Double.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childIndex Индекс дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Double или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Double getDoubleOrDefault(int childIndex, Double defaultValue) {
        Variant child = getOrNull(childIndex);
        if (child == null) return defaultValue;
        return child.asDoubleOrDefault(defaultValue);
    }

    /**
     * Преобразование значения дочернего объекта к типу Double.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Double.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public double getDouble(String childName) {
        return get(childName).asDouble();
    }

    /**
     * Преобразование значения дочернего объекта к типу Double.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Double или null.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public @Nullable Double getDoubleOrNull(String childName) {
        Variant child = getOrNull(childName);
        if (child == null) return null;
        return child.asDoubleOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Double.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childName Имя дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Double или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Double getDoubleOrDefault(String childName, Double defaultValue) {
        Variant child = getOrNull(childName);
        if (child == null) return defaultValue;
        return child.asDoubleOrDefault(defaultValue);
    }

    /**
     * Установка значения объекта с типом Double.
     *
     * @param value Значение объекта с типом Double
     * @return Этот же объект.
     */
    public Variant setDouble(double value) {
        set(value);
        return this;
    }

    /**
     * Установка дочернего объекта со значением с типом Double.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Double
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    public Variant setDouble(int childIndex, double value) {
        set(childIndex, value);
        return this;
    }

    /**
     * Вставка дочернего объекта со значением с типом Double.
     * Если индекс меньше 0, объект будет добавлен в начало списка.
     * Если индекс больше или равен длине списка, объект будет добавлен в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Double
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant insertDouble(int childIndex, double value) {
        insert(childIndex, value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Double.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Double
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addDouble(double value) {
        add(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Double в начало списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Double
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addFirstDouble(double value) {
        addFirst(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Double в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Double
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addLastDouble(double value) {
        addLast(value);
        return this;
    }

    /**
     * Установка значения дочернего объекта с типом Double.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @param value Значение дочернего объекта с типом Double
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Variant setDouble(String childName, double value) {
        set(childName, value);
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Double.
     * Если этот объект не является списком или таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком или таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachDouble(Consumer<Double> handler) {
        forEach((child) -> { if (child.isDouble()) handler.accept(child.asDouble()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Double в списке.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его индекса
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachDoubleInList(BiConsumer<Integer, Double> handler) {
        forEachInList((childIndex, child) -> { if (child.isDouble()) handler.accept(childIndex, child.asDouble()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Double в таблице.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его имени
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachDoubleInMap(BiConsumer<String, Double> handler) {
        forEachInMap((childName, child) -> { if (child.isDouble()) handler.accept(childName, child.asDouble()); });
        return this;
    }

    /**
     * Создание объекта со значением типа Character.
     *
     * @param value Значение объекта
     * @return Объект со значением типа Character.
     */
    public static Variant newCharacter(char value) {
        return new Variant(value);
    }

    /**
     * Создание объекта со значением типа Character по умолчанию.
     *
     * @return Объект со значением типа Character по умолчанию.
     */
    public static Variant newCharacter() {
        return new Variant(' ');
    }

    /**
     * Проверка, является ли значение этого объекта Character.
     *
     * @return Является ли значение этого объекта Character.
     */
    public boolean isCharacter() {
        return type == VariantType.STRING && ((String) object).length() == 1;
    }

    /**
     * Проверка типа значения дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isCharacter(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        return ((List<Variant>) object).get(childIndex).isCharacter();
    }

    /**
     * Проверка типа значения дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isCharacter(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) throw new VariantChildNameError(childName);
        return ((Map<String, Variant>) object).get(childName).isCharacter();
    }

    /**
     * Преобразование значения объекта к типу Character.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @return Значение объекта с типом Character.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public char asCharacter() {
        if (!isCharacter()) throw new VariantValueError(type, "char");
        return ((String) object).charAt(0);
    }

    /**
     * Преобразование значения объекта к типу Character.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @return Значение объекта с типом Character или null.
     */
    @SuppressWarnings("unchecked")
    public @Nullable Character asCharacterOrNull() {
        if (!isCharacter()) return null;
        return ((String) object).charAt(0);
    }

    /**
     * Преобразование значения объекта к типу Character.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param defaultValue Значение по умолчанию
     * @return Значение объекта с типом Character или значение по умолчанию.
     */
    @SuppressWarnings("unchecked")
    public Character asCharacterOrDefault(Character defaultValue) {
        if (!isCharacter()) return defaultValue;
        return ((String) object).charAt(0);
    }

    /**
     * Преобразование значения дочернего объекта к типу Character.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Character.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public char getCharacter(int childIndex) {
        return get(childIndex).asCharacter();
    }

    /**
     * Преобразование значения дочернего объекта к типу Character.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Character или null.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public @Nullable Character getCharacterOrNull(int childIndex) {
        Variant child = getOrNull(childIndex);
        if (child == null) return null;
        return child.asCharacterOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Character.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childIndex Индекс дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Character или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Character getCharacterOrDefault(int childIndex, Character defaultValue) {
        Variant child = getOrNull(childIndex);
        if (child == null) return defaultValue;
        return child.asCharacterOrDefault(defaultValue);
    }

    /**
     * Преобразование значения дочернего объекта к типу Character.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Character.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public char getCharacter(String childName) {
        return get(childName).asCharacter();
    }

    /**
     * Преобразование значения дочернего объекта к типу Character.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Character или null.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public @Nullable Character getCharacterOrNull(String childName) {
        Variant child = getOrNull(childName);
        if (child == null) return null;
        return child.asCharacterOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Character.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childName Имя дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Character или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Character getCharacterOrDefault(String childName, Character defaultValue) {
        Variant child = getOrNull(childName);
        if (child == null) return defaultValue;
        return child.asCharacterOrDefault(defaultValue);
    }

    /**
     * Установка значения объекта с типом Character.
     *
     * @param value Значение объекта с типом Character
     * @return Этот же объект.
     */
    public Variant setCharacter(char value) {
        set(value);
        return this;
    }

    /**
     * Установка дочернего объекта со значением с типом Character.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Character
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    public Variant setCharacter(int childIndex, char value) {
        set(childIndex, value);
        return this;
    }

    /**
     * Вставка дочернего объекта со значением с типом Character.
     * Если индекс меньше 0, объект будет добавлен в начало списка.
     * Если индекс больше или равен длине списка, объект будет добавлен в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Character
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant insertCharacter(int childIndex, char value) {
        insert(childIndex, value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Character.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Character
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addCharacter(char value) {
        add(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Character в начало списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Character
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addFirstCharacter(char value) {
        addFirst(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Character в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Character
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addLastCharacter(char value) {
        addLast(value);
        return this;
    }

    /**
     * Установка значения дочернего объекта с типом Character.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @param value Значение дочернего объекта с типом Character
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Variant setCharacter(String childName, char value) {
        set(childName, value);
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Character.
     * Если этот объект не является списком или таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком или таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachCharacter(Consumer<Character> handler) {
        forEach((child) -> { if (child.isCharacter()) handler.accept(child.asCharacter()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Character в списке.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его индекса
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachCharacterInList(BiConsumer<Integer, Character> handler) {
        forEachInList((childIndex, child) -> { if (child.isCharacter()) handler.accept(childIndex, child.asCharacter()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Character в таблице.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его имени
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachCharacterInMap(BiConsumer<String, Character> handler) {
        forEachInMap((childName, child) -> { if (child.isCharacter()) handler.accept(childName, child.asCharacter()); });
        return this;
    }

    /**
     * Создание объекта со значением типа String.
     *
     * @param value Значение объекта
     * @return Объект со значением типа String.
     */
    public static Variant newString(String value) {
        return new Variant(value);
    }

    /**
     * Создание объекта со значением типа String по умолчанию.
     *
     * @return Объект со значением типа String по умолчанию.
     */
    public static Variant newString() {
        return new Variant("");
    }

    /**
     * Проверка, является ли значение этого объекта String.
     *
     * @return Является ли значение этого объекта String.
     */
    public boolean isString() {
        return type == VariantType.STRING;
    }

    /**
     * Проверка типа значения дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isString(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        return ((List<Variant>) object).get(childIndex).isString();
    }

    /**
     * Проверка типа значения дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isString(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) throw new VariantChildNameError(childName);
        return ((Map<String, Variant>) object).get(childName).isString();
    }

    /**
     * Преобразование значения объекта к типу String.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @return Значение объекта с типом String.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public String asString() {
        if (!isString()) throw new VariantValueError(type, "String");
        return (String) object;
    }

    /**
     * Преобразование значения объекта к типу String.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @return Значение объекта с типом String или null.
     */
    @SuppressWarnings("unchecked")
    public @Nullable String asStringOrNull() {
        if (!isString()) return null;
        return (String) object;
    }

    /**
     * Преобразование значения объекта к типу String.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param defaultValue Значение по умолчанию
     * @return Значение объекта с типом String или значение по умолчанию.
     */
    @SuppressWarnings("unchecked")
    public String asStringOrDefault(String defaultValue) {
        if (!isString()) return defaultValue;
        return (String) object;
    }

    /**
     * Преобразование значения дочернего объекта к типу String.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом String.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public String getString(int childIndex) {
        return get(childIndex).asString();
    }

    /**
     * Преобразование значения дочернего объекта к типу String.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом String или null.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public @Nullable String getStringOrNull(int childIndex) {
        Variant child = getOrNull(childIndex);
        if (child == null) return null;
        return child.asStringOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу String.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childIndex Индекс дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом String или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public String getStringOrDefault(int childIndex, String defaultValue) {
        Variant child = getOrNull(childIndex);
        if (child == null) return defaultValue;
        return child.asStringOrDefault(defaultValue);
    }

    /**
     * Преобразование значения дочернего объекта к типу String.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом String.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public String getString(String childName) {
        return get(childName).asString();
    }

    /**
     * Преобразование значения дочернего объекта к типу String.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом String или null.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public @Nullable String getStringOrNull(String childName) {
        Variant child = getOrNull(childName);
        if (child == null) return null;
        return child.asStringOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу String.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childName Имя дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом String или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public String getStringOrDefault(String childName, String defaultValue) {
        Variant child = getOrNull(childName);
        if (child == null) return defaultValue;
        return child.asStringOrDefault(defaultValue);
    }

    /**
     * Установка значения объекта с типом String.
     *
     * @param value Значение объекта с типом String
     * @return Этот же объект.
     */
    public Variant setString(String value) {
        set(value);
        return this;
    }

    /**
     * Установка дочернего объекта со значением с типом String.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом String
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    public Variant setString(int childIndex, String value) {
        set(childIndex, value);
        return this;
    }

    /**
     * Вставка дочернего объекта со значением с типом String.
     * Если индекс меньше 0, объект будет добавлен в начало списка.
     * Если индекс больше или равен длине списка, объект будет добавлен в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом String
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant insertString(int childIndex, String value) {
        insert(childIndex, value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом String.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом String
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addString(String value) {
        add(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом String в начало списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом String
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addFirstString(String value) {
        addFirst(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом String в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом String
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addLastString(String value) {
        addLast(value);
        return this;
    }

    /**
     * Установка значения дочернего объекта с типом String.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @param value Значение дочернего объекта с типом String
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Variant setString(String childName, String value) {
        set(childName, value);
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом String.
     * Если этот объект не является списком или таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком или таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachString(Consumer<String> handler) {
        forEach((child) -> { if (child.isString()) handler.accept(child.asString()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом String в списке.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его индекса
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachStringInList(BiConsumer<Integer, String> handler) {
        forEachInList((childIndex, child) -> { if (child.isString()) handler.accept(childIndex, child.asString()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом String в таблице.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его имени
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachStringInMap(BiConsumer<String, String> handler) {
        forEachInMap((childName, child) -> { if (child.isString()) handler.accept(childName, child.asString()); });
        return this;
    }

    /**
     * Создание объекта со значением типа List.
     *
     * @param value Значение объекта
     * @return Объект со значением типа List.
     */
    public static Variant newList(List<Variant> value) {
        return new Variant(value);
    }

    /**
     * Создание объекта со значением типа List по умолчанию.
     *
     * @return Объект со значением типа List по умолчанию.
     */
    public static Variant newList() {
        return new Variant(new ArrayList<>());
    }

    /**
     * Проверка, является ли значение этого объекта List.
     *
     * @return Является ли значение этого объекта List.
     */
    public boolean isList() {
        return type == VariantType.LIST;
    }

    /**
     * Проверка типа значения дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isList(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        return ((List<Variant>) object).get(childIndex).isList();
    }

    /**
     * Проверка типа значения дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isList(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) throw new VariantChildNameError(childName);
        return ((Map<String, Variant>) object).get(childName).isList();
    }

    /**
     * Преобразование значения объекта к типу List.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @return Значение объекта с типом List.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public List<Variant> asList() {
        if (!isList()) throw new VariantValueError(type, "List<Variant>");
        return (List<Variant>) object;
    }

    /**
     * Преобразование значения объекта к типу List.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @return Значение объекта с типом List или null.
     */
    @SuppressWarnings("unchecked")
    public @Nullable List<Variant> asListOrNull() {
        if (!isList()) return null;
        return (List<Variant>) object;
    }

    /**
     * Преобразование значения объекта к типу List.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param defaultValue Значение по умолчанию
     * @return Значение объекта с типом List или значение по умолчанию.
     */
    @SuppressWarnings("unchecked")
    public List<Variant> asListOrDefault(List<Variant> defaultValue) {
        if (!isList()) return defaultValue;
        return (List<Variant>) object;
    }

    /**
     * Преобразование значения дочернего объекта к типу List.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом List.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public List<Variant> getList(int childIndex) {
        return get(childIndex).asList();
    }

    /**
     * Преобразование значения дочернего объекта к типу List.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом List или null.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public @Nullable List<Variant> getListOrNull(int childIndex) {
        Variant child = getOrNull(childIndex);
        if (child == null) return null;
        return child.asListOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу List.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childIndex Индекс дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом List или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public List<Variant> getListOrDefault(int childIndex, List<Variant> defaultValue) {
        Variant child = getOrNull(childIndex);
        if (child == null) return defaultValue;
        return child.asListOrDefault(defaultValue);
    }

    /**
     * Преобразование значения дочернего объекта к типу List.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом List.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public List<Variant> getList(String childName) {
        return get(childName).asList();
    }

    /**
     * Преобразование значения дочернего объекта к типу List.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом List или null.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public @Nullable List<Variant> getListOrNull(String childName) {
        Variant child = getOrNull(childName);
        if (child == null) return null;
        return child.asListOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу List.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childName Имя дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом List или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public List<Variant> getListOrDefault(String childName, List<Variant> defaultValue) {
        Variant child = getOrNull(childName);
        if (child == null) return defaultValue;
        return child.asListOrDefault(defaultValue);
    }

    /**
     * Установка значения объекта с типом List.
     *
     * @param value Значение объекта с типом List
     * @return Этот же объект.
     */
    public Variant setList(List<Variant> value) {
        set(value);
        return this;
    }

    /**
     * Установка дочернего объекта со значением с типом List.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом List
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    public Variant setList(int childIndex, List<Variant> value) {
        set(childIndex, value);
        return this;
    }

    /**
     * Вставка дочернего объекта со значением с типом List.
     * Если индекс меньше 0, объект будет добавлен в начало списка.
     * Если индекс больше или равен длине списка, объект будет добавлен в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом List
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant insertList(int childIndex, List<Variant> value) {
        insert(childIndex, value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом List.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом List
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addList(List<Variant> value) {
        add(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом List в начало списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом List
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addFirstList(List<Variant> value) {
        addFirst(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом List в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом List
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addLastList(List<Variant> value) {
        addLast(value);
        return this;
    }

    /**
     * Установка значения дочернего объекта с типом List.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @param value Значение дочернего объекта с типом List
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Variant setList(String childName, List<Variant> value) {
        set(childName, value);
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом List.
     * Если этот объект не является списком или таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком или таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachList(Consumer<List<Variant>> handler) {
        forEach((child) -> { if (child.isList()) handler.accept(child.asList()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом List в списке.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его индекса
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachListInList(BiConsumer<Integer, List<Variant>> handler) {
        forEachInList((childIndex, child) -> { if (child.isList()) handler.accept(childIndex, child.asList()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом List в таблице.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его имени
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachListInMap(BiConsumer<String, List<Variant>> handler) {
        forEachInMap((childName, child) -> { if (child.isList()) handler.accept(childName, child.asList()); });
        return this;
    }

    /**
     * Создание объекта со значением типа Map.
     *
     * @param value Значение объекта
     * @return Объект со значением типа Map.
     */
    public static Variant newMap(Map<String, Variant> value) {
        return new Variant(value);
    }

    /**
     * Создание объекта со значением типа Map по умолчанию.
     *
     * @return Объект со значением типа Map по умолчанию.
     */
    public static Variant newMap() {
        return new Variant(new HashMap<>());
    }

    /**
     * Проверка, является ли значение этого объекта Map.
     *
     * @return Является ли значение этого объекта Map.
     */
    public boolean isMap() {
        return type == VariantType.MAP;
    }

    /**
     * Проверка типа значения дочернего объекта по индексу.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isMap(int childIndex) {
        if (type != VariantType.LIST) throw new VariantTypeError(type, VariantType.LIST);
        if (childIndex < 0 || childIndex >= ((List<?>) object).size()) throw new VariantChildIndexError(childIndex, ((List<?>) object).size());
        return ((List<Variant>) object).get(childIndex).isMap();
    }

    /**
     * Проверка типа значения дочернего объекта по имени.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Соответствие типа значения.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public boolean isMap(String childName) {
        if (type != VariantType.MAP) throw new VariantTypeError(type, VariantType.MAP);
        if (!((Map<?, ?>) object).containsKey(childName)) throw new VariantChildNameError(childName);
        return ((Map<String, Variant>) object).get(childName).isMap();
    }

    /**
     * Преобразование значения объекта к типу Map.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @return Значение объекта с типом Map.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Variant> asMap() {
        if (!isMap()) throw new VariantValueError(type, "Map<String, Variant>");
        return (Map<String, Variant>) object;
    }

    /**
     * Преобразование значения объекта к типу Map.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @return Значение объекта с типом Map или null.
     */
    @SuppressWarnings("unchecked")
    public @Nullable Map<String, Variant> asMapOrNull() {
        if (!isMap()) return null;
        return (Map<String, Variant>) object;
    }

    /**
     * Преобразование значения объекта к типу Map.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param defaultValue Значение по умолчанию
     * @return Значение объекта с типом Map или значение по умолчанию.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Variant> asMapOrDefault(Map<String, Variant> defaultValue) {
        if (!isMap()) return defaultValue;
        return (Map<String, Variant>) object;
    }

    /**
     * Преобразование значения дочернего объекта к типу Map.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Map.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public Map<String, Variant> getMap(int childIndex) {
        return get(childIndex).asMap();
    }

    /**
     * Преобразование значения дочернего объекта к типу Map.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childIndex Индекс дочернего объекта
     * @return Значение дочернего объекта с типом Map или null.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public @Nullable Map<String, Variant> getMapOrNull(int childIndex) {
        Variant child = getOrNull(childIndex);
        if (child == null) return null;
        return child.asMapOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Map.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childIndex Индекс дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Map или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Map<String, Variant> getMapOrDefault(int childIndex, Map<String, Variant> defaultValue) {
        Variant child = getOrNull(childIndex);
        if (child == null) return defaultValue;
        return child.asMapOrDefault(defaultValue);
    }

    /**
     * Преобразование значения дочернего объекта к типу Map.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Map.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     * @throws VariantChildIndexError Если в таблице нет объекта с указанным именем, будет вызвано исключение.
     * @throws VariantValueError Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.
     */
    public Map<String, Variant> getMap(String childName) {
        return get(childName).asMap();
    }

    /**
     * Преобразование значения дочернего объекта к типу Map.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено null.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено null.
     *
     * @param childName Имя дочернего объекта
     * @return Значение дочернего объекта с типом Map или null.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public @Nullable Map<String, Variant> getMapOrNull(String childName) {
        Variant child = getOrNull(childName);
        if (child == null) return null;
        return child.asMapOrNull();
    }

    /**
     * Преобразование значения дочернего объекта к типу Map.
     * Если этот объект не является таблицей, будет вызвано исключение.
     * Если в таблице нет объекта с указанным именем, будет возвращено значение по умолчанию.
     * Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.
     *
     * @param childName Имя дочернего объекта
     * @param defaultValue Значение по умолчанию
     * @return Значение дочернего объекта с типом Map или значение по умолчанию.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Map<String, Variant> getMapOrDefault(String childName, Map<String, Variant> defaultValue) {
        Variant child = getOrNull(childName);
        if (child == null) return defaultValue;
        return child.asMapOrDefault(defaultValue);
    }

    /**
     * Установка значения объекта с типом Map.
     *
     * @param value Значение объекта с типом Map
     * @return Этот же объект.
     */
    public Variant setMap(Map<String, Variant> value) {
        set(value);
        return this;
    }

    /**
     * Установка дочернего объекта со значением с типом Map.
     * Если этот объект не является списком, будет вызвано исключение.
     * Если индекс за границей списка, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Map
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     * @throws VariantChildIndexError Если индекс за границей списка, будет вызвано исключение.
     */
    public Variant setMap(int childIndex, Map<String, Variant> value) {
        set(childIndex, value);
        return this;
    }

    /**
     * Вставка дочернего объекта со значением с типом Map.
     * Если индекс меньше 0, объект будет добавлен в начало списка.
     * Если индекс больше или равен длине списка, объект будет добавлен в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param childIndex Индекс дочернего объекта
     * @param value Значение дочернего объекта с типом Map
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant insertMap(int childIndex, Map<String, Variant> value) {
        insert(childIndex, value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Map.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Map
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addMap(Map<String, Variant> value) {
        add(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Map в начало списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Map
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addFirstMap(Map<String, Variant> value) {
        addFirst(value);
        return this;
    }

    /**
     * Добавление дочернего объекта со значением с типом Map в конец списка.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param value Значение дочернего объекта с типом Map
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    public Variant addLastMap(Map<String, Variant> value) {
        addLast(value);
        return this;
    }

    /**
     * Установка значения дочернего объекта с типом Map.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param childName Имя дочернего объекта
     * @param value Значение дочернего объекта с типом Map
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    public Variant setMap(String childName, Map<String, Variant> value) {
        set(childName, value);
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Map.
     * Если этот объект не является списком или таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком или таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachMap(Consumer<Map<String, Variant>> handler) {
        forEach((child) -> { if (child.isMap()) handler.accept(child.asMap()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Map в списке.
     * Если этот объект не является списком, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его индекса
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является списком, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachMapInList(BiConsumer<Integer, Map<String, Variant>> handler) {
        forEachInList((childIndex, child) -> { if (child.isMap()) handler.accept(childIndex, child.asMap()); });
        return this;
    }

    /**
     * Обработка значения каждого дочернего объекта с типом Map в таблице.
     * Если этот объект не является таблицей, будет вызвано исключение.
     *
     * @param handler Обработчик значения дочернего объекта и его имени
     * @return Этот же объект.
     * @throws VariantTypeError Если этот объект не является таблицей, будет вызвано исключение.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Variant forEachMapInMap(BiConsumer<String, Map<String, Variant>> handler) {
        forEachInMap((childName, child) -> { if (child.isMap()) handler.accept(childName, child.asMap()); });
        return this;
    }
}