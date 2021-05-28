const path = require('path');
const curPath = __dirname;
const projectPath = path.join(curPath, '../../');

export default class Config {

    public static version: number = 100;
    public static remoteUrl: string = "https://s.t3uel30.com/flutter/";
    public static curPath: string = curPath;
    public static projectPath: string = projectPath;
    //public static md5Key: string = 'gjieG245KjbejKEJ';
    public static apkCmd: string = path.join(projectPath, 'apk.bat');
    public static buildLibPath: string = path.join(projectPath, 'build/app/intermediates/merged_native_libs/release/out');
    public static hotPath: string = path.join(curPath, 'hot');
}