function module() {
    return {
        id: "raffle",
        name: "Raffle Module",
        desc: "Provides the !raffle command to create raffles that people can enter by typing a keyword in chat."
    };
}

function configure() {
    return [
        {
            name: "userlevel.raffle",
            value: "mod",
            desc: "The minimum userlevel required to run the !raffle command."
        }
    ];
}

function commands() {
    return [
        {
            name: "raffle",
            usage: "(start|draw|stop) <keyword> [description]",
            func: raffle
        }
    ]
}

function events() {
    return {
        "channel_chat": onChannelChat
    };
}

/**
 * @type {{prototype, users: {number}, searchText: string}}
 */
var Raffle = {};
Raffle.prototype = {
    searchText: "",
    users: {}
};

/**
 * @type {{raffle : Raffle}}
 */
var CURRENT_RAFFLE = {};

/**
 * @param channel : JString
 * @param user : JUser
 * @param message : JString
 */
function onChannelChat(channel, user, message) {
    var currentRaffle = CURRENT_RAFFLE[channel];
    if(currentRaffle == null || message != currentRaffle.searchText) {
        return;
    }
    currentRaffle.users[user]++;
    if(currentRaffle.users[user] > config["max_allowed_entries"]) {
        JBalyBot.message(channel, config["message.too_many_entries"].replace("{NICK}", user.getNick()));
        JBalyBot.timeout(user.getNick(), config["too_many_entries_timeout"]);
        delete currentRaffle.users[user];
    }
}

/**
 * @param channel : JString
 * @param user : JUser
 * @param args : [JString]
 * @returns {string}
 */
function raffle(channel, user, args) {
    if(args.length < 1) {
        return JError.notEnoughParameters(this);
    }
    var currentRaffle;
    switch (args[0]) {
        case "start":
            if (args.length < 2) {
                return JError.notEnoughParameters(this);
            }
            var description = "Raffle started";
            if(args.length > 2) {
                description = args[2];
            }
            currentRaffle = Object.create(Raffle);
            currentRaffle.searchText = args[1];
            CURRENT_RAFFLE[channel] = currentRaffle;
            return description + " - type '" + currentRaffle.searchText + "' to enter for a chance to win. Don't spam it!";
        case "draw":
            currentRaffle = CURRENT_RAFFLE[channel];
            if(currentRaffle != null) {
                var validUsers = [];
                for(var key in currentRaffle.users) {
                    if(currentRaffle.users.hasOwnProperty(key)) {
                        if(currentRaffle.users[key] > 0) {
                            validUsers.push(key);
                        }
                    }
                }
                var winner = validUsers[Math.floor(Math.random() * (validUsers.length + 1))];
                return "Lucky!! Congratulations, " + winner + "! You won the thingie.";
            }
            return "There was no raffle running, silly!";
        case "stop":
            delete CURRENT_RAFFLE[channel];
            return "Raffle entries have been cleared.";
        default:
            return JError.invalidParameters(this);
    }
}