package me.junioraww.authvee.handler;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.protocol.packet.ClientSettingsPacket;
import me.junioraww.authvee.AuthVee;
import me.junioraww.authvee.auth.CachedPlayer;
import me.junioraww.authvee.auth.Credentials;
import me.junioraww.authvee.events.HerculesSessionEvent;
import me.junioraww.authvee.utils.*;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboSessionHandler;
import net.elytrium.limboapi.api.material.Item;
import net.elytrium.limboapi.api.player.LimboPlayer;
import net.elytrium.limboapi.api.protocol.packets.PacketFactory;
import net.elytrium.limboapi.protocol.packets.s2c.*;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.title.Title;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SessionHandler implements LimboSessionHandler {
    private LimboPlayer limboPlayer;
    private CachedPlayer cachedPlayer;
    private ScheduledFuture scheduled;
    private final AuthVee plugin;

    private Player player;
    private String language = "en";

    private int totalTime = 25;
    private int timeLeft = totalTime;
    private int wrongTries;

    public SessionHandler(AuthVee plugin, CachedPlayer cachedPlayer) {
        this.plugin = plugin;
        this.cachedPlayer = cachedPlayer;
    }

    @Override
    public void onDisconnect() {
        if(scheduled != null && !scheduled.isCancelled()) scheduled.cancel(false);
    }

    @Override
    public void onSpawn(Limbo server, LimboPlayer limboPlayer) {
        this.limboPlayer = limboPlayer;
        this.player = limboPlayer.getProxyPlayer();
        this.limboPlayer.disableFalling();
        this.wrongTries = 0;

        System.out.println("spawning");

        Player player = limboPlayer.getProxyPlayer();

        plugin.getServer().getEventManager().fire(new HerculesSessionEvent(player.getUsername())).thenAccept((event) -> {
            System.out.println("giving");
            sendExperienceBar();
            System.out.println("and then");
            giveFoxMap();
            System.out.println("given");

            limboPlayer.flushPackets();

            if (cachedPlayer.state == CachedPlayer.State.Unresolved) {
                try {
                    Credentials credentials = Hercules.requestCredentials(event.getUsername());
                    System.out.println("creds" + credentials);
                    if(credentials == null) cachedPlayer.state = CachedPlayer.State.Register;
                    else {
                        cachedPlayer.state = CachedPlayer.State.Login;
                        cachedPlayer.setCredentials(credentials);
                    }
                } catch (InterruptedException e) {
                    Message.send(player, Locale.ServersDown.get(language));
                }
            }

            System.out.println("status " + cachedPlayer.state);

            if (cachedPlayer.state == CachedPlayer.State.Register) player.showTitle(
                    Title.title(Message.get(Locale.RegTitle.get(language)), Message.get(Locale.RegSubtitle.get(language)), Titles.times)
            );
            else if (cachedPlayer.state == CachedPlayer.State.Login) player.showTitle(
                    Title.title(Message.get(Locale.LoginTitle.get(language)), Message.get(Locale.LoginSubtitle.get(language)), Titles.times)
            );
            else if (cachedPlayer.state == CachedPlayer.State.Unresolved) player.showTitle(Titles.error);
        });
    }

    private void sendExperienceBar() {
        Message.sendActionbar(player, Locale.LauncherAuthorization.get(language));

        scheduled = limboPlayer.getScheduledExecutor().scheduleAtFixedRate(() -> {
            timeLeft -= 1;
            if(timeLeft % 5 == 0) Logger.getLogger("AuthVee").info(player.getUsername() + " осталось " + timeLeft + " на авторизацию");
            if(timeLeft < 1) {
                player.disconnect(Message.get(Locale.Timeout.get(language)));
            } else {
                limboPlayer.writePacketAndFlush(new SetExperiencePacket((float) timeLeft / totalTime, timeLeft, 1));
            }
        }, 1L, 1L, TimeUnit.SECONDS);

        limboPlayer.writePacket(new SetExperiencePacket(1, timeLeft, 1));
    }

    private void giveFoxMap() {
        String imgPath = AuthVee.getConfig().get("fox_image");

        ImageCache imageCache = new ImageCache();
        BufferedImage img = imageCache.getImage(imgPath);

        System.out.println("[MapDebug] start; img=" + img.getWidth() + "x" + img.getHeight());
        limboPlayer.sendImage(1, img, false);

        limboPlayer.writePacket(plugin.getFactory().getPacketFactory().createSetSlotPacket(
                0, 45, plugin.getFactory().getItem(Item.FILLED_MAP), 1, 0,
                plugin.getFactory().createItemComponentMap().add(ProtocolVersion.MINECRAFT_1_20_5, "minecraft:map_id", 1)));
    }


    @Override
    public void onChat(String message) {
        String[] args = message.split(" ");

        if (args.length > 0) {
            if (args[0].matches("/r(eg(ister)?)?") && cachedPlayer.state == CachedPlayer.State.Register) {
                if(args.length < 2) Message.send(player, Locale.Timeout.get(language));
                else {
                    String password = args[1];
                    int length = password.length();
                    if(length < 8) Message.send(player, Locale.PasswordIsTooShort.get(language));
                    else if(length > 60) Message.send(player, Locale.PasswordIsTooLong.get(language));
                    else {
                        try {
                            boolean isSuccess = Hercules.register(player.getUsername(), password);

                            if(isSuccess) {
                                if (scheduled != null) scheduled.cancel(false);
                                Message.send(player, Locale.RegisterSuccess.get(language));
                                cachedPlayer.state = CachedPlayer.State.Authenticated;
                                plugin.successAuth(limboPlayer, language);
                            } else throw new InterruptedException();

                        } catch (InterruptedException e) {
                            Message.send(player, Locale.ServersDown.get(language));
                        }
                    }
                }
                //String hashed = ApricotIntegration.sha256(password);
                return;
            }
            else if (args[0].matches("/l(og(in)?)?") && cachedPlayer.state == CachedPlayer.State.Login) {
                if(args.length != 2) Message.send(player, Locale.EmptyLoginPassword.get(language));
                else {
                    String password = args[1];
                    boolean verified = cachedPlayer.getCredentials().verifyPassword(password);
                    if(verified) {
                        if(scheduled != null) scheduled.cancel(false);
                        Message.send(player, Locale.LoginSuccess.get(language));
                        cachedPlayer.state = CachedPlayer.State.Authenticated;
                        plugin.successAuth(limboPlayer, language);
                    }
                    else {
                        this.wrongTries += 1;
                        if(this.wrongTries == 3) {
                            player.disconnect(Message.get(Locale.WrongPassKick.get(language)));
                        }
                        else Message.send(player, Locale.WrongPass.get(language));
                    }
                }
                return;
            }
        }

        Message.send(player, Locale.WrongMessage.get(language));
    }

    @Override
    public void onGeneric(Object packet) {
        Logger.getLogger("packet").info(packet + "");
        /*
        Клиент изменил язык в настройках
         */
        if(packet instanceof ClientSettingsPacket) {
            language = ((ClientSettingsPacket) packet).getLocale();

            if(player == null) return;

            if (cachedPlayer.state == CachedPlayer.State.Register) player.showTitle(
                    Title.title(Message.get(Locale.RegTitle.get(language)), Message.get(Locale.RegSubtitle.get(language)), Titles.times)
            );
            else if (cachedPlayer.state == CachedPlayer.State.Login) player.showTitle(
                    Title.title(Message.get(Locale.LoginTitle.get(language)), Message.get(Locale.LoginSubtitle.get(language)), Titles.times)
            );
        }
    }
}
