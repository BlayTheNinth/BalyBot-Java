function module() {
    return {
        id: "token",
        name: "Token Command",
        desc: "Provides the !token command to reset your BalyBot API token."
    };
}

function configure() {
    return [
        {
            name: "userlevel.token",
            value: "channel_owner",
            desc: "The minimum userlevel required to run the !token command."
        }
    ];
}

function preInit() {
    JDatabase.createTable("api_tokens", false, ["channel_fk INTEGER", "token VARCHAR(255)", "PRIMARY KEY (channel_fk)"]);
}

function commands() {
    return [
        {
            name: "token",
            usage: "(get|reset)",
            func: token
        }
    ]
}

function new_token() {
    return Math.random().toString(36).substr(2);
}

function generate_token(channelId) {
    var token = null;
    var rs = null;
    while(token == null || rs.next()) {
        token = new_token();
        rs = JDatabase.executeQuery("SELECT channel_fk FROM api_tokens WHERE token = '" + token + "' LIMIT 1;");
    }
    JDatabase.execute("REPLACE INTO api_tokens (channel_fk, token) VALUES (" + channelId + ", '" + token + "')");
    return token;
}

/**
 * @param channel : JChannel
 * @param user : JUser
 * @param args : [JString]
 * @returns {string}
 */
function token(channel, user, args) {
    if(args.length < 1) {
        return JError.notEnoughParameters(this);
    }
    switch(args[0]) {
        case "get":
            var rs = JDatabase.executeQuery("SELECT token FROM api_tokens WHERE channel_fk = " + channel.getId());
            if(rs.next()) {
                JBalyBot.whisper(channel, user.getNick(), "Your BalyBot API token is: " + rs.getString("token"));
                rs.close();
                return user.getNick() + ": You've been whispered your API token. Make sure you are able to receive whispers from this bot account! Also, do not share the API token on stream.";
            } else {
                rs.close();
                JBalyBot.whisper(channel, user.getNick(), "Your new BalyBot API token is: " + generate_token(channel.getId()));
                return user.getNick() + ": You've been whispered an API token. Make sure you are able to receive whispers from this bot account! Also, do not share the API token on stream.";
            }
            break;
        case "reset":
            JBalyBot.whisper(channel, user.getNick(), "Your new BalyBot API token is: " +generate_token(channel.getId()));
            return user.getNick() + ": You've been whispered a new API token. Make sure you are able to receive whispers from this bot account! Also, do not share the API token on stream.";
    }
}