package com.github.x6ud.puppetview.misc;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

public class MenuBuilder {

    public static PopupMenu popup() {
        return new PopupMenu();
    }

    public static MenuItem item(String label, ActionListener actionListener) {
        MenuItem menuItem = new MenuItem(label);
        menuItem.addActionListener(actionListener);
        return menuItem;
    }

    public static CheckboxMenuItem checkbox(String label, boolean state, ItemListener itemListener) {
        CheckboxMenuItem checkboxMenuItem = new CheckboxMenuItem(label);
        checkboxMenuItem.setState(state);
        checkboxMenuItem.addItemListener(itemListener);
        return checkboxMenuItem;
    }

}
