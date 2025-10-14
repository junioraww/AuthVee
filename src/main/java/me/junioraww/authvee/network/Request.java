package me.junioraww.authvee.network;

import java.util.Arrays;

public class Request {
    private final Action action;
    private final String[] args;

    public Request(Action action, String... args) {
        this.action = action;
        this.args = args;
    }

    public static Request fromString(String s) {
        String[] parts = s.split(":");
        Action action = Action.values()[Integer.parseInt(parts[0])];
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);
        return new Request(action, args);
    }

    @Override
    public String toString() {
        return action.id + ":" + String.join(":", args);
    }

    public Action getAction() { return action; }
    public String[] getArgs() { return args; }

    public enum Action {
        CREDENTIALS(0),
        REGISTER(1);

        private final int id;
        Action(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
