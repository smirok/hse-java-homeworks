package ru.itmo.mit.git;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws GitException {
        Path directoryForGit = Path.of(args[0]);
        String command = args[1];
        GitCliImpl gitCli = new GitCliImpl(Path.of(System.getProperty("user.dir")).resolve(directoryForGit).toString());
        gitCli.setOutputStream(System.out);

        List<String> arguments = Arrays.stream(args).skip(2).collect(Collectors.toList());
        gitCli.runCommand(command, arguments);
    }
}
