package me.junioraww.authvee.events;

public class HerculesSessionEvent {
    private final String username;

    public HerculesSessionEvent(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }
}
