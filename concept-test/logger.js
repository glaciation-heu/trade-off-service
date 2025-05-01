const chalk = require("chalk");

const levelStyles = {
  DEBUG: chalk.bold.gray,
  INFO: chalk.bold.blue,
  SUCCESS: chalk.bold.green,
  WARNING: chalk.bold.yellow,
  ERROR: chalk.bold.red,
}

class Logger {
  _writeLog(level, msg) {
    const date = new Date();
    console.log(chalk.grey(date.toISOString()), `[${levelStyles[level](level)}]`, ...msg);
  }
  debug(...msg) { this._writeLog("DEBUG", msg) }
  info(...msg) { this._writeLog("INFO", msg) }
  success(...msg) { this._writeLog("SUCCESS", msg) }
  warning(...msg) { this._writeLog("WARNING", msg) }
  error(...msg) { this._writeLog("ERROR", msg) }
}

module.exports = new Logger();