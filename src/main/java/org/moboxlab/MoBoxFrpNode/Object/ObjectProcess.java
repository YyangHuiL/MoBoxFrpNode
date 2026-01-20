package org.moboxlab.MoBoxFrpNode.Object;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.MoBoxFrpNode.BasicInfo;
import org.moboxlab.MoBoxFrpNode.Task.TaskRemoveConfig;
import org.moboxlab.MoBoxFrpNode.Task.TaskRemoveLimit;
import org.moboxlab.MoBoxFrpNode.Task.TaskSetLimit;
import org.moboxlab.MoBoxFrpNode.Task.TaskWriteConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ObjectProcess {
    //进程名称
    public String name;
    //进程基本数据
    public JSONObject data;

    //运行状态
    public boolean alive = true;
    //守护线程
    public Thread daemon;
    //进程本体
    public Process process;

    //配置文件名称
    public String configFile;
    //可执行文件名称
    public String executeFile;

    //启动方法
    public void start() throws Exception{
        BasicInfo.logger.sendInfo("正在启动穿透码："+name);
        //写入配置
        configFile = "./MoBoxFrp/frp/"+data.getString("token")+".toml";
        executeFile = "./MoBoxFrp/frp/frps_"+data.getString("token");
        if (BasicInfo.config.getString("systemType").equals("Windows")) executeFile += ".exe";
        TaskWriteConfig.executeTask(configFile,data);
        //设置限速
        TaskSetLimit.executeTask(data);
        //启动进程
        String command = executeFile+" -c "+configFile;
        process = Runtime.getRuntime().exec(command);
        daemon = new Thread(() -> daemonVoid(this));
        daemon.start();
    }

    //停止方法
    public void stop() throws Exception{
        BasicInfo.logger.sendInfo("正在停止穿透码："+name);
        //停止进程
        alive = false;
        process.destroy();
        daemon.interrupt();
        //删除配置
        TaskRemoveConfig.executeTask(configFile,data);
        //删除限速
        TaskRemoveLimit.executeTask(data);
    }

    public static void daemonVoid(ObjectProcess object) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(object.process.getInputStream()));
        while (true) {
            try {
                String readLine;
                while ((readLine = reader.readLine())!=null) {
                    BasicInfo.sendDebug(readLine);
                }
            } catch (Exception e) {
                BasicInfo.logger.sendException(e);
                BasicInfo.logger.sendWarn("守护进程出现错误！穿透码名称："+object.name);
            }
            if (!object.process.isAlive()) break;
            if (!object.alive) break;
        }
    }
}
