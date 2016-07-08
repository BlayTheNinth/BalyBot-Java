/**
 * This file is not meant to be included. It simply aids IntelliJ's code analysis and provides documentation.
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
     * @param channelName : object
     * @param text : string
     */
    message: function(channelName, text){},
    /**
     * @param channelName : string
     * @param userName : string
     * @param seconds : number
     */
    timeout: function(channelName, userName, seconds){},
    /**
     * @param channelName : string
     * @param user : JUser
     * @param userLevel : number
     * @returns {boolean}
     */
    passesUserLevel: function(channelName, user, userLevel){return false;}
};

var JTwitchAPI = {
    /**
     * @param channelName
     * @returns {JStreamData}
     */
    getStreamData: function(channelName){return Object.create(JStreamData);}
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