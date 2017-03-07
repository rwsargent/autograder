package autograder.canvas;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

import autograder.Constants;
import autograder.configuration.Configuration;


/**
 * The basis Network code. You should not use these calls directly, but wrap them in a Connection class that subclasses Network.
 * Feel free to had any missing endpoints. Currently only GET and POST are implemented.
 * 
 * Given a URL and a response class, Network will try to parse the response as JSON and map it to the supplied response class. 
 * @author Ryan
 *
 */
public abstract class Network {

	@Inject protected Configuration configuration;
	
	private static final Logger LOGGER = Logger.getLogger(Network.class.getName()); 
	public static final String BASE_URL = "https://utah.instructure.com/api/v1/";
	protected String token = configuration.canvasToken;
	
	public byte[] downloadFile(String url) {
		HttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet(url);
		try {
			HttpResponse response = client.execute(request);
			return EntityUtils.toByteArray(response.getEntity());
		} catch (IOException e) {
			return new byte[0];
		}
	}
	
	protected <E> E httpPostCall(String url, String contentType, HttpEntity data, Class<E> responseClass) {
        HttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost(BASE_URL + url);
        configureRequest(request);
        request.addHeader("Content-type","application/json");
        String responseAsJSONString = null;
        request.setEntity(data);
        try {
        	client.execute(request);
//            responseAsJSONString = validResponse());
        } catch (IOException e) {
        	LOGGER.severe(e.getMessage());
        	responseAsJSONString = String.format("{\"appError\" : \"%s\"}", e.getMessage());
        }
        Gson gson = new GsonBuilder().setDateFormat("YYYY-MM-ddTHH:mm:ss-zzz").create();
        return gson.fromJson(responseAsJSONString, responseClass);
    }
	
	protected <T> T httpGetCall(String url, Class<T> responseClass) {
		HttpClient client = HttpClients.createDefault();
		Gson gson = new GsonBuilder().setDateFormat("YYYY-MM-dd'T'HH:mm:ss-zzz").create();
		HttpGet request = new HttpGet(BASE_URL + url);
		configureRequest(request);
		StringBuilder sb = new StringBuilder();
		httpGetRecur(client, request, sb);
		if(sb.length() == 0) {
			sb.append("{ \"appError\" : \"Something went wrong\" }"); // make an empty object
		}
		String jsonString = sb.toString().replaceAll("\\]\\s*\\[", ",");
		return gson.fromJson(jsonString, responseClass);
	}
	
	private void httpGetRecur(HttpClient client, HttpGet request, StringBuilder responseStringBuilder) {
		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(request);
			if(validResponse(httpResponse)) {
				responseStringBuilder.append(EntityUtils.toString(httpResponse.getEntity()));
				Header[] headers = httpResponse.getHeaders("Link");
				Pattern regex = Pattern.compile(Constants.LINK_REGEX);
				for(Header header : headers) {
					Matcher matcher = regex.matcher(header.getValue());
					if(matcher.find()) {
						request.setURI(new URI(matcher.group(1)));
						httpGetRecur(client, request, responseStringBuilder);
					}
				}
			} else {
				responseStringBuilder.append(buildErrorMessage(httpResponse));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private String buildErrorMessage(HttpResponse httpResponse) {
		return String.format("{\"appError\" : \"Error code is: %d\" }", httpResponse.getStatusLine().getStatusCode());
	}

	protected <Response> Response executeRequest(HttpRequestBase request, Class<Response> responseClazz) {
		HttpClient httpClient = HttpClients.createDefault();
		try {
			HttpResponse httpResponse = httpClient.execute(request);
			validResponse(httpResponse);
			Header[] headers = httpResponse.getHeaders("Link");
			for(Header header : headers) {
				System.out.println(header.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void configureRequest(HttpRequest request) { 
		request.addHeader("Authorization", "Bearer " + token);
	}
	
	private boolean validResponse(HttpResponse response) throws ParseException, IOException {
		return response.getStatusLine().getStatusCode() == 200;
	}
}
