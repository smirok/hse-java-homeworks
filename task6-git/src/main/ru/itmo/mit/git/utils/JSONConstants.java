package ru.itmo.mit.git.utils;

import org.jetbrains.annotations.NotNull;

public final class JSONConstants {
    private JSONConstants() {
    }

    public static final @NotNull String ADDED = "added";
    public static final @NotNull String REMOVED = "removed";
    public static final @NotNull String TRACKED = "tracked";
    public static final @NotNull String FILENAME = "filename";
    public static final @NotNull String HASH = "hash";

    public static final @NotNull String TREE = "tree";
    public static final @NotNull String PARENTS = "parents";
    public static final @NotNull String AUTHOR = "author";
    public static final @NotNull String DATE = "date";
    public static final @NotNull String MESSAGE = "message";

}
