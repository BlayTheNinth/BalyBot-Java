function module() {
    return {
        id: "simplepoll",
        name: "Simple Polls",
        desc: "Provides the !poll command to create polls that are voted for within chat by posting an option name."
    };
}

function configure() {
    return [
        {
            name: "message.start_prefix",
            value: "Vote in Chat:",
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
            usage: "(start|stop) <option> <option2> [...]",
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
 * @type {{prototype, users: [string], options: [string], votes: [number]}}
 */
var Poll = function() {}
Poll.prototype = {
    users: [],
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
            results[i] = new PollResult();
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
var PollResult = function() {}
PollResult.prototype = {
    optionName: "",
    votes: 0
};

/**
 * @type {{poll : Poll}}
 */
var m_currentPoll = {};

/**
 * @param channel : JChannel
 * @param user : JUser
 * @param message : JString
 */
function onChannelChat(channel, user, message) {
    var currentPoll = m_currentPoll[channel];
    if(currentPoll == null || message.startsWith("!poll")) { // TODO fix me
        return;
    }
    if(currentPoll.users.indexOf(user.getNick()) != -1) {
        return;
    }
    for(var i = 0; i < currentPoll.options.length; i++) {
        if(message.indexOf(currentPoll.options[i]) != -1) {
            currentPoll.votes[i]++;
            currentPoll.users.push(user.getNick());
            break;
        }
    }
}

/**
 * @param channel : JChannel
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
            var options = [];
            var idx = 0;
            for(var i = 1; i < args.length; i++) {
                options[idx] = args[i];
                idx++;
            }
            currentPoll = new Poll();
            message = config["message.start_prefix"] + " ";
            for(var j = 0; j < options.length; j++) {
                if (j > 0) {
                    message += " // ";
                }
                message += options[j];
            }
            m_currentPoll[channel] = currentPoll;
            return message;
        case "stop":
            currentPoll = m_currentPoll[channel];
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