const colors = require('colors');
const fs = require('fs');

const logFile = './log.log';

export default class Log {

	public static log(obj: any) {
		console.log(colors.bold.blue(obj));
	}

	public static error(obj: any) {
		console.log(colors.bold.red(obj));
		//console.log(obj.red.bold);
	}

	public static warn(obj: any) {
		console.log(colors.bold.yellow(obj));
	}

	public static notice(obj: any) {
		console.log(colors.bold.green(obj));
	}

	public static info(obj: any) {
		console.log(colors.bold.gray(obj));
	}

	public static writeLog(str: string) {

		if (str == null) {
			str = 'NULL';
		}

		if (typeof(str) == 'object') {
			str = JSON.stringify(str);
		}

		this.log(str);

		let time = new Date().toJSON();
		str = `[${time}] ${str}\r\n`;
		let flag = "";
		if (fs.existsSync(logFile)) {
			flag = "a+";
		} else {
			flag = "w";
		}

		fs.writeFileSync(logFile, str, {flag: flag});
	}
}