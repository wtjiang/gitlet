/**
 *
 */
package gitlet;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Winston
 */
public class CommandManager {
    /**
     * map of commands.
     */
    private HashMap<String, CommandInterface> commandsHashMap;

    /**
     * command manager constructor.
     */
    public CommandManager() {
        this.commandsHashMap = new HashMap<>();
    }

    /**
     * add functions for commands.
     *
     * @param comm                string
     * @param newCommandInterface new command
     */
    public void add(String comm, CommandInterface newCommandInterface) {
        this.commandsHashMap.put(comm, newCommandInterface);
    }

    /**
     * Processes a particular string of arguments to the program.
     *
     * @param localRepo Given a local repository.
     * @param args      The arguments.
     * @throws GitletException
     */
    public void process(Repo localRepo, String[] args)
            throws GitletException {
        if (args == null || args.length == 0) {
            throw new GitletException("Please enter a command.");
        }

        String trigger = args[0];
        CommandInterface command = this.commandsHashMap.get(trigger);

        if (command == null) {
            throw new GitletException(
                    "No command with that name exists.");
        }

        String[] operands = Arrays.copyOfRange(args, 1, args.length);
        if (!command.checkOperands(operands)) {
            throw new GitletException("Incorrect operands.");
        }

        if (command.needsRepo() && !localRepo.isOpen()) {
            throw new GitletException(
                    "Not in an initialized Gitlet directory.");
        }

        command.runCommand(localRepo, operands);
    }
}
