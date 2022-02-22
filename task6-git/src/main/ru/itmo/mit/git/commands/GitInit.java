package ru.itmo.mit.git.commands;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.utils.GitPaths;
import ru.itmo.mit.git.utils.JSONConstants;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitInit {

    @SuppressWarnings("unchecked")
    public static void init(PrintStream outputStream) throws GitException {
        try {
            if (!Files.exists(GitPaths.getWorkingDirectory())) {
                Files.createDirectories(GitPaths.getWorkingDirectory());
            }
            if (!Files.exists(GitPaths.getGitDirectory())) {
                Files.createDirectories(GitPaths.getGitDirectory());
            }
            if (!Files.exists(GitPaths.getOBJECTS())) {
                Files.createDirectories(GitPaths.getOBJECTS());
            }
            if (!Files.exists(GitPaths.getREFS())) {
                Files.createDirectories(GitPaths.getREFS());
            }
            if (!Files.exists(GitPaths.getHEADS())) {
                Files.createDirectories(GitPaths.getHEADS());
            }

            if (!Files.exists(GitPaths.getHEAD())) {
                Files.createFile(GitPaths.getHEAD());
            }

            if (!Files.exists(GitPaths.getBRANCH())) {
                Path createdFilePath = Files.createFile(GitPaths.getBRANCH());
                FileUtils.writeStringToFile(createdFilePath.toFile(), "master", StandardCharsets.UTF_8);
            }

            if (!Files.exists(GitPaths.getMasterHead())) {
                Files.createFile(GitPaths.getMasterHead());
            }

            if (!Files.exists(GitPaths.getINDEX())) {
                try (FileWriter file = new FileWriter(GitPaths.getINDEX().toFile())) {
                    JSONObject object = new JSONObject();

                    object.put(JSONConstants.ADDED, new JSONArray());
                    object.put(JSONConstants.REMOVED, new JSONArray());
                    object.put(JSONConstants.TRACKED, new JSONArray());

                    file.write(object.toJSONString());
                    file.flush();
                } catch (IOException e) {
                    throw new GitException("Cannot create index file", e);
                }
            }

            outputStream.println("Инициализирован пустой репозиторий в " + GitPaths.getGitDirectory());
        } catch (IOException exception) {
            throw new GitException();
        }
    }
}
