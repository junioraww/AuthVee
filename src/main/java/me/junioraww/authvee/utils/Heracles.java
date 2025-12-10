package me.junioraww.authvee.utils;

import me.junioraww.authvee.AuthVee;
import me.junioraww.authvee.auth.Credentials;
import me.junioraww.authvee.network.Request;
import me.junioraww.authvee.network.Response;

import java.util.Arrays;

public class Heracles {
    private static class CredentialsRequest extends Request {
        public CredentialsRequest(String u) {
            super(Action.CREDENTIALS, u);
        }
    }

    private static class RegisterRequest extends Request {
        public RegisterRequest(String u, String p) {
            super(Action.REGISTER, u, p);
        }
    }

    public static Credentials requestCredentials(String username) throws InterruptedException {
        Request request = new CredentialsRequest(username);

        Response response = AuthVee.getClient().sendRequest(request);

        if(response.getResult() == Response.Result.FAILURE) return null;

        return new Credentials(response.getArgs()[0]);
    }

    public static boolean register(String username, String password) throws InterruptedException {
        String hashedPassword = PasswordHasher.getHash(password);
        Request request = new RegisterRequest(username, hashedPassword);
        Response response = AuthVee.getClient().sendRequest(request);
        return response.getResult() == Response.Result.SUCCESS;
    }
}
