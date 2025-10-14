package me.junioraww.authvee.network;

import java.util.Arrays;

public class Response {
    private final Result result;
    private final String[] args;

    public Response(Result action, String... args) {
        this.result = action;
        this.args = args;
    }

    public static Response fromString(String s) {
        System.out.println("r" + s);
        String[] parts = s.split(":");
        Result result = Result.values()[Integer.parseInt(parts[0])];
        System.out.println("2" + s);
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);
        System.out.println("3" + s);
        return new Response(result, args);
    }

    @Override
    public String toString() {
        return result + ":" + String.join(":", args);
    }

    public Result getResult() { return result; }
    public String[] getArgs() { return args; }

    public enum Result {
        SUCCESS(0),
        FAILURE(1);

        private final int id;
        Result(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
