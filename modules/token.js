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
            value: "broadcaster",
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
            usage: "",
            func: token
        }
    ]
}

function create_token() {
    return Math.random().toString(36).substr(2);
}

/**
 * @param channel : JChannel
 * @param user : JUser
 * @param args : [JString]
 * @returns {string}
 */
function token(channel, user, args) {
    var token = null;
    var rs = null;
    while(token == null || rs.next()) {
        token = create_token();
        rs = JDatabase.executeQuery("SELECT channel_fk FROM api_tokens WHERE token = '" + token + "' LIMIT 1;");
    }
    JDatabase.execute("REPLACE INTO api_tokens (channel_fk, token) VALUES (" + channel.getId() + ", '" + token + "')");
    JBalyBot.whisper(channel, user.getNick(), "Your new BalyBot API token is: " + token);
    return user.getNick() + ": You've been whispered a new API token. Make sure you are able to receive whispers from this bot account! Also, do not share the API token on stream.";
}