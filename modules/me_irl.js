function module() {
    return {
        id: "me_irl",
        name: "me_irl Module",
        desc: "fuck_you_irl."
    };
}

function configure() {
    return [
        {
            name: "message",
            value: "fuck_you_irl",
            desc: "im serious"
        }
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
 * @param channel : JChannel
 * @param user : JUser
 * @param message : JString
 */
function onChannelChat(channel, user, message) {
    if(message.indexOf("me_irl") !== -1) {
        JBalyBot.message(channel, config["message"]);
    }
}