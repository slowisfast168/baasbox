package com.kingblinginteractive.pushnotification;

import com.baasbox.service.logging.BaasBoxLogger;
import com.kingblinginteractive.facebookwrapper.DefaultFacebookClient;
import com.kingblinginteractive.facebookwrapper.FacebookClient;
import com.kingblinginteractive.facebookwrapper.Parameter;
import com.kingblinginteractive.facebookwrapper.exception.FacebookOAuthException;
import com.kingblinginteractive.facebookwrapper.json.JsonObject;

/**
 * Created by ayang on 4/24/15.
 */
public class SendFacebookPushNotification {

    /**
     * RestFB Graph API client.
     */
    private static String accessToken = "1503579963262889|xLzwq9aZTahxgRqwmixK9KxhMMs";
    private static FacebookClient facebookClient = new DefaultFacebookClient(accessToken);

    public static boolean sendFacebookPush(String fbid, String notificationContent, String ref) {
        String connection = fbid + "/notifications";

        try {
            JsonObject publishResponse = facebookClient.publish(
                    connection,
                    JsonObject.class,
//                Parameter.with("access_token", accessToken),
                    Parameter.with("template", notificationContent),
                    Parameter.with("ref", ref)
            );

            if (publishResponse.getBoolean("success")) {
                BaasBoxLogger.debug("Facebook push notification published successfully for userID: {}", fbid);
                return true;
            } else {
                BaasBoxLogger.error("Facebook push notification published failed for userID: {}", fbid);
                return false;
            }
        } catch (FacebookOAuthException e) {
            BaasBoxLogger.error("Received Facebook error response of type OAuthException: (#803) Some of the aliases you requested do not exist: {})", fbid);
        } catch (Exception e) {
            BaasBoxLogger.error("Send facebook push notification exception", e.getMessage());
        }

        return false;
    }
}
