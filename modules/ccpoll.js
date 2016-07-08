function module() {
    return {
        id: "ccpoll",
        name: "Counted Poll",
        desc: "Provides the !ccp command to create polls that are voted for within chat by posting a certain amount of emoticons. Returns percentages and an average."
    };
}

function configure() {
    return [
        {
            name: "message.poll_started",
            value: "Counted Chat Poll started",
            desc: "The message that appears in front of the instructions, when no description is supplied."
        },
        {
            name: "message.instructions",
            value: "Type up to {MAX}x {TEXT} ({TEXT}) in chat or --- to vote zero!",
            desc: "The instructions that appear after the poll description. Variables: MAX, TEXT"
        },
        {
            name: "message.results",
            value: "Average Result: {AVERAGE} [{PERCENTAGE}]",
            desc: "The result output when the poll is ended. Variables: AVERAGE, PERCENTAGE"
        },
        {
            name: "message.no_poll",
            value: "There was no poll running, silly.",
            desc: "The message displayed if no poll was running."
        }
    ];
}

function commands() {
    return [
        {
            name: "ccp",
            usage: "(start|stop) [maxCount] [keyword] [description]",
            func: ccp
        }
    ]
}

function events() {
    return {
        "channel_chat": onChannelChat
    };
}

/**
 * @type {{prototype, users: [string], searchText: string, votes: [number]}}
 */
var Poll = {};
Poll.prototype = {
    users: [],
    searchText: "",
    votes: []
};

/**
 * @type {{poll : Poll}}
 */
var CURRENT_POLL = {};

/**
 * @param channel : JString
 * @param user : JUser
 * @param message : JString
 */
function onChannelChat(channel, user, message) {
    if(message.startsWith("!cpp")) { // TODO fix me
        return;
    }
    var currentPoll = CURRENT_POLL[channel];
    if(currentPoll != null) {
        if(currentPoll.users.indexOf(user.getNick()) != -1) {
            return;
        }
        if(message.startsWith("-") || message.startsWith("_")) {
            currentPoll.votes[0]++;
            currentPoll.users.push(user.getNick());
        }
        var countIdx = 0;
        var idx = -1;
        while((idx = message.indexOf(currentPoll.searchText, idx + 1)) != -1) {
            countIdx++;
        }
        if(countIdx > 0) {
            currentPoll.votes[Math.min(countIdx, currentPoll.votes.length - 1)]++;
            currentPoll.users.push(user.getNick());
        }
    }
}

/**
 * @param channel : JString
 * @param user : JUser
 * @param args : [JString]
 * @returns {string}
 */
function ccp(channel, user, args) {
    if(args.length < 1) {
        return JError.notEnoughParameters(this);
    }
    var currentPoll;
    switch (args[0]) {
        case "start":
            if (args.length < 3) {
                return JError.notEnoughParameters(this);
            }
            var maxCount = parseInt(args[1]);
            if(isNaN(maxCount)) {
                return "Expected numeric value for parameter 'maxCount'.";
            }
            if (maxCount < 1 || maxCount > 12) {
                return "Parameter 'maxCount' must be within 1 and 12.";
            }
            var description = config["message.poll_started"];
            if(args.length > 3) {
                description = JString.join(args.slice(3), " ");
            }
            currentPoll = Object.create(Poll);
            currentPoll.searchText = args[2];
            currentPoll.maxCount = maxCount;
            return description + " - " + config["message.instructions"].replace("{MAX}", maxCount).replace("{TEXT}", currentPoll.searchText);
        case "stop":
            currentPoll = CURRENT_POLL[channel];
            if(currentPoll != null) {
                var totalCount = 0;
                for(var i = 0; i < currentPoll.votes.length; i++) {
                    totalCount += currentPoll.votes[i];
                }
                var voteCount = 0;
                var sb = "";
                for(var j = 0; j < currentPoll.votes.length; j++) {
                    voteCount += currentPoll.votes[j] * j;
                    if(sb.length > 0) {
                        sb += ", ";
                    }
                    sb += getNumberName(j) + ": " + currentPoll.votes[j] + " (" + (currentPoll.votes[j] / totalCount * 100) + "%)";
                }
                delete CURRENT_POLL[channel];
                return config["message.results"].replace("{AVERAGE}", JString.format("%.1f", voteCount / totalCount)).replace("{PERCENTAGE}", sb);
            }
            return config["message.no_poll"];
        default:
            return JError.invalidParameters(this);
    }
}

function getNumberName(i) {
    switch(i) {
        case 0:
            return "None";
        case 1:
            return "Single";
        case 2:
            return "Double";
        case 3:
            return "Triple";
        case 4:
            return "Quadruple";
        case 5:
            return "Quintuple";
        case 6:
            return "Sextuple";
        case 7:
            return "Septuple";
        case 8:
            return "Octuple";
        case 9:
            return "Nonuple";
        case 10:
            return "Decuple";
        case 11:
            return "Undecuple";
        case 12:
            return "Duodecuple";
    }
    return null;
}