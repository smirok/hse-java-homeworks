package ru.itmo.mit.git.commands;

import org.apache.commons.io.FileUtils;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.utils.GitPaths;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class GitShowBranches {
    public static void show_branches(PrintStream outputStream) throws GitException {
        String currentBranch;
        try {
            currentBranch = FileUtils.readFileToString(GitPaths.getBRANCH().toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GitException("Cannot read file", e);
        }

        List<String> branches;
        try {
            branches = Files.walk(GitPaths.getHEADS())
                    .map(path -> GitPaths.getHEADS().relativize(path).toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new GitException("Cannot write to file", e);
        }

        for (String branchName : branches) {
            if (branchName.equals(currentBranch)) {
                outputStream.print("* ");
            } else {
                outputStream.print("  ");
            }
            outputStream.println(branchName);
        }
    }
}
