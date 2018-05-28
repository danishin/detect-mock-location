var exec = require("cordova/exec");

/* success responses */
var MOCKED = "mocked";
var NOT_MOCKED = "not_mocked";

exports.check = function(win, fail) {
  var success = function(res) {
    if (res === MOCKED) win(true);
    else if (res === NOT_MOCKED) win(false);
    else fail("Unknown success response: " + res);
  }

  exec(success, fail, "DetectMockLocation", "check", []);
};