function module() {
    return {
        id: "lewd",
        name: "Lewd Module",
        desc: "Provides the !lewd command, which posts a random anime face reacting to lewdness."
    };
}

function configure() {
    return [
        {
            name: "userlevel.lewd",
            value: "all",
            desc: "The minimum user level requires to run the !lewd command."
        }
    ];
}

function commands() {
    return [
        {
            name: "lewd",
            usage: "",
            func: lewd
        }
    ];
}

var m_lewdFaces = [
    "http://blay09.net/bot/lewd/001.gif",
    "http://blay09.net/bot/lewd/01QllrK.png",
    "http://blay09.net/bot/lewd/193.png",
    "http://blay09.net/bot/lewd/4bf.jpg",
    "http://blay09.net/bot/lewd/658.png",
    "http://blay09.net/bot/lewd/7db.gif",
    "http://blay09.net/bot/lewd/979.png",
    "http://blay09.net/bot/lewd/c6e.png",
    "http://blay09.net/bot/lewd/DbXNNWT.png",
    "http://blay09.net/bot/lewd/EnUrwCl.png",
    "http://blay09.net/bot/lewd/fed.gif",
    "http://blay09.net/bot/lewd/kQeES9o.gif",
    "http://blay09.net/bot/lewd/lFcWSFs.gif",
    "http://blay09.net/bot/lewd/QCvT9Tu.gif",
    "http://blay09.net/bot/lewd/XrZeD2q.gif",
    "http://blay09.net/bot/lewd/002.jpg",
    "http://blay09.net/bot/lewd/0CaU1Cd.jpg",
    "http://blay09.net/bot/lewd/20b.png",
    "http://blay09.net/bot/lewd/509.gif",
    "http://blay09.net/bot/lewd/6e7.gif",
    "http://blay09.net/bot/lewd/7ec.jpg",
    "http://blay09.net/bot/lewd/a43.gif",
    "http://blay09.net/bot/lewd/c7c.gif",
    "http://blay09.net/bot/lewd/e75BJh0.jpg",
    "http://blay09.net/bot/lewd/f39.jpg",
    "http://blay09.net/bot/lewd/Kscx9g5.png",
    "http://blay09.net/bot/lewd/piilUQc.jpg",
    "http://blay09.net/bot/lewd/RZFy6NB.jpg",
    "http://blay09.net/bot/lewd/zAp2LzJ.png",
    "http://blay09.net/bot/lewd/003.gif",
    "http://blay09.net/bot/lewd/1405745376585.jpg",
    "http://blay09.net/bot/lewd/446.png",
    "http://blay09.net/bot/lewd/5eZuh2C.gif",
    "http://blay09.net/bot/lewd/751.jpg",
    "http://blay09.net/bot/lewd/8eIIXak.jpg",
    "http://blay09.net/bot/lewd/Bil1UNp.png",
    "http://blay09.net/bot/lewd/cdc.jpg",
    "http://blay09.net/bot/lewd/e82.jpg",
    "http://blay09.net/bot/lewd/fe8.png",
    "http://blay09.net/bot/lewd/ipKr8zG.gif",
    "http://blay09.net/bot/lewd/L0xyKWD.png",
    "http://blay09.net/bot/lewd/Q4FKsUq.gif",
    "http://blay09.net/bot/lewd/SyRFPmy.gif"
];

/**
 * @param channel : JChannel
 * @param user : JUser
 * @param args : [string]
 * @returns {string}
 */
function lewd(channel, user, args) {
    return m_lewdFaces[Math.floor(Math.random() * m_lewdFaces.length)];
}