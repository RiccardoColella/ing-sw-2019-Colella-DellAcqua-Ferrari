package it.polimi.ingsw.server.view;

import java.util.*;

public interface Interviewer {
    <T> T select(Collection<T> options);
    <T> Optional<T> selectOptional(Collection<T> options);
    <T> Collection<T> selectMultiple(Collection<T> options);
}
