function module() {
    return {
        id: "uptime",
        name: "Uptime Module",
        desc: "Provides the !uptime command which displays the length of time the channel has been live for."
    };
}

function configure() {
    return [
        {
            name: "message.prefix",
            value: "Stream uptime: ",
            desc: "The prefix put in front of the uptime."
        },
        {
            name: "message.not_live",
            value: "This channel is not live.",
            desc: "The message displayed if the channel is not live."
        },
        {
            name: "userlevel.uptime",
            value: "all",
            desc: "The minimum user level required to run the !uptime command."
        }
    ];
}

function commands() {
    return [
        {
            name: "uptime",
            usage: "",
            func: uptime
        }
    ];
}

/**
 * @param channel : string
 * @param user : JUser
 * @param args : [string]
 * @returns {string}
 */
function uptime(channel, user, args) {
    var streamData = JTwitch.getStreamData(channel);
    if(!streamData.isLive()) {
        return config["message.not_live"];
    }
    var text = "";
    var uptime = streamData.getUptime() / 1000;
    var days = (uptime / (60*60*24));
    uptime -= days * 60*60*24;
    var hours = (uptime / (60*60));
    uptime -= hours * 60*60;
    var minutes = (uptime / 60);
    uptime -= minutes * 60;
    var seconds =  uptime;
    if(days > 0) {
        text += days + " day";
        if(days > 1) {
            text += "s";
        }
    }
    if(hours > 0) {
        if(text.length > 0) {
            text += ", ";
        }
        text += hours + " hour";
        if(hours > 1) {
            text += "s";
        }
    }
    if(minutes > 0) {
        if(text.length > 0) {
            text += ", ";
        }
        text += minutes + " minute";
        if(minutes > 1) {
            text += "s";
        }
    }
    if(seconds > 0) {
        if(text.length > 0) {
            text += ", ";
        }
        text += seconds + " second";
        if(seconds > 1) {
            text += "s";
        }
    }
    return config["message.prefix"] + text;
}