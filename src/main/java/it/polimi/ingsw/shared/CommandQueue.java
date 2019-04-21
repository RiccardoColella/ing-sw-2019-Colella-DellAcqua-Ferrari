package it.polimi.ingsw.shared;

import it.polimi.ingsw.shared.commands.Command;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CommandQueue {

    private Map<String, BlockingQueue<Command>> commandQueues = new HashMap<>();

    public void enqueue(Command command) {
        if (!commandQueues.containsKey(command.getName())) {
            commandQueues.put(command.getName(), new LinkedBlockingQueue<>());
        }
        commandQueues.get(command.getName()).add(command);
    }

    @SuppressWarnings("unchecked")
    public Command dequeue(String commandName) throws InterruptedException {
        if (!commandQueues.containsKey(commandName)) {
            commandQueues.put(commandName, new LinkedBlockingQueue<>());
        }
        return commandQueues.get(commandName).take();
    }

}
