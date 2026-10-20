
package fr.gameocracy.chat;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

@Mod(GameocracyChat.MOD_ID)
public final class GameocracyChat {
    public static final String MOD_ID = "gameocracychat";

    private volatile LuckPerms luckPerms;

    public GameocracyChat(IEventBus modBus, ModContainer container) {
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onChat);
    }

    private void onServerStarting(
            net.neoforged.neoforge.event.server.ServerStartingEvent event) {
        try {
            this.luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException ex) {
            this.luckPerms = null;
        }
    }

    private void onChat(ServerChatEvent event) {
        LuckPerms lp = this.luckPerms;
        if (lp == null) {
            return;
        }

        ServerPlayer player = event.getPlayer();

        CachedMetaData meta =
                lp.getPlayerAdapter(ServerPlayer.class).getMetaData(player);

        String prefix = empty(meta.getPrefix());
        String suffix = empty(meta.getSuffix());
        String usernameColor = empty(meta.getMetaValue("username-color"));
        String messageColor = empty(meta.getMetaValue("message-color"));

        MutableComponent output = Component.empty();

        output.append(parseLegacy(prefix));
        output.append(parseLegacy(
                usernameColor + player.getGameProfile().name()));
        output.append(Component.literal(": "));
        output.append(parseLegacy(messageColor + event.getRawText()));
        output.append(parseLegacy(suffix));

        // Empêche le message original d'être affiché une seconde fois.
        event.setCanceled(true);

        // Diffuse la ligne formatée à tous les joueurs.
        player.getServer().getPlayerList()
                .broadcastSystemMessage(output, false);
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }

    private static MutableComponent parseLegacy(String input) {
        MutableComponent result = Component.empty();
        StringBuilder plain = new StringBuilder();
        Style style = Style.EMPTY;

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            if (ch == '&' && i + 1 < input.length()) {
                ChatFormatting formatting =
                        ChatFormatting.getByCode(input.charAt(i + 1));

                if (formatting != null) {
                    if (plain.length() > 0) {
                        result.append(
                                Component.literal(plain.toString())
                                        .withStyle(style));
                        plain.setLength(0);
                    }

                    if (formatting == ChatFormatting.RESET) {
                        style = Style.EMPTY;
                    } else if (formatting.isColor()) {
                        style = Style.EMPTY.withColor(formatting);
                    } else {
                        style = style.applyFormat(formatting);
                    }

                    i++;
                    continue;
                }
            }

            plain.append(ch);
        }

        if (plain.length() > 0) {
            result.append(
                    Component.literal(plain.toString()).withStyle(style));
        }

        return result;
    }
}
