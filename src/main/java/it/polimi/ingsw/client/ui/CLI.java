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

import java.util.Optional;

public class CLI implements CommandReceivedListener {

    private static final Gson gson = new Gson();
    private final Connector connector;

    public CLI(Connector connector) {
        this.connector = connector;
        connector.addCommandReceivedListener(this);
    }

    @Override
    public void onCommandReceived(CommandReceived e) {

        ClientApi commandType = EnumValueByString.findByString(e.getCommand().getName(), ClientApi.class);

        switch (commandType) {
            //TODO case EVENT_*

            case BLOCK_QUESTION:
            case DIRECTION_QUESTION:
                manageQuestion(e.getCommand());
                break;
        }


    }

    private void manageQuestion(Command command) {
        Question question = gson.fromJson(command.getPayload(), new TypeToken<Question>(){}.getType());
        System.console().printf(question.getText());
        Object[] options = question.getAvailableOptions().toArray();
        if (question.isSkippable()) {
            System.console().printf("0) None");
        }
        for (int i = 0; i < options.length; i++) {
            System.console().printf("%d) %s", (i + 1), options[i].toString());
        }
        int chosenIndex = Integer.parseInt(System.console().readLine());
        if (question.isSkippable()) {
            connector.sendCommand(new Command(ServerApi.ANSWER.toString(), chosenIndex == 0 ? Optional.empty() : Optional.of(options[chosenIndex])));
        } else {
            connector.sendCommand(new Command(ServerApi.ANSWER.toString(), options[chosenIndex]));
        }
    }
}
