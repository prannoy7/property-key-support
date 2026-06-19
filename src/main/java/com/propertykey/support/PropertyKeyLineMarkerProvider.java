package com.propertykey.support;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shows a gutter warning for unresolved braced property keys in {@code message} attributes.
 */
public class PropertyKeyLineMarkerProvider implements LineMarkerProvider {

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiLiteralExpression literal)) {
            return null;
        }

        if (PropertyKeysUtil.getMessageLiteral(literal) != literal) {
            return null;
        }

        Object value = literal.getValue();
        if (!(value instanceof String text)) {
            return null;
        }

        String key = PropertyKeysUtil.extractKey(text);
        if (key == null) {
            return null;
        }

        if (PropertyKeysUtil.findProperty(literal.getProject(), key) != null) {
            return null;
        }

        return new LineMarkerInfo<PsiLiteralExpression>(
                literal,
                literal.getTextRange(),
                AllIcons.General.Error,
                ignored -> "Unresolved property key: " + key,
                null,
                GutterIconRenderer.Alignment.CENTER
        );
    }
}
