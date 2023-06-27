package com.femtioprocent.omega.lesson.machine;

import java.util.ArrayList;
import java.util.List;

public class ItemEntryVirtualList {
    public String type;
    public String tid;
    List items;
    //    public int ord;

    ItemEntryVirtualList() {
        items = new ArrayList();
    }

    int count(String s, char ch) {
        int a = 0;
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) == ch)
                a++;
        return a;
    }

    void addMore(ItemEntry it_ent) {
    }

    public Item getItemAt(int ix) {
        if (ix >= items.size())
            return null;
        return (Item) items.get(ix);
    }

    public int howManyItems() {
        return items.size();
    }

    public String toString() {
        return "ItemEntryList{" + type +
                ", Tid=" + tid +
                ", items=" + items +
                "}";
    }
}
