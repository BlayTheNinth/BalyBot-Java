function module() {
    return {
        id: "song",
        name: "Song Command",
        desc: "Provides the !song command which displays the currently playing song. Use the token module to grab an API token and Snip for BalyBot to have this command populated."
    };
}

function configure() {
    return [
        {
            name: "userlevel.song",
            value: "all",
            desc: "The minimum userlevel required to run the !song command."
        }
    ];
}

function preInit() {
    JDatabase.createTable("channel_songs", false, ["channel_fk INTEGER", "song VARCHAR(255)", "PRIMARY KEY (channel_fk)"]);
}

function commands() {
    return [
        {
            name: "song",
            usage: "",
            func: song
        }
    ]
}

/**
 * @param channel : JChannel
 * @param user : JUser
 * @param args : [JString]
 * @returns {string}
 */
function song(channel, user, args) {
    var rs = JDatabase.executeQuery("SELECT song FROM channel_songs WHERE channel_fk = " + channel.getId() + " LIMIT 1;");
    if(rs.next()) {
        var song = rs.getString("song");
        rs.close();
        return song;
    } else {
        return "The song command is not properly set up yet for this channel.";
    }
}