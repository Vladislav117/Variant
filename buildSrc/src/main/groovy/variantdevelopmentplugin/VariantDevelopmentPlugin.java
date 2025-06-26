package variantdevelopmentplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Плагин разработки Variant.
 */
public class VariantDevelopmentPlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project project) {
        project.getTasks().register("buildVariantClass", BuildVariantClassTask.class);
    }
}
