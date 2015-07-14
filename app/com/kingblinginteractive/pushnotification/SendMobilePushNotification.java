package com.kingblinginteractive.pushnotification;

import com.baasbox.service.logging.BaasBoxLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SendMobilePushNotification
{
    public static final String PUSHWOOSH_SERVICE_BASE_URL = "https://cp.pushwoosh.com/json/1.3/";
    private static final String AUTH_TOKEN = "V4g4GDv0AcmMcuduTG6sja9w8Cm7COgm1VlYjJY4zfPmTeOp33GksORgEsiyaJJwSn27vUzlzz3Pu0pi3g2c";
    private static final String APPLICATION_CODE = "54AA1-C6CFC"; //songshanslots
//    private static final String APPLICATION_CODE = "C623E-4F22C"; //songshanslots debug

    private static boolean createMessage(List<String> hwidList, String content, String time) throws JSONException, MalformedURLException
    {
        try {
            String method = "createMessage";
            URL url = new URL(PUSHWOOSH_SERVICE_BASE_URL + method);

            JSONArray devices = new JSONArray();
            for (String hwid : hwidList) {
                devices.put(hwid);
            }

            JSONObject custom_data = new JSONObject();
            custom_data.put("custom", "{\"ref\":\"" + PushMessageSender.FB_REF_MESSAGE + "\"}");

            JSONArray platformsArray = new JSONArray(); // 1 - iOS; 2 - BB; 3 - Android; 4 - Nokia ASHA; 5 - Windows Phone; 7 - OS X; 8 - Windows 8; 9 - Amazon; 10 - Safari; 11 - Chrome
            platformsArray.put(1)
                    .put(3);


            JSONArray notificationsArray = new JSONArray()
                    .put(new JSONObject()
                            .put("send_date", time) //1.date: YYYY-MM-DD HH:mm  OR 'now'
                            .put("ignore_user_timezone", false) //2. timezone: true or false
                                    //                        .put("campaign", "test") //3."CAMPAIGN_CODE",     // Optional. Campaign code to which you want to assign this push)
                            .put("content", content) //4.content: Object( language1: 'content1', language2: 'content2' ) OR string. For Windows 8 this parameter is ignored, use "wns_content" instead.
                            .put("data", custom_data) //add the custom data to track push notification
                                    //                        .put("page_id", 39) //5.
                                    //                        .put("rich_page_id", 42)//6.
                                    //                        .put("link", "http://apps.facebook.com/songshanslot?ref=pushnotification") //7.
                                    //                        .put("minimize", 0) //8.
                                    //                                .put("platform", platformsArray)//9
                            /*
                                // iOS related
                                    "ios_badges": 5,               // Optional. Integer. This value will be sent to ALL devices given in "devices"
                                    "ios_sound": "sound file.wav",    // Optional. Sound file name in the main bundle of application
                                    "ios_ttl": 3600, // Optional. Time to live parameter - the maximum lifespan of a message in seconds
                                    "ios_category_id": "1",       // Optional. Integer. iOS8 category ID from Pushwoosh
                                    "ios_root_params" : {     //Optional - root level parameters to the aps dictionary
                                      "aps":{
                                                "content-available": "1"
                                                }
                                                    },
                                    "apns_trim_content":1,     // Optional. (0|1) Trims the exceeding content strings with ellipsis
                                    "ios_trim_content": 1,       // Deprecated, use "apns_trim_content" instead.
                             */
                            .put("ios_badges", "+1")
                            /*
                            // Android related
                                "android_root_params": {"key": "value"}, // custom key-value object. root level parameters for the android payload recipients
                                "android_sound" : "soundfile", // Optional. Sound file name in the "res/raw" folder, do not include the extension
                                "android_header":"header",    // Optional. Android notification header
                                "android_icon": "icon",
                                "android_custom_icon": "http://example.com/image.png", // Optional. Full path URL to the image file
                                "android_banner": "http://example.com/banner.png", // Optional. Full path URL to the image file
                                "android_gcm_ttl": 3600, // Optional. Time to live parameter - the maximum lifespan of a message in seconds

                                "android_vibration": 0,   // Android force-vibration for high-priority pushes, boolean
                                "android_led":"#rrggbb",  // LED hex color, device will do its best approximation
                             */
                            .put("devices", devices));
            //                                .put("filter", null) //"filter":"FILTER_NAME", //Optional.
            //                        .put("conditions", conditions));


            JSONObject requestObject = new JSONObject()
                    .put("application", APPLICATION_CODE)
                    .put("auth", AUTH_TOKEN)
                    .put("notifications", notificationsArray);

            JSONObject mainRequest = new JSONObject().put("request", requestObject);
            JSONObject response = SendServerRequest.sendJSONRequest(url, mainRequest.toString());
            if (response != null && response.getInt("status_code") == 200 && response.getString("status_message").equals("OK")) {
                BaasBoxLogger.debug("send push notification status code is 200, status message is ok");
                return true;
            } else {
                BaasBoxLogger.error("send push notification status code is not 200!");
                return false;
            }
        }catch (Exception e){
            BaasBoxLogger.error("Send push woosh mobile notification error: ", e.getMessage());
        }

        return false;
    }

    private static void setTags(String hwid, String tagKey, String tagValue) throws JSONException, MalformedURLException
    {
        String method = "setTags";
        URL url = new URL(PUSHWOOSH_SERVICE_BASE_URL + method);

        JSONArray tagsArray = new JSONArray()
                .put(new JSONObject().put(tagKey, tagValue));

        JSONObject requestObject = new JSONObject()
                .put("application", APPLICATION_CODE)
                .put("auth", AUTH_TOKEN)
                .put("hwid", hwid)
                .put("tags", tagsArray);

        JSONObject mainRequest = new JSONObject().put("request", requestObject);
        JSONObject response = SendServerRequest.sendJSONRequest(url, mainRequest.toString());

        BaasBoxLogger.debug("Response is: " + response);
    }

    private static void getTags(String hwid) throws JSONException, MalformedURLException
    {
        String method = "getTags";
        URL url = new URL(PUSHWOOSH_SERVICE_BASE_URL + method);

        JSONObject requestObject = new JSONObject()
                .put("application", APPLICATION_CODE)
                .put("auth", AUTH_TOKEN)
                .put("hwid", hwid);

        JSONObject mainRequest = new JSONObject().put("request", requestObject);
        JSONObject response = SendServerRequest.sendJSONRequest(url, mainRequest.toString());

        System.out.println("Response is: " + response);
    }


    public static void setPushWooshTagToDevice(String hwid, String tagKey, String tagValue) {
        try {
            SendMobilePushNotification.getTags(hwid);
            SendMobilePushNotification.setTags(hwid, tagKey, tagValue);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @param hwidList
     * @param content
     * @param time, format is "YYYY-MM-DD HH:mm  OR 'now'"
     */
    public static void sendPushWooshNotificaitonToDevices(List<String> hwidList, String content, String time) {
        int page = 0;
        int pageSize = 1000;
        if(hwidList.size() % pageSize == 0)
            page = hwidList.size() / pageSize;
        else
            page = hwidList.size() / pageSize + 1;

        for(int i = 0; i < page; i++)
        {
            List<String> subList = new ArrayList<String>();
            if(i < page - 1){
                subList = hwidList.subList(i*pageSize, i*pageSize + (pageSize));
            }
            else{
                subList = hwidList.subList(i*pageSize, hwidList.size());
            }

            try {
                //push woosh just can send Not more than 1000 devices in an array.
                boolean result = SendMobilePushNotification.createMessage(subList, content, time);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (Exception e){
                BaasBoxLogger.error("Send push woosh mobile notification error: ", e.getMessage());
                e.printStackTrace();
            }
        }


    }
    /**
     *
     * @param
     * @param content
     * @param time, format is "YYYY-MM-DD HH:mm  OR 'now'"
     */
    public static boolean sendPushWooshNotificaitonToSingleDevice(String hwid, String content, String time) {
        List<String> subList = new ArrayList<String>();
        subList.add(hwid);

        try {
            //push woosh just can send Not more than 1000 devices in an array.
            boolean result = SendMobilePushNotification.createMessage(subList, content, time);
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e){
            BaasBoxLogger.error("Send push woosh mobile notification error: ", e.getMessage());
            e.printStackTrace();
        }

        return false;
    }


}

class SendServerRequest
{
    static JSONObject sendJSONRequest(URL url, String request)
    {
        HttpURLConnection connection = null;
        try
        {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(request.getBytes("UTF-8"));
            writer.flush();
            writer.close();

            return parseResponse(connection);
        }
        catch (Exception e)
        {
            BaasBoxLogger.error("An error occurred: " + e.getMessage());
            return null;
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }
    }

    static JSONObject parseResponse(HttpURLConnection connection) throws IOException, JSONException
    {
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();

        while ((line = reader.readLine()) != null)
        {
            response.append(line).append('\r');
        }
        reader.close();

        return new JSONObject(response.toString());
    }
}