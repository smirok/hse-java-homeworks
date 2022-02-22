package ru.itmo.mit.git.model;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.commands.utils.GitUtil;
import ru.itmo.mit.git.utils.GitPaths;
import ru.itmo.mit.git.utils.JSONConstants;
import ru.itmo.mit.git.utils.compressor.ZstdCompressor;
import ru.itmo.mit.git.utils.hash.SHA1Hasher;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommitCreator {
    private final GitIndex index;
    private final Tree root = new Tree(".");
    private final String message;

    public CommitCreator(GitIndex index, String message) {
        this.index = index;
        this.message = message;
    }

    public static class Tree {
        String name;
        String hash = null;

        public String getName() {
            return name;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public HashMap<String, Tree> getChildren() {
            return children;
        }

        HashMap<String, Tree> children = new HashMap<>(); // name -> tree

        public Tree(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Tree tree = (Tree) o;
            return Objects.equals(name, tree.name)
                    && Objects.equals(hash, tree.hash)
                    && Objects.equals(children, tree.children);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, hash, children);
        }
    }

    public void createIndexTree() throws GitException {
        index.readIndex();

        HashMap<String, String> addedFiles = index.getAddedFiles();
        HashMap<String, String> trackedFiles = index.getTrackedFiles();

        for (Map.Entry<String, String> entry : addedFiles.entrySet()) {
            String key = entry.getKey();
            String hash = entry.getValue();
            if (trackedFiles.containsKey(key)) {
                trackedFiles.replace(key, hash);
            }
        }

        for (String fileName : Stream.concat(addedFiles.keySet().stream(),
                trackedFiles.keySet().stream()).collect(Collectors.toList())) {

            String[] pathParts = fileName.split(File.separator);

            Tree currentNode = root;
            StringBuilder relativePathBuilder = new StringBuilder();
            for (String part : pathParts) {
                relativePathBuilder.append(part);
                if (!currentNode.children.containsKey(part)) {
                    currentNode.children.put(part, new Tree(relativePathBuilder.toString()));
                }
                relativePathBuilder.append(File.separator);
                currentNode = currentNode.children.get(part);
            }
        }
    }

    public void evaluateHashes() throws GitException {
        evaluateHashesInner(root);
    }

    private void evaluateHashesInner(Tree node) throws GitException {
        if (node.getChildren().isEmpty()) {
            if (index.getTrackedFiles().containsKey(node.getName())) {
                node.setHash(index.getTrackedFiles().get(node.getName()));
            } else {
                node.setHash(SHA1Hasher.getFileHash(GitUtil.getFilePath(node.getName())));
            }
            return;
        }

        for (Tree nextNode : node.children.values()) {
            evaluateHashesInner(nextNode);
        }

        node.setHash(SHA1Hasher.getTreeHash(node));
    }

    public void createNecessaryZippedFiles() throws GitException {
        createNecessaryZippedFilesInner(root);
    }

    public void createNecessaryZippedFilesInner(Tree node) throws GitException {
        if (node.getChildren().isEmpty() &&
                !Files.exists(Paths.get(GitPaths.getOBJECTS().toString(), node.getHash()))) {
            createBlob(node);
        }

        for (Tree nextNode : node.children.values()) {
            createNecessaryZippedFilesInner(nextNode);
        }

        if (!Files.exists(Paths.get(GitPaths.getOBJECTS().toString(), node.getHash()))) {
            createTree(node);
        }
    }

    private void createBlob(Tree node) throws GitException {
        Path fileNamePath = Paths.get(GitPaths.getWorkingDirectory().toString(), node.getName());

        try (FileOutputStream outputStream = new FileOutputStream(Paths.get(GitPaths.getOBJECTS().toString(), node.getHash()).toString())) {
            byte[] zippedData = ZstdCompressor.compress(Files.readAllBytes(fileNamePath));
            outputStream.write(zippedData);
        } catch (IOException e) {
            throw new GitException("Cannot write to file", e);
        }
    }

    private void createTree(Tree node) throws GitException {
        try (FileOutputStream outputStream = new FileOutputStream(Paths.get(GitPaths.getOBJECTS().toString(), node.getHash()).toString())) {
            for (Tree nextNode : node.children.values()) {

                String stringBuilder = (nextNode.getChildren().isEmpty() ? "blob " : "tree ") +
                        nextNode.getHash() +
                        " " +
                        nextNode.getName() +
                        "\n";
                outputStream.write(stringBuilder.getBytes());
            }
        } catch (IOException e) {
            throw new GitException("Cannot write to file", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void createCommitFile(@NotNull List<@Nullable String> parents) throws GitException {
        JSONObject object = new JSONObject();
        JSONArray parentsJA = new JSONArray();
        for (String parent : parents) {
            if (!Objects.equals(null, parent)) {
                parentsJA.add(parent);
            }
        }
        object.put(JSONConstants.TREE, root.getHash());
        object.put(JSONConstants.PARENTS, parentsJA);
        object.put(JSONConstants.AUTHOR, "user");
        object.put(JSONConstants.DATE, new Date().toString());
        object.put(JSONConstants.MESSAGE, message);

        String commitHash = SHA1Hasher.getCommitHash("commit " + object);

        try (FileWriter file = new FileWriter(Paths.get(GitPaths.getOBJECTS().toString(), commitHash + ".json").toString())) {
            file.write(object.toJSONString());
            file.flush();
        } catch (IOException e) {
            throw new GitException("Cannot write json file", e);
        }

        setHead(commitHash);
        updateFileHeadOfCurrentBranch(commitHash);
    }

    public static void updateFileHeadOfCurrentBranch(@NotNull String commitHash) throws GitException {
        String currentBranch;
        try {
            currentBranch = FileUtils.readFileToString(GitPaths.getBRANCH().toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GitException("Cannot read from file", e);
        }

        try {
            FileUtils.writeStringToFile(new File(GitPaths.getHEADS() + File.separator + currentBranch), commitHash, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GitException("Cannot get file", e);
        }
    }

    private String getParentCommitHash() {
        try {
            return Files.lines(GitPaths.getHEAD()).findFirst().orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    public void setHead(String commitNewHeadHash) throws GitException {
        try {
            FileUtils.writeStringToFile(GitPaths.getHEAD().toFile(), commitNewHeadHash, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GitException("Cannot write to file", e);
        }
    }

    public void commit() throws GitException {
        index.clearState();
        createIndexTree();
        evaluateHashes();
        createNecessaryZippedFiles();
        createCommitFile(Collections.singletonList(getParentCommitHash()));

        index.updateIndexAfterCommitting();
        index.writeIndex();
    }

    public void commitMerge(String fstParentHash, String sndParentHash) throws GitException {
        index.clearState();
        createIndexTree();
        evaluateHashes();
        createNecessaryZippedFiles();
        createCommitFile(List.of(fstParentHash, sndParentHash));

        index.updateIndexAfterCommitting();
        index.writeIndex();
    }
}
