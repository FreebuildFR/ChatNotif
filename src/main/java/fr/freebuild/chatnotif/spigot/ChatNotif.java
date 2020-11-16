package fr.freebuild.chatnotif.spigot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.clip.deluxechat.DeluxeChat;
import me.clip.deluxechat.compatibility.CompatibilityManager;
import me.clip.deluxechat.events.DeluxeChatEvent;
import me.clip.deluxechat.fanciful.FancyMessage;
import me.clip.deluxechat.objects.DeluxeFormat;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import fr.freebuild.chatnotif.bungee.BungeeChannel;

public class ChatNotif extends JavaPlugin implements Listener {
  private DeluxeChat deluxeChat;
  private BungeeListener bungeeListener;

  @Override
  public void onEnable() {
    this.bungeeListener = new BungeeListener(this);

    this.deluxeChat = (DeluxeChat)Bukkit.getPluginManager().getPlugin("DeluxeChat");
    Bukkit.getPluginManager().registerEvents(this, this);

    this.getServer().getMessenger().registerOutgoingPluginChannel(this, BungeeChannel.CHAT.getChannel());
    this.getServer().getMessenger().registerIncomingPluginChannel(this, BungeeChannel.CHAT.getChannel(), this.bungeeListener);
  }

  @Override
  public void onDisable() {
    this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
  }

  /**
   * Event catch when DeluxeChat send a message in chat
   * Cancel sending a message by DeluxeChat and handle itself
   *
   * @param event Event sent by DeluxeChat
   */
  @EventHandler
  public void DeluxeChat(final DeluxeChatEvent event) {
    if (event.isCancelled()) {
      return;
    }

    event.setCancelled(true);
    event.getRecipients().clear();

    final FancyMessage fm = this.deluxeChat.getFancyChatFormat(event.getPlayer(), event.getDeluxeFormat());
    final String message = String.valueOf(fm.getLastColor()) + fm.getChatColor() + event.getChatMessage();
    final String format = serializeFormat(fm);

    this.handleSendingChatMessage(message, format, fm.getChatColor());
    this.bungeeListener.sendPluginMessage(event.getPlayer(), format, message, fm.getChatColor(), DeluxeChat.getServerName());
  }

  /**
   * Send message in chat of current server
   *
   * @param message   Message to send
   * @param format    Format to use with message
   * @param chatColor Color of chat to set after tag of player
   */
  public void handleSendingChatMessage(final String message, final String format, final String chatColor) {
    final Map<Player, String> players = this.getTaggedPlayers(message);
    final CompatibilityManager chat = deluxeChat.getChat();
    final String color = (chatColor == null || chatColor.isEmpty()) ? ChatColor.RESET.toString() : chatColor;

    for (Player player : Bukkit.getOnlinePlayers()) {

      String msg = message;
      if (players.containsKey(player)) {
        final String m = players.get(player);
        msg = msg.replace(m, ChatColor.RED + m + color);
      }

      final Set<Player> set = new HashSet<Player>();
      set.add(player);

      chat.sendDeluxeChat(player, format, chat.convertMsg(player, msg), set);

      if (players.containsKey(player)) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
      }
    }
  }

  /**
   * Get a map of tagged players in a message
   *
   * @param message Original message to check
   * @return        Map of players
  */
  private Map<Player, String> getTaggedPlayers(final String message) {
    final Map<Player, String> players = new HashMap<Player, String>();
    final String[] words = message.split(" ");

    for (String m : words) {
      m = m.replaceAll("(§[0-9a-frlonmk])", "");
      final Player p = Bukkit.getPlayerExact(m);
      if (p != null && p.isOnline() && !players.containsKey(p)) {
        players.put(p, m);
      }
    }
    return players;
  }

  /**
   * Serialize DeluxeChat format
   *
   * @param fm  FancyMessage format
   * @return    Return the format serialized
   */
  private String serializeFormat(final FancyMessage fm) {
    String format = fm.toJSONString().replace("%server%", "");

    format = this.deluxeChat.getChat().setHexColors(format);

    return format;
  }
}

