package variantdevelopmentplugin;

import ru.vladislav117.javawriter.code.StatementBasedCode;
import ru.vladislav117.javawriter.method.Method;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class ComplexStatement {
    protected StatementBasedCode.AbstractStatement statement;
    protected List<String> documentationLines = new ArrayList<>();
    protected List<AbstractMap.SimpleEntry<String, String>> throwsDescriptions = new ArrayList<>();

    public ComplexStatement(StatementBasedCode.AbstractStatement statement) {
        this.statement = statement;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ComplexStatement setDocumentationLines(List<String> documentationLines) {
        this.documentationLines = documentationLines;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ComplexStatement setThrowsDescriptions(List<AbstractMap.SimpleEntry<String, String>> throwsDescriptions) {
        this.throwsDescriptions = throwsDescriptions;
        return this;
    }

    public StatementBasedCode.AbstractStatement build(Method method) {
        for (String documentationLine : documentationLines) {
            method.getDocumentation().addLine(documentationLine);
        }
        for (AbstractMap.SimpleEntry<String, String> throwsDescription : throwsDescriptions) {
            method.getDocumentation().addThrowDescription(throwsDescription.getKey(), throwsDescription.getValue());
        }
        return statement;
    }
}
