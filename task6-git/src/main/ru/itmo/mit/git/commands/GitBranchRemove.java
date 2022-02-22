package ru.itmo.mit.git.commands;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.utils.GitPaths;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class GitBranchRemove {
    public static void branch_remove(@NotNull String branchName) throws GitException {
        FileUtils.deleteQuietly(new File(Paths.get(GitPaths.getHEADS().toString(), branchName).toString()));

        try {
            FileUtils.writeStringToFile(GitPaths.getBRANCH().toFile(), "master", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GitException("Cannot write to file", e);
        }
    }
}
