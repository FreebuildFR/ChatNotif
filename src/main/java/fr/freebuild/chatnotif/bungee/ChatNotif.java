package fr.freebuild.chatnotif.bungee;

import java.net.SocketAddress;

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

  @EventHandler
  public void receievePluginMessage(final PluginMessageEvent e) {
    if (!e.getTag().equals(BungeeChannel.CHAT.getChannel())) {
      return;
    }

    final SocketAddress senderServer = e.getSender().getSocketAddress();
    for (ServerInfo server : getProxy().getServers().values()) {
      if (!server.getSocketAddress().equals(senderServer) && server.getPlayers().size() > 0) {
        server.sendData(BungeeChannel.CHAT.getChannel(), e.getData());
      }
    }
  }
}
