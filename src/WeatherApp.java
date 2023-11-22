import netscape.javascript.JSObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

//retrieve weather data from API
public class WeatherApp {
    //fetch weather data for given location
    public static JSONObject getWeatherData(String locationName) {
        JSONArray locationData = getLocationData(locationName);

        //extract latitude and longitude data
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        //build API request with location coordinates
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude + "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&timezone=auto";

        try {
            //call API and get response
            HttpURLConnection connection = fetchApiResponse(urlString);

            //check for response status
            if(connection.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }

            //store resulting JSON data
            StringBuilder resultJSON = new StringBuilder();
            Scanner scanner = new Scanner(connection.getInputStream());
            while (scanner.hasNext()) {
                //read and store into the string builder
                resultJSON.append(scanner.nextLine());
            }

            //close scanner
            scanner.close();

            //close connection
            connection.disconnect();

            //parse through data
            JSONParser parser = new JSONParser();
            JSONObject resultJSONObject = (JSONObject) parser.parse(String.valueOf(resultJSON));

            //retrieve hourly data
            JSONObject hourly = (JSONObject) resultJSONObject.get("hourly");

            //need to get the index of current hour
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            //get temperature
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            //get weather code
            JSONArray weatherCode = (JSONArray) hourly.get("weathercode");
            String weatherCondition = convertWeatherCode((long) weatherCode.get(index));

            //get humidity
            JSONArray relativeHumidity = (JSONArray) hourly.get("relativehumidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            //get wind speed
            JSONArray windSpeedData = (JSONArray) hourly.get("windspeed_10m");
            double windSpeed = (double) windSpeedData.get(index);

            //build the weather json data object
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windSpeed);

            return weatherData;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //retrieves geographic coordinates for given location
    public static JSONArray getLocationData(String locationName) {
        //replace any whitespace in location name to '+' to adhere to API's request format
        locationName = locationName.replaceAll(" ", "+");

        //build API's URL with location parameter
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + locationName +"&count=10&language=en&format=json";

        try {
            HttpURLConnection connection = fetchApiResponse(urlString);

            //check response status
            if(connection.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            } else {
                //store API results
                StringBuilder resultsJSON = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());

                //read and store JSON data into string builder
                while (scanner.hasNext()) {
                    resultsJSON.append(scanner.nextLine());
                }

                //close scanner
                scanner.close();

                //close URL connection
                connection.disconnect();

                //parse the JSON string into a JSON object
                JSONParser parser = new JSONParser();
                JSONObject resultsJSONObject = (JSONObject) parser.parse(String.valueOf(resultsJSON));

                //get the list of location data the API generated from the location name
                return (JSONArray) resultsJSONObject.get("results");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //couldn't find location
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            //attempt to make a connection
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            //set request method to get
            connection.setRequestMethod("GET");

            //connect to API
            connection.connect();
            return connection;
        } catch(IOException e) {
            e.printStackTrace();
        }

        //could not make connection
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();

        //iterate through the time list and see which one matches our current time
        for(int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if(time.equalsIgnoreCase(currentTime)) {
                //return index
                return i;
            }
        }

        return 0;
    }

    public static String getCurrentTime() {
        //get current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();

        //format date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-MM-dd'T'HH':00'");

        //format and print the current date and time

        return currentDateTime.format(formatter);
    }

    private static String convertWeatherCode(long weatherCode) {
        String weatherCondition = "";
        if(weatherCode == 0L) {
            //clear
            weatherCondition = "Clear";
        } else if (weatherCode > 0L && weatherCode <= 3L) {
            //cloudy
            weatherCondition = "Cloudy";
        } else if ((weatherCode >= 45L && weatherCode <= 67L) || (weatherCode >= 80L && weatherCode <= 82L) || (weatherCode >= 95L && weatherCode <= 99L)) {
            //rain
            weatherCondition = "Rain";
        } else if ((weatherCode >= 71L && weatherCode <= 77L) || (weatherCode >= 85L && weatherCode <= 86L)) {
            //snow
            weatherCondition = "Snow";
        }

        return weatherCondition;
    }
}
