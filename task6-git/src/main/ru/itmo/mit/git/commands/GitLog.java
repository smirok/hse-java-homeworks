package ru.itmo.mit.git.commands;

import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.commands.utils.GitUtil;
import ru.itmo.mit.git.utils.JSONConstants;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class GitLog {
    public static void log(@Nullable String commitHash, PrintStream outputStream) throws GitException {
        if (Objects.equals(null, commitHash)) {
            commitHash = GitUtil.getHashHeadCommit();
        }

        Queue<String> queueHashes = new LinkedList<>();
        queueHashes.add(commitHash);
        HashSet<String> printedCommits = new HashSet<>();

        while (!queueHashes.isEmpty()) {
            String currentHash = queueHashes.remove();

            JSONObject jsonObject = GitUtil.getJSONOBjectFromCommit(currentHash);

            String author = (String) jsonObject.get(JSONConstants.AUTHOR);
            String date = (String) jsonObject.get(JSONConstants.DATE);
            String message = (String) jsonObject.get(JSONConstants.MESSAGE);

            outputStream.println("commit " + currentHash);
            outputStream.println("Author: " + author);
            outputStream.println("Date: " + date);
            outputStream.println();
            outputStream.println("\t" + message);
            outputStream.println();

            printedCommits.add(currentHash);

            JSONArray parents = (JSONArray) jsonObject.get(JSONConstants.PARENTS);
            if (!Objects.equals(null, parents)) {
                for (Object objectHash : parents) {
                    String hash = (String) objectHash;
                    if (!printedCommits.contains(hash)) {
                        queueHashes.add(hash);
                    }
                }
            }
        }
    }

}
