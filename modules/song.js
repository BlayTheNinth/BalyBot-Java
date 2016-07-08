function module() {
    return {
        id: "song",
        name: "Song Module",
        desc: "Looks up the title of the currently playing song from a configured file (song_file)."
    };
}

function configure() {
    return [
        {
            name: "song_file",
            value: "",
            desc: "The file containing the name of the currently playing song."
        },
        {
            name: "userlevel.song",
            value: "all",
            desc: "The minimum user level requires to run the !song command."
        }
    ];
}

function commands() {
    return [
        {
            name: "song",
            usage: "",
            func: song
        }
    ];
}

/**
 * @param channel : JString
 * @param user : JUser
 * @param args : [JString]
 * @returns {string}
 */
function song(channel, user, args) {
    return "I don't know, ask someone else >_>";
}