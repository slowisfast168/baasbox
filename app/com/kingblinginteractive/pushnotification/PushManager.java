package com.kingblinginteractive.pushnotification;

import com.baasbox.BBInternalConstants;
import com.baasbox.dao.DocumentDao;
import com.baasbox.dao.exception.InvalidCollectionException;
import com.baasbox.dao.exception.SqlInjectionException;
import com.baasbox.db.DbHelper;
import com.baasbox.exception.InvalidAppCodeException;
import com.baasbox.service.logging.BaasBoxLogger;
import com.baasbox.service.storage.DocumentService;
import com.baasbox.util.QueryParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordTx;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ayang on 7/11/15.
 */
public class PushManager {
    public static String username = "admin";
    public static String password = "admin";
    public static String appcode = "1234567890";

    private List<PushUser> pushUserList = null;
    private static PushManager instance = null;

    private PushManager(){
        pushUserList = new LinkedList<PushUser>();
    }

    public static PushManager getPushManager(){
        if(instance == null){
            instance = new PushManager();
        }

        return instance;
    }

    public enum PushMode{
        EVEN_DAY,
        ODD_DAY
    }

    public PushMode getPushMode(){
        int date = DateTime.now().getDayOfMonth();
        if(date % 2 == 0){
            return PushMode.EVEN_DAY;
        }else{
            return PushMode.ODD_DAY;
        }
    }

    public void sendPush(){
        org.joda.time.DateTime startTime = new org.joda.time.DateTime();
        BaasBoxLogger.info("Start to send push notification...");

        List<ODocument> userDocs = null;
        ODatabaseRecordTx database = null;

        try {
            DbHelper.close(DbHelper.getConnection());

            try {
                database = DbHelper.open(PushManager.appcode,PushManager.username,PushManager.password);
            } catch (InvalidAppCodeException e) {
                e.printStackTrace();
            }

            QueryParams criteria = QueryParams.getInstance();
            userDocs = DocumentService.getDocuments("ParseUsers", criteria);
            PushMode mode = getPushMode();
            if(userDocs != null && userDocs.size() > 0){
                for(ODocument document : userDocs){
                    //get user
                    PushUser user = identifyUser(mode, document);

                    //get message
                    PushMessage message = user.prepareMessageToSend();

                    //send
                    PushMessageSender.sendMessage(message, user);
                }
            }


        } catch (SqlInjectionException e) {
            e.printStackTrace();
        } catch (InvalidCollectionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            //save the message usedLastTime flag
            PushMessageAnalyzer.setUsedLastTimeFlag();

            //save the log to baasbox database
            org.joda.time.DateTime endTime = new org.joda.time.DateTime();
            int minutes = Minutes.minutesBetween(startTime, endTime).getMinutes();
            BaasBoxLogger.info("Send push ended with running about: {} minutes!", minutes);
            PushMessageLogger.saveResult(minutes);

            DbHelper.close(database);
        }
    }

    /**
     * Acitve User: TotalSpinedCredits > ### or made at least one purchase
     * Inactive User: Has not returned for 7 days
     * Regular: Other user
     * @param document
     * @return
     */
    public PushUser.PushUserType identifyUserType(ODocument document){
        //active condition
        int totalSpinedCredits = (Integer)document.field("totalspinedcredits");
        int totalBoughtCredits = (Integer)document.field("totalboughtcredits");
        int totalBoughtGems = (Integer)document.field("totalboughtgems");

        //inactive condition
        //get the modifiedOn and createdOn
        Date modifiedOn = null, createdOn = null;
        modifiedOn = document.field("_audit.modifiedOn");
        createdOn = document.field("_creation_date");

        Date today = new Date();
        long difference = today.getTime() - modifiedOn.getTime();

//        return PushUser.PushUserType.REGULAR; //debug only. comment it and un-comment below code for production

        if(totalSpinedCredits > PushMessageSender.TOTAL_SPINED_CREDITS ||
                totalBoughtCredits > PushMessageSender.TOTAL_BOUGHT_CREDITS ||
                totalBoughtGems > PushMessageSender.TOTAL_BOUGHT_GEMS){
            return PushUser.PushUserType.ACTIVE;
        }else if(difference > PushMessageSender.DAYS_7){
            return PushUser.PushUserType.INACTIVE;
        }else{
            return PushUser.PushUserType.REGULAR;
        }
    }

    public boolean checkInactive28Days(ODocument document){
        //get the modifiedOn and createdOn
        Date modifiedOn = null, createdOn = null;
        modifiedOn = document.field("_audit.modifiedOn");
        createdOn = document.field("_creation_date");

        Date today = new Date();
        BigDecimal difference = new BigDecimal(today.getTime() - modifiedOn.getTime()).divide(new BigDecimal(1000));
        BigDecimal diff28 = new BigDecimal(28 * 24 * 60 * 60);
        if(difference.compareTo(diff28) > 0){
            //more than 28 days, don't send for facebook push notification
            return true;
        }else{
            return false;
        }
    }



    public PushUser identifyUser(PushMode mode, ODocument document){
        //get the modifiedOn and createdOn
        Date modifiedOn = null, createdOn = null;
        modifiedOn = document.field("_audit.modifiedOn");
        createdOn = document.field("_creation_date");

        PushUser user = new PushUser();
        user.setDocument(document);
        user.setCreatedOn(createdOn);
        user.setModifiedOn(modifiedOn);
        user.setFbname(document.field("fbname").toString());

        if(mode == PushMode.EVEN_DAY){
            //even days, tag all users as regular
            user.setUserType(PushUser.PushUserType.REGULAR);
        }else if(mode == PushMode.ODD_DAY){
            //odd days, tag users as inactive / active / regular
            PushUser.PushUserType userType = identifyUserType(document);
            user.setUserType(userType);
        }

        boolean inactive28Days = checkInactive28Days(document);
        user.setInactive28(inactive28Days);

        //get devices
        List<String[]> deviceList = new LinkedList<String[]>();
        List<Map<String, Object>> devices = new LinkedList<Map<String, Object>>();
        try {
            devices = document.field("push.devices");
            if(devices != null && devices.size() >0){
                for(Map<String, Object> device : devices){
                    String hwid = device.get("hwid").toString();
                    String hwtype = device.get("hwtype").toString();
                    String[] arr = {hwid, hwtype};
                    deviceList.add(arr);
                }

                user.setDevices(deviceList);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    public void clearUserList(){
        pushUserList.clear();
    }
}
