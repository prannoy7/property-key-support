package com.propertykey.support;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.properties.IProperty;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reference from a braced property key in Java source to the matching entry in {@code messages.properties}.
 */
public class PropertyKeyReference extends PsiReferenceBase<PsiLiteralExpression> {

    private final String key;

    public PropertyKeyReference(@NotNull PsiLiteralExpression element, @NotNull String key) {
        super(element, new TextRange(1, element.getTextLength() - 1));
        this.key = key;
    }

    public @NotNull String getKey() {
        return key;
    }

    @Override
    public @Nullable PsiElement resolve() {
        IProperty property = PropertyKeysUtil.findProperty(myElement.getProject(), key);
        return property != null ? property.getPsiElement() : null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return PropertyKeysUtil.getAllKeys(myElement.getProject()).stream()
                .map(propertyKey -> LookupElementBuilder.create(propertyKey)
                        .withPresentableText("{" + propertyKey + "}")
                        .withTypeText("messages.properties"))
                .toArray(LookupElement[]::new);
    }
}
