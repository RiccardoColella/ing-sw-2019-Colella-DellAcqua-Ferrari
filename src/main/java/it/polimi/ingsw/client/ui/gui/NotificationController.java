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

/**
 * This class handles and displays small notification windows
 *
 * @author Adriana Ferrari
 */
public class NotificationController extends WindowController {

    /**
     * The message of the notification
     */
    @FXML
    private Text message;

    /**
     * The title of the notification
     */
    @FXML
    private Text title;

    /**
     * The main pane of the notification
     */
    @FXML
    private GridPane window;

    /**
     * Default auto close timeout
     */
    private static final int CLOSE_TIMEOUT = 2000;

    /**
     * Set of listeners of the notification
     */
    private Set<NotificationListener> notificationListeners = new HashSet<>();

    /**
     * Constructor given the title and the content
     *
     * @param title The title of the notification
     * @param notification The content of the notification
     */
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

    /**
     * Adds a Listener to the Set
     *
     * @param l the new Listener
     */
    public void addNotificationListener(NotificationListener l) {
        notificationListeners.add(l);
    }

    /**
     * Removes a Listener from the Set
     *
     * @param l the Listener to remove
     */
    public void removeNotificationListener(NotificationListener l) {
        notificationListeners.remove(l);
    }

    /**
     * Shows the notification that will self-close in CLOSE_TIMEOUT milliseconds
     */
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

    /**
     * Allows for a notification to be manually closed (and notifies the listeners)
     */
    @FXML
    public void closeNotification() {
        close();
        NotificationClosed closed = new NotificationClosed(this);
        notificationListeners.forEach(l -> l.onNotificationClosed(closed));
    }

    /**
     * Initializes the viewport
     */
    @FXML
    public void initialize() {
        setupViewport(window);
    }
}
