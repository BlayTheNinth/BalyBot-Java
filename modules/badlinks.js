function module() {
    return {
        id: "badlinks",
        name: "Bad Links",
        desc: "Scans messages for links that are banned and automatically murders people posting them."
    };
}

function configure() {
    return [
        {
            name: "message_style",
            value: "reason",
            desc: "Should a message be displayed when a link timeout happens? Possible values: message, reason, none"
        },
        {
            name: "message.bad_link_found",
            value: "Hey {NICK}, you posted a banned link. How about you don't?",
            desc: "The message displayed when a user is timed out for posting a link. {NICK} will be replaced by the user's name. See message_style."
        },
        {
            name: "timeout_seconds",
            value: "60",
            desc: "The time in seconds the user will be timed out for."
        },
        {
            name: "userlevel.badlink",
            value: "mod",
            desc: "The userlevel required to run the !badlink command."
        }
    ];
}

function commands() {
    return [
        {
            name: "badlink",
            usage: "<user> or <link>",
            func: badlink
        }
    ];
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
    // TODO Check if message contains any of the links stored in the bad links list, issue timeout if so
}

/**
 * @param channel : JChannel
 * @param user : JUser
 * @param args : [string]
 * @returns {string}
 */
function badlink(channel, user, args) {
    if(args.length < 1) {
        return JError.notEnoughParameters(this);
    }
    // TODO Add a pattern to the bad links list
    return "Not yet implemented.";
}