package com.propertykey.support;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * Resolves and indexes keys from {@code messages.properties} files in the current project.
 */
final class PropertyKeysUtil {

    private static final String MESSAGES_FILE = "messages.properties";

    private PropertyKeysUtil() {
    }

    /**
     * Returns the property key from a braced message value such as {@code {name.required}}.
     */
    static @Nullable String extractKey(@NotNull String text) {
        if (!text.startsWith("{") || !text.endsWith("}") || text.length() < 3) {
            return null;
        }
        return text.substring(1, text.length() - 1);
    }

    /**
     * Locates the string literal for a {@code message} annotation attribute at or around {@code element}.
     */
    static @Nullable PsiLiteralExpression getMessageLiteral(@NotNull PsiElement element) {
        PsiLiteralExpression literal = element instanceof PsiLiteralExpression
                ? (PsiLiteralExpression) element
                : PsiTreeUtil.getParentOfType(element, PsiLiteralExpression.class, false);
        if (literal == null) {
            return null;
        }
        PsiElement parent = literal.getParent();
        if (!(parent instanceof PsiNameValuePair pair) || !"message".equals(pair.getName())) {
            return null;
        }
        return literal;
    }

    static @NotNull Collection<PropertiesFile> findMessagePropertyFiles(@NotNull Project project) {
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        Collection<VirtualFile> virtualFiles =
                FilenameIndex.getVirtualFilesByName(MESSAGES_FILE, scope);

        PsiManager psiManager = PsiManager.getInstance(project);
        List<PropertiesFile> files = new ArrayList<>();
        for (VirtualFile virtualFile : virtualFiles) {
            var psiFile = psiManager.findFile(virtualFile);
            if (psiFile instanceof PropertiesFile propertiesFile) {
                files.add(propertiesFile);
            }
        }
        return files;
    }

    static @Nullable IProperty findProperty(@NotNull Project project, @NotNull String key) {
        for (PropertiesFile file : findMessagePropertyFiles(project)) {
            IProperty property = file.findPropertyByKey(key);
            if (property != null) {
                return property;
            }
            // Some projects store the braces in the property key itself.
            property = file.findPropertyByKey("{" + key + "}");
            if (property != null) {
                return property;
            }
        }
        return null;
    }

    static @NotNull List<String> getAllKeys(@NotNull Project project) {
        TreeSet<String> keys = new TreeSet<>();
        for (PropertiesFile file : findMessagePropertyFiles(project)) {
            for (IProperty property : file.getProperties()) {
                String key = property.getKey();
                if (key == null || key.isBlank()) {
                    continue;
                }
                if (key.startsWith("{") && key.endsWith("}")) {
                    keys.add(key.substring(1, key.length() - 1));
                } else {
                    keys.add(key);
                }
            }
        }
        return List.copyOf(keys);
    }
}
