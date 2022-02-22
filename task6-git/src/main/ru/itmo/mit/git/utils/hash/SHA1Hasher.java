package ru.itmo.mit.git.utils.hash;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.model.CommitCreator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SHA1Hasher {
    public static String getFileHash(@NotNull Path filePath) throws GitException {
        try (InputStream is = Files.newInputStream(filePath)) {
            return DigestUtils.sha1Hex(is);
        } catch (IOException e) {
            throw new GitException("Cannot get SHA1 hash the file " + filePath.getFileName(), e);
        }
    }

    public static String getCommitHash(@NotNull String commitInfo) {
        return DigestUtils.sha1Hex(commitInfo);
    }

    /**
     * tree hash - concatenation of subtrees hashes
     */
    public static String getTreeHash(@NotNull CommitCreator.Tree tree) {
        StringBuilder stringBuilder = new StringBuilder();
        for (CommitCreator.Tree childTree : tree.getChildren().values()) {
            stringBuilder.append(childTree.getHash());
        }

        return DigestUtils.sha1Hex(stringBuilder.toString());
    }
}