package net.blay09.balybot.irc;

public class IRCConfig {

    public String proxyHost = "";
    public String proxyUsername = "";
    public String proxyPassword = "";
    public int proxyPort;

    public String host = "";
    public String serverPassword = "";
    public int[] ports = {IRCConnection.DEFAULT_PORT};
    public String charset = "UTF-8";
    public String ident = "";
    public String realName = "";

    public String bindIP = "";
    public long antiFloodTime = 300;
}
