package fr.freebuild.chatnotif.bungee;

import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class ChatNotif extends Plugin implements Listener {
  @Override
  public void onEnable() {
    getProxy().registerChannel(BungeeChannel.CHAT.getChannel());
    getProxy().getPluginManager().registerListener(this, this);
  }

  @Override
  public void onDisable() {
    getProxy().unregisterChannel(BungeeChannel.CHAT.getChannel());
  }

  /**
   * Extract whitelist from datas and rebuild the datas without the whitelist
   *
   * @param bytes       Input bytes received
   * @return            Return a pair with the whitelist and the new bytes
   * @throws Exception  Build new byte array can throw exception
   */
  public Pair<List<String>, byte[]> extractDatas(final byte[] bytes) throws Exception {
    final ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
    final String format = in.readUTF();
    final String message = in.readUTF();
    final String server = in.readUTF();
    final List<String> whitelist = Arrays.asList(in.readUTF().split(","));

    final ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF(format);
    out.writeUTF(message);
    out.writeUTF(server);

    return new Pair<List<String>, byte[]>(whitelist, out.toByteArray());
  }

  @EventHandler
  public void receievePluginMessage(final PluginMessageEvent e) {
    if (!e.getTag().equals(BungeeChannel.CHAT.getChannel())) {
      return;
    }

    try {
      final Pair<List<String>, byte[]> datas = this.extractDatas(e.getData());
      final SocketAddress senderServer = e.getSender().getSocketAddress();

      for (ServerInfo server : getProxy().getServers().values()) {
        if (!server.getSocketAddress().equals(senderServer) && server.getPlayers().size() > 0 && datas.key.contains(server.getName())) {
          server.sendData(BungeeChannel.CHAT.getChannel(), datas.value);
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Pair of key/value
   *
   * @param <T> Key type
   * @param <U> Value type
   */
  private class Pair<T, U> {
    T key;
    U value;

    Pair(T key, U value) {
      this.key = key;
      this.value = value;
    }
  }
}
