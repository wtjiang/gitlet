package gitlet;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 * @author Winston
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        CommandManager commands = new CommandManager();
        commands.add("init", new InitCommand());
        commands.add("init-remote", new InitRemoteCommand());
        commands.add("log", new LogCommand());
        commands.add("global-log", new GlobalLogCommand());
        commands.add("commit", new CommitCommand());
        commands.add("ls-commit", new LsCommitCommand());
        commands.add("checkout", new CheckoutCommand());
        commands.add("branch", new BranchCommand());
        commands.add("rm-branch", new RemoveBranchCommand());
        commands.add("rm", new RemoveCommand());
        commands.add("status", new StatusCommand());
        commands.add("reset", new ResetCommand());
        commands.add("merge", new MergeCommand());
        commands.add("find", new FindCommand());
        commands.add("add", new AddCommand());

        commands.add("add-remote", new AddRemoteCommand());
        commands.add("rm-remote", new RemoveRemoteCommand());
        commands.add("push", new PushCommand());
        commands.add("fetch", new FetchCommand());
        commands.add("pull", new PullCommand());

        Repo repo = new Repo(System.getProperty("user.dir"));

        try {
            commands.process(repo, args);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

        repo.close();
        System.exit(0);
    }

}
