package fr.freebuild.chatnotif.bungee;

public enum BungeeChannel {
  CHAT("chatnotif:chat");

  private final String channel;

  BungeeChannel(final String channel) {
    this.channel = channel;
  }

  public String getChannel() {
    return this.channel;
  }

  public String toString() {
    return this.channel;
  }
}
