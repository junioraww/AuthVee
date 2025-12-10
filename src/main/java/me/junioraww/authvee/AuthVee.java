package me.junioraww.authvee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.junioraww.authvee.auth.CachedPlayer;
import me.junioraww.authvee.auth.PlayerCache;
import me.junioraww.authvee.command.LogoutCommand;
import me.junioraww.authvee.handler.Events;
import me.junioraww.authvee.handler.SessionHandler;
import me.junioraww.authvee.utils.ImageCache;
import me.junioraww.authvee.utils.TLSClient;
import me.junioraww.authvee.utils.Titles;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboFactory;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualWorld;
import net.elytrium.limboapi.api.command.LimboCommandMeta;
import net.elytrium.limboapi.api.player.GameMode;
import net.elytrium.limboapi.api.player.LimboPlayer;
import net.elytrium.limboapi.thirdparty.org.bstats.velocity.Metrics;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Plugin(
        id = "authvee",
        name = "AuthVee",
        version = BuildConstants.VERSION,
        dependencies = {
            @Dependency(id = "limboapi"),
        }
)

public class AuthVee {
    private LimboFactory factory;
    private Limbo authServer;
    private ProxyServer server;
    public PlayerCache playerCache = new PlayerCache();
    public static BufferedImage image;

    @Inject
    @DataDirectory
    private Path dataDirectory;

    private static Map<String, String> config;

    public static Map<String, String> getConfig() {
        return config;
    }

    // TODO
    // Если игрок зашел с прокси #1, не зарегистрировался, затем зашел с прокси #2 и зарегистрировался
    // То на прокси #1 он останется как "кешированный без пароля/регистрации"
    // Для исправления необходимо реализовать прямое общение между прокси #1, #2.. при регистрации игроков/смене состояний
    // + Придумать, как исправить гонку состояний (если произойдет /unregister или прочее)
    // TODO
    // Кеширование всех зарегистрированных/зашедших username за 30 дней

    private void initConfig() throws IOException {
        Files.createDirectories(dataDirectory);
        Path path = dataDirectory.resolve("config.yml");
        if (Files.notExists(path)) {
            Files.createFile(path);
        }

        final YamlConfigurationLoader loader =
                YamlConfigurationLoader.builder().path(path).build();

        ConfigurationNode root = loader.load();
        config = new LinkedHashMap<>();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : root.childrenMap().entrySet()) {
            config.put(entry.getKey().toString(), entry.getValue().getString());
        }
    }

    public static final MinecraftChannelIdentifier GLOBAL_BAN_IDENTIFIER = MinecraftChannelIdentifier.from("global:ban");

    public LimboFactory getFactory() {
        return factory;
    }

    private static TLSClient client;

    public static TLSClient getClient() { return client; }

    @Inject
    public AuthVee(Logger logger,
                   ProxyServer server,
                   Metrics.Factory metricsFactory,
                   @DataDirectory Path dataDirectory) {
        this.server = server;
        this.factory = (LimboFactory) server.getPluginManager().getPlugin("limboapi").flatMap(PluginContainer::getInstance).orElseThrow();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        VirtualWorld authWorld = this.factory.createVirtualWorld(
                Dimension.THE_END,
                0, 0, 0,
                (float) 180, (float) 0
        );

        try {
            initConfig();
        } catch (IOException e) {
            Logger.getLogger("AuthVee").warning("Couldn't init config!");
        }

        client = new TLSClient();
        client.init();

        getServer().getCommandManager().register("logout", new LogoutCommand(this));

        EventManager eventManager = this.server.getEventManager();
        eventManager.unregisterListeners(this);
        eventManager.register(this, new Events(this));

        this.authServer = this.factory
                .createLimbo(authWorld)
                .setName("Auth")
                .setWorldTime(1L)
                .setGameMode(GameMode.ADVENTURE)
                .registerCommand(new LimboCommandMeta(List.of("l", "log", "login")))
                .registerCommand(new LimboCommandMeta(List.of("r", "reg", "register")))
                .registerCommand(new LimboCommandMeta(List.of("c", "code")));

        server.getScheduler()
                .buildTask(this, () -> {
                    playerCache.clearExpired();
                })
                .repeat(60L, TimeUnit.SECONDS)
                .schedule();

        String imgPath = AuthVee.getConfig().get("fox_image");

        ImageCache imageCache = new ImageCache();
        image = imageCache.getImage(imgPath);
    }

    @Subscribe
    public void onPluginMessageFromBackend(PluginMessageEvent event) {
        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }

        if (event.getIdentifier() == GLOBAL_BAN_IDENTIFIER) {
            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            String username = in.readLine();
            var player = server.getPlayer(username);
            if(player.isEmpty() || !player.get().isActive()) return;
            String reason = in.readLine();
            long until = in.readLong();

            player.get().disconnect(Titles.blocked(reason, until));
            CachedPlayer cachedPlayer = playerCache.getPlayer(player.get());
            cachedPlayer.setBlock(reason, until);
        }
    }

    public ProxyServer getServer() {
        return server;
    }

    public void handlePlayer(Player player, CachedPlayer cachedPlayer) {
        this.authServer.spawnPlayer(player, new SessionHandler(this, cachedPlayer));
    }

    public void successAuth(LimboPlayer player, String language) {
        Player proxyPlayer = player.getProxyPlayer();
        player.getProxyPlayer().playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.AMBIENT, 1, 1));

        int textNum = new Random().nextInt(5);

        int localeNum;
        if(language.equals("ru_ru")) localeNum = 1;
        else if(language.equals("uk_ua")) localeNum = 2;
        else {
            localeNum = 0;
        }

        var task = server.getScheduler()
                .buildTask(this, () -> {
                    player.getProxyPlayer().showTitle(Titles.success(textNum, localeNum));
                })
                .repeat(50L, TimeUnit.MILLISECONDS)
                .schedule();

        playerCache.getPlayer(proxyPlayer).setPlayer(proxyPlayer);

        server.getScheduler().buildTask(this, () -> {
            task.cancel();
            player.disconnect();
        }).delay(3L, TimeUnit.SECONDS).schedule();
    }
}
