package it.polimi.ingsw.server.controller.weapons;

import java.util.*;

public interface Communicator {
    <T> T select(Collection<T> options);
    <T> Optional<T> selectOptional(List<T> options);
    <T> Optional<T> selectOptional(Set<T> options);
    <T> List<T> selectMultiple(List<T> options);
}
