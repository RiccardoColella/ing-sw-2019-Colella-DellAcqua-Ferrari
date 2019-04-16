package it.polimi.ingsw.server.controller.weapons;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Communicator {
    <T> T select(List<T> options);
    <T, V> T select(Map<T, V> options);
    <T> Optional<T> selectOptional(List<T> options);
    <T> List<T> selectMultiple(List<T> options);
}
