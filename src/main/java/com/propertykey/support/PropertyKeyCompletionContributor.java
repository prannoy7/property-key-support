package com.propertykey.support;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Suggests keys from message property files while editing annotation {@code message} attributes.
 */
public class PropertyKeyCompletionContributor extends CompletionContributor {

    public PropertyKeyCompletionContributor() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(
                    @NotNull CompletionParameters parameters,
                    @NotNull ProcessingContext context,
                    @NotNull CompletionResultSet result
            ) {
                PsiLiteralExpression literal =
                        PropertyKeysUtil.getMessageLiteral(parameters.getPosition());
                if (literal == null) {
                    return;
                }

                String currentText = getCurrentText(literal);
                if (!currentText.isEmpty() && !currentText.startsWith("{")) {
                    return;
                }

                String prefix = currentText.startsWith("{")
                        ? currentText.substring(1)
                        : "";
                CompletionResultSet filtered = prefix.isEmpty()
                        ? result
                        : result.withPrefixMatcher(prefix);

                Project project = parameters.getPosition().getProject();
                for (String key : PropertyKeysUtil.getAllKeys(project)) {
                    LookupElementBuilder element = PropertyKeyLookupElements.create(project, key)
                            .withInsertHandler(PropertyKeyCompletionContributor::insertBracedKey);
                    filtered.addElement(element);
                }
            }
        };

        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement()
                        .inside(PsiJavaPatterns.psiNameValuePair().withName("message")),
                provider
        );
    }

    private static @NotNull String getCurrentText(@NotNull PsiLiteralExpression literal) {
        Object value = literal.getValue();
        if (value instanceof String text) {
            return text;
        }
        String raw = literal.getText();
        if (raw.length() >= 2 && raw.startsWith("\"") && raw.endsWith("\"")) {
            return raw.substring(1, raw.length() - 1);
        }
        return raw;
    }

    private static void insertBracedKey(@NotNull InsertionContext context, @NotNull LookupElement item) {
        PsiElement leaf = context.getFile().findElementAt(context.getStartOffset());
        PsiLiteralExpression literal = PropertyKeysUtil.getMessageLiteral(leaf);
        if (literal == null) {
            return;
        }

        String bracedValue = "\"{" + item.getLookupString() + "}\"";
        PsiElementFactory factory = PsiElementFactory.getInstance(context.getProject());
        PsiExpression replacement = factory.createExpressionFromText(bracedValue, literal);
        literal.replace(replacement);
    }
}
