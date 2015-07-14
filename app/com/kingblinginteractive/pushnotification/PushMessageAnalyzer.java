package com.kingblinginteractive.pushnotification;

import com.baasbox.dao.exception.InvalidCollectionException;
import com.baasbox.dao.exception.SqlInjectionException;
import com.baasbox.service.logging.BaasBoxLogger;
import com.baasbox.service.storage.DocumentService;
import com.baasbox.util.QueryParams;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ayang on 7/11/15.
 */
public class PushMessageAnalyzer {
    public static ODocument activeMessagePrevious = null, activeMessageCurrent = null; //the active message category used last time for all users
    public static ODocument inactiveMessagePrevious = null, inactiveMessageCurrent = null; //the inactive message category used last time for all users
    public static ODocument regularMessageCategoryPrevious = null, regularMessageCategoryCurrent = null; //the regular message used this time for all users
    public static String activeMessageCurrent_Content = null, inactiveMessageCurrent_Content = null, regularMessageCategoryCurrent_Content = null;

    public static Boolean isHoliday = null; //the flag to show if it's holiday
    public static String holidayMessage = null;

    public static String gameFeatureCurrent_Content = null, gameFeatureCurrent_Type = null; //game features
    public static ODocument gameFeaturesPrevious = null, gameFeatureCurrent = null; //the regular message used this time for all users

    public static String regularMessage_Bonus = null; ////the regular message - bonus,

    public static String regularMessage_Episode2 = null; //episode
    public static String regularMessage_Episode3 = null; //episode
    public static String regularMessage_Episode4 = null; //episode
    public static String regularMessage_Episode5 = null; //episode
    public static String regularMessage_Episode6 = null; //episode
    public static String regularMessage_Episode7 = null; //episode
    public static String regularMessage_Episode8 = null; //episode
    public static String regularMessage_Episode9 = null; //episode
    public static String regularMessage_Episode10 = null; //episode



    public static PushMessage getMessage(String query){
        PushMessage pushMessage = new PushMessage();

        //already found the message
        if(query.equals("ACTIVE") && activeMessageCurrent_Content != null){
            pushMessage.setMessageType(query);
            pushMessage.setMessageContent(activeMessageCurrent_Content);
            return pushMessage;
        }else if(query.equals("INACTIVE") && inactiveMessageCurrent_Content != null){
            pushMessage.setMessageType(query);
            pushMessage.setMessageContent(inactiveMessageCurrent_Content);
            return pushMessage;
        }

        QueryParams criteria = QueryParams.getInstance().where("messageType=?").params(new String[]{query});
        List<ODocument> activeDocs = null;
        try {
            activeDocs = DocumentService.getDocuments("PushMessage", criteria);
            if(activeDocs != null && activeDocs.size() > 0){
                int nextIndex = 0;
                for(int i=0; i< activeDocs.size(); i++){
                    ODocument document = activeDocs.get(i);
                    String status = document.field("usedLastTime").toString();
                    if(status.equals("true")){
                        if(query.equals("ACTIVE")){
                            activeMessagePrevious = document;
                        }else if(query.equals("INACTIVE")){
                            inactiveMessagePrevious = document;
                        }
//
                        int currentIndex = Integer.parseInt(document.field("index"));
                        if(currentIndex < activeDocs.size() - 2){
                            nextIndex = currentIndex + 1;
                        }else{
                            nextIndex = 0;
                        }
                    }
                }


                if(query.equals("ACTIVE")){
                    activeMessageCurrent = activeDocs.get(nextIndex);
                    activeMessageCurrent_Content = activeMessageCurrent.field("messageContent").toString();
                    pushMessage.setMessageType(query);
                    pushMessage.setMessageContent(activeMessageCurrent_Content);
                }else if(query.equals("INACTIVE")){
                    inactiveMessageCurrent = activeDocs.get(nextIndex);
                    inactiveMessageCurrent_Content = inactiveMessageCurrent.field("messageContent").toString();
                    pushMessage.setMessageType(query);
                    pushMessage.setMessageContent(inactiveMessageCurrent_Content);
                }

                return pushMessage;
            }
        } catch (SqlInjectionException e) {
            BaasBoxLogger.debug("error: DocumentService.getDocuments(\"PushMessage\", criteria) ");
            e.printStackTrace();
        } catch (InvalidCollectionException e) {
            BaasBoxLogger.debug("error: DocumentService.getDocuments(\"PushMessage\", criteria) ");
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * if active, rotate the active message
     * @return
     */
    public static PushMessage getActiveMessage() {
        return getMessage("ACTIVE");
    }

    /**
     * if inactive, rotate the inactive message
     * @return
     */
    public static PushMessage getInactiveMessage(){
        return getMessage("INACTIVE");
    }

    /**
     * if regular, rotation order:
     a1	for all users within the first-time offer period, show first-time offer message if haven't shown before
     a2	for all users within the holiday offer period, show first-time offer message if the same offer hasn't been shown before.  (note a new holiday offer should erase all shown flags)
     a3	for all users within the regular offer period, show regular offer message if the same offer hasn't been shown before.  (note a new holiday offer should erase all shown flags)
     b1	show one message from Daily Bonus; (rotate category messages on daily basis for all users.  That is, we can send the same message to all users on the same day.)
     b2	show one message from Episodes category: show the message for the next episode that's locked which has not been shown
     b3	show one message from Game Features; rotate messages from each subcategory (rotate messages on daily basis for all users.  That is, we can send the same message to all users on the same day.)
     * @param user
     * @return
     */
    public static PushMessage getRegularMessage(PushUser user){
        PushMessage pushMessage = null;
        try{

            //check limited time offer firstly
            pushMessage = checkFirstTimeOffer(user);
            if(pushMessage != null) {
                BaasBoxLogger.debug("First time offer message is qualified!");
                return pushMessage;
            }

            pushMessage = checkHolidayOffer(user);
            if(pushMessage != null) {
                BaasBoxLogger.debug("Holiday offer message is qualified!");
                return pushMessage;
            }

            pushMessage = checkLimitedTimeRegularOffer(user);
            if(pushMessage != null){
                BaasBoxLogger.debug("Limited time regular offer message is qualified!");
                 return pushMessage;
            }

            //then check the regular message
            String category = getNextRegularMessageType();
            if(category.equals(PushMessage.Target.RegularCategory.DAILY_BONUS)){
                pushMessage = checkDailyBonus(user);
                if(pushMessage != null) {
                    BaasBoxLogger.debug("Daily bonus message is qualified!");
                    return pushMessage;
                }
            }
            else if(category.equals(PushMessage.Target.RegularCategory.EPISODES)) {
                pushMessage = checkEpisodes(user);
                if(pushMessage != null) {
                    BaasBoxLogger.debug("Episodes message is qualified!");
                    return pushMessage;
                }
            }else if(category.equals(PushMessage.Target.RegularCategory.GAME_FEATURES)) {
                pushMessage = checkGameFeatures(user);
                if(pushMessage != null) {
                    BaasBoxLogger.debug("Game features message is qualified!");
                    return pushMessage;
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        //always return a default message if it's not found
        BaasBoxLogger.debug("There is no qualified message for this user");
        pushMessage = new PushMessage();
        pushMessage.setMessageType(PushMessage.Target.RegularCategory.DailyBonusSubCategory.QUOTE);
        pushMessage.setMessageContent("King Bling says: Come back! Collect your daily bonus now!");
        return pushMessage;
    }

    public static String getNextRegularMessageType(){
        if(regularMessageCategoryCurrent_Content != null)
            return regularMessageCategoryCurrent_Content;

        List<ODocument> activeDocs = null;
        try {
            QueryParams criteria = QueryParams.getInstance().orderBy("messageType");
            activeDocs = DocumentService.getDocuments("PushMessageCategory", criteria);
            if(activeDocs != null && activeDocs.size() > 0) {
                int nextIndex = 0;
                for(int i=0; i<activeDocs.size(); i++){
                    ODocument document = activeDocs.get(i);
                    String status = document.field("usedLastTime").toString();
                    if(status.equals("true")){
                        regularMessageCategoryPrevious = document;

                        if(i < activeDocs.size() - 2){
                            nextIndex = i + 1;
                        }else{
                            nextIndex = 0;
                        }

                        break;
                    }
                }

                regularMessageCategoryCurrent = activeDocs.get(nextIndex);
                regularMessageCategoryCurrent_Content = regularMessageCategoryCurrent.field("messageType").toString();

                return regularMessageCategoryCurrent_Content;
            }
        } catch (SqlInjectionException e) {
            e.printStackTrace();
        } catch (InvalidCollectionException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }

    public static PushMessage checkFirstTimeOffer(PushUser user){
        ODocument userDoc = user.getDocument();
        //the user is first time login and within 3 days and the FirstTimeOfferSent == false
        Date createdOn = user.getCreatedOn();
        long difference = new Date().getTime() - createdOn.getTime();
        long diff3 = PushMessageSender.DAYS_3;
        if(difference <  diff3 && !user.isFirtTimeOfferSent()){

            QueryParams criteria = QueryParams.getInstance().where("messageType=?").params(new String[]{PushMessage.Target.RegularCategory.LimitedTimeOfferSubCategory.FIRST_TIME}).
                    orderBy("index");
            List<ODocument> activeDocs = null;
            try {
                activeDocs = DocumentService.getDocuments("PushMessage", criteria);
                if(activeDocs != null && activeDocs.size() > 0) {
                    PushMessage pushMessage = new PushMessage();
                    pushMessage.setMessageType(PushMessage.Target.RegularCategory.LimitedTimeOfferSubCategory.FIRST_TIME);
                    pushMessage.setMessageContent(activeDocs.get(0).field("messageContent").toString());
                    return pushMessage;
                }
            } catch (SqlInjectionException e) {
                e.printStackTrace();
            } catch (InvalidCollectionException e) {
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return null;
    }

    public static PushMessage checkHolidayOffer(PushUser user){
        PushMessage pushMessage = new PushMessage();
        if(isHoliday != null && isHoliday.booleanValue()){
            pushMessage.setMessageType(PushMessage.Target.RegularCategory.LimitedTimeOfferSubCategory.HOLIDAY);
            pushMessage.setMessageContent(holidayMessage);
            return pushMessage;
        }

        ODocument userDoc = user.getDocument();
        QueryParams criteria = QueryParams.getInstance().where("messageType=?").params(new String[]{PushMessage.Target.RegularCategory.LimitedTimeOfferSubCategory.HOLIDAY}).
                orderBy("startDate DESC");
        List<ODocument> activeDocs = null;
        try {
            activeDocs = DocumentService.getDocuments("PushMessage", criteria);
            if(activeDocs != null && activeDocs.size() > 0) {
                ODocument holidayDoc = activeDocs.get(0);
                Date startDate = parseDate(holidayDoc.field("startDate").toString());
                DateTime startDate2 = new DateTime(startDate);
                startDate2 = startDate2.minusDays(1);
                Date endDate = parseDate(holidayDoc.field("endDate").toString());
                DateTime endDate2 = new DateTime(endDate);
                endDate2 = endDate2.plusDays(1);
                DateTime today = DateTime.now();
                if (today.isAfter(startDate2.getMillis()) && today.isBefore(endDate2.getMillis())) {
                    isHoliday = new Boolean(true);
                    holidayMessage = holidayDoc.field("messageContent").toString();
                    pushMessage.setMessageType(PushMessage.Target.RegularCategory.LimitedTimeOfferSubCategory.HOLIDAY);
                    pushMessage.setMessageContent(holidayMessage);
                    return pushMessage;
                }

            }
        } catch (SqlInjectionException e) {
            e.printStackTrace();
        } catch (InvalidCollectionException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    private static Date parseDate(String dateStr){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static PushMessage checkLimitedTimeRegularOffer(PushUser user){
        boolean regularOfferSent = user.isRegularLimitedTimeOfferSent();
        if(regularOfferSent)
            return null;

        QueryParams criteria = QueryParams.getInstance().where("messageType=?").params(new String[]{PushMessage.Target.RegularCategory.LimitedTimeOfferSubCategory.REGULAR}).
                orderBy("index");
        List<ODocument> activeDocs = null;
        try {
            activeDocs = DocumentService.getDocuments("PushMessage", criteria);
            if(activeDocs != null && activeDocs.size() > 0) {
                PushMessage pushMessage = new PushMessage();
                pushMessage.setMessageType(PushMessage.Target.RegularCategory.LimitedTimeOfferSubCategory.REGULAR);
                pushMessage.setMessageContent(activeDocs.get(0).field("messageContent").toString());
                return pushMessage;
            }
        } catch (SqlInjectionException e) {
            e.printStackTrace();
        } catch (InvalidCollectionException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static PushMessage checkDailyBonus(PushUser user){
        PushMessage pushMessage = new PushMessage();
        if(regularMessage_Bonus != null){
            pushMessage.setMessageType(PushMessage.Target.RegularCategory.DailyBonusSubCategory.QUOTE);
            pushMessage.setMessageContent(regularMessage_Bonus);
            return pushMessage;
        }

        QueryParams criteria = QueryParams.getInstance().where("messageType=?").params(new String[]{PushMessage.Target.RegularCategory.DailyBonusSubCategory.QUOTE}).
                orderBy("index");
        List<ODocument> activeDocs = null;
        try {
            activeDocs = DocumentService.getDocuments("PushMessage", criteria);
            if(activeDocs != null && activeDocs.size() > 0) {
                regularMessage_Bonus = activeDocs.get(0).field("messageContent").toString();
                pushMessage.setMessageType(PushMessage.Target.RegularCategory.DailyBonusSubCategory.QUOTE);
                pushMessage.setMessageContent(regularMessage_Bonus);
                return pushMessage;
            }
        } catch (SqlInjectionException e) {
            e.printStackTrace();
        } catch (InvalidCollectionException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static PushMessage checkEpisodes(PushUser user){
        ODocument userDoc = user.getDocument();
        int currentlevel = (Integer)userDoc.field("currentlevel");

        String type = PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE2; //default
        if(currentlevel >= 0 && currentlevel < 10){
            type = PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE2;
        }else if(currentlevel >= 10 && currentlevel < 20){
            type = PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE3;
        }else if(currentlevel >= 20 && currentlevel < 30){
            type = PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE4;
        }else if(currentlevel >= 30 && currentlevel < 40){
            type = PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE5;
        }else if(currentlevel >= 40 && currentlevel < 50){
            type = PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE6;
        }else if(currentlevel >= 50 && currentlevel < 60){
            type = PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE7;
        }else if(currentlevel >= 60 && currentlevel < 70){
            type = PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE8;
        }else if(currentlevel >= 70 && currentlevel < 80){
            type = PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE9;
        }else if(currentlevel >= 80 && currentlevel < 90){
            type = PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE10;
        }

        return getEpisode(type);
    }

    private static PushMessage getEpisode(String type){
        String messageContent = "";

        switch (type){
            case PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE2:
                if(regularMessage_Episode2 !=null){
                    messageContent = regularMessage_Episode2;
                }else{
                    regularMessage_Episode2 = getEpisodeMessage(type);
                }
                break;
            case PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE3:
                if(regularMessage_Episode3 !=null){
                    messageContent = regularMessage_Episode3;
                }else{
                    regularMessage_Episode3 = getEpisodeMessage(type);
                }
                break;
            case PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE4:
                if(regularMessage_Episode4 !=null){
                    messageContent = regularMessage_Episode4;
                }else{
                    regularMessage_Episode2 = getEpisodeMessage(type);
                }
                break;
            case PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE5:
                if(regularMessage_Episode5 !=null){
                    messageContent = regularMessage_Episode5;
                }else{
                    regularMessage_Episode5 = getEpisodeMessage(type);
                }
                break;
            case PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE6:
                if(regularMessage_Episode6 !=null){
                    messageContent = regularMessage_Episode6;
                }else{
                    regularMessage_Episode6 = getEpisodeMessage(type);
                }
                break;
            case PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE7:
                if(regularMessage_Episode7 !=null){
                    messageContent = regularMessage_Episode7;
                }else{
                    regularMessage_Episode7 = getEpisodeMessage(type);
                }
                break;
            case PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE8:
                if(regularMessage_Episode8 !=null){
                    messageContent = regularMessage_Episode8;
                }else{
                    regularMessage_Episode8 = getEpisodeMessage(type);
                }
                break;
            case PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE9:
                if(regularMessage_Episode9 !=null){
                    messageContent = regularMessage_Episode9;
                }else{
                    regularMessage_Episode9 = getEpisodeMessage(type);
                }
                break;
            case PushMessage.Target.RegularCategory.EpisodesSubCategory.EPISODE10:
                if(regularMessage_Episode10 !=null){
                    messageContent = regularMessage_Episode10;
                }else{
                    regularMessage_Episode10 = getEpisodeMessage(type);
                }
                break;
            default:
                break;
        }


        PushMessage pushMessage = new PushMessage();
        pushMessage.setMessageType(type);
        pushMessage.setMessageContent(messageContent);
        return pushMessage;
    }

    private static String getEpisodeMessage(String type){
        QueryParams criteria = QueryParams.getInstance().where("messageType=?").params(new String[]{type}).
                orderBy("index");
        List<ODocument> activeDocs = null;
        try {
            activeDocs = DocumentService.getDocuments("PushMessage", criteria);
            if(activeDocs != null && activeDocs.size() > 0) {
                return activeDocs.get(0).field("messageContent").toString();
            }
        } catch (SqlInjectionException e) {
            e.printStackTrace();
        } catch (InvalidCollectionException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }

    public static PushMessage checkGameFeatures(PushUser user){
        PushMessage pushMessage = new PushMessage();
        if(gameFeatureCurrent_Content != null && gameFeatureCurrent_Type != null){
            pushMessage.setMessageType(gameFeatureCurrent_Type);
            pushMessage.setMessageContent(gameFeatureCurrent_Content);
            return pushMessage;
        }

        QueryParams criteria = QueryParams.getInstance().where("messageType LIKE ?").params(new String[]{PushMessage.Target.RegularCategory.GAME_FEATURES + "%"}).
                orderBy("messageType");
        List<ODocument> activeDocs = null;
        try {
            activeDocs = DocumentService.getDocuments("PushMessage", criteria);
            if(activeDocs != null && activeDocs.size() > 0) {
                int nextIndex = 0;
                for(int i=0; i<activeDocs.size(); i++){
                    ODocument document = activeDocs.get(i);
                    String status = document.field("usedLastTime").toString();
                    if(status.equals("true")){
                        gameFeaturesPrevious = document;
                        if(i < activeDocs.size() - 2){
                            nextIndex = i + 1;
                        }else{
                            nextIndex = 0;
                        }

                        break;
                    }
                }

                gameFeatureCurrent = activeDocs.get(nextIndex);
                gameFeatureCurrent_Content = gameFeatureCurrent.field("messageContent").toString();
                gameFeatureCurrent_Type = gameFeatureCurrent.field("messageType").toString();
                pushMessage.setMessageType(gameFeatureCurrent_Type);
                pushMessage.setMessageContent(gameFeatureCurrent_Content);

                return pushMessage;
            }
        } catch (SqlInjectionException e) {
            e.printStackTrace();
        } catch (InvalidCollectionException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static void setUsedLastTimeFlag(){
        try {
            //regular message category: bonus, episode, game features
            if(regularMessageCategoryPrevious != null){
                regularMessageCategoryPrevious.field("usedLastTime", "false");
                regularMessageCategoryPrevious.save();
                regularMessageCategoryPrevious = null;
            }
            if(regularMessageCategoryCurrent != null){
                regularMessageCategoryCurrent.field("usedLastTime", "true");
                regularMessageCategoryCurrent.save();
                regularMessageCategoryCurrent = null;
                regularMessageCategoryCurrent_Content = null;
            }

            //game features
            if(gameFeaturesPrevious != null){
                gameFeaturesPrevious.field("usedLastTime", "false");
                gameFeaturesPrevious.save();
                gameFeaturesPrevious = null;
            }
            if(gameFeatureCurrent != null){
                gameFeatureCurrent.field("usedLastTime", "true");
                gameFeatureCurrent.save();
                gameFeatureCurrent = null;
                gameFeatureCurrent_Content = null;
                gameFeatureCurrent_Type = null;
            }

            //bonus
            if(regularMessage_Bonus != null){
                regularMessage_Bonus = null;
            }

            //holiday
            if(isHoliday != null){
                isHoliday = null;
                holidayMessage = null;
            }

            //episode
            if(regularMessage_Episode2 != null){
                regularMessage_Episode2 = null;
            }
            if(regularMessage_Episode3 != null){
                regularMessage_Episode3 = null;
            }
            if(regularMessage_Episode4 != null){
                regularMessage_Episode4 = null;
            }
            if(regularMessage_Episode5 != null){
                regularMessage_Episode5 = null;
            }
            if(regularMessage_Episode6 != null){
                regularMessage_Episode6 = null;
            }
            if(regularMessage_Episode7 != null){
                regularMessage_Episode7 = null;
            }
            if(regularMessage_Episode8 != null){
                regularMessage_Episode8 = null;
            }
            if(regularMessage_Episode9 != null){
                regularMessage_Episode9 = null;
            }
            if(regularMessage_Episode10 != null){
                regularMessage_Episode10 = null;
            }

            //active message
            if(activeMessagePrevious != null){
                activeMessagePrevious.field("usedLastTime", "false");
                activeMessagePrevious.save();
                activeMessagePrevious = null;
            }
            if(activeMessageCurrent != null){
                activeMessageCurrent.field("usedLastTime", "true");
                activeMessageCurrent.save();
                activeMessageCurrent = null;
                activeMessageCurrent_Content = null;
            }

            //inactive message
            if(inactiveMessagePrevious != null){
                inactiveMessagePrevious.field("usedLastTime", "false");
                inactiveMessagePrevious.save();
                inactiveMessagePrevious = null;
            }
            if(inactiveMessageCurrent != null){
                inactiveMessageCurrent.field("usedLastTime", "true");
                inactiveMessageCurrent.save();
                inactiveMessageCurrent = null;
                inactiveMessageCurrent_Content = null;
            }

        }catch (Exception e){
            BaasBoxLogger.debug("PushMessageLogger.setUsedLastTimeFlag() exception");
            e.printStackTrace();
        }
    }
}
