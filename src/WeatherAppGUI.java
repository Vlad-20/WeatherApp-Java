import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGUI extends JFrame {

    private JSONObject weatherData;
    public WeatherAppGUI() {
        //add a title
        super("Weather App");

        //end program's process once it has been closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //set the size of the window (pixels)
        setSize(450, 650);

        //load the window at the center of the screen
        setLocationRelativeTo(null);

        //manually position the components within the GUI
        setLayout(null);

        //prevent any resize
        setResizable(false);

        addGUIComponents();
    }

    private void addGUIComponents() {
        //search bar
        JTextField searchTextField = new JTextField();

        //configure the search bar
        searchTextField.setBounds(15, 15, 350, 45);

        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        add(searchTextField);

        //weather image
        JLabel weatherImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherImage.setBounds(0, 85, 450, 260);
        add(weatherImage);

        //temperature text
        JLabel tempText = new JLabel("10 C");
        tempText.setBounds(0, 350, 450, 54);
        tempText.setFont(new Font("Dialog", Font.BOLD, 48));
        tempText.setHorizontalAlignment(SwingConstants.CENTER);
        add(tempText);

        //weather condition description
        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        //humidity image
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        humidityImage.setIcon(resizeImage(humidityImage.getIcon(), 74, 66));
        add(humidityImage);

        //humidity text
        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
        humidityText.setBounds(100, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        //wind speed image
        JLabel windSpeedImage = new JLabel(loadImage("src/assets/wind.png"));
        windSpeedImage.setBounds(220, 500, 74, 66);
        windSpeedImage.setIcon((resizeImage(windSpeedImage.getIcon(), 74, 66)));
        add(windSpeedImage);

        //wind speed text
        JLabel windSpeedText = new JLabel("<html><b>Wind Speed</b> 15km/h</html>");
        windSpeedText.setBounds(310, 500, 95, 55);
        windSpeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windSpeedText);

        //search button
        JButton searchButton = new JButton(loadImage("src/assets/loupe.png"));

        //change cursor to hand cursor when hovering over button
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.setIcon(resizeImage(searchButton.getIcon(), 40, 40));
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //get location from user
                String userInput = searchTextField.getText();

                //validate input
                if(userInput.replaceAll("\\s", "").length() <= 0) {
                    return;
                }

                //retrieve weather data
                weatherData = WeatherApp.getWeatherData(userInput);

                //update GUI
                //update weather images
                String weatherCondition = (String) weatherData.get("weather_condition");

                switch (weatherCondition) {
                    case "Clear":
                        weatherImage.setIcon(loadImage("src/assets/sunny.png"));
                        break;
                    case "Cloudy":
                        weatherImage.setIcon(loadImage("src/assets/cloudy.png"));
                        break;
                    case "Rain":
                        weatherImage.setIcon(loadImage("src/assets/rainy.png"));
                        break;
                    case "Snow":
                        weatherImage.setIcon(loadImage("src/assets/snowy.png"));
                        break;
                }

                //update temperature text
                double temperature = (double) weatherData.get("temperature");
                tempText.setText(temperature + " C");

                //update weather condition text
                weatherConditionDesc.setText(weatherCondition);

                //update humidity text
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");

                //update wind speed text
                double windSpeed = (double) weatherData.get("windspeed");
                windSpeedText.setText("<html><b>Wind Speed</b> " + windSpeed + "km/h</html>");
            }
        });
        add(searchButton);
    }

    private Icon resizeImage(Icon icon, int w, int h) {
        Image image = ((ImageIcon) icon).getImage();
        Image resizedImage = image.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    //create images in GUI components
    private ImageIcon loadImage(String resourcePath) {
        try {
            BufferedImage image = ImageIO.read(new File(resourcePath));

            return new ImageIcon(image);
        }catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Could not find image");
        return null;
    }
}
