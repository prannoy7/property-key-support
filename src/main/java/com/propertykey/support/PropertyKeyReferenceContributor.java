package com.propertykey.support;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Registers references for {@code message = "{key}"} values on Java annotation attributes.
 */
public class PropertyKeyReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(PsiLiteralExpression.class)
                        .withParent(PsiJavaPatterns.psiNameValuePair().withName("message")),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(
                            @NotNull PsiElement element,
                            @NotNull ProcessingContext context
                    ) {
                        return createReferences((PsiLiteralExpression) element);
                    }
                }
        );
    }

    static PsiReference @NotNull [] createReferences(@NotNull PsiLiteralExpression literal) {
        Object value = literal.getValue();
        if (!(value instanceof String text)) {
            return PsiReference.EMPTY_ARRAY;
        }

        String key = PropertyKeysUtil.extractKey(text);
        if (key == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        return new PsiReference[]{new PropertyKeyReference(literal, key)};
    }
}
