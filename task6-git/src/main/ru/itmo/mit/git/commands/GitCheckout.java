package ru.itmo.mit.git.commands;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.commands.utils.GitUtil;
import ru.itmo.mit.git.model.GitIndex;
import ru.itmo.mit.git.utils.GitPaths;
import ru.itmo.mit.git.utils.compressor.ZstdCompressor;
import ru.itmo.mit.git.utils.hash.SHA1Hasher;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class GitCheckout {
    @SuppressWarnings({"ResultOfMethodCallIgnored", "OptionalUsedAsFieldOrParameterType"})
    public static void checkout(GitIndex index, PrintStream outputStream, @NotNull String toRevision, Optional<String> branchToCheckout) throws GitException {
        try {
            FileUtils.writeStringToFile(GitPaths.getHEAD().toFile(), toRevision, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GitException("Cannot write to file", e);
        }

        index.readIndex();
        index.searchUntrackedFiles();
        index.updateModifying();
        if (!verifyCheckout(index, toRevision)) {
            outputStream.println("error: файлы будут перезаписаны при переключении на состояние\nСделайте коммит\nПрерываю");
            return;
        }

        File detachedHead = GitPaths.getDetachedHead().toFile();
        if (branchToCheckout.isPresent()) {
            if (detachedHead.exists())
                FileUtils.deleteQuietly(detachedHead);

            try {
                FileUtils.writeStringToFile(GitPaths.getBRANCH().toFile(), branchToCheckout.get(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new GitException("Cannot write to file", e);
            }
        } else {
            try {
                detachedHead.createNewFile();
            } catch (IOException e) {
                throw new GitException("Cannot create file", e);
            }
            outputStream.println("you have detached HEAD. Be careful!");
        }

        GitUtil.cleanDirectory(index, new ArrayList<>(GitUtil.getAllFilesFromRevision(toRevision).keySet()));
        index.clearState();

        JSONObject jsonObject = GitUtil.getJSONOBjectFromCommit(toRevision);

        String treeRootHash = (String) jsonObject.get("tree");

        try {
            GitUtil.deployFiles(index, treeRootHash);
        } catch (IOException e) {
            throw new GitException("Cannot deploy files", e);
        }

        index.writeIndex();
    }

    private static boolean verifyCheckout(GitIndex index, @NotNull String toRevision) throws GitException {
        List<String> verificationFiles = new ArrayList<>(GitUtil.getAllFilesFromRevision(toRevision).keySet());

        HashSet<String> addedTrackedFiles = new HashSet<>();
        index.getAddedFiles()
                .entrySet()
                .stream()
                .filter(entry -> index.getTrackedFiles().containsKey(entry.getKey()))
                .forEach(entry -> addedTrackedFiles.add(entry.getKey()));

        HashSet<String> trackedFiles = new HashSet<>(index.getModifiedTrackedFiles().keySet());

        HashSet<String> removedTrackedFiles = new HashSet<>(index.getRemovedFiles().keySet());

        for (String fileName : verificationFiles) {
            if (addedTrackedFiles.contains(fileName) ||
                    trackedFiles.contains(fileName) ||
                    removedTrackedFiles.contains(fileName)) {
                return false;
            }
        }

        return true;
    }

    public static void checkoutFiles(GitIndex index, List<@NotNull String> filesToCheckout) throws GitException {
        index.readIndex();
        index.searchUntrackedFiles();
        index.updateModifying();

        HashMap<String, String> removedTrackedFiles = index.getRemovedFiles();
        HashMap<String, String> trackedModifiedFiles = new HashMap<>(index.getModifiedTrackedFiles());

        for (String fileName : filesToCheckout) {
            if (removedTrackedFiles.containsKey(fileName) || trackedModifiedFiles.containsKey(fileName)) {
                String hash = removedTrackedFiles.containsKey(fileName)
                        ? removedTrackedFiles.get(fileName)
                        : index.getTrackedFiles().get(fileName);

                byte[] decompressedBlobBytes;
                try {
                    decompressedBlobBytes = ZstdCompressor
                            .decompress(Files.readAllBytes(Paths.get(GitPaths.getOBJECTS().toString(), hash)));
                } catch (IOException e) {
                    throw new GitException("Cannot read file", e);
                }

                File deployedFile = new File(GitPaths.getWorkingDirectory().toString(), fileName);
                try (FileOutputStream fileOutputStream = new FileOutputStream(deployedFile, false)) { // false to overwrite
                    fileOutputStream.write(decompressedBlobBytes);
                } catch (IOException e) {
                    throw new GitException("Cannot write to file", e);
                }

                String newHash = SHA1Hasher.getFileHash(deployedFile.toPath());

                index.getRemovedFiles().remove(fileName); // if contains - remove, otherwise - nothing
                index.getTrackedFiles().put(fileName, newHash);
            }
        }

        index.writeIndex();
    }
}
