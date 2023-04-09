package com.himesh.tagman.tagmanplugin;

import com.intellij.openapi.editor.markup.GutterIconRenderer;

import javax.swing.*;
import java.util.Objects;

public class MyGutterIconRenderer extends GutterIconRenderer {
    private Icon icon;

    public MyGutterIconRenderer(Icon icon) {
        this.icon = icon;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof MyGutterIconRenderer)) {
            return false;
        }
        MyGutterIconRenderer icon = (MyGutterIconRenderer) obj;

        return this.getIcon() == icon.getIcon();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getIcon());
    }
}
