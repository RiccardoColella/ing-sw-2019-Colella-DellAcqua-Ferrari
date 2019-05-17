package it.polimi.ingsw.shared.messages;

/**
 * A list of all the available messages supported by the client.
 * This enum is translated into the message name
 *
 * @author Carlo Dell'Acqua
 */
public enum ClientApi {

    DIRECTION_QUESTION,
    BLOCK_QUESTION,
    TARGET_QUESTION,
    TARGET_SET_QUESTION,
    ATTACK_QUESTION,
    BASIC_ACTION_QUESTION,
    SPAWNPOINT_QUESTION,
    PAYMENT_METHOD_QUESTION,
    WEAPON_CHOICE_QUESTION,
    RELOAD_QUESTION,
    POWERUP_QUESTION,

    MATCH_STARTED_EVENT,
    DUPLICATE_NICKNAME_EVENT,
    MATCH_ENDED_EVENT,
    MATCH_MODE_CHANGED_EVENT,
    MATCH_KILLSHOT_TRACK_CHANGED_EVENT,
    PLAYER_MOVED_EVENT,
    PLAYER_TELEPORTED_EVENT,
    PLAYER_DIED_EVENT,
    PLAYER_OVERKILLED_EVENT,
    PLAYER_REBORN_EVENT,
    PLAYER_BOARD_FLIPPED_EVENT,
    PLAYER_WALLET_CHANGED_EVENT,
    PLAYER_HEALTH_CHANGED_EVENT,
    WEAPON_RELOADED_EVENT,
    WEAPON_UNLOADED_EVENT,
    WEAPON_PICKED_EVENT,
    WEAPON_DROPPED_EVENT,
    PLAYER_DISCONNECTED_EVENT,
    PLAYER_RECONNECTED_EVENT,
    ACTIVE_PLAYER_CHANGED_EVENT,
    NEW_WEAPON_AVAILABLE_EVENT, PAYMENT_COLOR_QUESTION, PLAYER_SPAWNED_EVENT
}

