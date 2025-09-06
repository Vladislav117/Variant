package variantdevelopmentplugin;

import ru.vladislav117.javawriter.AccessModifier;
import ru.vladislav117.javawriter.Annotation;
import ru.vladislav117.javawriter.code.StatementBasedCode;
import ru.vladislav117.javawriter.field.Field;
import ru.vladislav117.javawriter.klass.Class;
import ru.vladislav117.javawriter.klass.ClassDocumentation;
import ru.vladislav117.javawriter.method.Argument;
import ru.vladislav117.javawriter.method.Method;
import ru.vladislav117.javawriter.method.MethodDocumentation;
import ru.vladislav117.javawriter.method.Return;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;


public class VariantClass extends Class {
    public static final class Annotations {
        static Annotation UNCHECKED = new Annotation("@SuppressWarnings(\"unchecked\")");
        static Annotation UNUSED_RETURN_VALUE = new Annotation("@SuppressWarnings(\"UnusedReturnValue\")");
        static Annotation UNCHECKED_AND_UNUSED_RETURN_VALUE = new Annotation("@SuppressWarnings({\"unchecked\", \"UnusedReturnValue\"})");
        static Annotation NULLABLE = new Annotation("@Nullable");
        static Annotation OVERRIDE = new Annotation("@Override");
    }

    public static final class Documentation {
        static String IF_NOT_LIST_THROW_DESCRIPTION = "Если этот объект не является списком, будет вызвано исключение.";
        static String IF_INDEX_OUT_OF_BOUNDS_THROW_ERROR_DESCRIPTION = "Если индекс за границей списка, будет вызвано исключение.";
        static String IF_INDEX_OUT_OF_BOUNDS_RETURN_NULL_DESCRIPTION = "Если индекс за границей списка, будет возвращено null.";
        static String IF_INDEX_OUT_OF_BOUNDS_RETURN_DEFAULT_DESCRIPTION = "Если индекс за границей списка, будет возвращено значение по умолчанию.";
        static String IF_NOT_MAP_THROW_DESCRIPTION = "Если этот объект не является таблицей, будет вызвано исключение.";
        static String IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_THROW_ERROR_DESCRIPTION = "Если в таблице нет объекта с указанным именем, будет вызвано исключение.";
        static String IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_RETURN_NULL_DESCRIPTION = "Если в таблице нет объекта с указанным именем, будет возвращено null.";
        static String IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_RETURN_DEFAULT_DESCRIPTION = "Если в таблице нет объекта с указанным именем, будет возвращено значение по умолчанию.";
        static String SET_VALUE_ERROR_DESCRIPTION = "Если тип устанавливаемого значения не поддерживается, будет вызвано исключение.";
        static String CONVERT_VALUE_THROW_ERROR_DESCRIPTION = "Если значение не может быть преобразовано к указанному типу, будет вызвано исключение.";
        static String CONVERT_VALUE_RETURN_NULL_DESCRIPTION = "Если значение не может быть преобразовано к указанному типу, будет возвращено null.";
        static String CONVERT_VALUE_RETURN_DEFAULT_DESCRIPTION = "Если значение не может быть преобразовано к указанному типу, будет возвращено значение по умолчанию.";
    }

    public static final class Statements {
        static StatementBasedCode.SimpleIfStatement IF_NOT_LIST_THROW_ERROR = new StatementBasedCode.SimpleIfStatement("type != VariantType.LIST", "throw new VariantTypeError(type, VariantType.LIST)");
        static StatementBasedCode.SimpleIfStatement IF_INDEX_OUT_OF_BOUNDS_THROW_ERROR = new StatementBasedCode.SimpleIfStatement("childIndex < 0 || childIndex >= ((List<?>) object).size()", "throw new VariantChildIndexError(childIndex, ((List<?>) object).size())");
        static StatementBasedCode.SimpleIfStatement IF_INDEX_OUT_OF_BOUNDS_RETURN_NULL = new StatementBasedCode.SimpleIfStatement("childIndex < 0 || childIndex >= ((List<?>) object).size()", "return null");
        static StatementBasedCode.SimpleIfStatement IF_INDEX_OUT_OF_BOUNDS_RETURN_DEFAULT = new StatementBasedCode.SimpleIfStatement("childIndex < 0 || childIndex >= ((List<?>) object).size()", "return defaultValue");
        static StatementBasedCode.SimpleIfStatement IF_NOT_MAP_THROW_ERROR = new StatementBasedCode.SimpleIfStatement("type != VariantType.MAP", "throw new VariantTypeError(type, VariantType.MAP)");
        static StatementBasedCode.SimpleIfStatement IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_THROW_ERROR = new StatementBasedCode.SimpleIfStatement("!((Map<?, ?>) object).containsKey(childName)", "throw new VariantChildNameError(childName)");
        static StatementBasedCode.SimpleIfStatement IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_RETURN_NULL = new StatementBasedCode.SimpleIfStatement("!((Map<?, ?>) object).containsKey(childName)", "return null");
        static StatementBasedCode.SimpleIfStatement IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_RETURN_DEFAULT = new StatementBasedCode.SimpleIfStatement("!((Map<?, ?>) object).containsKey(childName)", "return defaultValue");
        static StatementBasedCode.Statement RETURN_THIS = new StatementBasedCode.Statement("return this");

        static StatementBasedCode.SimpleIfStatement CONVERT_VALUE_THROW_ERROR(VariantDataType dataType) {
            return new StatementBasedCode.SimpleIfStatement("!is" + dataType.getName() + "()", "throw new VariantValueError(type, \"" + dataType.getPrimitive() + "\")");
        }

        static StatementBasedCode.SimpleIfStatement CONVERT_VALUE_RETURN_NULL(VariantDataType dataType) {
            return new StatementBasedCode.SimpleIfStatement("!is" + dataType.getName() + "()", "return null");
        }

        static StatementBasedCode.SimpleIfStatement CONVERT_VALUE_RETURN_DEFAULT(VariantDataType dataType) {
            return new StatementBasedCode.SimpleIfStatement("!is" + dataType.getName() + "()", "return defaultValue");
        }
    }

    public static final class Arguments {
        static Argument CHILD_INDEX = new Argument("int", "childIndex") {{
            setDescription("Индекс дочернего объекта");
        }};
        static Argument CHILD_NAME = new Argument("String", "childName") {{
            setDescription("Имя дочернего объекта");
        }};
        static Argument VARIANT_DEFAULT_VALUE = new Argument("Variant", "defaultValue") {{
            setDescription("Значение по умолчанию");
        }};
        static Argument OBJECT_VALUE = new Argument("Object", "value") {{
            setDescription("Значение объекта");
        }};
    }

    public static final class Returns {
        static Return THIS = new Return("Variant") {{
            setDescription("Этот же объект.");
        }};
    }

    public static final class ComplexStatements {
        static ComplexStatement IF_NOT_LIST_THROW_ERROR = new ComplexStatement(Statements.IF_NOT_LIST_THROW_ERROR) {{
            setDocumentationLines(new ArrayList<String>() {{
                add(Documentation.IF_NOT_LIST_THROW_DESCRIPTION);
            }});
            setThrowsDescriptions(new ArrayList<AbstractMap.SimpleEntry<String, String>>() {{
                add(new AbstractMap.SimpleEntry<>("VariantTypeError", Documentation.IF_NOT_LIST_THROW_DESCRIPTION));
            }});
        }};
        static ComplexStatement IF_INDEX_OUT_OF_BOUNDS_THROW_ERROR = new ComplexStatement(Statements.IF_INDEX_OUT_OF_BOUNDS_THROW_ERROR) {{
            setDocumentationLines(new ArrayList<String>() {{
                add(Documentation.IF_INDEX_OUT_OF_BOUNDS_THROW_ERROR_DESCRIPTION);
            }});
            setThrowsDescriptions(new ArrayList<AbstractMap.SimpleEntry<String, String>>() {{
                add(new AbstractMap.SimpleEntry<>("VariantChildIndexError", Documentation.IF_INDEX_OUT_OF_BOUNDS_THROW_ERROR_DESCRIPTION));
            }});
        }};
        static ComplexStatement IF_INDEX_OUT_OF_BOUNDS_RETURN_NULL = new ComplexStatement(Statements.IF_INDEX_OUT_OF_BOUNDS_RETURN_NULL) {{
            setDocumentationLines(new ArrayList<String>() {{
                add(Documentation.IF_INDEX_OUT_OF_BOUNDS_RETURN_NULL_DESCRIPTION);
            }});
        }};
        static ComplexStatement IF_INDEX_OUT_OF_BOUNDS_RETURN_DEFAULT = new ComplexStatement(Statements.IF_INDEX_OUT_OF_BOUNDS_RETURN_DEFAULT) {{
            setDocumentationLines(new ArrayList<String>() {{
                add(Documentation.IF_INDEX_OUT_OF_BOUNDS_RETURN_DEFAULT_DESCRIPTION);
            }});
        }};
        static ComplexStatement IF_NOT_MAP_THROW_ERROR = new ComplexStatement(Statements.IF_NOT_MAP_THROW_ERROR) {{
            setDocumentationLines(new ArrayList<String>() {{
                add(Documentation.IF_NOT_MAP_THROW_DESCRIPTION);
            }});
            setThrowsDescriptions(new ArrayList<AbstractMap.SimpleEntry<String, String>>() {{
                add(new AbstractMap.SimpleEntry<>("VariantTypeError", Documentation.IF_NOT_MAP_THROW_DESCRIPTION));
            }});
        }};
        static ComplexStatement IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_ERROR = new ComplexStatement(Statements.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_THROW_ERROR) {{
            setDocumentationLines(new ArrayList<String>() {{
                add(Documentation.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_THROW_ERROR_DESCRIPTION);
            }});
            setThrowsDescriptions(new ArrayList<AbstractMap.SimpleEntry<String, String>>() {{
                add(new AbstractMap.SimpleEntry<>("VariantChildIndexError", Documentation.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_THROW_ERROR_DESCRIPTION));
            }});
        }};
        static ComplexStatement IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_RETURN_NULL = new ComplexStatement(Statements.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_RETURN_NULL) {{
            setDocumentationLines(new ArrayList<String>() {{
                add(Documentation.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_RETURN_NULL_DESCRIPTION);
            }});
        }};
        static ComplexStatement IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_RETURN_DEFAULT = new ComplexStatement(Statements.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_RETURN_DEFAULT) {{
            setDocumentationLines(new ArrayList<String>() {{
                add(Documentation.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_RETURN_DEFAULT_DESCRIPTION);
            }});
        }};

        static ComplexStatement CONVERT_VALUE_THROW_ERROR(VariantDataType dataType) {
            return new ComplexStatement(Statements.CONVERT_VALUE_THROW_ERROR(dataType)) {{
                setDocumentationLines(new ArrayList<String>() {{
                    add(Documentation.CONVERT_VALUE_THROW_ERROR_DESCRIPTION);
                }});
                setThrowsDescriptions(new ArrayList<AbstractMap.SimpleEntry<String, String>>() {{
                    add(new AbstractMap.SimpleEntry<>("VariantValueError", Documentation.CONVERT_VALUE_THROW_ERROR_DESCRIPTION));
                }});
            }};
        }

        static ComplexStatement CONVERT_VALUE_RETURN_NULL(VariantDataType dataType) {
            return new ComplexStatement(Statements.CONVERT_VALUE_RETURN_NULL(dataType)) {{
                setDocumentationLines(new ArrayList<String>() {{
                    add(Documentation.CONVERT_VALUE_RETURN_NULL_DESCRIPTION);
                }});
            }};
        }

        static ComplexStatement CONVERT_VALUE_RETURN_DEFAULT(VariantDataType dataType) {
            return new ComplexStatement(Statements.CONVERT_VALUE_RETURN_DEFAULT(dataType)) {{
                setDocumentationLines(new ArrayList<String>() {{
                    add(Documentation.CONVERT_VALUE_RETURN_DEFAULT_DESCRIPTION);
                }});
            }};
        }
    }

    public static class VariantDataType {
        protected String name;
        protected String primitive;
        protected String klass;
        protected String isCode;
        protected String asCode;
        protected String defaultCode;

        public VariantDataType(String name, String primitive, String klass, String isCode, String asCode, String defaultCode) {
            this.name = name;
            this.primitive = primitive;
            this.klass = klass;
            this.isCode = isCode;
            this.asCode = asCode;
            this.defaultCode = defaultCode;
        }

        public String getName() {
            return name;
        }

        public String getPrimitive() {
            return primitive;
        }

        public String getKlass() {
            return klass;
        }

        public String getIsCode() {
            return isCode;
        }

        public String getAsCode() {
            return asCode;
        }

        public String getDefaultCode() {
            return defaultCode;
        }
    }

    public VariantClass() {
        super("Variant");
        setDocumentation(new ClassDocumentation() {{
            addLine("Объект, который может иметь значения различных типов, таких как");
            addLine("логическое значение, число, строка, список или таблица.");
        }});
        setAccessModifier(AccessModifier.PUBLIC);
        createFields();
        createMethods();
    }

    protected void createFields() {
        Field.defaultAccessModifier = AccessModifier.PROTECTED;
        addField(new Field("Gson", "gson") {{
            setAccessModifier(AccessModifier.NOTHING);
            setStatic(true);
            setInitializer("new Gson()");
        }});
        addField(new Field("VariantType", "type") {{
            setInitializer("VariantType.NULL");
        }});
        addField(new Field("Object", "object") {{
            setInitializer("null");
        }});
    }

    protected void createMethods() {
        Method.defaultAccessModifier = AccessModifier.PUBLIC;
        addMethod(new Method("Variant") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Создание объекта.");
                addLine(Documentation.SET_VALUE_ERROR_DESCRIPTION);
                addThrowDescription("VariantSetValueError", Documentation.SET_VALUE_ERROR_DESCRIPTION);
                addSeeLink("Variant");
            }});
            setConstructor(true);
            addArgument(new Argument("@Nullable Object", "value") {{
                setDescription("Значение объекта");
            }});
            setCode(new StatementBasedCode() {{
                addStatement("set(value)");
            }});
        }});

        addMethod(new Method("fromJson") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Создание объекта из json-элемента.");
                addSeeLink("Variant#fromJsonString(String)");
            }});
            setStatic(true);
            setReturn(new Return("Variant") {{
                setDescription("Объект из json-элемента");
            }});
            addArgument(new Argument("JsonElement", "json") {{
                setDescription("Json-элемент");
            }});
            setCode(new StatementBasedCode() {{
                addIf("json.isJsonPrimitive()", new StatementBasedCode() {{
                    addStatement("JsonPrimitive primitive = json.getAsJsonPrimitive()");
                    addSimpleIf("primitive.isBoolean()", "return new Variant(primitive.getAsBoolean())");
                    addSimpleIf("primitive.isNumber()", "return new Variant(primitive.getAsNumber().doubleValue())");
                    addSimpleIf("primitive.isString()", "return new Variant(primitive.getAsString())");
                }});
                addIf("json.isJsonArray()", new StatementBasedCode() {{
                    addStatement("JsonArray array = json.getAsJsonArray()");
                    addStatement("ArrayList<Variant> list = new ArrayList<>()");
                    addFor("JsonElement entry : array", new StatementBasedCode() {{
                        addStatement("list.add(fromJson(entry))");
                    }});
                    addStatement("return new Variant(list)");
                }});
                addIf("json.isJsonObject()", new StatementBasedCode() {{
                    addStatement("JsonObject object = json.getAsJsonObject()");
                    addStatement("HashMap<String, Variant> map = new HashMap<>()");
                    addFor("Map.Entry<String, JsonElement> entry : object.entrySet()", new StatementBasedCode() {{
                        addStatement("map.put(entry.getKey(), fromJson(entry.getValue()))");
                    }});
                    addStatement("return new Variant(map)");
                }});
                addStatement("return new Variant(null)");
            }});
        }});

        addMethod(new Method("fromJsonString") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Создание объекта из json-строки.");
                addSeeLink("Variant#fromJson(JsonElement)");
            }});
            setStatic(true);
            setReturn(new Return("Variant") {{
                setDescription("Объект из json-строки");
            }});
            addArgument(new Argument("String", "json") {{
                setDescription("Json-строка");
            }});
            setCode(new StatementBasedCode() {{
                addStatement("return fromJson(gson.fromJson(json, JsonElement.class))");
            }});
        }});

        addMethod(new Method("getType") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Получение типа объекта");
            }});
            setReturn(new Return("VariantType") {{
                setDescription("Тип объекта.");
            }});
            setCode(new StatementBasedCode() {{
                addStatement("return type");
            }});
        }});

        addMethod(new Method("getSize") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Получение размера объекта: длины строки, размера списка или размера таблицы.");
                addLine("Если этот объект не является строкой, списком или таблицей, будет вызвано исключение.");
                addThrowDescription("VariantTypeError", "Если этот объект не является строкой, списком или таблицей, будет вызвано исключение.");
            }});
            setReturn(new Return("int") {{
                setDescription("Длина строки, размер списка или размер таблицы.");
            }});
            setCode(new StatementBasedCode() {{
                addSimpleIf("type == VariantType.STRING", "return ((String) object).length()");
                addSimpleIf("type == VariantType.LIST", "return ((List<?>) object).size()");
                addSimpleIf("type == VariantType.MAP", "return ((Map<?, ?>) object).size()");
                addStatement("throw new VariantTypeError(type, VariantType.STRING, VariantType.LIST, VariantType.MAP)");
            }});
        }});

        addMethod(new Method("getChildrenNames") {{
            addAnnotation(Annotations.UNCHECKED);
            setDocumentation(new MethodDocumentation() {{
                addLine("Получение имён дочерних объектов этой таблицы.");
                addLine("Изменение набора имён повлечёт за собой изменение таблицы.");
                addLine(Documentation.IF_NOT_MAP_THROW_DESCRIPTION);
                addThrowDescription("VariantTypeError", Documentation.IF_NOT_MAP_THROW_DESCRIPTION);
            }});
            setReturn(new Return("Set<String>") {{
                setDescription("Имена дочерних объектов.");
            }});
            setCode(new StatementBasedCode() {{
                addStatement(Statements.IF_NOT_MAP_THROW_ERROR);
                addStatement("return ((Map<String, Variant>) object).keySet()");
            }});
        }});

        addMethod(new Method("is") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Проверка типа Variant.");
            }});
            setReturn(new Return("boolean") {{
                setDescription("Равенство типов.");
            }});
            addArgument(new Argument("VariantType", "type") {{
                setDescription("Тип для сравнения");
            }});
            setCode(new StatementBasedCode() {{
                addStatement("return this.type == type");
            }});
        }});

        addMethod(new Method("is") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Проверка типа дочернего объекта по индексу.");
            }});
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return("boolean") {{
                setDescription("Равенство типов.");
            }});
            addArgument(Arguments.CHILD_INDEX);
            addArgument(new Argument("VariantType", "type") {{
                setDescription("Тип для сравнения");
            }});
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method));
                addStatement(ComplexStatements.IF_INDEX_OUT_OF_BOUNDS_THROW_ERROR.build(method));
                addStatement("return ((List<Variant>) object).get(childIndex).type == type");
            }});
        }});

        addMethod(new Method("is") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Проверка типа дочернего объекта по имени.");
            }});
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return("boolean") {{
                setDescription("Равенство типов.");
            }});
            addArgument(Arguments.CHILD_NAME);
            addArgument(new Argument("VariantType", "type") {{
                setDescription("Тип для сравнения");
            }});
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_MAP_THROW_ERROR.build(method));
                addStatement(ComplexStatements.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_ERROR.build(method));
                addStatement("return ((Map<String, Variant>) object).get(childName).type == type");
            }});
        }});

        addMethod(new Method("contains") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Проверка наличия дочернего объекта по индексу.");
            }});
            setReturn(new Return("boolean") {{
                setDescription("Наличие дочернего объекта.");
            }});
            addArgument(Arguments.CHILD_INDEX);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method));
                addStatement("return childIndex > 0 && childIndex < ((List<?>) object).size()");
            }});
        }});

        addMethod(new Method("contains") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Проверка наличия дочернего объекта по имени.");
            }});
            setReturn(new Return("boolean") {{
                setDescription("Наличие дочернего объекта.");
            }});
            addArgument(Arguments.CHILD_NAME);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_MAP_THROW_ERROR.build(method));
                addStatement("return ((Map<?, ?>) object).containsKey(childName)");
            }});
        }});

        addMethod(new Method("get") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Получение дочернего объекта по индексу.");
            }});
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return("Variant") {{
                setDescription("Дочерний объект.");
            }});
            addArgument(Arguments.CHILD_INDEX);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method));
                addStatement(ComplexStatements.IF_INDEX_OUT_OF_BOUNDS_THROW_ERROR.build(method));
                addStatement("return ((List<Variant>) object).get(childIndex)");
            }});
        }});

        addMethod(new Method("getOrNull") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Получение дочернего объекта по индексу.");
            }});
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return("Variant") {{
                setDescription("Дочерний объект или null.");
                setAnnotation(Annotations.NULLABLE);
            }});
            addArgument(Arguments.CHILD_INDEX);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method));
                addStatement(ComplexStatements.IF_INDEX_OUT_OF_BOUNDS_RETURN_NULL.build(method));
                addStatement("return ((List<Variant>) object).get(childIndex)");
            }});
        }});

        addMethod(new Method("getOrDefault") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Получение дочернего объекта по индексу.");
            }});
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return("Variant") {{
                setDescription("Дочерний объект или null.");
                setAnnotation(Annotations.NULLABLE);
            }});
            addArgument(Arguments.CHILD_INDEX);
            addArgument(Arguments.VARIANT_DEFAULT_VALUE);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method));
                addStatement(ComplexStatements.IF_INDEX_OUT_OF_BOUNDS_RETURN_DEFAULT.build(method));
                addStatement("return ((List<Variant>) object).get(childIndex)");
            }});
        }});

        addMethod(new Method("get") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Получение дочернего объекта по имени.");
            }});
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return("Variant") {{
                setDescription("Дочерний объект.");
            }});
            addArgument(Arguments.CHILD_NAME);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_MAP_THROW_ERROR.build(method));
                addStatement(ComplexStatements.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_ERROR.build(method));
                addStatement("return ((Map<String, Variant>) object).get(childName)");
            }});
        }});

        addMethod(new Method("getOrNull") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Получение дочернего объекта по имени.");
            }});
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return("Variant") {{
                setDescription("Дочерний объект или null.");
                setAnnotation(Annotations.NULLABLE);
            }});
            addArgument(Arguments.CHILD_NAME);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_MAP_THROW_ERROR.build(method));
                addStatement(ComplexStatements.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_RETURN_NULL.build(method));
                addStatement("return ((Map<String, Variant>) object).get(childName)");
            }});
        }});

        addMethod(new Method("getOrDefault") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Получение дочернего объекта по имени.");
            }});
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return("Variant") {{
                setDescription("Дочерний объект или null.");
                setAnnotation(Annotations.NULLABLE);
            }});
            addArgument(Arguments.CHILD_NAME);
            addArgument(Arguments.VARIANT_DEFAULT_VALUE);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_MAP_THROW_ERROR.build(method));
                addStatement(ComplexStatements.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_RETURN_DEFAULT.build(method));
                addStatement("return ((Map<String, Variant>) object).get(childName)");
            }});
        }});

        addMethod(new Method("set") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Установка значения объекта.");
                addLine(Documentation.SET_VALUE_ERROR_DESCRIPTION);
                addThrowDescription("VariantSetValueError", Documentation.SET_VALUE_ERROR_DESCRIPTION);
            }});
            addAnnotation(Annotations.UNCHECKED_AND_UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(Arguments.OBJECT_VALUE);
            setCode(new StatementBasedCode() {{
                addSimpleIf("type == VariantType.LIST", "((List<?>) object).clear()");
                addSimpleIf("type == VariantType.MAP", "((Map<?, ?>) object).clear()");
                addStatement("type = VariantType.NULL");
                addStatement("object = null");
                addIf("value == null", new StatementBasedCode() {{
                    addStatement(Statements.RETURN_THIS);
                }});
                addIf("value instanceof Boolean", new StatementBasedCode() {{
                    addStatement("type = VariantType.BOOLEAN");
                    addStatement("object = value");
                    addStatement(Statements.RETURN_THIS);
                }});
                addIf("value instanceof Byte", new StatementBasedCode() {{
                    addStatement("type = VariantType.NUMBER");
                    addStatement("object = ((Byte) value).doubleValue()");
                    addStatement(Statements.RETURN_THIS);
                }});
                addIf("value instanceof Short", new StatementBasedCode() {{
                    addStatement("type = VariantType.NUMBER");
                    addStatement("object = ((Short) value).doubleValue()");
                    addStatement(Statements.RETURN_THIS);
                }});
                addIf("value instanceof Integer", new StatementBasedCode() {{
                    addStatement("type = VariantType.NUMBER");
                    addStatement("object = ((Integer) value).doubleValue()");
                    addStatement(Statements.RETURN_THIS);
                }});
                addIf("value instanceof Long", new StatementBasedCode() {{
                    addStatement("type = VariantType.NUMBER");
                    addStatement("object = ((Long) value).doubleValue()");
                    addStatement(Statements.RETURN_THIS);
                }});
                addIf("value instanceof Float", new StatementBasedCode() {{
                    addStatement("type = VariantType.NUMBER");
                    addStatement("object = ((Float) value).doubleValue()");
                    addStatement(Statements.RETURN_THIS);
                }});
                addIf("value instanceof Double", new StatementBasedCode() {{
                    addStatement("type = VariantType.NUMBER");
                    addStatement("object = value");
                    addStatement(Statements.RETURN_THIS);
                }});
                addIf("value instanceof Character", new StatementBasedCode() {{
                    addStatement("type = VariantType.STRING");
                    addStatement("object = ((Character) value).toString()");
                    addStatement(Statements.RETURN_THIS);
                }});
                addIf("value instanceof String", new StatementBasedCode() {{
                    addStatement("type = VariantType.STRING");
                    addStatement("object = value");
                    addStatement(Statements.RETURN_THIS);
                }});
                addIf("value instanceof List<?>", new StatementBasedCode() {{
                    addStatement("type = VariantType.LIST");
                    addStatement("object = new ArrayList<Variant>(((List<?>) value).size())");
                    addFor("Object innerObject : ((List<?>) value)", new StatementBasedCode() {{
                        addStatement("((List<Variant>) object).add(new Variant(innerObject))");
                    }});
                    addStatement(Statements.RETURN_THIS);
                }});
                addIf("value instanceof Map<?, ?>", new StatementBasedCode() {{
                    addStatement("type = VariantType.MAP");
                    addStatement("object = new HashMap<String, Variant>(((Map<?, ?>) value).size())");
                    addFor("Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()", new StatementBasedCode() {{
                        addStatement("((Map<String, Variant>) object).put(entry.getKey().toString(), new Variant(entry.getValue()))");
                    }});
                    addStatement(Statements.RETURN_THIS);
                }});
                addIf("value instanceof Variant", new StatementBasedCode() {{
                    addStatement("set(((Variant) value).object)");
                    addStatement(Statements.RETURN_THIS);
                }});
                addStatement("throw new VariantSetValueError(value)");
            }});
        }});

        addMethod(new Method("set") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Установка дочернего объекта по индексу.");
                addLine(Documentation.SET_VALUE_ERROR_DESCRIPTION);
                addThrowDescription("VariantSetValueError", Documentation.SET_VALUE_ERROR_DESCRIPTION);
            }});
            addAnnotation(Annotations.UNCHECKED_AND_UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(Arguments.CHILD_INDEX);
            addArgument(Arguments.OBJECT_VALUE);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method));
                addStatement(ComplexStatements.IF_INDEX_OUT_OF_BOUNDS_THROW_ERROR.build(method));
                addStatement("((List<Variant>) object).set(childIndex, new Variant(value))");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("insert") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Вставка дочернего объекта в список.");
                addLine("Если индекс меньше 0, объект будет добавлен в начало списка.");
                addLine("Если индекс больше или равен длине списка, объект будет добавлен в конец списка.");
            }});
            addAnnotation(Annotations.UNCHECKED_AND_UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(Arguments.CHILD_INDEX);
            addArgument(Arguments.OBJECT_VALUE);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method));
                addStatement(new IfStatement("childIndex < 0", new StatementBasedCode() {{
                    addStatement("((List<Variant>) object).add(0, new Variant(value))");
                    addStatement(Statements.RETURN_THIS);
                }}));
                addStatement(new IfStatement("childIndex >= ((List<?>) object).size()", new StatementBasedCode() {{
                    addStatement("((List<Variant>) object).add(new Variant(value))");
                    addStatement(Statements.RETURN_THIS);
                }}));
                addStatement("((List<Variant>) object).add(childIndex, new Variant(value))");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("add") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Добавление дочернего объекта в список.");
            }});
            addAnnotation(Annotations.UNCHECKED_AND_UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(Arguments.OBJECT_VALUE);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method));
                addStatement("((List<Variant>) object).add(new Variant(value))");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("addFirst") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Добавление дочернего объекта в начало списка.");
            }});
            addAnnotation(Annotations.UNCHECKED_AND_UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(Arguments.OBJECT_VALUE);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method));
                addStatement("((List<Variant>) object).add(0, new Variant(value))");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("addLast") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Добавление дочернего объекта в конец списка.");
            }});
            addAnnotation(Annotations.UNCHECKED_AND_UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(Arguments.OBJECT_VALUE);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method));
                addStatement("((List<Variant>) object).add(new Variant(value))");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("set") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Установка дочернего объекта по имени.");
                addLine(Documentation.SET_VALUE_ERROR_DESCRIPTION);
                addThrowDescription("VariantSetValueError", Documentation.SET_VALUE_ERROR_DESCRIPTION);
            }});
            addAnnotation(Annotations.UNCHECKED_AND_UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(Arguments.CHILD_NAME);
            addArgument(Arguments.OBJECT_VALUE);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_MAP_THROW_ERROR.build(method));
                addStatement("((Map<String, Variant>) object).put(childName, new Variant(value))");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("remove") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Удаление дочернего объекта по индексу.");
            }});
            addAnnotation(Annotations.UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(Arguments.CHILD_INDEX);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method));
                addStatement(ComplexStatements.IF_INDEX_OUT_OF_BOUNDS_THROW_ERROR.build(method));
                addStatement("((List<?>) object).remove(childIndex)");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("remove") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Удаление дочернего объекта по имени.");
            }});
            addAnnotation(Annotations.UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(Arguments.CHILD_NAME);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_MAP_THROW_ERROR.build(method));
                addStatement(ComplexStatements.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_ERROR.build(method));
                addStatement("((Map<?, ?>) object).remove(childName)");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("removeIf") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Удаление всех дочерних объектов, подходящих условию.");
                addLine("Если этот объект не списком или таблицей, будет вызвано исключение.");
                addThrowDescription("VariantTypeError", "Если этот объект не списком или таблицей, будет вызвано исключение.");
            }});
            addAnnotation(Annotations.UNCHECKED_AND_UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(new Argument("Predicate<Variant>", "filter") {{
                setDescription("Фильтр дочерних объектов");
            }});
            setCode(new StatementBasedCode() {{
                addIf("type == VariantType.LIST", new StatementBasedCode() {{
                    addStatement("((List<Variant>) object).removeIf(filter)");
                    addStatement(Statements.RETURN_THIS);
                }});
                addIf("type == VariantType.MAP", new StatementBasedCode() {{
                    addStatement("((Map<String, Variant>) object).entrySet().removeIf(entry -> filter.test(entry.getValue()))");
                    addStatement(Statements.RETURN_THIS);
                }});
                addStatement("throw new VariantTypeError(type,VariantType.LIST, VariantType.MAP)");
            }});
        }});

        addMethod(new Method("forEach") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Обработка каждого дочернего объекта.");
                addLine("Если этот объект не является списком или таблицей, будет вызвано исключение.");
                addThrowDescription("VariantTypeError", "Если этот объект не является списком или таблицей, будет вызвано исключение.");
            }});
            addAnnotation(Annotations.UNCHECKED_AND_UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(new Argument("Consumer<Variant>", "handler") {{
                setDescription("Обработчик дочернего объекта");
            }});
            setCode(new StatementBasedCode() {{
                addIf("type == VariantType.LIST", new StatementBasedCode() {{
                    addStatement("((List<Variant>) object).forEach(handler)");
                    addStatement(Statements.RETURN_THIS);
                }});
                addIf("type == VariantType.MAP", new StatementBasedCode() {{
                    addStatement("((Map<String, Variant>) object).forEach((key, value) -> handler.accept(value))");
                    addStatement(Statements.RETURN_THIS);
                }});
                addStatement("throw new VariantTypeError(type, VariantType.LIST, VariantType.MAP)");
            }});
        }});

        addMethod(new Method("forEachInList") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Обработка каждого дочернего объекта в списке.");
            }});
            addAnnotation(Annotations.UNCHECKED_AND_UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(new Argument("BiConsumer<Integer, Variant>", "handler") {{
                setDescription("Обработчик дочернего объекта и его индекса");
            }});
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method));
                addStatement("int childIndex = 0");
                addFor("Variant child : ((List<Variant>) object)", new StatementBasedCode() {{
                    addStatement("handler.accept(childIndex, child)");
                    addStatement("childIndex++");
                }});
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("forEachInMap") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Обработка каждого дочернего объекта в таблице.");
            }});
            addAnnotation(Annotations.UNCHECKED_AND_UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(new Argument("BiConsumer<String, Variant>", "handler") {{
                setDescription("Обработчик дочернего объекта и его имени");
            }});
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_MAP_THROW_ERROR.build(method));
                addStatement("((Map<String, Variant>) object).forEach(handler)");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("toJson") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Преобразование объекта в json.");
            }});
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return("JsonElement") {{
                setDescription("Json-объект");
            }});
            setCode(new StatementBasedCode() {{
                addSimpleIf("type == VariantType.NULL", "return JsonNull.INSTANCE");
                addSimpleIf("type == VariantType.BOOLEAN", "return new JsonPrimitive((Boolean) object)");
                addSimpleIf("type == VariantType.NUMBER", "return new JsonPrimitive((Double) object)");
                addSimpleIf("type == VariantType.STRING", "return new JsonPrimitive((String) object)");
                addIf("type == VariantType.LIST", new StatementBasedCode() {{
                    addStatement("JsonArray array = new JsonArray()");
                    addFor("Variant child : (List<Variant>) object", new StatementBasedCode() {{
                        addStatement("array.add(child.toJson())");
                    }});
                    addStatement("return array");
                }});
                addIf("type == VariantType.MAP", new StatementBasedCode() {{
                    addStatement("JsonObject object = new JsonObject()");
                    addFor("Map.Entry<String, Variant> entry : ((Map<String, Variant>) this.object).entrySet()", new StatementBasedCode() {{
                        addStatement("object.add(entry.getKey(), entry.getValue().toJson())");
                    }});
                    addStatement("return object");
                }});
                addStatement("return null");
            }});
        }});

        addMethod(new Method("toJsonString") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Преобразование объекта в json-строку.");
            }});
            setReturn(new Return("String") {{
                setDescription("Json-строка");
            }});
            setCode(new StatementBasedCode() {{
                addStatement("return toJson().toString()");
            }});
        }});

        addMethod(new Method("toString") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Преобразование объекта в строку.");
            }});
            addAnnotation(Annotations.OVERRIDE);
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return("String") {{
                setDescription("Строка");
            }});
            setCode(new StatementBasedCode() {{
                addSimpleIf("type == VariantType.NULL", "return \"null\"");
                addSimpleIf("type == VariantType.BOOLEAN", "return ((Boolean) object) ? \"true\" : \"false\"");
                addIf("type == VariantType.NUMBER", new StatementBasedCode() {{
                    addSimpleIf("((Double) object).longValue() == (Double) object", "return Long.toString(((Double) object).longValue())");
                    addStatement("return ((Double) object).toString()");
                }});
                addSimpleIf("type == VariantType.STRING", "return \"\\\"\" + object + \"\\\"\"");
                addIf("type == VariantType.LIST", new StatementBasedCode() {{
                    addStatement("StringBuilder string = new StringBuilder(\"[\")");
                    addFor("Variant child : (List<Variant>) object", new StatementBasedCode() {{
                        addStatement("string.append(child.toString()).append(\", \")");
                    }});
                    addIf("!((List<?>) object).isEmpty()", new StatementBasedCode() {{
                        addStatement("string.delete(string.length() - 2, string.length())");
                    }});
                    addStatement("string.append(\"]\")");
                    addStatement("return string.toString()");
                }});
                addIf("type == VariantType.MAP", new StatementBasedCode() {{
                    addStatement("StringBuilder string = new StringBuilder(\"{\")");
                    addFor("Map.Entry<String, Variant> entry : ((Map<String, Variant>) this.object).entrySet()", new StatementBasedCode() {{
                        addStatement("string.append(entry.getKey()).append(\"=\").append(entry.getValue().toString()).append(\", \")");
                    }});
                    addIf("!((Map<?, ?>) object).isEmpty()", new StatementBasedCode() {{
                        addStatement("string.delete(string.length() - 2, string.length())");
                    }});
                    addStatement("string.append(\"}\")");
                    addStatement("return string.toString()");
                }});
                addStatement("return null");
            }});
        }});

        addMethod(new Method("hashCode") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Получение хеша объекта.");
            }});
            addAnnotation(Annotations.OVERRIDE);
            setReturn(new Return("int") {{
                setDescription("Хеш.");
            }});
            setCode(new StatementBasedCode() {{
                addStatement("return object.hashCode()");
            }});
        }});

        addMethod(new Method("equals") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Сравнение объектов.");
            }});
            addAnnotation(Annotations.OVERRIDE);
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return("boolean") {{
                setDescription("Равенство объектов.");
            }});
            addArgument(new Argument("Object", "obj") {{
                setDescription("Объект для сравнения");
            }});
            setCode(new StatementBasedCode() {{
                addSimpleIf("!(obj instanceof Variant)", "return false");
                addStatement("Variant other = (Variant) obj");
                addSimpleIf("type != other.type", "return false");
                addSimpleIf("type == VariantType.NULL", "return true");
                addSimpleIf("type == VariantType.BOOLEAN", "return object == other.object");
                addSimpleIf("type == VariantType.NUMBER", "return object == other.object");
                addSimpleIf("type == VariantType.STRING", "return object.equals(other.object)");
                addIf("type == VariantType.LIST", new StatementBasedCode() {{
                    addStatement("List<Variant> list = ((List<Variant>) object)");
                    addStatement("List<Variant> otherList = ((List<Variant>) other.object)");
                    addSimpleIf("list.size() != otherList.size()", "return false");
                    addFor("int index = 0; index < list.size(); index++", new StatementBasedCode() {{
                        addSimpleIf("!list.get(index).equals(otherList.get(index))", "return false");
                    }});
                    addStatement("return true");
                }});
                addIf("type == VariantType.MAP", new StatementBasedCode() {{
                    addStatement("Map<String, Variant> map = ((Map<String, Variant>) object)");
                    addStatement("Map<String, Variant> otherMap = ((Map<String, Variant>) other.object)");
                    addSimpleIf("map.size() != otherMap.size()", "return false");
                    addFor("Map.Entry<String, Variant> entry : map.entrySet()", new StatementBasedCode() {{
                        addSimpleIf("!entry.getValue().equals(otherMap.get(entry.getKey()))", "return false");
                    }});
                    addStatement("return true");
                }});
                addStatement("return false");
            }});
        }});

        createDataTypesMethods();
    }

    public void createDataTypesMethods() {
        List<VariantDataType> dataTypes = new ArrayList<>();
        dataTypes.add(new VariantDataType("Boolean", "boolean", "Boolean", "return type == VariantType.BOOLEAN", "return (Boolean) object", "false"));
        dataTypes.add(new VariantDataType("Byte", "byte", "Byte", "return type == VariantType.NUMBER && ((Double) object).byteValue() == (Double) object", "return ((Double) object).byteValue()", "0"));
        dataTypes.add(new VariantDataType("Short", "short", "Short", "return type == VariantType.NUMBER && ((Double) object).shortValue() == (Double) object", "return ((Double) object).shortValue()", "0"));
        dataTypes.add(new VariantDataType("Integer", "int", "Integer", "return type == VariantType.NUMBER && ((Double) object).intValue() == (Double) object", "return ((Double) object).intValue()", "0"));
        dataTypes.add(new VariantDataType("Long", "long", "Long", "return type == VariantType.NUMBER && ((Double) object).longValue() == (Double) object", "return ((Double) object).longValue()", "0"));
        dataTypes.add(new VariantDataType("Float", "float", "Float", "return type == VariantType.NUMBER && ((Double) object).floatValue() == (Double) object", "return ((Double) object).floatValue()", "0"));
        dataTypes.add(new VariantDataType("Double", "double", "Double", "return type == VariantType.NUMBER", "return (Double) object", "0"));
        dataTypes.add(new VariantDataType("Character", "char", "Character", "return type == VariantType.STRING && ((String) object).length() == 1", "return ((String) object).charAt(0)", "' '"));
        dataTypes.add(new VariantDataType("String", "String", "String", "return type == VariantType.STRING", "return (String) object", "\"\""));
        dataTypes.add(new VariantDataType("List", "List<Variant>", "List<Variant>", "return type == VariantType.LIST", "return (List<Variant>) object", "new ArrayList<>()"));
        dataTypes.add(new VariantDataType("Map", "Map<String, Variant>", "Map<String, Variant>", "return type == VariantType.MAP", "return (Map<String, Variant>) object", "new HashMap<>()"));
        for (VariantDataType dataType : dataTypes) {
            createDataTypeMethods(dataType);
        }
    }

    public void createDataTypeMethods(VariantDataType dataType) {
        addMethod(new Method("new" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Создание объекта со значением типа " + dataType.getName() + ".");
            }});
            setStatic(true);
            setReturn(new Return("Variant") {{
                setDescription("Объект со значением типа " + dataType.getName() + ".");
            }});
            addArgument(new Argument(dataType.getPrimitive(), "value") {{
                setDescription("Значение объекта");
            }});
            setCode(new StatementBasedCode() {{
                addStatement("return new Variant(value)");
            }});
        }});

        addMethod(new Method("new" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Создание объекта со значением типа " + dataType.getName() + " по умолчанию.");
            }});
            setStatic(true);
            setReturn(new Return("Variant") {{
                setDescription("Объект со значением типа " + dataType.getName() + " по умолчанию.");
            }});
            setCode(new StatementBasedCode() {{
                addStatement("return new Variant(" + dataType.getDefaultCode() + ")");
            }});
        }});

        addMethod(new Method("is" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Проверка, является ли значение этого объекта " + dataType.getName() + ".");
            }});
            setReturn(new Return("boolean") {{
                setDescription("Является ли значение этого объекта " + dataType.getName() + ".");
            }});
            setCode(new StatementBasedCode() {{
                addStatement(dataType.getIsCode());
            }});
        }});

        addMethod(new Method("is" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Проверка типа значения дочернего объекта по индексу.");
            }});
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return("boolean") {{
                setDescription("Соответствие типа значения.");
            }});
            addArgument(Arguments.CHILD_INDEX);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method));
                addStatement(ComplexStatements.IF_INDEX_OUT_OF_BOUNDS_THROW_ERROR.build(method));
                addStatement("return ((List<Variant>) object).get(childIndex).is" + dataType.getName() + "()");
            }});
        }});

        addMethod(new Method("is" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Проверка типа значения дочернего объекта по имени.");
            }});
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return("boolean") {{
                setDescription("Соответствие типа значения.");
            }});
            addArgument(Arguments.CHILD_NAME);
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.IF_NOT_MAP_THROW_ERROR.build(method));
                addStatement(ComplexStatements.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_ERROR.build(method));
                addStatement("return ((Map<String, Variant>) object).get(childName).is" + dataType.getName() + "()");
            }});
        }});

        addMethod(new Method("as" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Преобразование значения объекта к типу " + dataType.getName() + ".");
            }});
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return(dataType.getPrimitive()) {{
                setDescription("Значение объекта с типом " + dataType.getName() + ".");
            }});
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.CONVERT_VALUE_THROW_ERROR(dataType).build(method));
                addStatement(dataType.getAsCode());
            }});
        }});

        addMethod(new Method("as" + dataType.getName() + "OrNull") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Преобразование значения объекта к типу " + dataType.getName() + ".");
            }});
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return(dataType.getKlass()) {{
                setDescription("Значение объекта с типом " + dataType.getName() + " или null.");
                setAnnotation(Annotations.NULLABLE);
            }});
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.CONVERT_VALUE_RETURN_NULL(dataType).build(method));
                addStatement(dataType.getAsCode());
            }});
        }});

        addMethod(new Method("as" + dataType.getName() + "OrDefault") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Преобразование значения объекта к типу " + dataType.getName() + ".");
            }});
            addAnnotation(Annotations.UNCHECKED);
            setReturn(new Return(dataType.getKlass()) {{
                setDescription("Значение объекта с типом " + dataType.getName() + " или значение по умолчанию.");
            }});
            addArgument(new Argument(dataType.getKlass(), "defaultValue") {{
                setDescription("Значение по умолчанию");
            }});
            setCode(new StatementBasedCode() {{
                addStatement(ComplexStatements.CONVERT_VALUE_RETURN_DEFAULT(dataType).build(method));
                addStatement(dataType.getAsCode());
            }});
        }});

        addMethod(new Method("get" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Преобразование значения дочернего объекта к типу " + dataType.getName() + ".");
            }});
            setReturn(new Return(dataType.getPrimitive()) {{
                setDescription("Значение дочернего объекта с типом " + dataType.getName() + ".");
            }});
            addArgument(Arguments.CHILD_INDEX);
            setCode(new StatementBasedCode() {{
                ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method);
                ComplexStatements.IF_INDEX_OUT_OF_BOUNDS_THROW_ERROR.build(method);
                ComplexStatements.CONVERT_VALUE_THROW_ERROR(dataType).build(method);
                addStatement("return get(childIndex).as" + dataType.getName() + "()");
            }});
        }});

        addMethod(new Method("get" + dataType.getName() + "OrNull") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Преобразование значения дочернего объекта к типу " + dataType.getName() + ".");
            }});
            setReturn(new Return(dataType.getKlass()) {{
                setDescription("Значение дочернего объекта с типом " + dataType.getName() + " или null.");
                setAnnotation(Annotations.NULLABLE);
            }});
            addArgument(Arguments.CHILD_INDEX);
            setCode(new StatementBasedCode() {{
                ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method);
                ComplexStatements.IF_INDEX_OUT_OF_BOUNDS_RETURN_NULL.build(method);
                ComplexStatements.CONVERT_VALUE_RETURN_NULL(dataType).build(method);
                addStatement("Variant child = getOrNull(childIndex)");
                addSimpleIf("child == null", "return null");
                addStatement("return child.as" + dataType.getName() + "OrNull()");
            }});
        }});

        addMethod(new Method("get" + dataType.getName() + "OrDefault") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Преобразование значения дочернего объекта к типу " + dataType.getName() + ".");
            }});
            setReturn(new Return(dataType.getKlass()) {{
                setDescription("Значение дочернего объекта с типом " + dataType.getName() + " или значение по умолчанию.");
            }});
            addArgument(Arguments.CHILD_INDEX);
            addArgument(new Argument(dataType.getKlass(), "defaultValue") {{
                setDescription("Значение по умолчанию");
            }});
            setCode(new StatementBasedCode() {{
                ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method);
                ComplexStatements.IF_INDEX_OUT_OF_BOUNDS_RETURN_DEFAULT.build(method);
                ComplexStatements.CONVERT_VALUE_RETURN_DEFAULT(dataType).build(method);
                addStatement("Variant child = getOrNull(childIndex)");
                addSimpleIf("child == null", "return defaultValue");
                addStatement("return child.as" + dataType.getName() + "OrDefault(defaultValue)");
            }});
        }});

        addMethod(new Method("get" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Преобразование значения дочернего объекта к типу " + dataType.getName() + ".");
            }});
            setReturn(new Return(dataType.getPrimitive()) {{
                setDescription("Значение дочернего объекта с типом " + dataType.getName() + ".");
            }});
            addArgument(Arguments.CHILD_NAME);
            setCode(new StatementBasedCode() {{
                ComplexStatements.IF_NOT_MAP_THROW_ERROR.build(method);
                ComplexStatements.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_ERROR.build(method);
                ComplexStatements.CONVERT_VALUE_THROW_ERROR(dataType).build(method);
                addStatement("return get(childName).as" + dataType.getName() + "()");
            }});
        }});

        addMethod(new Method("get" + dataType.getName() + "OrNull") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Преобразование значения дочернего объекта к типу " + dataType.getName() + ".");
            }});
            setReturn(new Return(dataType.getKlass()) {{
                setDescription("Значение дочернего объекта с типом " + dataType.getName() + " или null.");
                setAnnotation(Annotations.NULLABLE);
            }});
            addArgument(Arguments.CHILD_NAME);
            setCode(new StatementBasedCode() {{
                ComplexStatements.IF_NOT_MAP_THROW_ERROR.build(method);
                ComplexStatements.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_RETURN_NULL.build(method);
                ComplexStatements.CONVERT_VALUE_RETURN_NULL(dataType).build(method);
                addStatement("Variant child = getOrNull(childName)");
                addSimpleIf("child == null", "return null");
                addStatement("return child.as" + dataType.getName() + "OrNull()");
            }});
        }});

        addMethod(new Method("get" + dataType.getName() + "OrDefault") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Преобразование значения дочернего объекта к типу " + dataType.getName() + ".");
            }});
            setReturn(new Return(dataType.getKlass()) {{
                setDescription("Значение дочернего объекта с типом " + dataType.getName() + " или значение по умолчанию.");
            }});
            addArgument(Arguments.CHILD_NAME);
            addArgument(new Argument(dataType.getKlass(), "defaultValue") {{
                setDescription("Значение по умолчанию");
            }});
            setCode(new StatementBasedCode() {{
                ComplexStatements.IF_NOT_MAP_THROW_ERROR.build(method);
                ComplexStatements.IF_CHILD_WITH_NAME_DOES_NOT_EXISTS_RETURN_DEFAULT.build(method);
                ComplexStatements.CONVERT_VALUE_RETURN_DEFAULT(dataType).build(method);
                addStatement("Variant child = getOrNull(childName)");
                addSimpleIf("child == null", "return defaultValue");
                addStatement("return child.as" + dataType.getName() + "OrDefault(defaultValue)");
            }});
        }});

        addMethod(new Method("set" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Установка значения объекта с типом " + dataType.getName() + ".");
            }});
            setReturn(Returns.THIS);
            addArgument(new Argument(dataType.getPrimitive(), "value") {{
                setDescription("Значение объекта с типом " + dataType.getName());
            }});
            setCode(new StatementBasedCode() {{
                addStatement("set(value)");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("set" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Установка дочернего объекта со значением с типом " + dataType.getName() + ".");
            }});
            setReturn(Returns.THIS);
            addArgument(Arguments.CHILD_INDEX);
            addArgument(new Argument(dataType.getPrimitive(), "value") {{
                setDescription("Значение дочернего объекта с типом " + dataType.getName());
            }});
            setCode(new StatementBasedCode() {{
                ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method);
                ComplexStatements.IF_INDEX_OUT_OF_BOUNDS_THROW_ERROR.build(method);
                addStatement("set(childIndex, value)");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("insert" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Вставка дочернего объекта со значением с типом " + dataType.getName() + ".");
                addLine("Если индекс меньше 0, объект будет добавлен в начало списка.");
                addLine("Если индекс больше или равен длине списка, объект будет добавлен в конец списка.");
            }});
            setReturn(Returns.THIS);
            addArgument(Arguments.CHILD_INDEX);
            addArgument(new Argument(dataType.getPrimitive(), "value") {{
                setDescription("Значение дочернего объекта с типом " + dataType.getName());
            }});
            setCode(new StatementBasedCode() {{
                ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method);
                addStatement("insert(childIndex, value)");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("add" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Добавление дочернего объекта со значением с типом " + dataType.getName() + ".");
            }});
            setReturn(Returns.THIS);
            addArgument(new Argument(dataType.getPrimitive(), "value") {{
                setDescription("Значение дочернего объекта с типом " + dataType.getName());
            }});
            setCode(new StatementBasedCode() {{
                ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method);
                addStatement("add(value)");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("addFirst" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Добавление дочернего объекта со значением с типом " + dataType.getName() + " в начало списка.");
            }});
            setReturn(Returns.THIS);
            addArgument(new Argument(dataType.getPrimitive(), "value") {{
                setDescription("Значение дочернего объекта с типом " + dataType.getName());
            }});
            setCode(new StatementBasedCode() {{
                ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method);
                addStatement("addFirst(value)");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("addLast" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Добавление дочернего объекта со значением с типом " + dataType.getName() + " в конец списка.");
            }});
            setReturn(Returns.THIS);
            addArgument(new Argument(dataType.getPrimitive(), "value") {{
                setDescription("Значение дочернего объекта с типом " + dataType.getName());
            }});
            setCode(new StatementBasedCode() {{
                ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method);
                addStatement("addLast(value)");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("set" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Установка значения дочернего объекта с типом " + dataType.getName() + ".");
            }});
            setReturn(Returns.THIS);
            addArgument(Arguments.CHILD_NAME);
            addArgument(new Argument(dataType.getPrimitive(), "value") {{
                setDescription("Значение дочернего объекта с типом " + dataType.getName());
            }});
            setCode(new StatementBasedCode() {{
                ComplexStatements.IF_NOT_MAP_THROW_ERROR.build(method);
                addStatement("set(childName, value)");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("forEach" + dataType.getName()) {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Обработка значения каждого дочернего объекта с типом " + dataType.getName() + ".");
                addLine("Если этот объект не является списком или таблицей, будет вызвано исключение.");
                addThrowDescription("VariantTypeError", "Если этот объект не является списком или таблицей, будет вызвано исключение.");
            }});
            addAnnotation(Annotations.UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(new Argument("Consumer<" + dataType.getKlass() + ">", "handler") {{
                setDescription("Обработчик значения дочернего объекта");
            }});
            setCode(new StatementBasedCode() {{
                addStatement("forEach((child) -> { if (child.is" + dataType.getName() + "()) handler.accept(child.as" + dataType.getName() + "()); })");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("forEach" + dataType.getName() + "InList") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Обработка значения каждого дочернего объекта с типом " + dataType.getName() + " в списке.");
            }});
            addAnnotation(Annotations.UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(new Argument("BiConsumer<Integer, " + dataType.getKlass() + ">", "handler") {{
                setDescription("Обработчик значения дочернего объекта и его индекса");
            }});
            setCode(new StatementBasedCode() {{
                ComplexStatements.IF_NOT_LIST_THROW_ERROR.build(method);
                addStatement("forEachInList((childIndex, child) -> { if (child.is" + dataType.getName() + "()) handler.accept(childIndex, child.as" + dataType.getName() + "()); })");
                addStatement(Statements.RETURN_THIS);
            }});
        }});

        addMethod(new Method("forEach" + dataType.getName() + "InMap") {{
            setDocumentation(new MethodDocumentation() {{
                addLine("Обработка значения каждого дочернего объекта с типом " + dataType.getName() + " в таблице.");
            }});
            addAnnotation(Annotations.UNUSED_RETURN_VALUE);
            setReturn(Returns.THIS);
            addArgument(new Argument("BiConsumer<String, " + dataType.getKlass() + ">", "handler") {{
                setDescription("Обработчик значения дочернего объекта и его имени");
            }});
            setCode(new StatementBasedCode() {{
                ComplexStatements.IF_NOT_MAP_THROW_ERROR.build(method);
                addStatement("forEachInMap((childName, child) -> { if (child.is" + dataType.getName() + "()) handler.accept(childName, child.as" + dataType.getName() + "()); })");
                addStatement(Statements.RETURN_THIS);
            }});
        }});
    }
}
