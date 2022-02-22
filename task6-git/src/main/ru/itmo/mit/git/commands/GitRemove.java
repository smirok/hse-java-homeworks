package ru.itmo.mit.git.commands;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.model.GitIndex;

import java.util.List;

public class GitRemove {
    public static void rm(GitIndex index, @NotNull List<@NotNull String> fileNames) throws GitException {
        // если мы сделали rm на файле, не добавленном в коммит - переносится в untracked
        // если мы сделали rm на закомиченном файле - он в индексе помечается удаленным, в статусе также отображается его untracked версия
        index.readIndex();

        for (String fileName : fileNames) {

            if (index.getAddedFiles().containsKey(fileName)) {
                String fileHash = index.getAddedFiles().get(fileName);
                index.getAddedFiles().remove(fileName);
                if (index.getTrackedFiles().containsKey(fileName)) {
                    index.getRemovedFiles().put(fileName, fileHash);
                }
            } else if (index.getTrackedFiles().containsKey(fileName)) {
                String fileHash = index.getTrackedFiles().get(fileName);
                index.getRemovedFiles().put(fileName, fileHash);
                index.getTrackedFiles().remove(fileName);
            }
        }

        index.writeIndex();
    }
}
