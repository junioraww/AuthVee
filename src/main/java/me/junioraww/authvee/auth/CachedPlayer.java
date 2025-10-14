package me.junioraww.authvee.auth;

import com.velocitypowered.api.proxy.Player;

public class CachedPlayer {
    private Player player;
    public State state;
    private Credentials credentials;
    private long expiresAfter = 0;


    public CachedPlayer(Player player) {
        this.player = player;
        this.state = State.Unresolved;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isOnline() {
        return this.player.isActive();
    }

    public boolean isExpired() {
        long unixTime = System.currentTimeMillis() / 1000L;
        return unixTime > expiresAfter;
    }

    public void setExpired(long delay) {
        long unixTime = System.currentTimeMillis() / 1000L;
        this.expiresAfter = unixTime + delay;
    }

    public long getExpiresAfter() {
        return expiresAfter;
    }

    public boolean requireAuth(Player joiningPlayer) {
        if(this.state != State.Authenticated) return true;
        // vv Сохранение сессии на 5 минут по IP
        if(!this.isExpired() && joiningPlayer.getRemoteAddress().getAddress().equals(this.player.getRemoteAddress().getAddress())) return false;
        // vv State авторизованный, и пароля нет
        this.state = this.credentials == null ? State.Register : State.Login;
        return true;
    }

    public void logout() {
        if(this.state != State.Authenticated) return;
        this.state = State.Login;
    }

    public void setBlock(String reason, long until) {
        //this.credentials. = reason;
        //this.banUntil = until;
    }

    /*public class BanInfo {
        private final String reason;
        private final long until;

        public BanInfo(String reason, long until) {
            this.reason = reason;
            this.until = until;
        }

        public String getReason() {
            return reason;
        }

        public long getUntil() {
            return until;
        }
    }

    public BanInfo getBanned() {
        return banUntil > System.currentTimeMillis() ? new BanInfo(banReason, banUntil) : null;
    }*/

    public void joined() {
        setExpired(300);
    }

    public void left() {
        setExpired(300);
    }

    public enum State {
        Unresolved,
        Authenticated,
        TwoFa,
        Discord,
        Login,
        Register
    }
}
