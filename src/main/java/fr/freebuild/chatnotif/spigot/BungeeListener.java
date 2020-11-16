package fr.freebuild.chatnotif.spigot;

import java.util.stream.Collectors;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import fr.freebuild.chatnotif.bungee.BungeeChannel;
import me.clip.deluxechat.DeluxeChat;

public class BungeeListener implements PluginMessageListener {
  private final ChatNotif plugin;

  public BungeeListener(final ChatNotif plugin) {
    this.plugin = plugin;
  }


  /**
   * Receive data sent by bungeecord
   * If it's ChatNotif message, send message in chat
   *
   * @param channel Channel used for data
   * @param player  Player used to receive the message
   * @param bytes   Data sent by bungeecord
   */
  @Override
  public void onPluginMessageReceived(final String channel, final Player player, final byte[] bytes) {
    if (!channel.equals(BungeeChannel.CHAT.getChannel())) {
      return;
    }

    final ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
    final String format = in.readUTF();
    final String message = in.readUTF();
    final String chatColor = in.readUTF();
    final String server = in.readUTF();

    if (DeluxeChat.useServerWhitelist() && !DeluxeChat.getServerWhitelist().contains(server)) {
      return;
    }
    this.plugin.handleSendingChatMessage(message, format, chatColor);
  }


  /**
   * Send a message to a bungee channel
   *
   * @param player      Player used to send message
   * @param format      Format of message
   * @param message     Message to send on other servers
   * @param chatColor   Color of chat to use
   * @param serverName  Name of current server
   */
  public void sendPluginMessage(final Player player, final String format, final String message, final String chatColor, final String serverName) {
    if (!DeluxeChat.useBungee()) {
      return;
    }

    try {
      final ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF(format);
      out.writeUTF(message);
      out.writeUTF(chatColor);
      out.writeUTF(serverName);
      out.writeUTF(DeluxeChat.getServerWhitelist().stream().collect(Collectors.joining(",")));

      player.sendPluginMessage(this.plugin, BungeeChannel.CHAT.getChannel(), out.toByteArray());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
