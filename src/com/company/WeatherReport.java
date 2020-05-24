package com.company;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.*;

public class WeatherReport {

    //Info is set to the base incorrect command text so if anything goes wrong, the bot doesn not quit.
    String data, city;
    String info = "Incorrect command, please refer to \u0002 \u001F \u00032 !WB_help";

    //Sub-maps for extracting data from the JSON received.
    Map<String, Object> mainMap, windMap, sysMap, coordMap;

    //Splits the received array into more comprehensible variables.
    public WeatherReport(String[] parts) {
        this.city = parts[0];
        this.data = parts[1].toLowerCase();
    }

    /*When called, this method converts the JSON received by the API into a map so data may be easily extracted
    * later.*/
    public static Map<String, Object> jsonToMap(String str) {

        Map<String, Object> map = new Gson().fromJson(
                str, new TypeToken<HashMap<String, Object>>() {
                }.getType()
        );
        return map;

    }

    public String feedReport() {
        //My API Key for https://openweathermap.org/api. You can create your own or use mine.
        String API_Key = "04267f01e21df1eb284c4aa6bc7616b1";
        String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" +
                city +
                "&appid=" +
                API_Key +
                "&units=metric";
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            //Write the JSON output to the result StringBuilder.
            while ((line = rd.readLine()) != null) {
                result.append(line);
                System.out.println(line);
            }
            rd.close();

            //Split the information into different maps.
            Map<String, Object> respMap = jsonToMap(result.toString());
            mainMap = jsonToMap(respMap.get("main").toString());
            windMap = jsonToMap(respMap.get("wind").toString());
            sysMap = jsonToMap(respMap.get("sys").toString());
            coordMap = jsonToMap(respMap.get("coord").toString());

            info = resolveCommand();

        } catch (IOException e) {
            e.printStackTrace();
            return info;
        }
        return info;
    }


    /*Matches the command entered by the user with the respective output.
    *
    * The program looks through the map for the needed information and pulls it out,
    *   returning it to the variable that initially called it.
    *
    * The \ u X X X X Y represent unicode characters that change the format of the text, whether
    *   that represents colour, bold, etc. Note that this was specifically implemented with mIRC
    *   and might not work with other IRC clients. */

    public String resolveCommand() {
        switch (data) {
            case "temp":
                return "\u00031 " +
                        Math.round(Float.parseFloat(mainMap.get("temp").toString())) +
                        " °C";
            case "pressure":
                return "\u000310 " +
                        mainMap.get("pressure").toString() +
                        " hpa";
            case "humidity":
                return "\u00032 " +
                        mainMap.get("humidity").toString() +
                        " %";
            case "windspeed":
                return "\u00034 " +
                        windMap.get("speed").toString() +
                        " m/s";
            case "coord":
                return "\u00035 " + "longitude: " +
                        coordMap.get("lon").toString() +
                        "°N - - - - latitude: " +
                        coordMap.get("lat").toString() +
                        "°W";
            case "mintemp":
                return "\u00036 " +
                        mainMap.get("temp_min").toString() +
                        " °C";
            case "maxtemp":
                return "\u00037 " +
                        mainMap.get("temp_max").toString() +
                        " °C";
        }
        return "Incorrect command, please refer to \u0002\u001F\u0003!WB_help";
    }

}
