package com.propertykey.support;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.properties.IProperty;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reference from a braced property key in Java source to the matching entry in message property files.
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
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return resolve() == element;
    }

    @Override
    public @Nullable PsiElement bindToElement(@NotNull PsiElement element) {
        if (element instanceof IProperty property) {
            String newKey = property.getKey();
            if (newKey == null) {
                return super.bindToElement(element);
            }
            if (newKey.startsWith("{") && newKey.endsWith("}")) {
                newKey = newKey.substring(1, newKey.length() - 1);
            }
            return handleElementRename(newKey);
        }
        return super.bindToElement(element);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        PsiElementFactory factory = PsiElementFactory.getInstance(myElement.getProject());
        PsiExpression replacement = factory.createExpressionFromText(
                "\"{" + newElementName + "}\"",
                myElement
        );
        return myElement.replace(replacement);
    }

    @Override
    public Object @NotNull [] getVariants() {
        return PropertyKeysUtil.getAllKeys(myElement.getProject()).stream()
                .map(propertyKey -> PropertyKeyLookupElements.create(myElement.getProject(), propertyKey))
                .toArray(LookupElement[]::new);
    }
}
