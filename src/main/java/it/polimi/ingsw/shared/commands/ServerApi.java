package it.polimi.ingsw.shared.commands;

public enum ServerApi {

        DIRECTION_ANSWER("DirectionA"),
        BLOCK_ANSWER("BlockA");

        private String name;

        ServerApi(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
}
