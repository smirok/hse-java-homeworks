package ru.hse.java.trie;

import java.util.TreeSet;

public class StressTreeSetWrapper {

    private TreeSet<String> tree = new TreeSet<>();

    public boolean add(String element) {
        return tree.add(element);
    }

    public boolean contains(String element) {
        return tree.contains(element);
    }

    public boolean remove(String element) {
        return tree.remove(element);
    }

    public int size() {
        return tree.size();
    }

    public int howManyStartsWithPrefix(String prefix) {
        int result = 0;
        for (String str : tree) {
            if (str.startsWith(prefix))
                result++;
        }
        return result;
    }

    public String nextString(String element, int k) {
        if (k == 0) {
            if (tree.contains(element))
                return element;
            else
                return null;
        } else {
            for (int i = 0; i < k; i++) {
                element = tree.higher(element);
                if (element == null)
                    return null;
            }
            return element;
        }
    }
}
