function module() {
    return {
        id: "test",
        name: "Test Module",
        desc: "The test module providing the !test command."
    };
}

function configure() {
    return [
        {
            name: "message",
            value: "BalyBot best bot.",
            desc: "The message to display when the !test command is run."
        },
        {
            name: "userlevel.test",
            value: "all",
            desc: "The minimum user level requires to run the !test command."
        }
    ];
}

function commands() {
    return [
        {
            name: "test",
            usage: "",
            func: test
        }
    ];
}

/**
 * @param channel : string
 * @param user : JUser
 * @param args : [string]
 * @returns {string}
 */
function test(channel, user, args) {
    return config["message"];
}