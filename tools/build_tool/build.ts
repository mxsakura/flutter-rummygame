const path = require('path');
import Config from "./config";
import Log from './scripts/log';
import Utils from './scripts/utils';
const fs = require('fs');
class Main {

    public static main(rgv: string[]) {
        
        this.buildAPK();
        this.writeVersion();
    }


    private static buildAPK(): void {

        Log.log('**** begin build apk *****');
        Utils.exec(Config.apkCmd, []);

        let hotDesPath = path.join(Config.hotPath, Config.version.toString());
        Utils.removeDir(hotDesPath);

        Utils.copyDir(Config.buildLibPath, hotDesPath, (fullPath: string): boolean => {
            if (fullPath.endsWith('libflutter.so') == true) {
                return false;
            }

            return true;
        });

        Log.log('**** build apk finish ****');
    }

    private static writeVersion(): void {

        let info = {version: Config.version, remoteUrl: Config.remoteUrl, assets: null};
        let hotPath = path.join(Config.hotPath, Config.version.toString());
        let files: string[] = Utils.getFiles(hotPath);
        let assertsInfo = {};
        for (let i = 0; i < files.length; i++) {
            let file = files[i];
            let relativePath = Utils.getRelativePath(file, hotPath);
            let size = Utils.getFileSize(file);
            let md5 = Utils.getMD5ByFile(file);
            assertsInfo[relativePath] = {size: size, md5: md5};
        }

        info.assets = assertsInfo;
        fs.writeFileSync(path.join(Config.hotPath, "version.json"), JSON.stringify(info));
    }
}

Main.main(process.argv);