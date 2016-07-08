function module() {
    return {
        id: "purgew",
        name: "Purge W",
        desc: "Purges people using horrible W-emotes."
    };
}

function configure() {
    return [
    ];
}

function commands() {
    return [
    ]
}

function events() {
    return {
        "channel_chat": onChannelChat
    };
}

/**
 * @param channel : string
 * @param user : JUser
 * @param message : string
 */
function onChannelChat(channel, user, message) {
    var banned = [
        "wyldW"
    ];
    for(var i = 0; i < banned.length; i++) {
        if(message.indexOf(banned[i]) != -1) {
            JBalyBot.timeout(channel, user.getNick(), 1);
            JBalyBot.message(channel, "NO! BAD! WE DON'T USE THOSE EMOTES HERE!");
            JBalyBot.message(channel, "/me slaps " + user.getNick() + " with a potato.");
        }
    }
}