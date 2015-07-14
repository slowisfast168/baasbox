package com.kingblinginteractive.pushnotification;

import java.util.Date;

/**
 * Created by ayang on 7/11/15.
 */
public class PushMessage {

    //======hiarachy constants
    public final class Target{
        public final static String ACTIVE = "ACTIVE";
        public final static String INACTIVE = "INACTIVE";
        public final static String REGULAR = "REGULAR";


        public final class RegularCategory{
            public final static String LIMITED_TIME_OFFER = "REGULAR.LIMITED_TIME_OFFER";
            public final static String DAILY_BONUS = "REGULAR.DAILY_BONUS";
            public final static String EPISODES = "REGULAR.EPISODES";
            public final static String GAME_FEATURES = "REGULAR.GAME_FEATURES";

            public final class LimitedTimeOfferSubCategory{
                public final static String FIRST_TIME = "REGULAR.LIMITED_TIME_OFFER.FIRST_TIME";
                public final static String HOLIDAY = "REGULAR.LIMITED_TIME_OFFER.HOLIDAY";
                public final static String REGULAR = "REGULAR.LIMITED_TIME_OFFER.REGULAR";
            }

            public final class DailyBonusSubCategory{
                public final static String QUOTE = "REGULAR.DAILY_BONUS.QUOTE";
            }

            public final class EpisodesSubCategory{
                public final static String EPISODE2 = "REGULAR.EPISODES.EPISODE2";
                public final static String EPISODE3 = "REGULAR.EPISODES.EPISODE3";
                public final static String EPISODE4 = "REGULAR.EPISODES.EPISODE4";
                public final static String EPISODE5 = "REGULAR.EPISODES.EPISODE5";
                public final static String EPISODE6 = "REGULAR.EPISODES.EPISODE6";
                public final static String EPISODE7 = "REGULAR.EPISODES.EPISODE7";
                public final static String EPISODE8 = "REGULAR.EPISODES.EPISODE8";
                public final static String EPISODE9 = "REGULAR.EPISODES.EPISODE9";
                public final static String EPISODE10 = "REGULAR.EPISODES.EPISODE10";
            }

            public final class GameFeaturesSubCategory{
                public final static String WHACK = "REGULAR.GAME_FEATURES.WHACK";
                public final static String STRIKE_BACK = "REGULAR.GAME_FEATURES.STRIKE_BACK";
                public final static String PUPPY_TRICKS = "REGULAR.GAME_FEATURES.PUPPY_TRICKS";
                public final static String FEED_THE_PUPPY = "REGULAR.GAME_FEATURES.FEED_THE_PUPPY";

                public final class GameFeatures_STRIKBACK_SubCategory{
                    public final static String BIG_DOG = "REGULAR.GAME_FEATURES.STRIKE_BACK.BIG_DOG";
                    public final static String KETCHUP = "REGULAR.GAME_FEATURES.STRIKE_BACK.KETCHUP";
                    public final static String WATERBUCKET = "REGULAR.GAME_FEATURES.STRIKE_BACK.WATERBUCKET";
                }

                public final class GameFeatures_PUPPY_TRICKS_SubCategory{
                    public final static String TRICK_1 = "REGULAR.GAME_FEATURES.PUPPY_TRICKS.TRICK_1";
                    public final static String TRICK_2 = "REGULAR.GAME_FEATURES.PUPPY_TRICKS.TRICK_2";
                    public final static String TRICK_3 = "REGULAR.GAME_FEATURES.PUPPY_TRICKS.TRICK_3";
                    public final static String TRICK_4 = "REGULAR.GAME_FEATURES.PUPPY_TRICKS.TRICK_4";
                }
            }
        }
    }

    //=========

    private String messageType;
    private String messageContent;
    private Date startDate;
    private Date endDate;
    private boolean usedLastTime;
    private int index;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isUsedLastTime() {
        return usedLastTime;
    }

    public void setUsedLastTime(boolean usedLastTime) {
        this.usedLastTime = usedLastTime;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
