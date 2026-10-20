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
        NeoForge.EVENT_BUS.addListener(this::onChat);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
    }

    private void onServerStarting(net.neoforged.neoforge.event.server.ServerStartingEvent event) {
        try {
            this.luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException ex) {
            this.luckPerms = null;
        }
    }

    private void onChat(ServerChatEvent event) {
        LuckPerms lp = this.luckPerms;
        if (lp == null) return;
        
        ServerPlayer player = event.getPlayer();
        CachedMetaData meta = lp.getPlayerAdapter(ServerPlayer.class).getMetaData(player);
        
        String suffix = empty(meta.getSuffix());
        String messageColor = empty(meta.getMetaValue("message-color"));

        MutableComponent output = Component.empty();
        output.append(parseLegacy(messageColor + event.getRawText()));
        output.append(parseLegacy(suffix));

        event.setMessage(output);
    }

    private static String empty(String value) { return value == null ? "" : value; }

    private static MutableComponent parseLegacy(String input) {
        MutableComponent result = Component.empty();
        StringBuilder plain = new StringBuilder();
        Style style = Style.EMPTY;
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '&' && i + 1 < input.length()) {
                ChatFormatting f = ChatFormatting.getByCode(input.charAt(i + 1));
                if (f != null) {
                    if (plain.length() > 0) {
                        result.append(Component.literal(plain.toString()).withStyle(style));
                        plain.setLength(0);
                    }
                    if (f == ChatFormatting.RESET) style = Style.EMPTY;
                    else if (f.isColor()) style = Style.EMPTY.withColor(f);
                    else style = style.applyFormat(f);
                    i++;
                    continue;
                }
            }
            plain.append(ch);
        }
        if (plain.length() > 0) result.append(Component.literal(plain.toString()).withStyle(style));
        return result;
    }
}
