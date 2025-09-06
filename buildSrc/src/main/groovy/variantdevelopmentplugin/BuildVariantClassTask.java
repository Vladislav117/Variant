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
        sourceCode += "import com.google.gson.*;\n";
        sourceCode += "import org.jetbrains.annotations.Nullable;\n";
        sourceCode += "import ru.vladislav117.variant.error.*;\n";
        sourceCode += "\n";
        sourceCode += "import java.util.*;\n";
        sourceCode += "import java.util.function.BiConsumer;\n";
        sourceCode += "import java.util.function.Consumer;\n";
        sourceCode += "import java.util.function.Predicate;\n\n";

        VariantClass variantClass = new VariantClass();
        sourceCode += variantClass.build(0);

        try {
            com.google.common.io.Files.write(sourceCode.getBytes(StandardCharsets.UTF_8), new File(sourceCodeDirectory, "Variant.java"));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
