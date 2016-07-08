function module() {
    return {
        id: "linkfilter",
        name: "Link Filter",
        desc: "Prevents people below a certain user level from posting links, unless they have been permitted using the !permit command."
    };
}

function configure() {
    return [
        {
            name: "show_message",
            value: "true",
            desc: "Should a message be shown when a link purge happens?"
        },
        {
            name: "link_userlevel",
            value: "sub",
            desc: "The minimum user level required to post links."
        },
        {
            name: "message.link_purged",
            value: "Nooo! {NICK}, stop posting links or IPs without permission, please!",
            desc: "The message displayed when a user is purged for posting a link. Variables: NICK"
        },
        {
            name: "message.permitted",
            value: "{NICK}, you may now post one link. Make it count!",
            desc: "The message displayed when a user is permitted to post a link. Variables: NICK"
        }
    ];
}

function commands() {
    return [
        {
            name: "permit",
            usage: "<user>",
            func: permit
        }
    ];
}

function events() {
    return {
        "channel_chat": onChannelChat
    };
}

var IP_PATTERN = /[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}/;
var LINK_PATTERN = /((?:ftp|https?):\/\/)?([A-Za-z0-9\.-]+)\.([A-Za-z]{2,6})([\/\w \.-]*)*\/?/;
var PERMISSIONS = {};

/**
 * @param channel : string
 * @param user : JUser
 * @param message : string
 */
function onChannelChat(channel, user, message) {
    if (message.search(IP_PATTERN) != -1 || message.search(LINK_PATTERN) != -1) {
        if(JBalyBot.passesUserLevel(channel, user, config["link_userlevel"])) {
            return;
        }
        if(PERMISSIONS[channel] != null) {
            for(var i = 0; i < PERMISSIONS[channel].length; i++) {
                if(PERMISSIONS[channel][i].toLowerCase() == user.getNick()) {
                    PERMISSIONS[channel].splice(i, 1);
                    return;
                }
            }
        }
        JBalyBot.timeout(user.getNick(), 1);
        if (config["show_message"]) {
            JBalyBot.message(channel, config["message.link_purged"].replace("{NICK}", user.getNick()));
        }
    }
}

/**
 * @param channel : string
 * @param user : JUser
 * @param args : [string]
 * @returns {string}
 */
function permit(channel, user, args) {
    if(args.length < 1) {
        return JError.notEnoughParameters(this);
    }
    if(PERMISSIONS[channel] == null) {
        PERMISSIONS[channel] = [];
    }
    PERMISSIONS[channel].push(args[0]);
    return config["message.permitted"].replace("{NICK}", user.getNick());
}