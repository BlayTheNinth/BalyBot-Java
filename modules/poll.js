function module() {
    return {
        id: "poll",
        name: "Word Trigger Polls",
        desc: "Provides the !poll command to create polls that are voted for within chat by posting a trigger word bound to an option."
    };
}

function configure() {
    return [
        {
            name: "message.start_prefix",
            value: "[New Poll]",
            desc: "The prefix used for the poll started message."
        },
        {
            name: "message.results_prefix",
            value: "[Results]",
            desc: "The prefix used for the poll results message."
        },
        {
            name: "message.no_poll",
            value: "There was no poll running, stupid.",
            desc: "The message displayed if no poll was running."
        }
    ];
}

function commands() {
    return [
        {
            name: "poll",
            usage: "(start|stop) <keyword> <option> <keyword2> <option2> [...]",
            func: poll
        }
    ]
}

function events() {
    return {
        "channel_chat": onChannelChat
    };
}

/**
 * @type {{prototype, users: [string], triggers: [string], options: [string], votes: [number]}}
 */
var Poll = {};
Poll.prototype = {
    users: [],
    triggers: [],
    options: [],
    votes: [],
    /**
     * @returns {number}
     */
    getTotalVotes: function() {
        var totalVotes = 0;
        for(var i = 0; i < this.votes.length; i++) {
            totalVotes += this.votes[i];
        }
        return totalVotes;
    },
    getResults: function() {
        var results = [];
        for(var i = 0; i < this.votes.length; i++) {
            results[i] = Object.create(PollResult);
            results[i].optionName = this.options[i];
            results[i].votes = this.votes[i];
        }
        results.sort(function(a, b) {
            return b.votes - a.votes;
        });
        return results;
    }
};

/**
 * @type {{prototype, optionName: string, votes: number}}
 */
var PollResult = {};
PollResult.prototype = {
    optionName: "",
    votes: 0
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
    var currentPoll = CURRENT_POLL[channel];
    if(currentPoll == null || message.startsWith("!poll")) { // TODO fix me
        return;
    }
    if(currentPoll.users.indexOf(user.getNick()) != -1) {
        return;
    }
    for(var i = 0; i < currentPoll.triggers.length; i++) {
        if(message.indexOf(currentPoll.triggers[i]) != -1) {
            currentPoll.votes[i]++;
            currentPoll.users.push(user.getNick());
            break;
        }
    }
}

/**
 * @param channel : JString
 * @param user : JUser
 * @param args : [JString]
 * @returns {string}
 */
function poll(channel, user, args) {
    if(args.length < 1) {
        return JError.notEnoughParameters(this);
    }
    var currentPoll, message;
    switch (args[0]) {
        case "start":
            if (args.length < 5 || args.length % 2 == 0) {
                return JError.notEnoughParameters(this);
            }
            var triggers = [];
            var options = [];
            var idx = 0;
            for(var i = 1; i < args.length; i += 2) {
                triggers[idx] = args[i];
                options[idx] = args[i + 1];
                idx++;
            }
            currentPoll = Object.create(Poll);
            message = config["message.start_prefix"] + " ";
            for(var j = 0; j < triggers.length; j++) {
                if (j > 0) {
                    message += " // ";
                }
                message += triggers[j] + " (" + triggers[j] + "): " + options[j];
            }
            CURRENT_POLL[channel] = currentPoll;
            return message;
        case "stop":
            currentPoll = CURRENT_POLL[channel];
            if(currentPoll != null) {
                var totalVotes = currentPoll.getTotalVotes();
                message = config["message.results_prefix"] + " ";
                var first = true;
                var results = currentPoll.getResults();
                for (var k = 0; k < results.length; k++) {
                    if (!first) {
                        message += " // ";
                    }
                    first = false;
                    message += results[k].optionName + ": " + results[k].votes + " (" + (results[k].votes / totalVotes * 100) + "%)";
                }
                currentPoll = null;
                return message.toString();
            }
            return config["message.no_poll"];
        default:
            return JError.invalidParameters(this);
    }
}