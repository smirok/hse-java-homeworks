package ru.hse.java.trie;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.Random;

public class TrieTest {

    private TrieImpl trie;
    private StressTreeSetWrapper stressTree;
    private String[] buffer;

    private static final int STRING_LENGTH = 15;
    private static final int TIME_TEST_SIZE = 100_000;
    private static final int STRESS_TEST_SIZE = 10_000;

    private void generateRandomStrings(int bufferSize) {
        Random random = new Random();
        buffer = new String[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < STRING_LENGTH; j++) {
                stringBuilder.append((char) (random.nextInt(26) + 'a'));
            }
            buffer[i] = stringBuilder.toString();
        }
    }

    @BeforeEach
    public void beforeEachTest() {
        trie = new TrieImpl();
        stressTree = new StressTreeSetWrapper();
    }

    @Test
    public void testCreation() {
        assertEquals(trie.size(), 0);
    }

    @Test
    public void testAddOneString() {
        assertTrue(trie.add("AbAca"));
        assertEquals(trie.size(), 1);
    }

    @Test
    public void testAddThreeUniqueStrings() {
        assertTrue(trie.add("abC"));
        assertTrue(trie.add("CDE"));
        assertTrue(trie.add("Umasd"));
        assertEquals(trie.size(), 3);
    }

    @Test
    public void testAddEmptyString() {
        assertTrue(trie.add(""));
        assertEquals(trie.size(), 1);
    }

    @Test
    public void testAddStringsWithCommonPrefix() {
        assertTrue(trie.add(""));
        assertTrue(trie.add("a"));
        assertTrue(trie.add("ab"));
        assertEquals(trie.size(), 3);
    }

    @Test
    public void testAddEqualStrings() {
        assertTrue(trie.add("abc"));
        assertEquals(trie.size(), 1);
        assertFalse(trie.add("abc"));
        assertEquals(trie.size(), 1);
    }

    @Test
    public void testIllegalArgument() {
        assertThrows(
                IllegalArgumentException.class,
                () -> trie.add("приветики")
        );
    }

    @Test
    public void testRemoveExistingString() {
        assertTrue(trie.add("ae"));
        assertTrue(trie.remove("ae"));
        assertEquals(trie.size(), 0);
    }

    @Test
    public void testRemoveNonExistentString() {
        assertTrue(trie.add("abc"));
        assertTrue(trie.add("cbm"));
        assertFalse(trie.remove("nonexist"));
        assertEquals(trie.size(), 2);
    }

    @Test
    public void testRemoveAfterRemove() {
        assertTrue(trie.add("abc"));
        assertTrue(trie.remove("abc"));
        assertFalse(trie.remove("abc"));
        assertEquals(trie.size(), 0);
    }

    @Test
    public void testRemoveAll() {
        assertTrue(trie.add("abc"));
        assertTrue(trie.add("cdeka"));
        assertTrue(trie.add("eft"));
        assertTrue(trie.remove("abc"));
        assertTrue(trie.remove("eft"));
        assertTrue(trie.remove("cdeka"));
        assertEquals(trie.size(), 0);
    }

    @Test
    public void testContainsEmptyString() {
        assertFalse(trie.contains(""));
    }

    @Test
    public void testContainsExistingString() {
        assertTrue(trie.add("abc"));
        assertTrue(trie.contains("abc"));
    }

    @Test
    public void testContains() {
        assertTrue(trie.add("abracadabra"));
        assertFalse(trie.contains("abracadabr"));
        assertTrue(trie.contains("abracadabra"));
        assertFalse(trie.contains("abracadabrac"));
        assertFalse(trie.contains(""));
    }

    @Test
    public void testContainsAfterRemove() {
        assertTrue(trie.add("unit"));
        assertTrue(trie.add("g"));
        assertTrue(trie.add("cde"));
        assertTrue(trie.contains("g"));
        assertFalse(trie.contains("cd"));
        assertTrue(trie.contains("unit"));
        assertTrue(trie.remove("unit"));
        assertFalse(trie.contains("unit"));
    }

    @Test
    public void testSizeEmptyTrie() {
        assertEquals(trie.size(), 0);
    }

    @Test
    public void testSizeAfterAddEqualStrings() {
        assertTrue(trie.add("apk"));
        assertFalse(trie.add("apk"));
        assertFalse(trie.add("apk"));
        assertEquals(trie.size(), 1);
    }

    @Test
    public void testSizeAfterDoubleRemove() {
        assertTrue(trie.add("jdk"));
        assertTrue(trie.remove("jdk"));
        assertFalse(trie.remove("jdk"));
        assertEquals(trie.size(), 0);
    }

    @Test
    public void testSizeAfterRemoveInEmptyTrie() {
        assertFalse(trie.remove("smth"));
        assertFalse(trie.remove(""));
        assertEquals(trie.size(), 0);
    }

    @Test
    public void testSize() {
        assertTrue(trie.add("aq"));
        assertTrue(trie.add("clock"));
        assertFalse(trie.add("aq"));
        assertTrue(trie.add("mice"));
        assertEquals(trie.size(), 3);
        assertFalse(trie.remove("whatelse"));
        assertTrue(trie.remove("clock"));
        assertEquals(trie.size(), 2);
        assertFalse(trie.remove("clock"));
        assertEquals(trie.size(), 2);
        assertTrue(trie.remove("aq"));
        assertTrue(trie.remove("mice"));
        assertEquals(trie.size(), 0);
    }

    @Test
    public void testHowManyStartsWithPrefixEmpty() {
        assertEquals(trie.howManyStartsWithPrefix(""), 0);
        assertEquals(trie.howManyStartsWithPrefix("prefix"), 0);
    }

    @Test
    public void testEqualHowManyStartsWithEmptyANDSize() {
        assertEquals(trie.size(), trie.howManyStartsWithPrefix(""));
        assertTrue(trie.add("raiti"));
        assertTrue(trie.add("punch"));
        assertEquals(trie.size(), trie.howManyStartsWithPrefix(""));
        assertTrue(trie.remove("punch"));
        assertEquals(trie.size(), trie.howManyStartsWithPrefix(""));
    }

    @Test
    public void testHowManyStartsWithPrefixFull() {
        assertTrue(trie.add("abracadabra"));
        assertEquals(trie.howManyStartsWithPrefix("abracadabra"), 1);
    }

    @Test
    public void testHowManyStartsWithPrefix() {
        assertTrue(trie.add("aaaaa"));
        assertTrue(trie.add("aaabcde"));
        assertTrue(trie.add("aa"));
        assertTrue(trie.add("azaza"));
        assertTrue(trie.add("impl"));
        assertEquals(trie.howManyStartsWithPrefix("abracadabra"), 0);
        assertEquals(trie.howManyStartsWithPrefix(""), 5);
        assertEquals(trie.howManyStartsWithPrefix("a"), 4);
        assertEquals(trie.howManyStartsWithPrefix("aa"), 3);
        assertEquals(trie.howManyStartsWithPrefix("aaa"), 2);
        assertEquals(trie.howManyStartsWithPrefix("aaaa"), 1);
        assertEquals(trie.howManyStartsWithPrefix("aaaaa"), 1);
        assertEquals(trie.howManyStartsWithPrefix("imp"), 1);
    }

    @Test
    public void testHowManyStartsWithPrefixAfterRemove() {
        assertTrue(trie.add("aaaaa"));
        assertTrue(trie.add("aaabcde"));
        assertTrue(trie.add("aa"));
        assertTrue(trie.add("azaza"));
        assertTrue(trie.add("impl"));
        assertTrue(trie.remove("aa"));
        assertEquals(trie.howManyStartsWithPrefix("abracadabra"), 0);
        assertEquals(trie.howManyStartsWithPrefix(""), 4);
        assertEquals(trie.howManyStartsWithPrefix("a"), 3);
        assertTrue(trie.remove("aaaaa"));
        assertEquals(trie.howManyStartsWithPrefix("a"), 2);
        assertEquals(trie.howManyStartsWithPrefix(""), 3);
    }

    @Test
    public void testNextStringK0nonExistingString() {
        assertTrue(trie.add("abc"));
        assertNull(trie.nextString("ab", 0));
    }

    @Test
    public void testNextStringK0ExistingString() {
        assertTrue(trie.add("abc"));
        assertTrue(trie.add("kek"));
        assertEquals(trie.nextString("abc", 0), "abc");
    }

    @Test
    public void testNextStringKgt0ExistingStringNotNull() {
        assertTrue(trie.add("abc"));
        assertTrue(trie.add("kek"));
        assertTrue(trie.add("ABC"));
        assertEquals(trie.nextString("ABC", 2), "kek");
    }

    @Test
    public void testNextStringKgt0ExistingStringNull() {
        assertTrue(trie.add("abc"));
        assertTrue(trie.add("kek"));
        assertTrue(trie.add("ABC"));
        assertNull(trie.nextString("ABC", 3));
    }

    @Test
    public void testNextStringKgt0NonExistingStringNotNull() {
        assertTrue(trie.add("b"));
        assertTrue(trie.add("c"));
        assertTrue(trie.add("d"));
        assertEquals(trie.nextString("", 2), "c");
    }

    @Test
    public void testNextStringKgt0NonExistingStringNull() {
        assertTrue(trie.add("abc"));
        assertTrue(trie.add("kek"));
        assertTrue(trie.add("ABC"));
        assertNull(trie.nextString("q", 3));
    }

    @Test
    public void testStressAdd() {
        generateRandomStrings(STRESS_TEST_SIZE);
        for (int i = 0; i < STRESS_TEST_SIZE; i++) {
            assertEquals(trie.add(buffer[i]), stressTree.add(buffer[i]));
        }
        assertEquals(trie.size(), stressTree.size());
    }

    @Test
    public void testStressContains() {
        generateRandomStrings(STRESS_TEST_SIZE);
        for (int i = 0; i < STRESS_TEST_SIZE; i++) {
            assertEquals(trie.add(buffer[i]), stressTree.add(buffer[i]));
        }
        assertEquals(trie.size(), stressTree.size());
        for (int i = STRESS_TEST_SIZE - 1; i >= 0; i--) {
            assertEquals(trie.contains(buffer[i]), stressTree.contains(buffer[i]));
        }
    }

    @Test
    public void testStressRemove() {
        generateRandomStrings(STRESS_TEST_SIZE);
        for (int i = 0; i < STRESS_TEST_SIZE; i++) {
            assertEquals(trie.add(buffer[i]), stressTree.add(buffer[i]));
        }
        assertEquals(trie.size(), stressTree.size());
        for (int i = STRESS_TEST_SIZE - 1; i >= 0; i--) {
            assertEquals(trie.remove(buffer[i]), stressTree.remove(buffer[i]));
        }
        assertEquals(trie.size(), 0);
    }

    @Test
    public void testStressHowManyStartsWithPrefix() {
        generateRandomStrings(STRESS_TEST_SIZE);
        for (int i = 0; i < STRESS_TEST_SIZE; i++) {
            assertEquals(trie.add(buffer[i]), stressTree.add(buffer[i]));
        }
        assertEquals(trie.size(), stressTree.size());
        for (int i = STRESS_TEST_SIZE - 1; i >= 0; i--) {
            int prefixLength = i % STRING_LENGTH;
            assertEquals(trie.howManyStartsWithPrefix(buffer[i].substring(0, prefixLength)),
                    stressTree.howManyStartsWithPrefix(buffer[i].substring(0, prefixLength)));
        }
    }

    @Test
    public void testStressNextString() {
        generateRandomStrings(STRESS_TEST_SIZE);
        for (int i = 0; i < STRESS_TEST_SIZE; i++) {
            assertEquals(trie.add(buffer[i]), stressTree.add(buffer[i]));
        }
        assertEquals(trie.size(), stressTree.size());

        Random random = new Random();
        for (int i = 0; i <= STRESS_TEST_SIZE; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < STRING_LENGTH; j++) {
                stringBuilder.append((char) (random.nextInt(26) + 'a'));
            }
            int k = random.nextInt(STRESS_TEST_SIZE / 2);
            assertEquals(trie.nextString(stringBuilder.toString(), k),
                    stressTree.nextString(stringBuilder.toString(), k));
        }
    }

    @Test
    public void testTimeLimitAdd() {
        generateRandomStrings(TIME_TEST_SIZE);
        assertTimeout(Duration.ofMillis(1500), () -> {
            for (int i = 0; i < TIME_TEST_SIZE; i++)
                trie.add(buffer[i]);
        });
    }

    @Test
    public void testTimeLimitNextString() {
        Random random = new Random();
        for (int i = 0; i < TIME_TEST_SIZE; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < STRING_LENGTH; j++) {
                stringBuilder.append((char) (random.nextInt(26) + 'a'));
            }
            trie.add(stringBuilder.toString());
        }

        generateRandomStrings(TIME_TEST_SIZE);
        assertTimeout(Duration.ofSeconds(4), () -> {
            for (int i = 0; i < TIME_TEST_SIZE; i++)
                trie.nextString(buffer[i], random.nextInt(TIME_TEST_SIZE));
        });
    }
}
