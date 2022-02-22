package ru.itmo.mit.git.commands;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.commands.utils.GitUtil;
import ru.itmo.mit.git.model.CommitCreator;
import ru.itmo.mit.git.model.GitIndex;
import ru.itmo.mit.git.utils.GitPaths;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class GitReset {
    public static void reset(GitIndex index, @NotNull String toRevision) throws GitException {
        try {
            FileUtils.writeStringToFile(GitPaths.getHEAD().toFile(), toRevision, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GitException("Cannot write to file", e);
        }

        CommitCreator.updateFileHeadOfCurrentBranch(toRevision);

        // разворачиваем состояние
        index.readIndex();
        index.searchUntrackedFiles();
        GitUtil.cleanDirectory(index, null);
        index.clearState();

        JSONParser parser = new JSONParser();

        JSONObject jsonObject;
        try {
            jsonObject = (JSONObject) parser.parse(
                    new FileReader(Paths.get(GitPaths.getOBJECTS().toString(), toRevision + ".json").toFile())
            );
        } catch (ParseException e) {
            throw new GitException("Cannot parse json", e);
        } catch (IOException e) {
            throw new GitException("Cannot read file", e);
        }

        String treeRootHash = (String) jsonObject.get("tree");

        try {
            GitUtil.deployFiles(index, treeRootHash);
        } catch (IOException e) {
            throw new GitException("Cannot deploy compressed files");
        }

        index.writeIndex();
    }
}
