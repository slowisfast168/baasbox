package com.kingblinginteractive.pushnotification;

import com.baasbox.service.logging.BaasBoxLogger;
import com.baasbox.service.storage.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.util.*;

/**
 * Created by ayang on 7/11/15.
 */
public class PushUser {
    private String fbname;
    private ODocument document;
    private Date createdOn;
    private Date modifiedOn;
    private PushUserType userType;
    private List<String[]> devices;
    private boolean isInactive28Days;


    public enum PushUserType{
        REGULAR,
        ACTIVE,
        INACTIVE
    }

    public boolean isFirtTimeOfferSent() {
        Object field = document.field("FirstTimeOfferSent");
        if(field == null)
            return false;
        else
            return (Boolean)field;
    }

    public boolean isInactive28() {
        return isInactive28Days;
    }

    public void setInactive28(boolean isInactive28Days) {
        this.isInactive28Days = isInactive28Days;
    }

    public boolean isRegularLimitedTimeOfferSent() {
        Object field = document.field("RegularLimitedTimeOfferSent");
        if(field == null)
            return false;
        else
            return (Boolean)field;
    }

    public List<String[]> getDevices() {
        return devices;
    }

    public void setDevices(List<String[]> devices) {
        this.devices = devices;
    }

    public PushUserType getUserType() {
        return userType;
    }

    public void setUserType(PushUserType userType) {
        this.userType = userType;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public String getFbname() {
        return fbname;
    }

    public void setFbname(String fbname) {
        this.fbname = fbname;
    }

    public ODocument getDocument() {
        return document;
    }

    public void setDocument(ODocument document) {
        this.document = document;
    }

    public PushMessage prepareMessageToSend(){
        if(userType == PushUserType.ACTIVE){
            return PushMessageAnalyzer.getActiveMessage();
        }else if(userType == PushUserType.INACTIVE){
            return PushMessageAnalyzer.getInactiveMessage();
        }else{
            return PushMessageAnalyzer.getRegularMessage(this);
        }
    }

    public boolean savePushHistory(PushMessage message){
        try {
            //save First-time offer flag or Regular limited-time offer flag
            ODocument userDoc = this.getDocument();
            if(this.getUserType() == PushUserType.REGULAR){
                if(message.getMessageType() == PushMessage.Target.RegularCategory.LimitedTimeOfferSubCategory.FIRST_TIME){
                    userDoc.field("FirstTimeOfferSent", true);
                    userDoc.save();
                }else if(message.getMessageType() == PushMessage.Target.RegularCategory.LimitedTimeOfferSubCategory.REGULAR){
                    userDoc.field("RegularLimitedTimeOfferSent", true);
                    userDoc.save();
                }
            }

            //save the push message history
            try {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode fieldsNode = mapper.getNodeFactory().objectNode();
                fieldsNode.put("fbid", fbname);
                fieldsNode.put("messageType", message.getMessageType());
                fieldsNode.put("messageContent", message.getMessageContent());
                fieldsNode.put("sentDate", new Date().toLocaleString());
                DocumentService.create("PushMessageLog", fieldsNode);
            } catch (Throwable throwable) {
                BaasBoxLogger.debug("save to PushMessageHistory error");
                throwable.printStackTrace();
            }

            return true;
        } catch (Throwable throwable) {
            BaasBoxLogger.debug("Exception in PushUser.savePushHistoryEx()");
            throwable.printStackTrace();
        }

        return false;
    }
}
