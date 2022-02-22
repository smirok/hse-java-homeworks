package ru.itmo.mit.git.commands.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.itmo.mit.git.GitException;
import ru.itmo.mit.git.model.GitIndex;
import ru.itmo.mit.git.utils.GitPaths;
import ru.itmo.mit.git.utils.compressor.ZstdCompressor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class GitUtil {
    public static String getHashHeadCommit() throws GitException {
        try {
            return FileUtils.readFileToString(GitPaths.getHEAD().toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GitException("Cannot read file", e);
        }
    }

    public static String getHashBranchCommit(@NotNull String branchName) throws GitException {
        try {
            return FileUtils.readFileToString(
                    Paths.get(GitPaths.getHEADS().toString(), branchName).toFile(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new GitException("Cannot read file", e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored ")
    public static void cleanDirectory(GitIndex index, @Nullable List<String> notRemovableFiles) {
        File workingDir = GitPaths.getWorkingDirectory().toFile();
        workingDir.mkdirs();
        Collection<File> files = FileUtils.listFilesAndDirs(workingDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        for (File file : files) {
            Path relativePath = GitPaths.getWorkingDirectory().relativize(file.toPath().normalize());
            if (!file.equals(workingDir)
                    && !relativePath.startsWith(".hsegit")
                    && !index.getUntrackedFiles().containsKey(relativePath.toString())
                    && (!Objects.equals(notRemovableFiles, null) && !notRemovableFiles.contains(relativePath.toString()))) {
                // untracked файлы не удаляются!
                // остальные удаляются
                FileUtils.deleteQuietly(file);
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deployFiles(GitIndex index, @NotNull String nodeHash) throws IOException {
        List<String[]> lines = Files.lines(Path.of(GitPaths.getOBJECTS() + File.separator + nodeHash))
                .map(s -> s.split("\\s+"))
                .collect(Collectors.toList());

        for (String[] line : lines) {
            String fileType = line[0];
            String fileHash = line[1];
            String fileName = line[2];
            // line[0] = blob/hash,
            // line[1] = HASH(строка 40 символов),
            // line[2] = полной имя файла относительно WORKING_DIR

            if (fileType.equals("blob")) {
                byte[] decompressedBlobBytes = ZstdCompressor
                        .decompress(Files.readAllBytes(Paths.get(GitPaths.getOBJECTS().toString(), fileHash)));

                File deployedFile = new File(Paths.get(GitPaths.getWorkingDirectory().toString(), fileName).toString());
                deployedFile.getParentFile().mkdirs();
                deployedFile.createNewFile();
                try (FileOutputStream fileOutputStream = new FileOutputStream(deployedFile)) {
                    fileOutputStream.write(decompressedBlobBytes);
                }

                index.getTrackedFiles().put(fileName, fileHash);
            } else {
                deployFiles(index, fileHash); // recursive with tree
            }
        }
    }

    public static @NotNull HashMap<String, String> getAllFilesFromRevision(String toRevision) throws GitException { // file to hash
        HashMap<String, String> result = new HashMap<>();

        Queue<String> queueHashes = new LinkedList<>();

        JSONObject jsonObject = GitUtil.getJSONOBjectFromCommit(toRevision);
        queueHashes.add((String) jsonObject.get("tree"));

        while (!queueHashes.isEmpty()) {
            String hash = queueHashes.remove();

            List<String[]> lines;
            try {
                lines = Files.lines(Path.of(GitPaths.getOBJECTS() + File.separator + hash))
                        .map(s -> s.split("\\s+"))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new GitException("Cannot read file", e);
            }

            for (String[] line : lines) {
                String fileType = line[0];
                String fileHash = line[1];
                String fileName = line[2];

                if (fileType.equals("blob")) {
                    result.put(fileName, fileHash);
                } else {
                    queueHashes.add(fileHash); // recursive with tree
                }
            }
        }

        return result;
    }

    public static JSONObject getJSONOBjectFromCommit(@NotNull String commitHash) throws GitException {
        JSONParser parser = new JSONParser();

        try {
            return (JSONObject) parser.parse(
                    new FileReader(Paths.get(GitPaths.getOBJECTS().toString(), commitHash + ".json").toString())
            );
        } catch (ParseException e) {
            throw new GitException("Cannot parse json", e);
        } catch (IOException e) {
            throw new GitException("Cannot read file", e);
        }
    }

    public static String getCurrentBranchName() throws GitException {
        try {
            return FileUtils.readFileToString(GitPaths.getBRANCH().toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GitException("Cannot read file", e);
        }
    }

    public static Path getFilePath(@NotNull String fileName) {
        return Paths.get(GitPaths.getWorkingDirectory().toString(), fileName);
    }
}
