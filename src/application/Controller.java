/*
 * Author: Thanos Moschou
 * Last Modification Date: 19/10/2023
 * Description: This is a simple weather app. It is my first app using a request to an api
 */

package application;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Controller 
{
	@FXML
	private TextField inputField;
	@FXML
	private Button searchButton;
	@FXML
	private Label predictionLabel, temperatureLabel;
	@FXML
	private ImageView weatherIconContainer, thermometerIconContainer;
		
	private String currTemperature;
	private double currGeocode;
	
	
	public void retrieveData()
	{
		try 
		{
			if(validDataFromRequests()) //if everything goes well then we can specify the weather otherwise show not found
		    	specifyWeather();  
		} 
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	//this method is making requests to apis and is setting some attributes 
	//of the Controller class. If everything is ok it returns true so data are valid
	//but if something goes wrong with the data that retrieved from the apis
	//it returns false. It also throws some exceptions
	private boolean validDataFromRequests() throws URISyntaxException, IOException, InterruptedException
	{
		HttpRequest request;
		HttpClient client;
		HttpResponse<String> response;
		
		String inputCity = inputField.getText().toUpperCase().replace(" ", "+");
		
		if(inputCity.isBlank() || inputCity.isEmpty())
		{
			setEmptyToTheScreen();
			return false;
		}
		
		
		String geocodingUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + inputCity + "&count=10&language=en&format=json";
		
		request = HttpRequest.newBuilder()
							 .uri(new URI(geocodingUrl))
							 .build();
		
		client = HttpClient.newHttpClient();
		response = client.send(request, BodyHandlers.ofString());
		
		if(!isGeocodingResponseOk(response))
		{
			setEmptyToTheScreen();
			return false;
		}
		
		/*
		 * {"results":[{"id":2950159,"name":"Berlin","latitude":52.52437,"longitude":13.41053,"elevation":74.0,"feature_code":"PPLC","country_code":"DE","admin1_id":2950157,"admin3_id":6547383,"admin4_id":6547539,"timezone":"Europe/Berlin","population":3426354,"postcodes":["10967","13347"],"country_id":2921044,"country":"Germany","admin1":"Land Berlin","admin3":"Berlin, Stadt","admin4":"Berlin"},{"id":5083330,"name":"Berlin","latitude":44.46867,"longitude":-71.18508,"elevation":311.0,"feature_code":"PPL","country_code":"US","admin1_id":5090174,"admin2_id":5084973,"admin3_id":5083340,"timezone":"America/New_York","population":9367,"postcodes":["03570"],"country_id":6252001,"country":"United States","admin1":"New Hampshire","admin2":"Coos","admin3":"City of Berlin"},{"id":4500771,"name":"Berlin","latitude":39.79123,"longitude":-74.92905,"elevation":50.0,"feature_code":"PPL","country_code":"US","admin1_id":5101760,"admin2_id":4501019,"admin3_id":4500776,"timezone":"America/New_York","population":7590,"postcodes":["08009"],"country_id":6252001,"country":"United States","admin1":"New Jersey","admin2":"Camden","admin3":"Borough of Berlin"},{"id":5245497,"name":"Berlin","latitude":43.96804,"longitude":-88.94345,"elevation":246.0,"feature_code":"PPL","country_code":"US","admin1_id":5279468,"admin2_id":5255015,"admin3_id":5245510,"timezone":"America/Chicago","population":5420,"postcodes":["54923"],"country_id":6252001,"country":"United States","admin1":"Wisconsin","admin2":"Green Lake","admin3":"City of Berlin"},{"id":4348460,"name":"Berlin","latitude":38.32262,"longitude":-75.21769,"elevation":11.0,"feature_code":"PPL","country_code":"US","admin1_id":4361885,"admin2_id":4374180,"timezone":"America/New_York","population":4529,"postcodes":["21811"],"country_id":6252001,"country":"United States","admin1":"Maryland","admin2":"Worcester"},{"id":4930431,"name":"Berlin","latitude":42.3812,"longitude":-71.63701,"elevation":100.0,"feature_code":"PPL","country_code":"US","admin1_id":6254926,"admin2_id":4956199,"admin3_id":4930436,"timezone":"America/New_York","population":2422,"postcodes":["01503"],"country_id":6252001,"country":"United States","admin1":"Massachusetts","admin2":"Worcester","admin3":"Town of Berlin"},{"id":4556518,"name":"Berlin","latitude":39.92064,"longitude":-78.9578,"elevation":710.0,"feature_code":"PPL","country_code":"US","admin1_id":6254927,"admin2_id":5212857,"admin3_id":4556520,"timezone":"America/New_York","population":2019,"postcodes":["15530"],"country_id":6252001,"country":"United States","admin1":"Pennsylvania","admin2":"Somerset","admin3":"Borough of Berlin"},{"id":4557666,"name":"East Berlin","latitude":39.9376,"longitude":-76.97859,"elevation":131.0,"feature_code":"PPL","country_code":"US","admin1_id":6254927,"admin2_id":4556228,"admin3_id":4557667,"timezone":"America/New_York","population":1534,"postcodes":["17316"],"country_id":6252001,"country":"United States","admin1":"Pennsylvania","admin2":"Adams","admin3":"Borough of East Berlin"},{"id":5147132,"name":"Berlin","latitude":40.56117,"longitude":-81.7943,"elevation":391.0,"feature_code":"PPL","country_code":"US","admin1_id":5165418,"admin2_id":5157783,"admin3_id":5147154,"timezone":"America/New_York","population":898,"postcodes":["44610"],"country_id":6252001,"country":"United States","admin1":"Ohio","admin2":"Holmes","admin3":"Berlin Township"},{"id":1510159,"name":"Berlin","latitude":54.00603,"longitude":61.19308,"elevation":228.0,"feature_code":"PPL","country_code":"RU","admin1_id":1508290,"admin2_id":1489213,"timezone":"Asia/Yekaterinburg","population":613,"postcodes":["457130"],"country_id":2017370,"country":"Russia","admin1":"Chelyabinsk","admin2":"Troitskiy Rayon"}],"generationtime_ms":1.1230707}
		 * I have a json object (specified with {}). I get the value of "results" 
		 * which is a json array. I want to get the first element of this array specified by 0 index.
		 * Now I get it as json object. I want to get the value of "latitude"
   		 * JsonArray has JsonElements inside.
		 */
		
		double latitude = Double.parseDouble(JsonParser.parseString(response.body())
												.getAsJsonObject()
												.get("results")
												.getAsJsonArray()
												.get(0)
												.getAsJsonObject()
												.get("latitude")
												.toString()
									 		);
		
		double longitude = Double.parseDouble(JsonParser.parseString(response.body())
												 .getAsJsonObject()
											   	 .get("results")
												 .getAsJsonArray()
												 .get(0)
												 .getAsJsonObject()
												 .get("longitude")
												 .toString()
									   		);
		
		String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude + "&hourly=temperature_2m,weathercode&timezone=auto&forecast_days=1";
		
		request = HttpRequest.newBuilder()
							 .uri(new URI(weatherUrl))
							 .build();
		
		client = HttpClient.newHttpClient();
		response = client.send(request, BodyHandlers.ofString());
		
		int index = findIndex(response);
		
		JsonArray temperatures = JsonParser.parseString(response.body())
										   .getAsJsonObject()
										   .get("hourly")
										   .getAsJsonObject()
										   .get("temperature_2m")
										   .getAsJsonArray();
		
		currTemperature = temperatures.get(index).toString();
		
		JsonArray geocodes = JsonParser.parseString(response.body())
									   .getAsJsonObject()
									   .get("hourly")
									   .getAsJsonObject()
									   .get("weathercode")
									   .getAsJsonArray();
		
		currGeocode = Double.parseDouble(geocodes.get(index).toString());
				
		return true;
	}
	
	
	//each element inside the time JsonArray is like
	//"2023-10-13T12:00" so I want to take only the 2 digits after the T
	//and compare it with the current time
	private int findIndex(HttpResponse<String> response)
	{
		int index = 0;
		
		JsonArray time = JsonParser.parseString(response.body())
								   .getAsJsonObject()
								   .get("hourly")
								   .getAsJsonObject()
								   .get("time")
								   .getAsJsonArray();
		
		String currTime = Integer.toString(LocalDateTime.now().getHour());
		for(JsonElement o : time)
		{
			if(o.toString().substring(12, 14).equals(currTime))
				return index;
			else
				index++;
		}
		
		return -1;
	}

	
	
	private boolean isGeocodingResponseOk(HttpResponse<String> response)
	{
		return response.body().contains("results"); //if json does not contain the key "results" then there is no such city in the api
	}
	
	
	private void specifyWeather()
	{
		
		if(currGeocode <= 1)
			setContentToTheScreen("/widgets/sun.png", "Clear");
		else if(currGeocode <= 3)
			setContentToTheScreen("/widgets/cloud.png", "Cloudy");
		else if(currGeocode <= 48)
			setContentToTheScreen("/widgets/fog.png", "Fog");
		else if(currGeocode <= 57)
			setContentToTheScreen("/widgets/drizzle.png", "Drizzle");
		else if(currGeocode <= 67 || (currGeocode >= 80 && currGeocode <= 82))
			setContentToTheScreen("/widgets/rain.png", "Rain");
		else if(currGeocode <= 77 || (currGeocode >= 85 && currGeocode <= 86))
			setContentToTheScreen("/widgets/snow.png", "Snow");
		else if(currGeocode <= 99)
			setContentToTheScreen("/widgets/thunderstorm.png", "Thunderstorm");	
		
	}
	
	
	private void setContentToTheScreen(String imageName, String cast)
	{
		predictionLabel.setText(cast);
		weatherIconContainer.setImage(new Image(imageName));
		thermometerIconContainer.setImage(new Image("/widgets/thermometer.png"));
		temperatureLabel.setText(this.currTemperature + "°C"); //alt + 0176(in the num pad)
	}
	
	
	private void setEmptyToTheScreen()
	{
		inputField.setText("Not found :( Search something else!");
		predictionLabel.setText("");
		temperatureLabel.setText("");
		weatherIconContainer.setImage(new Image("/widgets/empty.png"));
		thermometerIconContainer.setImage(new Image("/widgets/empty.png"));
	}
}
