package ru.itmo.mit.git.commands;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.commands.utils.GitUtil;
import ru.itmo.mit.git.utils.GitPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Objects;

public class GitBranchCreate {
    public static void branch_create(@NotNull String branchName) throws GitException {
        String headCommitHash = GitUtil.getHashHeadCommit();

        try {
            FileUtils.writeStringToFile(Paths.get(GitPaths.getHEADS().toString(), branchName).toFile(),
                    Objects.equals(null, headCommitHash) ? "" : headCommitHash,
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GitException("Cannot write to file", e);
        }

        try {
            FileUtils.writeStringToFile(GitPaths.getBRANCH().toFile(), branchName, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GitException("Cannot write to file", e);
        }
    }
}
