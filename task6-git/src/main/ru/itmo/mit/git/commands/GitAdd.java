package ru.itmo.mit.git.commands;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.commands.utils.GitUtil;
import ru.itmo.mit.git.model.GitIndex;
import ru.itmo.mit.git.utils.hash.SHA1Hasher;

import java.util.List;

public class GitAdd {
    public static void add(GitIndex index, @NotNull List<@NotNull String> fileNames) throws GitException {
        index.clearState();
        index.readIndex();
        index.updateModifying();

        for (String fileName : fileNames) {
            String fileHash = SHA1Hasher.getFileHash(GitUtil.getFilePath(fileName));

            if (index.getAddedFiles().containsKey(fileName)) {
                String oldHash = index.getAddedFiles().get(fileName);
                if (!oldHash.equals(fileHash)) {
                    index.getAddedFiles().replace(fileName, fileHash);
                }
            } else if (index.getRemovedFiles().containsKey(fileName)) {
                if (!index.getRemovedFiles().get(fileName).equals(fileHash)) {
                    index.getRemovedFiles().remove(fileName);
                    index.getAddedFiles().put(fileName, fileHash);
                }
            } else if (index.getTrackedFiles().containsKey(fileName) && index.getModifiedTrackedFiles().containsKey(fileName)) {
                String oldHash = index.getTrackedFiles().get(fileName);
                if (!oldHash.equals(fileHash)) {
                    index.getModifiedTrackedFiles().remove(fileName);
                    index.getTrackedFiles().replace(fileName, fileHash);
                    index.getAddedFiles().put(fileName, fileHash);
                }
            } else {
                index.getAddedFiles().put(fileName, fileHash);
            }
        }

        index.writeIndex();
    }
}
