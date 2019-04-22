package it.polimi.ingsw.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.shared.commands.ClientApi;
import it.polimi.ingsw.shared.commands.Command;
import it.polimi.ingsw.shared.commands.Question;
import it.polimi.ingsw.shared.commands.ServerApi;
import it.polimi.ingsw.shared.events.CommandReceived;
import it.polimi.ingsw.shared.events.listeners.CommandReceivedListener;
import it.polimi.ingsw.utils.EnumValueByString;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;

public class CLI implements CommandReceivedListener {

    private static final Gson gson = new Gson();
    private final Connector connector;
    private final Scanner scanner;
    private final PrintStream printStream;


    public CLI(Connector connector, InputStream inputStream, OutputStream outputStream) {
        this.connector = connector;
        connector.addCommandReceivedListener(this);
        scanner = new Scanner(inputStream);
        printStream = new PrintStream(outputStream);
    }

    @Override
    public void onCommandReceived(CommandReceived e) {

        ClientApi commandType = EnumValueByString.findByString(e.getCommand().getName(), ClientApi.class);

        switch (commandType) {
            //TODO case EVENT_*

            case BLOCK_QUESTION:
            case DIRECTION_QUESTION:
            case ATTACK_QUESTION:
            case TARGET_QUESTION:
                manageQuestion(e.getCommand());
                break;
        }
    }

    private void manageQuestion(Command command) {
        Question question = gson.fromJson(command.getPayload(), new TypeToken<Question>(){}.getType());
        Object[] options = question.getAvailableOptions().toArray();

        int chosenIndex;
        do {
            printStream.println(question.getText());
            if (question.isSkippable()) {
                printStream.println("0) Skip");
            }
            for (int i = 0; i < options.length; i++) {
                printStream.println(String.format("%d) %s", (i + 1), options[i].toString()));
            }
            chosenIndex = Integer.parseInt(scanner.nextLine());
        } while ((question.isSkippable() ? 0 : 1) <= chosenIndex && chosenIndex <= options.length);
        connector.sendCommand(new Command(ServerApi.ANSWER.toString(), chosenIndex));
    }
}
