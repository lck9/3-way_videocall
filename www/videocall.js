var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'videocall', 'coolMethod', [arg0]);
};
exports.retriveData = function (arg0,arg1, success, error) {
    exec(success, error, 'videocall', 'retriveData', [arg0,arg1]);
};
