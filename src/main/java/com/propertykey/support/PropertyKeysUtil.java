package com.propertykey.support;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Resolves and indexes keys from message property files in the current project.
 */
final class PropertyKeysUtil {

    private static final List<String> EXACT_MESSAGE_FILES = List.of(
            "messages.properties",
            "ValidationMessages.properties"
    );

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

    static boolean isMessagePropertyFileName(@NotNull String fileName) {
        if (EXACT_MESSAGE_FILES.contains(fileName)) {
            return true;
        }
        return fileName.startsWith("messages_") && fileName.endsWith(".properties");
    }

    static @NotNull Collection<PropertiesFile> findMessagePropertyFiles(@NotNull Project project) {
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        Set<VirtualFile> virtualFiles = new LinkedHashSet<>();

        for (String fileName : EXACT_MESSAGE_FILES) {
            virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(fileName, scope));
        }

        for (VirtualFile virtualFile : FilenameIndex.getAllFilesByExt(project, "properties", scope)) {
            if (isMessagePropertyFileName(virtualFile.getName())) {
                virtualFiles.add(virtualFile);
            }
        }

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
        PropertyKeyInfo info = findPropertyInfo(project, key);
        return info != null ? info.property() : null;
    }

    static @Nullable PropertyKeyInfo findPropertyInfo(@NotNull Project project, @NotNull String key) {
        for (PropertiesFile file : findMessagePropertyFiles(project)) {
            IProperty property = file.findPropertyByKey(key);
            if (property != null) {
                return new PropertyKeyInfo(key, property, file);
            }
            property = file.findPropertyByKey("{" + key + "}");
            if (property != null) {
                return new PropertyKeyInfo(key, property, file);
            }
        }
        return null;
    }

    static @Nullable String getPropertyValue(@NotNull IProperty property) {
        String value = property.getValue();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.replace('\n', ' ').trim();
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

    /**
     * Preferred target for creating a new property: {@code messages.properties} when present,
     * otherwise the first indexed message property file.
     */
    static @Nullable PropertiesFile findPrimaryMessagePropertyFile(@NotNull Project project) {
        for (PropertiesFile file : findMessagePropertyFiles(project)) {
            if ("messages.properties".equals(file.getName())) {
                return file;
            }
        }
        Collection<PropertiesFile> files = findMessagePropertyFiles(project);
        return files.isEmpty() ? null : files.iterator().next();
    }

    /**
     * Returns the best {@code src/main/resources} directory for creating {@code messages.properties}.
     */
    static @Nullable VirtualFile findResourcesDirectory(@NotNull Project project) {
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            for (VirtualFile root : ModuleRootManager.getInstance(module).getSourceRoots(false)) {
                VirtualFile resources = root.getParent().findChild("resources");
                if (resources != null && resources.isDirectory()) {
                    return resources;
                }
            }
        }
        return null;
    }

    record PropertyKeyInfo(@NotNull String key, @NotNull IProperty property, @NotNull PropertiesFile file) {
        @NotNull String sourceFileName() {
            return file.getName();
        }
    }
}
