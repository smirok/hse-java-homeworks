package ru.hse.java.trie;

import java.util.*;

/**
 * Implementation of common trie.
 * Interface : add, remove, contains, howManyStartsWithPrefix, nextString.
 */
public class TrieImpl implements Trie {

    private static class TrieNode {
        private final HashMap<Character, TrieNode> nextNodes = new HashMap<>();
        private boolean isEnd = false;

        private int countWordsInSubtree = 0;

        public void makeNextNode(Character symbol) {
            nextNodes.put(symbol, new TrieNode());
        }
    }

    private final TrieNode root = new TrieNode();

    /**
     * If the string is in a trie, method returns its number
     * Otherwise, method return string number if it was in a trie
     *
     * @param element
     * @return the number of the passed string among all trie
     * strings sorted lexicographically
     */
    private int getNumberByString(String element) {
        int result = 0;
        TrieNode currentNode = root;
        for (char symbol : element.toCharArray()) {
            for (Map.Entry<Character, TrieNode> x : currentNode.nextNodes.entrySet()) {
                if (x.getKey() < symbol) {
                    result += x.getValue().countWordsInSubtree;
                }
            }

            if (currentNode.nextNodes.containsKey(symbol)) {
                currentNode = currentNode.nextNodes.get(symbol);
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * @param pos
     * @return String at position pos among all trie
     * strings sorted lexicographically or Optional.empty() if pos greater than trie size
     */
    private Optional<String> findStringByNumber(int pos) {
        StringBuilder stringBuilder = new StringBuilder();
        TrieNode currentNode = root;

        while (pos > 0) {
            if (currentNode.isEnd) {
                pos--;
            }

            if (pos == 0) {
                break;
            }

            char sym = 'A';
            for (; sym <= 'z'; sym++) {
                if (currentNode.nextNodes.containsKey(sym)) {
                    int wordsInSymSubtree = currentNode.nextNodes.get(sym).countWordsInSubtree;
                    if (pos > wordsInSymSubtree)
                        pos -= wordsInSymSubtree;
                    else
                        break;
                }
            }

            if (sym > 'z')
                return Optional.empty();

            stringBuilder.append(sym);
            currentNode = currentNode.nextNodes.get(sym);
        }
        return Optional.of(stringBuilder.toString());
    }

    /**
     * Сheck that passed string consists of English letters
     *
     * @param element
     */
    private void checkInput(String element) {
        for (char symbol : element.toLowerCase().toCharArray()) {
            if (symbol < 'a' || symbol > 'z')
                throw new IllegalArgumentException("The string must contain only letters of the English alphabet");
        }
    }

    /**
     * The descent on trie along the passed string
     *
     * @param path
     * @return last node on path or Optional.empty() if string is absent in trie
     */
    private Optional<TrieNode> traverse(String path) {
        TrieNode currentNode = root;

        for (char symbol : path.toCharArray()) {
            if (currentNode.nextNodes.get(symbol) == null)
                return Optional.empty();

            currentNode = currentNode.nextNodes.get(symbol);
        }
        return Optional.of(currentNode);
    }

    @Override
    public boolean add(String element) {
        checkInput(element);

        if (contains(element))
            return false;

        root.countWordsInSubtree++;
        TrieNode currentNode = root;
        for (char symbol : element.toCharArray()) {
            if (currentNode.nextNodes.get(symbol) == null)
                currentNode.makeNextNode(symbol);

            currentNode = currentNode.nextNodes.get(symbol);
            currentNode.countWordsInSubtree++;
        }
        currentNode.isEnd = true;

        return true;
    }

    @Override
    public boolean contains(String element) {
        Optional<TrieNode> optionalFinishNode = traverse(element);
        return optionalFinishNode.isPresent() && optionalFinishNode.get().isEnd;
    }

    @Override
    public boolean remove(String element) {
        if (!contains(element))
            return false;

        root.countWordsInSubtree--;
        TrieNode currentNode = root;
        for (char symbol : element.toCharArray()) {
            currentNode = currentNode.nextNodes.get(symbol);
            currentNode.countWordsInSubtree--;
        }
        currentNode.isEnd = false;

        return true;
    }

    @Override
    public int size() {
        return root.countWordsInSubtree;
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        Optional<TrieNode> optionalFinishNode = traverse(prefix);
        return optionalFinishNode.isEmpty() ? 0 : optionalFinishNode.get().countWordsInSubtree;
    }

    @Override
    public String nextString(String element, int k) {
        boolean isContains = contains(element);
        if (!isContains && k == 0) {
            return null;
        }
        Optional<String> optionalS = findStringByNumber(getNumberByString(element) + k + (isContains ? 1 : 0));
        return optionalS.isEmpty() ? null : optionalS.get();
    }
}