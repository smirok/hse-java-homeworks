package ru.itmo.mit.git.commands;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.model.CommitCreator;
import ru.itmo.mit.git.model.GitIndex;

public class GitCommit {
    public static void commit(GitIndex index, @NotNull String message) throws GitException {
        CommitCreator commitCreator = new CommitCreator(index, message);
        commitCreator.commit();
    }
}
