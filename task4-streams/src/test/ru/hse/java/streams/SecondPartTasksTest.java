package ru.hse.java.streams;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SecondPartTasksTest {

    private final String[] lines = {
            "Money's the motivation\n",
            "Money's the conversation\n",
            "You on vacation, we gettin' paid so\n",
            "We on paid-cation, I did it for the fam\n",
            "It's whatever we had to do, it's just who I am\n",
            "Yeah, it's the life I chose\n",
            "Gunshots in the dark, one eye closed\n",
            "And we got it cookin' like a one-eyed stove\n",
            "You can catch me kissin' my girl with both eye' closed\n",
            "Perfectin' my passion, thanks for askin'\n",
            "Couldn't slow down, so we had to crash it\n",
            "You use plastic, we 'bout cash\n",
            "I see some people ahead that we gon' pass, yeah!\n",
            "\n"
    };


    @Test
    public void testFindQuotes() throws IOException {

        List<String> fileNames = new ArrayList<>();

        for (int i = 0; i < lines.length / 2; i++) {
            File file = File.createTempFile("testQuote", ".txt");

            fileNames.add(file.getPath());

            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                fileOutputStream.write(lines[2 * i].getBytes());
                fileOutputStream.write(lines[2 * i + 1].getBytes());
            }
        }

        CharSequence pattern = "eye";

        List<String> answer = SecondPartTasks.findQuotes(fileNames, pattern);
        assertEquals(answer.size(), 3);
        assertEquals(answer.get(0), lines[6].substring(0, lines[6].length() - 1));
        assertEquals(answer.get(1), lines[7].substring(0, lines[7].length() - 1));
        assertEquals(answer.get(2), lines[8].substring(0, lines[8].length() - 1));
    }

    @Test
    public void testFindQuotesThrow() throws IOException {
        List<String> fileNames = new ArrayList<>();

        for (int i = 0; i < lines.length / 2; i++) {
            fileNames.add("file" + i);
        }

        CharSequence pattern = "eye";

        assertThrows(IOException.class, () -> SecondPartTasks.findQuotes(fileNames, pattern));
    }

    @Test
    public void testPiDividedBy4() {
        assertEquals(SecondPartTasks.piDividedBy4(100), Math.PI / 4, 0.1);
        assertEquals(SecondPartTasks.piDividedBy4(), Math.PI / 4, 0.01);
        assertEquals(SecondPartTasks.piDividedBy4(1_000_000), Math.PI / 4, 0.001);
        assertEquals(SecondPartTasks.piDividedBy4(10_000_000), Math.PI / 4, 0.0005);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> compositions = new TreeMap<>();
        compositions.put("Alexander Pushkin",
                Arrays.asList("Ruslan i Ludmila", "Stantsionny smotritel", "Kapitanskaya dochka", "Dubrovsky"));
        compositions.put("Mikhail Lermontov",
                Arrays.asList("Two Brothers", "Borodino", "Mtsyri"));
        compositions.put("J.R.R. Tolkien", Arrays.asList("The Lord of the Rings", "The Hobbit"));

        assertEquals(SecondPartTasks.findPrinter(compositions), "Alexander Pushkin");
    }

    @Test
    public void testFindPrinterEmpty() {
        assertEquals(SecondPartTasks.findPrinter(new TreeMap<>()), "Nobody");
    }

    @Test
    public void testCalculateGlobalOrder() {
        List<Map<String, Integer>> ordersList = new ArrayList<>();
        ordersList.add(
                Map.ofEntries(
                        Map.entry("potato", 10),
                        Map.entry("carrot", 25),
                        Map.entry("wine", 10)
                )
        );
        ordersList.add(
                Map.ofEntries(
                        Map.entry("beer", 100),
                        Map.entry("potato", 20),
                        Map.entry("wine", 15)
                )
        );
        ordersList.add(
                Map.ofEntries(
                        Map.entry("beer", 10),
                        Map.entry("potato", 35),
                        Map.entry("wine", 15),
                        Map.entry("carrot", 30),
                        Map.entry("chevapchichi", 1000)
                )
        );

        Map<String, Integer> answer = SecondPartTasks.calculateGlobalOrder(ordersList);
        assertEquals(answer.size(), 5);
        assertEquals(answer.get("beer"), 110);
        assertEquals(answer.get("wine"), 40);
        assertEquals(answer.get("potato"), 65);
        assertEquals(answer.get("carrot"), 55);
        assertEquals(answer.get("chevapchichi"), 1000);
        assertNull(answer.get("tomato"));
    }
}