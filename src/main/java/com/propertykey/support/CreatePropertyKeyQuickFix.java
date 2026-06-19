package com.propertykey.support;

import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Creates a missing property key in {@code messages.properties}.
 */
public class CreatePropertyKeyQuickFix extends LocalQuickFixOnPsiElement {

    private final String key;

    public CreatePropertyKeyQuickFix(@NotNull PsiLiteralExpression element, @NotNull String key) {
        super(element);
        this.key = key;
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Create property in messages.properties";
    }

    @Override
    public @NotNull String getText() {
        return "Create property '" + key + "' in messages.properties";
    }

    @Override
    public void invoke(
            @NotNull Project project,
            @NotNull PsiFile file,
            @NotNull PsiElement startElement,
            @NotNull PsiElement endElement
    ) {
        WriteCommandAction.runWriteCommandAction(project, getFamilyName(), null, () -> {
            PropertiesFile propertiesFile = findOrCreateMessagePropertyFile(project);
            if (propertiesFile != null) {
                appendProperty(propertiesFile, key);
            }
        });
    }

    private static @Nullable PropertiesFile findOrCreateMessagePropertyFile(@NotNull Project project) {
        PropertiesFile existing = PropertyKeysUtil.findPrimaryMessagePropertyFile(project);
        if (existing != null) {
            return existing;
        }

        VirtualFile resourcesDir = PropertyKeysUtil.findResourcesDirectory(project);
        if (resourcesDir == null) {
            return null;
        }

        VirtualFile propertyFile = resourcesDir.findChild("messages.properties");
        if (propertyFile == null) {
            try {
                propertyFile = resourcesDir.createChildData(CreatePropertyKeyQuickFix.class, "messages.properties");
            } catch (Exception ignored) {
                return null;
            }
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(propertyFile);
        return psiFile instanceof PropertiesFile propertiesFile ? propertiesFile : null;
    }

    private static void appendProperty(@NotNull PropertiesFile propertiesFile, @NotNull String key) {
        Document document = PsiDocumentManager.getInstance(propertiesFile.getProject())
                .getDocument((PsiFile) propertiesFile);
        if (document == null) {
            return;
        }

        String insertion = document.getTextLength() == 0
                ? key + "=\n"
                : "\n" + key + "=\n";
        document.insertString(document.getTextLength(), insertion);
        PsiDocumentManager.getInstance(propertiesFile.getProject()).commitDocument(document);
    }
}
