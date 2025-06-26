# JavaLibraryTemplate

JavaLibraryTemplate - шаблон java-библиотеки.

## Добавление зависимости

Для добавления зависимости необходимо внести изменения в `build.gradle`:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Vladislav117:JavaLibraryTemplate:0.0.0'
}
```

## Сборка

Сборка осуществляется командой `./gradlew build`

Результат сборки располагается в `build/libs`

## Документация

Документация представлена в исходном коде (javadoc).

## Использование

Для использования шаблона необходимо адаптировать его.
