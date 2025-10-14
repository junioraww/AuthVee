package me.junioraww.authvee.utils;

import com.velocitypowered.api.proxy.Player;

import java.util.logging.Logger;

public enum Locale {
    // TODO Изменить ampersand-цвета на Components

    LauncherAuthorization("&aДоступна автоматическая авторизация с &x&0&0&f&f&0&0лаунчера ReZane!",
            "&aQuick authorization via the ReZane launcher is available!",
            "&aДоступна швидка авторизація з лаунчер ReZane!"),
    Timeout("&6\uD83D\uDD25 &lВремя авторизации истекло!\n&aВы можете перезайти на сервер",
            "&6\uD83D\uDD25 &lThe login time has expired!\n&aYou can rejoin to the server.",
            "&6\uD83D\uDD25 &lЧас авторизації минув!\n&aВи можете перезайти на сервер"),
    EmptyPassword(
            "&cВы не указали пароль!\n&6Использование: &e/register <пароль>",
            "&cYou didn't specify a password!\n&6Usage: &e/register <password>",
            "&cВи не вказали пароль!\n&6Приклад: &e/register <пароль>"
    ),
    EmptyLoginPassword(
            "&cВы не указали пароль!\n&6Использование: &e/login <пароль>",
            "&cYou didn't specify a password!\n&6Usage: &e/login <password>",
            "&cВи не вказали пароль!\n&6Приклад: &e/login <пароль>"
    ),
    PasswordIsTooShort(
            "&cПароль должен содержать минимум 8 символов!\n&6Рекомендуем использовать в пароле специальные символы вида @, #, !",
            "&cThe password must contain at least 8 characters!\n&6We recommend using special characters in the password like @, #, !",
            "&cПароль повинен містити мінімум 8 символів!\n&6Використовуйте в паролі спеціальні символи виду @, #, !, чтобы вас не взломали."
    ),
    PasswordIsTooLong(
            "&cУвы, теперь у вас слишком длинный пароль!\n&6Сократите его до 60 символов.",
            "&cAlas, your password is too long!\n&6Please shorten it to 60 characters.",
            "&cНа жаль, у вас занадто довгий пароль!\n&6Скоротіть його до 60 символів."
    ),
    ServersDown(
            "&cНе удалось законнектиться!\n&6Похоже, что сервера регистрации отдыхают. Напишите в сообщество: discord.gg/MC6ccff",
            "&cFailed to connect!\n&6It looks like the registration servers are resting. You can contact us: discord.gg/MC6ccff",
            "&cНе вдалося увійти!\n&6Схоже, що сервера реєстрації відпочивають. Напишіть спільноті: discord.gg/MC6ccff"
    ),
    RegisterSuccess(
            "&aУспешная регистрация!",
            "&aSuccessful registration!",
            "&aУспішна реєстрація!"
    ),
    LoginSuccess(
            "&aУспешный вход!",
            "&aSuccessful login!",
            "&aУспішний вхід!"
    ),
    WrongPass(
            "&cНеверный пароль!\n&6Если вы забыли свой пароль, напишите в сообщество: discord.gg/MC6ccff",
            "&cWrong password!\n&eIf this is your first time playing with this nickname, then, alas, it is already occupied. Rejoin by coming up with a new nickname.\n&6If you just forgot your password, write to the community: discord.gg/MC6ccff",
            "&cНевірний пароль!\n&eЯкщо ви вперше граєте під цим ніком, то, на жаль, він вже зайнятий. Приєднуйтесь, придумавши новий Нік.\n&6Якщо ви просто забули свій пароль, напишіть спільноті: discord.gg/MC6ccff"
    ),
    WrongPassKick(
            "&c\uD83D\uDD25 &lНеверный пароль!\n&aВы можете перезайти на сервер.",
            "&c\uD83D\uDD25 &lWrong password!\n&aYou can rejoin to the server.",
            "&c\uD83D\uDD25 &lНевірний пароль!\n&aВи можете знову підключитися до сервера."
    ),
    WrongMessage(
             "&cВы в лимбо!\n&6Авторизуйтесь, используя &e/login &6или &e/register",
             "&cYou're in limbo!\n&6Log in using &e/login &6or &e/register",
             "&cВи в лімбо!\n&6Авторизуйтесь, використовуючи &e/login &6або &e/register"
    ),
    RegTitle(
            "&x&f&f&e&0&3&3\uD83D\uDC09 &lЗадайте пароль",
            "&x&f&f&e&0&3&3\uD83D\uDC09 &lSet the security password",
            "&x&f&f&e&0&3&3\uD83D\uDC09 &lВстановіть пароль безпеки"
    ),
    RegSubtitle(
            Titles.colorize("используя /register <пароль>"),
            Titles.colorize("using /register <password>"),
            Titles.colorize("використовувати /register <пароль>")
    ),
    LoginTitle(
            "&x&f&f&e&0&3&3\uD83D\uDC09 &lАвторизуйтесь",
            "&x&f&f&e&0&3&3\uD83D\uDC09 &lLog in",
            "&x&f&f&e&0&3&3\uD83D\uDC09 &lАвторизувати"
    ),
    LoginSubtitle(
            Titles.colorize("используя /login <пароль>"),
            Titles.colorize("using /login <password>"),
            Titles.colorize("використовувати /login <password>")
    );

    private final String ru;
    private final String en;
    private final String ua;

    Locale(String ru, String en, String ua) {
        this.ru = ru;
        this.en = en;
        this.ua = ua;
    }

    public String get(String locale) {
        if(locale.equals("ru_ru")) return this.ru;
        else if(locale.equals("uk_ua")) return this.ua;
        else return this.en;
    }
}
