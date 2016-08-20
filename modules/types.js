/**
 * This file is not meant to be included. It simply aids IntelliJ's code analysis and provides basic documentation.
 */

var JError = {
    /**
     * @param thisCommand
     * @returns {string}
     */
    notEnoughParameters: function(thisCommand){return "";},
    /**
     * @param thisCommand
     * @returns {string}
     */
    invalidParameters: function(thisCommand){return "";}
};

var JSystem = {
    /**
     * @returns {number}
     */
    currentTimeMillis: function(){return 0;}
};

var JDate = {
    /**
     * @param time : number
     * @param format : string
     * @param timeZone : string
     * @returns {string}
     */
    format: function(time, format, timeZone){return "";},
    /**
     * @param timeZone : string
     * @returns {string}
     */
    getTimeZoneName: function(timeZone){return "";}
};

var JBalyBot = {
    /**
     * @param channel : object
     * @param text : string
     */
    message: function(channel, text){},

    /**
     * @param channel : JChannel
     * @param user : JUser
     * @param userLevel : number
     * @returns {boolean}
     */
    passesUserLevel: function(channel, user, userLevel){return false;}
};

var JTwitch = {
    /**
     * @param channel : JChannel
     * @param userName : string
     * @param seconds : number
     * @param reason : string
     */
    timeout: function(channel, userName, seconds, reason){},

    /**
     * @param channel
     * @returns {JStreamData}
     */
    getStreamData: function(channel){return Object.create(JStreamData);}
};

var JString = {
    /**
     * @param arr : []
     * @param delimiter : string
     * @returns {string}
     */
    join: function(arr, delimiter) {return "";},
    format: function(format, data) {return "";}
};
JString.prototype = {
    /**
     * @param search : string
     * @returns {boolean}
     */
    startsWith: function(search){return false;},
    /**
     * @param search : string
     * @param startIdx : number
     * @returns {number}
     */
    indexOf: function(search, startIdx){return -1;}
};

var JUser = {};
JUser.prototype = {
    /**
     * @returns {string}
     */
    getNick: function(){return "";}
};
var JChannel = {};
JChannel.prototype = {
    
};

var JStreamData = {};
JStreamData.prototype = {
    /**
     * @returns {boolean}
     */
    isLive: function(){return false;},
    /**
     * @returns {number}
     */
    getUptime: function(){return 0;}
};

var config = {};
module();
configure();
commands();
events();