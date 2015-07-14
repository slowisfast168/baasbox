package com.kingblinginteractive.pushnotification;

import com.baasbox.dao.DocumentDao;
import com.baasbox.dao.exception.InvalidCollectionException;
import com.baasbox.dao.exception.SqlInjectionException;
import com.baasbox.db.DbHelper;
import com.baasbox.exception.InvalidAppCodeException;
import com.baasbox.service.logging.BaasBoxLogger;
import com.baasbox.service.storage.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kingblinginteractive.parsewrapper.ParseException;
import com.kingblinginteractive.parsewrapper.ParseObject;
import com.kingblinginteractive.parsewrapper.SaveCallback;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.Minutes;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by ayang on 7/12/15.
 */
public class PushMessageLogger {
    static int cntFacebookYes = 0, cntFacebookNo = 0;
    static int cntAndroidYes = 0, cntAndroidNo = 0;
    static int cntIOSYes = 0, cntIOSNo = 0;

    public static void cnt(String hwtype, boolean pushResult){
        if(hwtype.equals("Android")){
            if(pushResult)
                cntAndroidYes = cntAndroidYes + 1;
            else
                cntAndroidNo = cntFacebookNo + 1;
        }
        else if(hwtype.equals("iOS")){
            if(pushResult)
                cntIOSYes = cntIOSYes + 1;
            else
                cntIOSNo = cntIOSNo + 1;
        }
    }

    public static void cnt(boolean faceResult){
        if(faceResult)
            cntFacebookYes = cntFacebookYes + 1;
        else
            cntFacebookNo = cntFacebookNo + 1;
    }

    public static void saveResult(int minutes){
        savePushNotificationResultBaasBox(cntIOSYes, cntIOSNo, "iOS", minutes);
        savePushNotificationResultBaasBox(cntAndroidYes, cntAndroidNo, "Android", minutes);
        savePushNotificationResultBaasBox(cntFacebookYes, cntFacebookNo, "Facebook", minutes);

        resetCnt();
    }

    public static void resetCnt(){
        cntIOSYes = 0;
        cntIOSNo = 0;
        cntAndroidNo = 0;
        cntAndroidYes = 0;
        cntFacebookNo = 0;
        cntFacebookYes = 0;
    }

    public static void savePushNotificationResultParse(final int success, final int fail, final String platform, int minutes){
        //save to Parse database
        try{
            int cnt = success + fail;
            MathContext context = new MathContext(4);
            String ratio = "0.00%";
            if(cnt > 0){
                BigDecimal result = new BigDecimal(success).divide(new BigDecimal(cnt), context).multiply(new BigDecimal(100), context);
                if(result.compareTo(new BigDecimal(0)) > 0){
                    ratio = result.setScale(2).toString() + "%";
                }
            }
            ParseObject record = new ParseObject("PushNotification");
            record.put("cnt", cnt);
            record.put("success", success);
            record.put("fail", fail);
            record.put("ratio", ratio);
            record.put("platform", platform);
            record.put("minute", minutes);
            record.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    BaasBoxLogger.debug("Save the record to parse successfully. cnt: {}, platform: {}", success + fail, platform);
                }
            });
        }catch (Exception e){
            BaasBoxLogger.error("Save the record to parse error. cnt: {}, platform: {}", success+fail, platform);
        }

    }

    public static void savePushNotificationResultBaasBox(final int success, final int fail, final String platform, int minutes){
        int cnt = success + fail;
        MathContext context = new MathContext(4);
        String ratio = "0.00%";
        if(cnt > 0){
            BigDecimal result = new BigDecimal(success).divide(new BigDecimal(cnt), context).multiply(new BigDecimal(100), context);
            if(result.compareTo(new BigDecimal(0)) > 0){
                ratio = result.setScale(2).toString() + "%";
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode fieldsNode = mapper.getNodeFactory().objectNode();
        fieldsNode.put("cnt", cnt);
        fieldsNode.put("success", success);
        fieldsNode.put("fail", fail);
        fieldsNode.put("ratio", ratio);
        fieldsNode.put("platform", platform);
        fieldsNode.put("minute", minutes);

        BaasBoxLogger.debug("Save the record to baasbox successfully. cnt: {}, platform: {}", success + fail, platform);
        try {
            DocumentService.create("GA_PushNotificationSentHistory", fieldsNode);
        } catch (Throwable throwable) {
            BaasBoxLogger.debug("save to GA_PushNotificationSentHistory error");
            throwable.printStackTrace();
        }

    }
}
