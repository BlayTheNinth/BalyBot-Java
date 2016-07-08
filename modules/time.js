function module() {
    return {
        id: "time",
        name: "Time Module",
        desc: "Provides a !time command that prints the current time in a specific time zone."
    };
}

function configure() {
    return [
        {
            name: "timezone",
            value: "",
            desc: "The timezone to use, see https://docs.oracle.com/javase/7/docs/api/java/util/TimeZone.html for the format."
        },
        {
            name: "userlevel.time",
            value: "reg",
            desc: "The minimum user level required to run the !time command."
        }
    ];
}

function commands() {
    return [
        {
            name: "time",
            usage: "[timezone]",
            func: time
        }
    ];
}

/**
 * @param channel : string
 * @param user : JUser
 * @param args : [string]
 * @returns {string}
 */
function time(channel, user, args) {
    var timeZone = config["timezone"];
    if(args.length > 0) {
        timeZone = JString.join(args, " ");
    } else if(timeZone == "") {
        return JError.notEnoughParameters(this);
    }

    var timeZoneName = JDate.getTimeZoneName(timeZone);
    if(timeZoneName == null) {
        return "Invalid timezone '" + timeZone + "'.";
    }
    return "The time in " + timeZoneName + " is currently " + JDate.format(JSystem.currentTimeMillis(), "h:m a (H:m)", timeZone) + ".";
}