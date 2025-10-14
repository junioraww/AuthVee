package me.junioraww.authvee.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;

public class ImageCache {
    private final HashMap<String, BufferedImage> cache = new HashMap<>();

    public BufferedImage getImage(String path) {
        System.out.println("path " + path + " is url? " + isValidUrl(path));
        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        BufferedImage img;
        try {
            if (isValidUrl(path)) {
                img = ImageIO.read(URI.create(path).toURL());
            } else {
                img = ImageIO.read(new File(path));
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить изображение: " + path, e);
        }

        cache.put(path, img);
        return img;
    }

    private boolean isValidUrl(String s) {
        try {
            new URL(s).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
