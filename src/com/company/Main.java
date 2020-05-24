package com.company;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * IMPORTANT!
 *
 *      This bot uses the OpenWeatherMap API in order to retrieve weather information on user request.
 * No further configuration is required for OpenWeatherMap aside my personal key. You should probably use your
 * own but in order to make it easier to mark, I have left my key in. This is a free, standard key that allows for
 * 60 calls/min and 1000 calls/day.
 *
 *      In additon, Google's GSON is also used in order to de-serialise the JSON fed by the aforementioned API.
 *To download this library, go to https://github.com/google/gson and scroll down to "Gson jar downloads" and download
 * version 2.8.6. Alternatively, you may import this library from the jar file that I have provided in the submission.
 *
 * Commands used
 * 1. NICK
 * 2. USER
 * 3. JOIN
 * 4. NOTICE
 * 5. PRIVMSG
 * 6. QUIT
 * 7. PING
 * 8. PONG
 *
 * */

class Main {

    private static Scanner in;
    private static OutputStreamWriter outputStreamWriter;
    private static String helpText = "Type !WB_[city]_[data] for your weather information. " +
            "If you require information about a city with the same name but a different country, simply add a ,XX" +
            " to the end of your city (with XX representing the country code of that city). If your city contains " +
            "spaces (i.e. Mexico City), this is accepted. ";

    //method for sending messages to the server
    static void sendString(BufferedWriter bw, String str) throws IOException{
            bw.write(str + "\r\n");
            bw.flush();
    }
    public static void main(String args[]) throws IOException{

            //serverName and bot information
            String serverName   = "127.0.0.1";
            int port        = 6667;
            String host     = "selsey.nsqdc.city.ac.uk";
            String realName = "aczg117";
            String nickname = "WBot";
            String channel  = "#KuduChannel";
            String message  = "Hi! my name is WeatherBot and I am here at your service! \u0002\u001FType " +
                    "!WBhelp for the command list";

            //Establishes connection with the server.
            Socket socket = new Socket(serverName,port);
            in = new Scanner(socket.getInputStream());

            //Initialises the necessary tools for reading and writing.
            System.out.println("*** Connected to serverName.");
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            System.out.println("*** Opened OutputStreamWriter.");
            BufferedWriter bwriter = new BufferedWriter(outputStreamWriter);
            System.out.println("*** Opened BufferedWriter.");

            //Sends all the necessary information necessary for authentication and connection.
            sendString(bwriter,"NICK "+nickname);
            sendString(bwriter,"USER" + " " + nickname + " " + host + " " + serverName + " " + realName );
            //Joins a channel specified in the variable above, for testing purposes, I have made my own.
            sendString(bwriter,"JOIN "+channel);
            //Sends out a notice with a notification sound, letting everyone in the server know that WBot has entered.
            sendString(bwriter,"NOTICE " + channel + " :" + message);

            /*Ensures that the bot stays online by Pong-ing back any pings from the serverName.
            *
            * Also contains redirects if any users call the specific phrases.*/
            while (in.hasNext()) {
                String serverMessage = in.nextLine();
                System.out.println("<<<" + serverMessage);
                if (serverMessage.contains("!WB_help")){
                    sendString(bwriter, "PRIVMSG " + channel + " :" + helpText);
                    sendString(bwriter, "PRIVMSG " + channel + " :" + "\u0002[data] commands ");
                    sendString(bwriter, "PRIVMSG " + channel + " :" + "\u00031temp: most recent recorded average temperature");
                    sendString(bwriter, "PRIVMSG " + channel + " :" + "\u000310pressure: Pressure value");
                    sendString(bwriter, "PRIVMSG " + channel + " :" + "\u00032humidity: Humidity value");
                    sendString(bwriter, "PRIVMSG " + channel + " :" + "\u00034windspeed: Wind speed");
                    sendString(bwriter, "PRIVMSG " + channel + " :" + "\u00035coord: Coordinates of [city]");
                    sendString(bwriter, "PRIVMSG " + channel + " :" + "\u00036mintemp: Minimum temperature at the moment of calculation. This is minimal currently observed temperature (within large megalopolises and urban areas)");
                    sendString(bwriter, "PRIVMSG " + channel + " :" + "\u00037maxtemp: Maximum temperature at the moment of calculation. This is maximal currently observed temperature (within large megalopolises and urban areas)");
                    sendString(bwriter, "PRIVMSG " + channel + " :" + "\u0002Example: !WB_London,CA_temp");
                /*  Quits if the user desires for the bot to no longer exist on the channel.
                *This will then stop the program from running.*/
                }else if (serverMessage.contains("!WB_quit")){
                    sendString(bwriter, "QUIT " +" :" + "Bye!");
                } else if (serverMessage.contains("!WB")){
                    String string = breakDown(serverMessage);
                    sendString(bwriter, "PRIVMSG " + channel + " :" + string);
            }
                //Allows the bot to stay on the channel by sending a PONG whenever a PING is received.
                if (serverMessage.startsWith("PING")) {
                    String pingContents = serverMessage.split(" ", 2)[1];
                    sendString(bwriter, "PONG " + pingContents);
            }
        }
            bwriter.close();
    }

    /*  Breaks down the message sent by the client in order to find out the city and the data they requested.
    *   It then passes it onto a newly created weather report that analyses the inputs */

    public static String breakDown(String serverMessage){
        //Copied the server message into a new string, just in case I need the original form of serverMessage.
        String serverMessageCopy = serverMessage;
        serverMessageCopy = serverMessageCopy.substring(serverMessageCopy.indexOf(":", 1)+1);
        if (serverMessageCopy.startsWith("!"))
        {
            //Remove "!WB" from the string
            serverMessageCopy = serverMessageCopy.substring(serverMessageCopy.indexOf("!WB")+3);
            String cityDataPair = serverMessageCopy.substring(serverMessageCopy.indexOf("_")+1);

            String[] parts = new String[2];

            //City - remove everything after (and including) the first underscore in the phrase
            parts[0] = cityDataPair.split("_")[0];
            //Data - remove everything before (and including) the first underscore in the phrase
            parts[1] = cityDataPair.substring(cityDataPair.indexOf("_")+1);


            WeatherReport weatherReport = new WeatherReport(parts);

          return weatherReport.feedReport();
        }else
            return "Incorrect command, please refer to \u0002 \u001F \u0003!WB_help";
    }

}