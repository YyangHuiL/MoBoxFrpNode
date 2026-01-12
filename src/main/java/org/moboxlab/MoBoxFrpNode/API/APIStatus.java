package org.moboxlab.MoBoxFrpNode.API;

import com.alibaba.fastjson.JSONObject;

public class APIStatus {
    public static JSONObject getResult(JSONObject data) {
        String route = "/NodeAPI/Status";
        return APIBasic.postAPI(route,data);
    }
}
