package ru.itmo.mit.git;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.itmo.mit.git.commands.utils.GitUtil;
import ru.itmo.mit.git.utils.GitPaths;
import ru.itmo.mit.git.utils.JSONConstants;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class GitCliImpl implements GitCli {
    private final GitClient gitClient;

    public GitCliImpl(String workDir) {
        this.gitClient = new GitClient(workDir);
    }

    @Override
    public void runCommand(@NotNull String command, @NotNull List<@NotNull String> arguments) throws GitException {
        switch (command) {
            case GitConstants.INIT:
                gitClient.init();
                break;
            case GitConstants.ADD:
                gitClient.add(arguments);
                break;
            case GitConstants.RM:
                gitClient.rm(arguments);
                break;
            case GitConstants.STATUS:
                gitClient.status();
                break;
            case GitConstants.COMMIT:
                gitClient.commit(arguments.get(0));
                break;
            case GitConstants.LOG:
                gitClient.log(arguments.isEmpty() ? null : arguments.get(0));
                break;
            case GitConstants.RESET:
                gitClient.reset(getRevisionHash(arguments.get(0)));
                break;
            case GitConstants.CHECKOUT:
                if (arguments.size() == 1) {
                    String arg = arguments.get(0);
                    gitClient.checkout(getRevisionHash(arg), getBranchName(arg));
                } else if (arguments.size() > 1) {
                    if (arguments.get(0).equals("--")) {
                        gitClient.checkoutFiles(arguments.subList(1, arguments.size()));
                    } else {
                        throw new GitException("Unknown command");
                    }
                } else {
                    throw new GitException("Unknown command");
                }
                break;
            case GitConstants.BRANCH_CREATE:
                gitClient.branch_create(arguments.get(0));
                break;
            case GitConstants.BRANCH_REMOVE:
                gitClient.branch_remove(arguments.get(0));
                break;
            case GitConstants.SHOW_BRANCHES:
                gitClient.show_branches();
                break;
            case GitConstants.MERGE:
                gitClient.merge(arguments.get(0));
                break;
            default:
                throw new GitException("Unknown command");
        }
    }

    @Override
    public void setOutputStream(@NotNull PrintStream outputStream) {
        gitClient.setOutputStream(outputStream);
    }

    @Override
    public @NotNull String getRelativeRevisionFromHead(int n) throws GitException {
        String commitHash = GitUtil.getHashHeadCommit();

        for (int i = 0; i < n; ++i) {
            if (Objects.equals(commitHash, null) || commitHash.equals("")) {
                throw new GitException("Cannot iterate, commits are done");
            }

            JSONParser parser = new JSONParser();

            JSONObject jsonObject;
            try {
                jsonObject = (JSONObject) parser.parse(new FileReader(GitPaths.getOBJECTS() + File.separator + commitHash + ".json"));
            } catch (IOException | ParseException e) {
                throw new GitException("Cannot read file", e);
            }

            JSONArray parents = (JSONArray) jsonObject.get(JSONConstants.PARENTS);
            if (!Objects.equals(null, parents)) {
                if (!parents.isEmpty()) { // идем по одной дорожке, но их может быть не одна..
                    commitHash = (String) parents.get(0);
                } else {
                    throw new GitException("Cannot find parent commit");
                }
            }
        }

        return commitHash;
    }

    private @NotNull String getRevisionHash(@NotNull String arg) throws GitException {
        if (Pattern.compile("HEAD~(\\d+)").matcher(arg).matches()) {
            return getRelativeRevisionFromHead(Integer.parseInt(arg.substring(5)));
        }

        if (getBranchName(arg).isPresent()) {
            try {
                return new String(Files.readAllBytes(new File(GitPaths.getHEADS().toString(), arg).toPath()));
            } catch (IOException e) {
                throw new GitException("Cannot read file", e);
            }
        }

        if (Files.exists(Path.of(GitPaths.getOBJECTS() + File.separator + arg + ".json"))) {
            return arg; // сommit hash
        } else {
            throw new GitException("Unknown hash");
        }
    }

    private Optional<String> getBranchName(@NotNull String arg) {
        if (new File(GitPaths.getHEADS().toString(), arg).exists()) {
            return Optional.of(arg);
        } else {
            return Optional.empty();
        }
    }
}
