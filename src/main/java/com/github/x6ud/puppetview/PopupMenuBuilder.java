package com.github.x6ud.puppetview;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.function.Consumer;

public class PopupMenuBuilder {

    private PopupMenu popupMenu = new PopupMenu();

    public PopupMenuBuilder menuItem(String label, ActionListener actionListener) {
        MenuItem menuItem = new MenuItem(label);
        menuItem.addActionListener(actionListener);
        popupMenu.add(menuItem);
        return this;
    }

    public PopupMenuBuilder checkboxMenuItem(String label, boolean state, ItemListener itemListener) {
        return checkboxMenuItem(label, state, null, itemListener);
    }

    public PopupMenuBuilder checkboxMenuItem(String label, boolean state, Consumer<CheckboxMenuItem> holder, ItemListener itemListener) {
        CheckboxMenuItem checkboxMenuItem = new CheckboxMenuItem(label);
        checkboxMenuItem.setState(state);
        checkboxMenuItem.addItemListener(itemListener);
        popupMenu.add(checkboxMenuItem);
        if (holder != null) {
            holder.accept(checkboxMenuItem);
        }
        return this;
    }

    public PopupMenuBuilder separator() {
        popupMenu.addSeparator();
        return this;
    }

    public PopupMenuBuilder label(String label) {
        popupMenu.add(label);
        return this;
    }

    public PopupMenu get() {
        return popupMenu;
    }

}
