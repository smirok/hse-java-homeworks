package ru.itmo.mit.git;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.itmo.mit.git.commands.*;
import ru.itmo.mit.git.model.GitIndex;
import ru.itmo.mit.git.utils.GitPaths;

import java.io.*;
import java.util.*;

public class GitClient {
    private PrintStream outputStream;
    private final GitIndex index = new GitIndex();

    public GitClient(String workingDirectory) {
        GitPaths.initializePaths(workingDirectory);
    }

    public void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    public void init() throws GitException {
        GitInit.init(outputStream);
    }

    public void add(@NotNull List<@NotNull String> fileNames) throws GitException {
        GitAdd.add(index, fileNames);
    }

    public void rm(@NotNull List<@NotNull String> fileNames) throws GitException {
        GitRemove.rm(index, fileNames);
    }

    public void status() throws GitException {
        GitStatus.status(index, outputStream);
    }

    public void commit(@NotNull String message) throws GitException {
        GitCommit.commit(index, message);
    }

    public void log(@Nullable String commitHash) throws GitException {
        GitLog.log(commitHash, outputStream);
    }

    public void reset(@NotNull String toRevision) throws GitException {
        GitReset.reset(index, toRevision);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void checkout(@NotNull String toRevision, Optional<String> branchToCheckout) throws GitException {
        GitCheckout.checkout(index, outputStream, toRevision, branchToCheckout);
    }

    public void checkoutFiles(List<@NotNull String> filesToCheckout) throws GitException {
        GitCheckout.checkoutFiles(index, filesToCheckout);
    }

    public void branch_create(@NotNull String branchName) throws GitException {
        GitBranchCreate.branch_create(branchName);
    }

    public void branch_remove(@NotNull String branchName) throws GitException {
        GitBranchRemove.branch_remove(branchName);
    }

    public void show_branches() throws GitException {
        GitShowBranches.show_branches(outputStream);
    }

    public void merge(@NotNull String branchToMerge) throws GitException {
        GitMerge.merge(branchToMerge, outputStream);
    }
}
