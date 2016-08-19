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
            name: "show_reason",
            value: "true",
            desc: "Should a message be shown in the timeout reason when a link purge happens?"
        },
        {
            name: "link_userlevel",
            value: "sub",
            desc: "The minimum user level required to post links."
        },
        {
            name: "message.link_purged",
            value: "Nooo! {NICK}, stop posting links without permission, please!",
            desc: "The message displayed when a user is purged for posting a link. {NICK} will be replaced by the user's name."
        },
        {
            name: "message.timeout_reason",
            value: "Ask for permission before posting links, please!",
            desc: "The message displayed as timeout reason when a user is purged for posting a link. {NICK} will be replaced by the user's name."
        },
        {
            name: "message.permitted",
            value: "{NICK}, you may now post one link. Make it count!",
            desc: "The message displayed when a user is permitted to post a link. {NICK} will be replaced by the user's name."
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
var m_permissions = {};

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
        if(m_permissions[channel] != null) {
            for(var i = 0; i < m_permissions[channel].length; i++) {
                if(m_permissions[channel][i].toLowerCase() == user.getNick()) {
                    m_permissions[channel].splice(i, 1);
                    return;
                }
            }
        }
        JTwitch.timeout(channel, user.getNick(), 1, config["show_reason"] ? config["message.timeout_reason"].replace("{NICK}", user.getNick()) : "");
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
    if(m_permissions[channel] == null) {
        m_permissions[channel] = [];
    }
    m_permissions[channel].push(args[0]);
    return config["message.permitted"].replace("{NICK}", args[0]);
}