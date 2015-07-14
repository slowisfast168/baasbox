package com.kingblinginteractive.pushnotification;

import com.baasbox.service.logging.BaasBoxLogger;
import com.kingblinginteractive.parsewrapper.Parse;
import org.joda.time.Minutes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ayang on 7/11/15.
 */
public class PushMessageSender {
    /*------
     static keys
     -------*/
    public static String PARSE_REST_APP_KEY = "kl3b8T3tY0xS1C1jFLgOMxdTT61GW4rxoN2mOKTk";
    public static String PARSE_APP_ID = "yIhHkRGLugzooLIj1NZkrZ7fM5xVfoJboCq1bcmn";
    public static int PARSE_QUERY_LIMIT = 1000; //set rest-api query return 1000 by default
//    Parse.initialize(PARSE_APP_ID, PARSE_REST_APP_KEY);
//    logger.info("Parse initialized successfully!");


    public static String SCHEDULER_START_TIME = "0 00 15 ? * *"; //15:00 everyday
    public static String PUSH_NOTIFICATION_TIME = " 18:00"; //18:00 everyday
    public static String FB_REF_MESSAGE = "campaign";
    public static int TOTAL_SPINED_CREDITS = 2000; //active user
    public static int TOTAL_BOUGHT_CREDITS = 10000; //active user
    public static int TOTAL_BOUGHT_GEMS = 300; //active user
    public static long DAYS_7 = 7 * 24 * 60 * 60 * 1000; //inactive users
    public static long DAYS_3 = 3 * 24 * 60 * 60 * 1000; //first-time offer period

    public static void sendMessage(PushMessage message, PushUser user){

        boolean isMobileSuccess = false;
        String fbid = user.getFbname();
        String messageContent = message.getMessageContent();
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String time = dateFormat.format(currentDate) + PUSH_NOTIFICATION_TIME;

        try{

            //send to mobile
            List<String[]> devices = user.getDevices();
            if(devices.size() > 0){
                for(String[] device : devices){
                    String hwid = device[0], hwtype = device[1];
                    boolean pushResult = SendMobilePushNotification.sendPushWooshNotificaitonToSingleDevice(hwid, messageContent, time);
                    isMobileSuccess |= pushResult;
                    PushMessageLogger.cnt(hwtype, pushResult);
                }
            }

            //send to facebook. don't send to user if not login for more than 28 days
            if(!user.isInactive28()) {
                boolean faceResult = SendFacebookPushNotification.sendFacebookPush(fbid, messageContent, FB_REF_MESSAGE);
                PushMessageLogger.cnt(faceResult);
            }

            //save the log to baasbox user's message history
            if(isMobileSuccess){
                user.savePushHistory(message);
            }


        } catch (Exception e){
            BaasBoxLogger.debug("Some exception when sending message in PushMessageSender.sendMessage()");
        }
    }
}
