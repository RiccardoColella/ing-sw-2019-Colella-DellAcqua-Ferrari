package it.polimi.ingsw.shared.messages;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.shared.messages.templates.Question;
import it.polimi.ingsw.utils.EnumValueByString;

import java.io.Serializable;
import java.util.UUID;

/**
 * The smallest unit of information that is sent from and to clients in the IO process.
 * It's made by a type and a payload stored in JSON format
 *
 * @author Carlo Dell'Acqua
 */
public class Message implements Serializable {

    /**
     * The type of message
     */
    public enum Type {
        QUESTION,
        ANSWER,
        EVENT
    }

    /**
     * JSON conversion utility
     */
    private static transient final Gson gson = new Gson();

    /**
     * Name of the message
     */
    private final String name;
    /**
     * Payload of the message
     */
    private final String payload;
    /**
     * A stream id associated with the message, useful for parallel question/answer flows
     */
    private final String flowId;
    /**
     * Type of the message
     */
    private final Type type;

    /**
     * Constructs a message
     *
     * @param name name of the message
     * @param payload payload of the message
     * @param flowId stream id associated with the message
     * @param type type of the message
     */
    protected Message(String name, Object payload, String flowId, Type type) {
        this.name = name;
        this.payload = new Gson().toJsonTree(payload).toString();
        this.flowId = flowId;
        this.type = type;
    }

    /**
     * Factory method that creates a question
     *
     * @param name name of the message
     * @param payload payload of the message
     * @return a question message
     */
    public static Message createQuestion(String name, Question payload) {
        return new Message(name, payload, UUID.randomUUID().toString(), Type.QUESTION);
    }

    /**
     * Factory method that creates a question
     *
     * @param enumValue the enum value that will be used as the name of the message
     * @param payload payload of the message
     * @return a question message
     */
    public static Message createQuestion(Enum<?> enumValue, Question payload) {
        return new Message(enumValue.toString(), payload, UUID.randomUUID().toString(), Type.QUESTION);
    }

    /**
     * Factory method that creates an answer
     *
     * @param name name of the message
     * @param payload payload of the message
     * @param flowId stream identifier of the question/answer flow
     * @return an answer message
     */
    public static Message createAnswer(String name, Object payload, String flowId) {
        return new Message(name, payload, flowId, Type.ANSWER);
    }

    /**
     * Factory method that creates an answer
     *
     * @param enumValue the enum value that will be used as the name of the message
     * @param payload payload of the message
     * @param flowId stream identifier of the question/answer flow
     * @return an answer message
     */
    public static Message createAnswer(Enum<?> enumValue, Object payload, String flowId) {
        return new Message(enumValue.toString(), payload, flowId, Type.ANSWER);
    }

    /**
     * Factory method that creates an event
     *
     * @param name name of the message
     * @param payload payload of the message
     * @return an event message
     */
    public static Message createEvent(String name, Object payload) {
        return new Message(name, payload, "event", Type.EVENT);
    }

    /**
     * Factory method that creates an event
     *
     * @param enumValue the enum value that will be used as the name of the message
     * @param payload payload of the message
     * @return an event message
     */
    public static Message createEvent(Enum<?> enumValue, Object payload) {
        return createEvent(enumValue.toString(), payload);
    }

    /**
     * @return the name of the message
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the name of the message as an enum value
     *
     * @param enumType the enum type class
     * @param <T> the enum type
     * @return the enum value corresponding to the message name
     */
    public <T extends Enum<T>> T getNameAsEnum(Class<T> enumType) {
        return EnumValueByString.findByString(name, enumType);
    }

    /**
     * Return the payload wrapped in a JsonElement object
     *
     * @return the payload of the message
     */
    public JsonElement getPayload() {
        return gson.fromJson(payload, new TypeToken<JsonElement>(){}.getType());
    }

    /**
     * Constructs a message from a JSON string
     *
     * @param json the JSON string representing the message
     * @return a Message
     */
    public static Message fromJson(String json) {
        return gson.fromJson(json, new TypeToken<Message>(){}.getType());
    }

    /**
     * Converts this message into a JSON string
     *
     * @return the JSON representation of this message
     */
    public String toJson() {
        return gson.toJson(this);
    }

    public String getFlowId() {
        return flowId;
    }

    public Type getType() {
        return type;
    }
}
