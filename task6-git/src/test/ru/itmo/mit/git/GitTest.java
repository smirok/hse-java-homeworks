package ru.itmo.mit.git;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
 * Т.к. в коммитах при каждом новом запуске получаются разные хеши и
 *   разное время отправки, то в expected логах на их местах используются
 *   COMMIT_HASH и COMMIT_DATE заглушки соответственно
 */
public class GitTest extends AbstractGitTest {
    @Override
    protected GitCli createCli(String workingDir) {
        return new GitCliImpl(workingDir);
    }

    @Override
    protected TestMode testMode() {
        return TestMode.SYSTEM_OUT;
    }

    @Test
    public void testAdd() throws Exception {
        createFile("file.txt", "aaa");
        status();
        add("file.txt");
        status();
        commit("First commit");
        status();
        log();

        check("add.txt");
    }

    @Test
    public void testMultipleCommits() throws Exception {
        String file1 = "file1.txt";
        String file2 = "file2.txt";
        createFile(file1, "aaa");
        createFile(file2, "bbb");
        status();
        add(file1);
        add(file2);
        status();
        rm(file2);
        status();
        commit("Add file1.txt");
        add(file2);
        commit("Add file2.txt");
        status();
        log();

        check("multipleCommits.txt");
    }

    @Test
    public void testCheckoutFile() throws Exception {
        String file = "file.txt";
        createFile(file, "aaa");
        add(file);
        commit("Add file.txt");

        deleteFile(file);
        status();
        checkoutFiles("--", file);
        fileContent(file);
        status();

        createFile(file, "bbb");
        fileContent(file);
        status();
        checkoutFiles("--", file);
        fileContent(file);
        status();

        check("checkoutFile.txt");
    }

    @Test
    public void testReset() throws Exception {
        String file = "file.txt";
        createFile(file, "aaa");
        add(file);
        commit("First commit");

        createFile(file, "bbb");
        add(file);
        commit("Second commit");
        log();

        reset(1);
        fileContent(file);
        log();

        createFile(file, "ccc");
        add(file);
        commit("Third commit");
        log();

        check("reset.txt");
    }

    @Test
    public void testResetWithUntracked() throws Exception {
        String file = "file.txt";
        createFile(file, "aaa");
        add(file);
        commit("First commit");

        createFile(file, "bbb");
        add(file);
        commit("Second commit");
        log();

        String newFile = "newfile.txt";
        createFile(newFile, "somedata");

        reset(1);
        fileContent(file);
        log();
        status();
    }

    @Test
    public void testCheckout() throws Exception {
        String file = "file.txt";
        createFile(file, "aaa");
        add(file);
        commit("First commit");

        createFile(file, "bbb");
        add(file);
        commit("Second commit");
        log();

        checkoutRevision(1);
        status();
        log();

        checkoutMaster();
        status();
        log();

        check("checkout.txt");
    }

    @Test
    public void testVerifyCheckout() throws Exception {
        String file = "file.txt";
        createFile(file, "aaa");
        add(file);
        commit("First commit");

        createFile(file, "bbb");
        add(file);
        commit("Second commit");
        log();

        createFile(file, "ccc");
        checkoutRevision(1);
    }


    @Test
    public void testBranches() throws Exception {
        createFileAndCommit("file1.txt", "aaa");

        createBranch("develop");
        createFileAndCommit("file2.txt", "bbb");

        status();
        log();
        showBranches();
        checkoutMaster();
        status();
        log();

        createBranch("new-feature");
        createFileAndCommit("file3.txt", "ccc");
        status();
        log();

        checkoutBranch("develop");
        status();
        log();

        check("branches.txt");
    }

    @Test
    public void testBranchRemove() throws Exception {
        createFileAndCommit("file1.txt", "aaa");
        createBranch("develop");
        createFileAndCommit("file2.txt", "bbb");
        status();
        checkoutBranch("master");
        status();
        removeBranch("develop");
        showBranches();

        check("branchRemove.txt");
    }

    @Test
    public void testEmptyMerge() throws Exception {
        createFileAndCommit("file1.txt", "aaa");
        createFileAndCommit("file2.txt", "bbb");
        createBranch("develop");
        createFileAndCommit("file3.txt", "ccc");
        log();
        merge("master");
    }

    @Test
    public void testFastForwardMerge() throws Exception {
        createFileAndCommit("file1.txt", "aaa");
        createFileAndCommit("file2.txt", "bbb");
        createBranch("develop");
        createFileAndCommit("file3.txt", "ccc");
        log();
        checkoutBranch("master");
        merge("develop");
        log();
    }

    @Test
    public void testRecursiveMerge() throws Exception {
        createFileAndCommit("file1.txt", "aaa");
        createFileAndCommit("file2.txt", "bbb");
        createFileAndCommit("file3.txt", "ccc");
        createBranch("develop");
        createFileAndCommit("file4.txt", "ddd");
        createFileAndCommit("file5.txt", "eee");
        checkoutMaster();
        createBranch("custom");
        createFileAndCommit("file6.txt", "fff");

        merge("develop");
    }

    @Test
    public void testModify() throws Exception {
        createFileAndCommit("a.txt", "aaa");
        createFile("b.txt", "bbb");
        createFile("a.txt", "ccc");
        status();
        add("a.txt", "b.txt");
        status();
    }

    @Test
    public void testMergeConflict() throws Exception {
        createFileAndCommit("a.txt", "aaa");

        createBranch("dev");
        createFileAndCommit("a.txt", "bbb");

        checkoutMaster();
        merge("dev");

        checkoutRevision(1);
        createBranch("dev-2");
        createFileAndCommit("a.txt", "ccc");

        Assertions.assertThrows(GitException.class, () -> merge("dev"));
    }
}
