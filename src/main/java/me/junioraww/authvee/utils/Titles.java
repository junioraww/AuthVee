package me.junioraww.authvee.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;

import java.awt.*;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Objects;

public class Titles {
    public static final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    public static final Title.Times times = Title.Times.times(Duration.ofSeconds(1), Duration.ofHours(1), Duration.ofHours(1));
    public static final Title.Times instant = Title.Times.times(Duration.ZERO, Duration.ofMinutes(1), Duration.ZERO);


    public static final String[][] successTexts = new String[][] {
            new String[]{"Going camping",
                    "Scanning fingerprints", "Going through face control",
                    "Initializing your avatar", "Creating a metaverse"},
            new String[]{"Собираемся в поход",
                    "Сканируем отпечатки", "Проходим фейс-контроль",
                    "Оплачиваем билеты", "Создаем метавселенную"},
            new String[]{"Збираємося в похід",
                    "Скануємо відбитки", "Проходимо фейс-контроль",
                    "Оплачуємо квитки", "Створюємо метавсесвіт"}
    };

    public static final String[] successTitle = new String[] {
            "Success!",
            "Успешно!",
            "Успешно!"
    };

    public static final Title success(int textNum, int localeNum) {
        String coloredText = colorize(successTexts[localeNum][textNum]);

        Title title = Title.title(serializer.deserialize("&x&6&6&f&f&0&0&l" + successTitle[localeNum]), serializer.deserialize(coloredText), instant);
        return title;
    }

    public static Component blocked(String reason, long until) {
        var time = new Timestamp(until).toLocalDateTime();
        var formatUntil = String.format("%02d.%02d.%02d %02d:%02d:%02d",
                time.getDayOfMonth(), time.getMonthValue(), time.getYear(), time.getHour(), time.getMinute(), time.getSecond());
        return serializer.deserialize("&x&f&f&0&0&0&0&lВы забанены!\n&cПричина: &f" + reason
        + "\n&cСроки: &fдо " + formatUntil
        + "\n\n&6Вы можете обжаловать решение на &emake.artwld.net");
    }

    public static String colorize(String text) {
        String[] split = text.split("");
        StringBuilder colorized = new StringBuilder();
        float i = 0;
        float step = (float) (System.currentTimeMillis() % (text.length() * 350L) / 50);
        float frames = text.length() * 7;
        for(String symbol : split) {
            var RGB = getRGB(i + step, frames);
            var hex = getHex(RGB);
            colorized.append(hex).append(symbol);
            i++;
        }
        return colorized.toString();
    }

    private static String getHex(int color) {
        String hex = Integer.toHexString(color).substring(2);
        return "&x" + hex.replaceAll("(.)","&$1");
    }

    private static int getRGB(float step, float frames) {
        int color = Color.HSBtoRGB(step / frames, 1.0F, 1.0F);
        return color;
    }

    public static Title error =
            Title.title(serializer.deserialize("&x&f&f&0&0&0&0&lУпс!"), serializer.deserialize("&eАвторизация недоступна, напишите в Discord"), times);
}
