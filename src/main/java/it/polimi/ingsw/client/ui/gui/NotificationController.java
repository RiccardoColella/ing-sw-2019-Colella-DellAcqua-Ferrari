package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.ui.gui.events.NotificationClosed;
import it.polimi.ingsw.client.ui.gui.events.listeners.NotificationListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.StageStyle;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationController extends WindowController {

    @FXML
    private Text message;
    @FXML
    private Text title;

    @FXML
    private GridPane window;

    private static final int CLOSE_TIMEOUT = 2000;

    private Set<NotificationListener> notificationListeners = new HashSet<>();

    protected NotificationController(String title, String notification) {
        super(title, "/fxml/notification.fxml", "/css/notification.css");
        stage.setAlwaysOnTop(true);
        stage.initStyle(StageStyle.UNDECORATED);
        message.setText(notification);
        message.setFill(Color.rgb(194, 194, 214));
        message.setWrappingWidth(270);
        GridPane.setValignment(message, VPos.TOP);
        this.title.setText(title);
        this.title.setFill(Color.rgb(194, 194, 214));
        this.title.setWrappingWidth(270);
        stage.setX(Screen.getPrimary().getVisualBounds().getMaxX() - stage.getMinWidth());
        stage.setY(Screen.getPrimary().getVisualBounds().getMaxY() - stage.getMinHeight());
    }

    public void addNotificationListener(NotificationListener l) {
        notificationListeners.add(l);
    }

    public void removeNotificationListener(NotificationListener l) {
        notificationListeners.remove(l);
    }

    public void showWithAutoClose() {
        show();
        Timer timer = new Timer();
        timer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(NotificationController.this::closeNotification);
                }
            },
            CLOSE_TIMEOUT
        );
    }

    @FXML
    public void closeNotification() {
        close();
        NotificationClosed closed = new NotificationClosed(this);
        notificationListeners.forEach(l -> l.onNotificationClosed(closed));
    }

    @FXML
    public void initialize() {
        setupViewport(window);
    }
}
