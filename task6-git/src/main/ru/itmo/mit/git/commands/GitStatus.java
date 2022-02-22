package ru.itmo.mit.git.commands;

import org.apache.commons.io.FileUtils;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.model.GitIndex;
import ru.itmo.mit.git.utils.GitPaths;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GitStatus {
    public static void status(GitIndex index, PrintStream outputStream) throws GitException {
        index.clearState();
        index.readIndex();
        index.searchUntrackedFiles();
        index.updateModifying();

        String currentBranch;
        try {
            currentBranch = FileUtils.readFileToString(GitPaths.getBRANCH().toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GitException("Cannot read branch from file", e);
        }

        outputStream.println("На ветке " + currentBranch);

        boolean everythingIsUpToDate = true;
        if (!index.getAddedFiles().isEmpty() || !index.getRemovedFiles().isEmpty()) {
            everythingIsUpToDate = false;

            outputStream.println("Изменения, которые будут включены в коммит:\n" +
                    "  (используйте «git rm <файл>...», чтобы убрать из индекса)");

            for (String fileName : index.getAddedFiles().keySet()) {
                if (index.getTrackedFiles().containsKey(fileName)) {
                    outputStream.println("\tновый файл:\t" + fileName);
                } else {
                    outputStream.println("\tизменено:\t" + fileName);
                }
            }

            for (String fileName : index.getRemovedFiles().keySet()) {
                outputStream.println("\tудалено:\t" + fileName);
            }
        }

        List<String> modifiedTrackedFiles = new ArrayList<>(index.getModifiedTrackedFiles().keySet());

        if (!modifiedTrackedFiles.isEmpty()) {
            everythingIsUpToDate = false;
            outputStream.println("Изменения, которые не в индексе для коммита:");

            modifiedTrackedFiles.forEach(fileName -> outputStream.println("\tизменено:\t" + fileName));
        }

        if (!index.getUntrackedFiles().isEmpty()) {
            everythingIsUpToDate = false;
            outputStream.println("Неотслеживаемые файлы:");

            index.getUntrackedFiles().keySet().forEach(fileName -> outputStream.println("\t" + fileName));
        }

        if (everythingIsUpToDate) {
            outputStream.println("нечего коммитить, нет изменений в рабочем каталоге");
        }
    }
}
