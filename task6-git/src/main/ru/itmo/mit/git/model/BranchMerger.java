package ru.itmo.mit.git.model;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.commands.utils.GitUtil;
import ru.itmo.mit.git.utils.GitPaths;
import ru.itmo.mit.git.utils.JSONConstants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BranchMerger {
    private enum MergeStrategy {
        FAST_FORWARD, RECURSIVE, ALREADY_UPDATED
    }

    public void merge(@NotNull String branchToMerge, PrintStream outputStream) throws GitException {
        MergeStrategy currentStrategy = getMergeStrategy(branchToMerge);
        if (currentStrategy.equals(MergeStrategy.ALREADY_UPDATED)) {
            outputStream.println("Already up to date.");
            return;
        }

        String mergedBranchHeadHash = GitUtil.getHashBranchCommit(branchToMerge);

        String currentBranch;
        try {
            currentBranch = new String(Files.readAllBytes(GitPaths.getBRANCH()));
        } catch (IOException e) {
            throw new GitException("Cannot read file", e);
        }

        if (currentStrategy.equals(MergeStrategy.FAST_FORWARD)) {
            mergeFastForward(currentBranch, mergedBranchHeadHash);
            outputStream.println("Fast-forward merge completed.");
            return;
        }

        mergeRecursive(branchToMerge);
    }

    private MergeStrategy getMergeStrategy(@NotNull String branchToMerge) throws GitException {
        String currentBranchHeadHash = GitUtil.getHashHeadCommit();
        String mergedBranchHeadHash = GitUtil.getHashBranchCommit(branchToMerge);

        if (isAncestorAndDescendant(mergedBranchHeadHash, currentBranchHeadHash)) {
            return MergeStrategy.ALREADY_UPDATED;
        }

        if (isAncestorAndDescendant(currentBranchHeadHash, mergedBranchHeadHash)) {
            return MergeStrategy.FAST_FORWARD;
        }

        return MergeStrategy.RECURSIVE;
    }

    private boolean isAncestorAndDescendant(String ancestorHash, String descendantHash) throws GitException {
        // вообще parent-ов может быть несколько, но пока один
        String currentHash = descendantHash;
        while (!Objects.equals(currentHash, null)) {
            if (currentHash.equals(ancestorHash)) {
                return true;
            }

            currentHash = readParentHash(currentHash);
        }

        return false;
    }

    @Nullable
    private String readParentHash(String currentHash) throws GitException {
        JSONObject jsonObject = GitUtil.getJSONOBjectFromCommit(currentHash);

        JSONArray parents = (JSONArray) jsonObject.get(JSONConstants.PARENTS);
        if (!parents.isEmpty()) {
            currentHash = (String) parents.get(0);
        } else {
            currentHash = null;
        }

        return currentHash;
    }

    private void mergeFastForward(@NotNull String currentBranch, @NotNull String commitHash) throws GitException {
        try {
            FileUtils.writeStringToFile(
                    Paths.get(GitPaths.getHEADS().toString(), currentBranch).toFile(), commitHash, StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new GitException("Cannot write to file", e);
        }

        try {
            FileUtils.writeStringToFile(GitPaths.getHEAD().toFile(), commitHash, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GitException("Cannot write to file", e);
        }
    }

    private void mergeRecursive(@NotNull String branchToMerge) throws GitException {
        String currentBranchHeadHash = GitUtil.getHashHeadCommit();
        String mergedBranchHeadHash = GitUtil.getHashBranchCommit(branchToMerge);

        String ancestorCommitHash = getLCA(currentBranchHeadHash, mergedBranchHeadHash);

        HashMap<String, String> firstDiffs = getDiffFiles(ancestorCommitHash, currentBranchHeadHash);
        HashMap<String, String> secondDiffs = getDiffFiles(ancestorCommitHash, mergedBranchHeadHash);

        checkMergeConflicts(firstDiffs, secondDiffs);

        HashMap<String, String> ancestorAllFiles = GitUtil.getAllFilesFromRevision(ancestorCommitHash);

        HashMap<String, String> firstHashAllFiles = GitUtil.getAllFilesFromRevision(currentBranchHeadHash);
        ancestorAllFiles.putAll(firstHashAllFiles);

        HashMap<String, String> secondHashAllFiles = GitUtil.getAllFilesFromRevision(mergedBranchHeadHash);
        ancestorAllFiles.putAll(secondHashAllFiles);

        GitIndex index = new GitIndex();
        String message = "merge branches " + branchToMerge + " and " + GitUtil.getCurrentBranchName();

        HashMap<String, String> trackedFiles = new HashMap<>();
        for (Map.Entry<String, String> entry : ancestorAllFiles.entrySet()) {
            String fileName = entry.getKey();
            String fileHash = entry.getValue();
            trackedFiles.put(fileName, fileHash);
        }

        index.setTrackedFiles(trackedFiles);
        CommitCreator commitCreator = new CommitCreator(index, message);
        commitCreator.commitMerge(currentBranchHeadHash, mergedBranchHeadHash);
    }

    private String getLCA(String firstHash, String secondHash) throws GitException {
        String currentFirstHash = firstHash;

        while (!Objects.equals(currentFirstHash, null)) {
            String currentSecondHash = secondHash;
            while (!Objects.equals(currentSecondHash, null)) {
                if (currentFirstHash.equals(currentSecondHash)) {
                    return currentFirstHash;
                }

                currentSecondHash = readParentHash(currentSecondHash);
            }

            currentFirstHash = readParentHash(currentFirstHash);
        }

        return null;
    }

    private HashMap<String, String> getDiffFiles(String ancestorHash, String descendantHash) throws GitException {
        HashMap<String, String> result = new HashMap<>();

        HashMap<String, String> ancestorFiles = GitUtil.getAllFilesFromRevision(ancestorHash);
        HashMap<String, String> descendantFiles = GitUtil.getAllFilesFromRevision(descendantHash);

        for (Map.Entry<String, String> entry : descendantFiles.entrySet()) {
            String fileName = entry.getKey();
            String fileHash = entry.getValue();
            if (ancestorFiles.containsKey(fileName)) {
                String hash = ancestorFiles.get(fileName);
                if (!hash.equals(fileHash)) {
                    result.put(fileName, fileHash);
                }
            } else {
                result.put(fileName, fileHash);
            }
        }

        return result;
    }

    private void checkMergeConflicts(HashMap<String, String> firstDiffs, HashMap<String, String> secondDiffs) throws GitException {
        List<String> conflictFileNames = new ArrayList<>();
        for (String fileName : firstDiffs.keySet()) {
            if (secondDiffs.containsKey(fileName)) {
                conflictFileNames.add(fileName);
            }
        }

        if (!conflictFileNames.isEmpty()) {
            StringBuilder conflictStringBuilder = new StringBuilder();
            conflictStringBuilder.append("Merge conflict in files:\n");

            for (String fileName : conflictFileNames) {
                conflictStringBuilder.append("\t").append(fileName).append("\n");
            }

            conflictStringBuilder.append("Automatic merge failed; fix conflicts and then commit the result.\n");
            throw new GitException(conflictStringBuilder.toString());
        }
    }
}
