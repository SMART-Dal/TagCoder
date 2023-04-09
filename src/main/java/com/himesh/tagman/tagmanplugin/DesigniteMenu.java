package com.himesh.tagman.tagmanplugin;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DesigniteMenu extends ActionGroup {
    public DesigniteMenu() {
        super("_TagCoder", "Pass code to TagCoder",
                IconLoader.getIcon("Images/tagcoder_icon_16.png.png", DesigniteMenu.class));
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e) {
        return new AnAction[]{new DesigniteMenuRetrain(),new DesigniteMenuAnalyze(),
                new DesigniteMenuSettings()};
    }
}
