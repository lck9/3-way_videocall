var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'videocall', 'coolMethod', [arg0]);
};
exports.add = function (param1,param2, success, error) {
    exec(success, error, 'videocall', 'add', [param1,param2]);
};


