package com.propertykey.support;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiLiteralExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Reports unresolved braced property keys in validation {@code message} attributes.
 */
public class UnresolvedPropertyKeyInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull com.intellij.psi.PsiElement element) {
                if (!(element instanceof PsiLiteralExpression literal)) {
                    super.visitElement(element);
                    return;
                }

                if (PropertyKeysUtil.getMessageLiteral(literal) != literal) {
                    super.visitElement(element);
                    return;
                }

                Object value = literal.getValue();
                if (!(value instanceof String text)) {
                    super.visitElement(element);
                    return;
                }

                String key = PropertyKeysUtil.extractKey(text);
                if (key == null) {
                    super.visitElement(element);
                    return;
                }

                if (PropertyKeysUtil.findProperty(literal.getProject(), key) != null) {
                    super.visitElement(element);
                    return;
                }

                holder.registerProblem(
                        literal,
                        "Unresolved property key '" + key + "' in message property files",
                        new CreatePropertyKeyQuickFix(literal, key)
                );
            }
        };
    }
}
