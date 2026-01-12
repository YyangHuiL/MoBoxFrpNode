package org.moboxlab.MoBoxFrpNode;

import org.moboxlab.MoBoxFrpNode.Command.CommandDebug;
import org.moboxlab.MoBoxFrpNode.Command.CommandExit;
import org.moboxlab.MoBoxFrpNode.Task.TaskLogin;
import org.mossmc.mosscg.MossLib.Command.CommandManager;
import org.mossmc.mosscg.MossLib.Config.ConfigManager;
import org.mossmc.mosscg.MossLib.File.FileCheck;
import org.mossmc.mosscg.MossLib.File.FileDependency;
import org.mossmc.mosscg.MossLib.Object.ObjectLogger;

import java.util.Calendar;
import java.util.Date;

import static org.moboxlab.MoBoxFrpNode.BasicInfo.logger;

public class Main {
    public static void main(String[] args) {
        //计时
        long startTime = System.currentTimeMillis();

        //日志模块初始化
        FileCheck.checkDirExist("./MoBoxFrp");
        BasicInfo.logger = new ObjectLogger("./MoBoxFrp/logs");
        //周四检测（
        checkThursday();
        //外部依赖初始化（不包含MossLib）
        FileDependency.loadDependencyDir("./MoBoxFrp/dependency", "dependency");

        //基础信息输出
        logger.sendInfo("欢迎使用MoBoxFrp~这里是节点哦~");
        logger.sendInfo("软件版本：" + BasicInfo.version + " " + BasicInfo.versionType);
        logger.sendInfo("软件作者：" + BasicInfo.author);
        logger.sendInfo("感谢以下贡献者：");
        logger.sendInfo(BasicInfo.contributor);

        //配置文件初始化
        BasicInfo.logger.sendInfo("正在读取配置文件......");
        BasicInfo.config = ConfigManager.getConfigObject("./MoBoxFrp", "config.yml", "config.yml");

        //初始化API
        BasicInfo.logger.sendInfo("正在登录至主控......");
        TaskLogin.executeTask();

        //命令行初始化
        CommandManager.initCommand(BasicInfo.logger,true);
        CommandManager.registerCommand(new CommandExit());
        CommandManager.registerCommand(new CommandDebug());

        //计时
        long completeTime = System.currentTimeMillis();
        BasicInfo.logger.sendInfo("启动完成！耗时："+(completeTime-startTime)+"毫秒！");
    }

    public static void checkThursday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int index=calendar.get(Calendar.DAY_OF_WEEK)-1;
        if (index == 4) {
            try {
                throw new ThursdayKFCVMe50Exception();
            } catch (Exception e) {
                BasicInfo.logger.sendException(e);
            }
        }
    }
}
