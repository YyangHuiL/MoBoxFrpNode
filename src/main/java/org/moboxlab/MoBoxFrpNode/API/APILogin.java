package org.moboxlab.MoBoxFrpNode.API;

import com.alibaba.fastjson.JSONObject;

public class APILogin {
    public static JSONObject getResult() {
        String route = "/NodeAPI/Login";
        JSONObject request = new JSONObject();
        return APIBasic.postAPI(route,request);
    }
}
