package ru.itmo.mit.git.commands;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.model.BranchMerger;

import java.io.PrintStream;

public class GitMerge {
    public static void merge(@NotNull String branchToMerge, PrintStream outputStream) throws GitException {
        BranchMerger branchMerger = new BranchMerger();
        branchMerger.merge(branchToMerge, outputStream);
    }
}
