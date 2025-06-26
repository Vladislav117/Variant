package variantdevelopmentplugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BuildVariantClassTask extends DefaultTask {
    @TaskAction
    public void run() {
        File sourceCodeDirectory = new File(getProject().getProjectDir(), "src/main/java/ru/vladislav117/variant");

        // TODO: Move imports to JavaWriter methods

        String sourceCode = "";
        sourceCode += "package ru.vladislav117.variant;\n\n";
        sourceCode += """
                import com.google.gson.*;
                import org.jetbrains.annotations.Nullable;
                import ru.vladislav117.variant.error.*;
                
                import java.util.*;
                import java.util.function.BiConsumer;
                import java.util.function.Consumer;
                import java.util.function.Predicate;
                
                """;

        VariantClass variantClass = new VariantClass();
        sourceCode += variantClass.build(0);

        try {
            com.google.common.io.Files.write(sourceCode.getBytes(StandardCharsets.UTF_8), new File(sourceCodeDirectory, "Variant.java"));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
