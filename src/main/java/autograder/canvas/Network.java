package autograder.canvas;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import autograder.configuration.Configuration;

public class Network {
	
	private static final Logger LOGGER = Logger.getLogger(Network.class.getName()); 
	private static final String BASE_URL = "https://utah.instructure.com/api/v1/";
	protected static final String token = Configuration.getConfiguration().canvasToken;
	
	protected static <E extends BaseResponse> E httpPostCall(String url, String contentType, HttpEntity data, Class<E> responseClass) {
        HttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost(BASE_URL + url);
        configureRequest(request);
        request.addHeader("Content-type","application/json");
        String responseAsJSONString;
        request.setEntity(data);
        try {
            responseAsJSONString = validateResponse(client.execute(request));
        } catch (IOException e) {
        	LOGGER.severe(e.getMessage());
        	responseAsJSONString = String.format("{\"appError\" : \"%s\"}", e.getMessage());
        }
        Gson gson = new Gson();
        return gson.fromJson(responseAsJSONString, responseClass);
    }
	
	protected static <T extends BaseResponse> T httpGetCall(String url, Class<T> responseClass) {
		HttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet();
		configureRequest(request);
		String jsonString = null;
		try {
			HttpResponse httpResponse = client.execute(request);
			jsonString = validateResponse(httpResponse);
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
		Gson gson = new Gson();
		return gson.fromJson(jsonString, responseClass);
	}

	private static void configureRequest(HttpRequest request) { 
		request.addHeader("Authorization", "Bearer " + token);
	}
	
	private static String validateResponse(HttpResponse response) throws ParseException, IOException {
		int statusCode = response.getStatusLine().getStatusCode();
		if(statusCode == 200 ){
			return EntityUtils.toString(response.getEntity());
		}
		return "{}"; // return empty json object
	}
}
