package org.moboxlab.MoBoxFrpNode.Cache;

import org.moboxlab.MoBoxFrpNode.BasicInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheClassID {
    public static List<String> emptyIDList = new ArrayList<>();
    public static Map<String,String> tokenIDMap = new HashMap<>();

    public static void initClassID() {
        for (int i = 100;i <= 1000;i++) {
            emptyIDList.add(String.valueOf(i));
        }
    }
    public static String getClassID(String token) {
        if (tokenIDMap.containsKey(token)) return tokenIDMap.get(token);
        String classID = emptyIDList.get(0);
        emptyIDList.remove(classID);
        tokenIDMap.put(token,classID);
        BasicInfo.logger.sendInfo("隧道"+token+"已占用限流ID："+classID);
        return classID;
    }

    public static void releaseClassID(String token) {
        if (!tokenIDMap.containsKey(token)) return;
        String classID = tokenIDMap.get(token);
        tokenIDMap.remove(token);
        emptyIDList.add(classID);
        BasicInfo.logger.sendInfo("隧道"+token+"已释放限流ID："+classID);
    }
}
