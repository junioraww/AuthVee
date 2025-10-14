package me.junioraww.authvee.utils;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordHasher {
    static Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    public static String getHash(String password) {
        char[] chars = password.toCharArray();
        return argon2.hash(2, 32768, 1, chars);
    }

    public static boolean verify(String password, String hash) {
        char[] chars = password.toCharArray();
        return argon2.verify(hash, chars);
    }
}
