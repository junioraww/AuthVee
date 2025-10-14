package me.junioraww.authvee.auth;

import me.junioraww.authvee.utils.PasswordHasher;

public class Credentials {
    private String password;
    private boolean banned;
    private String ban_reason;
    private int ban_expires_at;

    public Credentials(String hashedPassword) {
        this.password = hashedPassword;
    }

    public boolean verifyPassword(String password) {
        return PasswordHasher.verify(password, this.password);
    }

    public boolean isBanned() {
        return banned;
    }

    public String getBanReason() {
        return ban_reason;
    }

    public int getBanExpiresAt() {
        return ban_expires_at;
    }
}
