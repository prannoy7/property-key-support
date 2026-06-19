package com.propertykey.support;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Builds lookup elements for property keys with file location and message preview.
 */
final class PropertyKeyLookupElements {

    private PropertyKeyLookupElements() {
    }

    static @NotNull LookupElementBuilder create(@NotNull Project project, @NotNull String key) {
        LookupElementBuilder builder = LookupElementBuilder.create(key)
                .withPresentableText("{" + key + "}");

        PropertyKeysUtil.PropertyKeyInfo info = PropertyKeysUtil.findPropertyInfo(project, key);
        if (info != null) {
            builder = builder.withTypeText(info.sourceFileName(), true);
            String value = PropertyKeysUtil.getPropertyValue(info.property());
            if (value != null) {
                builder = builder.withTailText("  " + truncate(value), true);
            }
        } else {
            builder = builder.withTypeText("messages.properties", true);
        }

        return builder;
    }

    private static @NotNull String truncate(@NotNull String value) {
        return value.length() <= 60 ? value : value.substring(0, 57) + "...";
    }
}
