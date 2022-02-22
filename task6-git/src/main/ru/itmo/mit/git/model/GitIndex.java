package ru.itmo.mit.git.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.commands.utils.GitUtil;
import ru.itmo.mit.git.utils.GitPaths;
import ru.itmo.mit.git.utils.JSONConstants;
import ru.itmo.mit.git.utils.hash.SHA1Hasher;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class GitIndex {
    // HashMap<FileName, FileHash>
    private HashMap<String, String> untrackedFiles = new HashMap<>();
    private HashMap<String, String> addedFiles = new HashMap<>();
    private HashMap<String, String> removedFiles = new HashMap<>();
    private HashMap<String, String> trackedFiles = new HashMap<>();

    public HashMap<String, String> getModifiedTrackedFiles() {
        return modifiedTrackedFiles;
    }

    private HashMap<String, String> modifiedTrackedFiles = new HashMap<>();

    public HashMap<String, String> getUntrackedFiles() {
        return untrackedFiles;
    }

    public HashMap<String, String> getAddedFiles() {
        return addedFiles;
    }

    public HashMap<String, String> getRemovedFiles() {
        return removedFiles;
    }

    public HashMap<String, String> getTrackedFiles() {
        return trackedFiles;
    }

    public void setTrackedFiles(HashMap<String, String> trackedFiles) {
        this.trackedFiles = trackedFiles;
    }

    public void clearState() {
        untrackedFiles = new HashMap<>();
        removedFiles = new HashMap<>();
        trackedFiles = new HashMap<>();
        addedFiles = new HashMap<>();
        modifiedTrackedFiles = new HashMap<>();
    }

    public void readIndex() throws GitException {
        JSONParser parser = new JSONParser();

        JSONObject jsonObject;
        try (FileReader fileReader = new FileReader(GitPaths.getINDEX().toFile())) {
            jsonObject = (JSONObject) parser.parse(fileReader);
        } catch (ParseException e) {
            throw new GitException("Cannot parse json file", e);
        } catch (IOException e) {
            throw new GitException("Cannot read index file", e);
        }

        JSONArray addedJA = (JSONArray) jsonObject.get(JSONConstants.ADDED);
        fillHashMapFromJSONArray(addedFiles, addedJA);

        JSONArray removedJA = (JSONArray) jsonObject.get(JSONConstants.REMOVED);
        fillHashMapFromJSONArray(removedFiles, removedJA);

        JSONArray trackedJA = (JSONArray) jsonObject.get(JSONConstants.TRACKED);
        fillHashMapFromJSONArray(trackedFiles, trackedJA);
    }

    @SuppressWarnings("unchecked")
    private void fillHashMapFromJSONArray(HashMap<String, String> hashMap, JSONArray jsonArray) {
        if (!Objects.equals(null, jsonArray)) {
            jsonArray.forEach(e -> {
                JSONObject object = (JSONObject) e;
                hashMap.put(
                        (String) object.get(JSONConstants.FILENAME),
                        (String) object.get(JSONConstants.HASH)
                );
            });
        }
    }

    @SuppressWarnings("unchecked")
    public void writeIndex() throws GitException {
        try {
            FileChannel.open(GitPaths.getINDEX(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new GitException("Cannot rewrite file", e);
        }

        JSONObject object = new JSONObject();
        JSONArray addedJA = new JSONArray();
        JSONArray removedJA = new JSONArray();
        JSONArray trackedJA = new JSONArray();

        fillJSONArrayFromHashMap(addedFiles, addedJA);
        fillJSONArrayFromHashMap(removedFiles, removedJA);
        fillJSONArrayFromHashMap(trackedFiles, trackedJA);

        object.put(JSONConstants.ADDED, addedJA);
        object.put(JSONConstants.REMOVED, removedJA);
        object.put(JSONConstants.TRACKED, trackedJA);

        try (FileWriter file = new FileWriter(GitPaths.getINDEX().toFile())) {
            file.write(object.toJSONString());
            file.flush();
        } catch (IOException e) {
            throw new GitException("Cannot create index file", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void fillJSONArrayFromHashMap(HashMap<String, String> hashMap, JSONArray jsonArray) {
        hashMap.forEach((fileName, hash) -> {
            JSONObject fileInfo = new JSONObject();
            fileInfo.put(JSONConstants.FILENAME, fileName);
            fileInfo.put(JSONConstants.HASH, hash);
            jsonArray.add(fileInfo);
        });
    }

    public void updateIndexAfterCommitting() {
        for (Map.Entry<String, String> entry : addedFiles.entrySet()) {
            String fileName = entry.getKey();
            String fileHash = entry.getValue();
            trackedFiles.put(fileName, fileHash);
        }
        addedFiles.clear();
    }

    public void searchUntrackedFiles() throws GitException {
        try {
            List<String> allPathesToFiles = Files
                    .walk(GitPaths.getWorkingDirectory())
                    .filter(path -> Files.isRegularFile(path) && !path.startsWith(GitPaths.getGitDirectory()))
                    .map(path -> GitPaths.getWorkingDirectory().relativize(path.normalize()).toString())
                    .collect(Collectors.toList());

            for (String fileName : allPathesToFiles) {
                if (!trackedFiles.containsKey(fileName) && !addedFiles.containsKey(fileName)) {
                    untrackedFiles.put(fileName,
                            SHA1Hasher.getFileHash(GitUtil.getFilePath(fileName)));
                }
            }
        } catch (IOException e) {
            throw new GitException("Cannot access to starting directory " + GitPaths.getWorkingDirectory(), e);
        }
    }

    public void updateModifying() throws GitException {
        Map<String, String> localRemovedFiles = new HashMap<>();

        for (Map.Entry<String, String> entry : trackedFiles.entrySet()) {
            String fileName = entry.getKey();
            String fileHash = entry.getValue();

            Path filePath = GitUtil.getFilePath(fileName);

            if (Files.notExists(filePath)) {
                localRemovedFiles.put(filePath.toString(), fileHash);
                continue;
            }

            String newHash = SHA1Hasher.getFileHash(filePath);
            if (!entry.getValue().equals(newHash)) {
                modifiedTrackedFiles.put(entry.getKey(), newHash);
            }
        }

        for (Map.Entry<String, String> entry : localRemovedFiles.entrySet()) {
            String absoluteFileName = entry.getKey();
            String fileHash = entry.getValue();

            String relativePath = GitPaths.getWorkingDirectory().normalize().relativize(Path.of(absoluteFileName)).toString();
            modifiedTrackedFiles.remove(relativePath);
            removedFiles.put(relativePath, fileHash);
        }
    }
}
