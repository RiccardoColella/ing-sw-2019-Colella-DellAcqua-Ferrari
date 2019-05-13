package it.polimi.ingsw.shared.messages.templates.gsonadapters;

import it.polimi.ingsw.shared.messages.templates.Answer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This class represents a wrapper for the Answer parametrized type
 *
 * @author Carlo Dell'Acqua
 * @param <T> the type of the response
 */
public class AnswerOf<T> implements ParameterizedType {
    private final Class<T> type;

    public AnswerOf(Class<T> type) {
        this.type = type;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[] { type };
    }

    @Override
    public Type getRawType() {
        return Answer.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}