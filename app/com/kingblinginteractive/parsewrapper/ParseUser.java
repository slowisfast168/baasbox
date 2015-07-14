package com.kingblinginteractive.parsewrapper;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by ayang on 4/28/15.
 */
public class ParseUser{

    private ParseObject userObject;

    public ParseUser(ParseObject obj) {
        userObject = obj;
    }

    public String login() throws ParseException {
        String userName = userObject.getString("username");
        String defaultPassword = "123456";

        String query = "https://api.parse.com/1/login?" +
                "username=" + userName +
                "&password=" + defaultPassword;
        try
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(query);
            httpget.addHeader("X-Parse-Application-Id", Parse.getApplicationId());
            httpget.addHeader("X-Parse-REST-API-Key", Parse.getRestAPIKey());
            httpget.addHeader("Content-Type","application/x-www-form-urlencoded");

            HttpResponse httpResponse = httpclient.execute(httpget);
            ParseResponse parseResponse = new ParseResponse(httpResponse);

            if (parseResponse.isFailed())
            {
                throw parseResponse.getException();
            }

            JSONObject obj = parseResponse.getJsonObject();

            if (obj == null)
            {
                throw parseResponse.getException();
            }

            try
            {
                String token = (String)obj.getJSONObject("authData").getJSONObject("facebook").get("access_token");
                return token;
            }
            catch (JSONException e)
            {
                throw new ParseException(ParseException.INVALID_JSON,
                        "Error parsing the array of results returned by query.", e);
            }
        }
        catch (ClientProtocolException e)
        {
            throw ParseResponse.getConnectionFailedException(e);
        }
        catch (IOException e)
        {
            throw ParseResponse.getConnectionFailedException(e);
        }
    }


    public void updateUserFeedPuppyTime(long time) throws ParseException
    {
        String maskterKey = "g4dO4hUL1tpQ71coJhelRPB07tPZfo8rYspLq4ae";
        long feedPuppyTime = time;
        //String token = login();
//        if(token == null || token.isEmpty())
//            throw new ParseException(202, "Can't login user to update");

        String objectId = userObject.getString("objectId");
        String query = "https://api.parse.com/1/users/" + objectId;

        try
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPut httpPut = new HttpPut(query);
            httpPut.addHeader("X-Parse-Application-Id", Parse.getApplicationId());
            httpPut.addHeader("X-Parse-REST-API-Key", Parse.getRestAPIKey());
//            httpPut.addHeader("X-Parse-Session-Token", token);
            httpPut.addHeader("X-Parse-Master-Key", maskterKey);

            httpPut.addHeader("Content-Type", "application/json;charset=UTF-8");

            String json = "{\"feedpuppytimeinmillisecond\":" + feedPuppyTime + "}";
            httpPut.setEntity(new StringEntity(json));
            HttpResponse httpresponse = httpclient.execute(httpPut);

            ParseResponse response = new ParseResponse(httpresponse);

            if (!response.isFailed())
            {
                JSONObject jsonResponse = response.getJsonObject();

                if (jsonResponse == null)
                {
                    throw response.getException();
                }
                System.out.println("Update the user's info successfully, updateAt:" + jsonResponse.get("updatedAt"));
            }
            else
            {
                throw response.getException();
            }
        }
        catch (ClientProtocolException e)
        {
            throw ParseResponse.getConnectionFailedException(e);
        }
        catch (IOException e)
        {
            throw ParseResponse.getConnectionFailedException(e);
        }
    }
}
