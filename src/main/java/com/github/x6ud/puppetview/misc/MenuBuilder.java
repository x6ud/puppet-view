package com.github.x6ud.puppetview.misc;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MenuBuilder {

    public static PopupMenu popup() {
        return new PopupMenu();
    }

    public static Menu menu(Menu parent, String label) {
        Menu menu = new Menu(label);
        parent.add(menu);
        return menu;
    }

    public static MenuItem item(Menu menu, String label, ActionListener actionListener) {
        MenuItem menuItem = new MenuItem(label);
        menuItem.addActionListener(actionListener);
        menu.add(menuItem);
        return menuItem;
    }

    public static CheckboxMenuItem checkbox(Menu menu, String label, boolean state, ItemListener itemListener) {
        CheckboxMenuItem checkboxMenuItem = new CheckboxMenuItem(label);
        checkboxMenuItem.setState(state);
        checkboxMenuItem.addItemListener(itemListener);
        menu.add(checkboxMenuItem);
        return checkboxMenuItem;
    }

    private static class RadioMenuItem {
        public CheckboxMenuItem menuItem;
        public String value;

        public RadioMenuItem(CheckboxMenuItem menuItem, String value) {
            this.menuItem = menuItem;
            this.value = value;
        }
    }

    public static class RadioGroup {
        private final Menu menu;
        private final String value;
        private final Consumer<String> callback;
        private final List<RadioMenuItem> items = new ArrayList<>();

        public RadioGroup(Menu menu, String value, Consumer<String> callback) {
            this.menu = menu;
            this.value = value;
            this.callback = callback;
        }

        public RadioGroup item(String label, String value) {
            this.items.add(new RadioMenuItem(
                    MenuBuilder.checkbox(menu, label, this.value.equals(value), e -> {
                        callback.accept(value);
                        for (RadioMenuItem item : items) {
                            item.menuItem.setState(item.value.equals(value));
                        }
                    }),
                    value
            ));
            return this;
        }
    }

    public static RadioGroup radioGroup(Menu menu, String value, Consumer<String> callback) {
        return new RadioGroup(menu, value, callback);
    }

}
