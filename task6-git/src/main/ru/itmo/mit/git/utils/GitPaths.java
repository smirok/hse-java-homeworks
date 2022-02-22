package ru.itmo.mit.git.utils;

import java.io.File;
import java.nio.file.Path;

public class GitPaths {
    private static Path WORKING_DIRECTORY;
    private static Path GIT_DIRECTORY;
    private static Path OBJECTS;
    private static Path REFS;
    private static Path HEADS; // папка с файлами(имя файла - имя ветки), внутри файла - head ветки
    private static Path HEAD; // текущая голова
    private static Path INDEX; // текущий индекс
    private static Path BRANCH; // текущая ветка
    private static Path DETACHED_HEAD; // если файл есть, то голова detached, иначе нет
    private static Path MASTER_HEAD; // если файл есть, то голова detached, иначе нет

    public static Path getWorkingDirectory() {
        return WORKING_DIRECTORY;
    }

    public static Path getGitDirectory() {
        return GIT_DIRECTORY;
    }

    public static Path getOBJECTS() {
        return OBJECTS;
    }

    public static Path getREFS() {
        return REFS;
    }

    public static Path getHEADS() {
        return HEADS;
    }

    public static Path getHEAD() {
        return HEAD;
    }

    public static Path getINDEX() {
        return INDEX;
    }

    public static Path getBRANCH() {
        return BRANCH;
    }

    public static Path getDetachedHead() {
        return DETACHED_HEAD;
    }

    public static Path getMasterHead() {
        return MASTER_HEAD;
    }

    public static void initializePaths(String workingDirectory) {
        WORKING_DIRECTORY = Path.of(workingDirectory);
        GIT_DIRECTORY = Path.of(WORKING_DIRECTORY + File.separator + ".hsegit");
        OBJECTS = Path.of(GIT_DIRECTORY + File.separator + "objects");
        REFS = Path.of(GIT_DIRECTORY + File.separator + "refs");
        HEADS = Path.of(REFS + File.separator + "heads");
        HEAD = Path.of(GIT_DIRECTORY + File.separator + "HEAD");
        DETACHED_HEAD = Path.of(GIT_DIRECTORY + File.separator + "DETACHED_HEAD");
        INDEX = Path.of(GIT_DIRECTORY + File.separator + "index.json");
        BRANCH = Path.of(GIT_DIRECTORY + File.separator + "branch");
        MASTER_HEAD = Path.of(HEADS + File.separator + "master");
    }
}
