const spawnSync = require('child_process').spawnSync;
const execFileSync = require('child_process').execFileSync;
import Log from './log';
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

export default class Utils {

	//执行console命令
	public static exec(cmd: string, agrs: Array<string>, cwd?: string) {

		Log.notice('cmd = ' + cmd);
		let option = {windowsHide: false, stdio:'inherit'};
		if (cwd) {
			option['cwd'] = cwd;
		}
		let ret = spawnSync(cmd, agrs, {windowsHide: false, stdio:'inherit'}, option);
		if (ret.error != null) {
			Log.error(ret.error);
			return false;
		}

		return true;
	}

	public static execFile(file: string, agrs: Array<string>, cwd?: string) {

		Log.notice('cmd file = ' + file);
		let option = {windowsHide: false, stdio:'inherit'};
		if (cwd) {
			option['cwd'] = cwd;
		}
		let ret = execFileSync(file, agrs, {windowsHide: false, stdio:'inherit'}, option);
		Log.log(ret);
		return true;
	}

	public static md5(str: string| Buffer) {

		if (str == null || str.length == 0) {
			return '';
		}

		let result = crypto.createHash('md5').update(str).digest('hex');
		return result;
	}

	public static getMD5ByFile(filePath: string) {

		if (fs.existsSync(filePath) == false) {
			return "";
		}

		let content = fs.readFileSync(filePath);
		return this.md5(content);
	}

	//删除文件夹
	public static removeDir(dir: string) {

		if (fs.existsSync(dir) == false) {
			return;
		}

		if (fs.statSync(dir).isDirectory() == false) {
			fs.unlinkSync(dir);
			return;
		}

		let paths = fs.readdirSync(dir);
		if (paths != null && paths.length > 0) {
			
			for (let i = 0; i < paths.length; i++) {
				let fullPath = path.join(dir, paths[i]);
				if (fs.statSync(fullPath).isDirectory() == false) {
					fs.unlinkSync(fullPath);
				} else {
					this.removeDir(fullPath);
				}
			}
		}

		fs.rmdirSync(dir);
	}

	//复制文件夹
	public static copyDir(sourcePath: string, desPath: string, filter: (fullPath: string) => boolean = null) {

		let self = this;
		function copy(source, des) {
			let dir = path.dirname(des);
			if (fs.existsSync(dir) == false) {
				self.mkDir(dir);
			}
			fs.copyFileSync(source, des);
		}

		if (sourcePath == null || sourcePath.length <= 0
			|| desPath == null || desPath.length <= 0) {
			return;
		}

		if (fs.existsSync(sourcePath) == false) {
			return;
		}

		/*if (fs.existsSync(desPath) == false) {
			this.mkDir(desPath);
		}*/

		let paths = fs.readdirSync(sourcePath);
		if (paths != null && paths.length > 0) {

			for (let i = 0; i < paths.length; i++) {
				let sourceFullPath = path.join(sourcePath, paths[i]);
				let desFullPath = path.join(desPath, paths[i]);
				sourceFullPath = sourceFullPath.replace(/\\/g, '/');
				desFullPath = desFullPath.replace(/\\/g, '/');
				if (fs.statSync(sourceFullPath).isDirectory() == false) {
					Log.log(`copy file:${sourcePath} -> ${desFullPath}`);
					if (filter) {
						if (filter(sourceFullPath) == true) {
							copy(sourceFullPath, desFullPath);
						}
					} else {
						copy(sourceFullPath, desFullPath);
					}
				} else {
					this.copyDir(sourceFullPath, desFullPath, filter);
				}
			}
		}

	}

	//创建文件夹
	public static mkDir(dir: string) {

		if (dir == null || dir.length <= 0) {
			return;
		}

		if (fs.existsSync(dir) == true) {
			return;
		}

		let str = dir.replace(/\\/g, '/');
		let parts = str.split('/');
		let tmpDir = '';
		for (let i = 0; i < parts.length; i++) {
			if (i == 0) {
				tmpDir = parts[i];
			} else {
				tmpDir = path.join(tmpDir, parts[i]);
			}

			if (fs.existsSync(tmpDir) == true) {
				continue;
			}

			fs.mkdirSync(tmpDir);			
		}
	}

	public static getDirs(dir: string) {

		let dirs = [];
		if (dir == null || dir.length <= 0) {
			return dirs;
		}

		if (fs.existsSync(dir) == false) {
			return dirs;
		}

		let paths = fs.readdirSync(dir);
		if (paths != null) {
			for (let i = 0; i < paths.length; i++) {

				let fullPath = path.join(dir, paths[i]);
				if (fs.statSync(fullPath).isDirectory() == false) {
					continue;
				}

				dirs.push(fullPath);
				let childDirs = this.getDirs(fullPath);
				dirs = dirs.concat(childDirs);
			}
		}

		return dirs;
	}

	//dir: string
	//filter: function, if return true, file is added to filelist
	public static getFiles(dir: string, filter?: (fullPath: string) => boolean): Array<string> {

		let files = [];
		if (dir == null || dir.length <= 0) {
			return files;
		}

		if (fs.existsSync(dir) == false) {
			return files;
		}

		let paths = fs.readdirSync(dir);
		if (paths != null) {
			for (let i = 0; i < paths.length; i++) {

				let fullPath = path.join(dir, paths[i]);
				if (fs.statSync(fullPath).isDirectory() == false) {

					let newPath = fullPath.replace(/\\/g, '/');
					if (filter) {
						if (filter(newPath) == true) {
							files.push(newPath);
						}

					} else {
						files.push(newPath);
					}
					
				} else {
					let childFiles = this.getFiles(fullPath, filter);
					files = files.concat(childFiles);
					/*for (let j = 0; j < childFiles.length; j++) {
						files.push(childFiles[j]);
					}*/
				}
			}
		}

		return files;
	}

	public static copyFile(sourcePath: string, desPath: string) {

		if (sourcePath == null || sourcePath.length <= 0) {
			return;
		}

		if (desPath == null || desPath.length <= 0) {
			return;
		}

		if (fs.existsSync(sourcePath) == false) {
			return;
		}

		let dir = path.dirname(desPath);
		if (fs.existsSync(dir) == false) {
			this.mkDir(dir);
		}

		fs.copyFileSync(sourcePath, desPath);
	}

	//删除文件夹
	public static removeFile(path: string) {

		if (fs.existsSync(path) == false) {
			return;
		}

		if (fs.statSync(path).isDirectory() == false) {
			fs.unlinkSync(path);
		}
	}

	public static getRelativePath(fullPath: string, basePath: string) {

		if (fullPath == null || fullPath.length <= 0) {
			return '';
		}

		if (basePath == null || basePath.length <= 0) {
			return '';
		}

		fullPath = fullPath.replace(/\\/g, '/');
		basePath = basePath.replace(/\\/g, '/');
		if (basePath.endsWith('/') == false) {
			basePath = basePath + '/';
		}

		if (fullPath.length < basePath.length) {
			return fullPath;
		}

		let pos = fullPath.indexOf(basePath);
		if (pos != 0) {
			return fullPath;
		}


		return fullPath.substr(basePath.length);
	}

	public static isSamePath(path1: string, path2: string) {
		path1 = path1.replace(/\\/g, '/');
		path2 = path2.replace(/\\/g, '/');
		if (path1.endsWith('/') == false) {
			path1 = path1 + '/';
		}
		if (path2.endsWith('/') == false) {
			path2 = path2 + '/';
		}

		return path1 == path2;
	}


	public static getCurTime() {
		return new Date().getTime();
	}

	public static getDateFormat(timestamp: number, format: string) {

		let dateFormat = function(fmt, date) {
		    let ret;
		    let opt = {
		        "Y+": date.getFullYear().toString(),        // 年
		        "m+": (date.getMonth() + 1).toString(),     // 月
		        "d+": date.getDate().toString(),            // 日
		        "H+": date.getHours().toString(),           // 时
		        "M+": date.getMinutes().toString(),         // 分
		        "S+": date.getSeconds().toString()          // 秒
		        // 有其他格式化字符需求可以继续添加，必须转化成字符串
		    };
		    for (let k in opt) {
		        ret = new RegExp("(" + k + ")").exec(fmt);
		        if (ret) {
		            fmt = fmt.replace(ret[1], (ret[1].length == 1) ? (opt[k]) : (opt[k].padStart(ret[1].length, "0")))
		        };
		    };
		    return fmt;
		}

		let date = new Date(timestamp);
		return dateFormat(format, date);
	}

	public static zipDirectory(srcPath: string, zipPath: string, srcBasePath: string) {

		//Utils.exec('C:/Program Files/WinRAR/WinRAR.exe', ['a', `${zipDir}_tmp.zip`, `./${zipDir}/`]);
		if (fs.existsSync(srcPath) == false) {
			return;
		}

		let relativePath = "";
		if (srcBasePath != null && srcBasePath.length > 0) {

			relativePath = this.getRelativePath(srcPath, srcBasePath);
			if (relativePath.length == srcPath.length || relativePath.length == 0) {
				Log.error(`${srcPath} : ${srcBasePath}`);
				return;
			}
		}

		let tmpDesPath = path.join(process.cwd(), relativePath);
		if (fs.existsSync(tmpDesPath)) {
			this.removeDir(tmpDesPath);
		}

		this.copyDir(srcPath, tmpDesPath);
		this.exec('C:/Program Files/WinRAR/WinRAR.exe', ['a', zipPath, relativePath]);
		this.removeDir(tmpDesPath);

		//this.exec('C:/Program Files/WinRAR/WinRAR.exe', ['a', zipPath, srcPath]);
	}

	public static getFileSize(fullPath: string) {

		if (fs.existsSync(fullPath) == false) {
			return 0;
		}

		return fs.statSync(fullPath).size;
	}
}