package com.sample;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.SignatureType;
import org.scribe.model.Verb;
import org.scribe.utils.OAuthEncoder;


public class LinkedinExample {

	private static final String AUTHORIZE_URL = "https://www.linkedin.com/uas/oauth2/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=DCEEFWF45453sdffef424";
	private static final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&scope=%s";
	private static final String ACCESS_TOKEN_ENDPOINT = "https://www.linkedin.com/uas/oauth2/accessToken";
	private static final int MAX_NUM_PROFILES = 25;
	private static final int SUCCESS_CODE=200;
	BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));


	public String getAccessTokenEndpoint() {
		return ACCESS_TOKEN_ENDPOINT;
	}

	public String getAuthorizationUrl(OAuthConfig config) {
		// Append scope if present
		if (config.hasScope()) {
			return String.format(SCOPED_AUTHORIZE_URL, config.getApiKey(),
					OAuthEncoder.encode(config.getCallback()),
					OAuthEncoder.encode(config.getScope()));
		} else {
			return String.format(AUTHORIZE_URL, config.getApiKey(),
					OAuthEncoder.encode(config.getCallback()));
		}
	}

	public Verb getAccessTokenVerb() {
		return Verb.POST;
	}
	String api_key = "";
	String secret_key = "";
	OAuthConfig config = null;// new OAuthConfig("kpc096l394ot", "0GJdvrYi84Z1k5rB", "http://localhost:10000/", SignatureType.Header, "r_fullprofile r_emailaddress r_network", null);
	private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
	private static final String GRANT_TYPE = "grant_type";

	private void init(){
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter api-key : ");
		api_key = sc.next();
		System.out.println("Enter secret-key : ");
		secret_key = "sc.next();
		config = new OAuthConfig(api_key, secret_key, "http://localhost:10000/", SignatureType.Header, "r_fullprofile r_emailaddress r_network", null);
	}

	public static void main(String a[]) throws Exception{
		LinkedinExample ob = new LinkedinExample();
		ob.init();
		String reqToken = ob.getRequestToken();

		String accessToken = ob.getAccessToken(reqToken);

		//uncomment below to see simple API calls
		ob.testPerson(accessToken); 
		
		System.out.println("Enter some keyword to search people");
		String keywords = ob.bufferRead.readLine();
		//below will print first name, last name, headline of people matching given keywords.
		ob.searchByKeyWords(keywords, accessToken);
	}

	public  String  getRequestToken(){
		String authrozationUrl = getAuthorizationUrl(config);
		String accessToken = null;

		Desktop d=Desktop.getDesktop(); 
		try {
			com.proxy.ProxyServer ps = new com.proxy.ProxyServer(null);
			ps.start();
			d.browse(new URI(authrozationUrl));
			try {
				ps.join();
				accessToken = ps.accessToken;
				System.out.println("AccessToken: " + ps.accessToken);
				System.out.println("State: " + ps.state);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return accessToken;
	}


	public String getAccessToken(String reqTok) throws Exception{

		OAuthRequest request = new OAuthRequest(getAccessTokenVerb(), getAccessTokenEndpoint());
		request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);        
		request.addBodyParameter(OAuthConstants.CODE, reqTok);
		request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
		request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
		request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
		System.out.println("redirect uri: "+config.getCallback());
		System.out.println("config api key: "+config.getApiKey());
		System.out.println("config api secret: "+config.getApiSecret());
		Response response = request.send();
		checkResponse(response);
		System.out.println(response.getBody());
		String json[] = response.getBody().split(":"); //extract access token from json
		String accessToken = json[2].substring(1,json[2].length()-2);
		return accessToken;		
	}

	public void testPerson(String accessToken) throws Exception{
		System.out.println();
		System.out.println();
		System.out.println("********A basic user profile call********");

		String url = "https://api.linkedin.com/v1/people/~";
		//getting your own profile
		OAuthRequest request = new OAuthRequest(Verb.GET, url);
		request.addQuerystringParameter("oauth2_access_token", accessToken );
		// send the request and get the response
		Response response = request.send();
		checkResponse(response);
		// print out the response body
		System.out.println("Response in XML: "+response.getBody());

		//reading data
		System.out.println("Sample connection's api");
		System.out.println("Now printing top 10 connections of you");
		//get your first ten connections in json
		url=url+"/connections?count=10";
		request = new OAuthRequest(Verb.GET, url);
		request.addQuerystringParameter("oauth2_access_token", accessToken );		
		request.addHeader("x-li-format", "json");
		response = request.send();
		checkResponse(response);
		System.out.println("response in json: "+response.getBody());

		//profile fields
		System.out.println("Enter some member id received from connections api - also use field selectors to select first and last name");
		String memberId = bufferRead.readLine();
		url = "https://api.linkedin.com/v1/people/id="+memberId+":(first-name,last-name)";
		request = new OAuthRequest(Verb.GET,url);
		request.addQuerystringParameter("oauth2_access_token", accessToken );
		response = request.send();
		checkResponse(response);
		System.out.println("Basic profile of "+memberId);
		System.out.println(response.getBody());

		//people search
		System.out.println("searching people from first name");
		System.out.println("Enter a first name to search: ");
		String firstName = bufferRead.readLine();
		url = "https://api.linkedin.com/v1/people-search?first-name="+firstName;
		request = new OAuthRequest(Verb.GET,url);
		request.addQuerystringParameter("oauth2_access_token", accessToken );
		response = request.send();
		checkResponse(response);
		System.out.println("search results for people with first name: "+firstName);
		System.out.println(response.getBody());		

	}
	
	public void checkResponse(Response response)throws Exception{
		if(response.getCode() != SUCCESS_CODE){
			throw new Exception("Some error occured !\n" + response.getBody());
		}
	}

	public void searchByKeyWords(String keywords, String accessToken) throws Exception{
		System.out.println("Change request 1");		

		//1. get total number of visible profiles for given keyword.
		String url = "https://api.linkedin.com/v1/people-search:(people:(id,first-name,last-name,headline),num-results)?keywords="+OAuthEncoder.encode(keywords);
		OAuthRequest request = new OAuthRequest(Verb.GET,url);
		request.addQuerystringParameter("oauth2_access_token", accessToken );
		request.addHeader("x-li-format", "json");
		Response response = request.send();
		checkResponse(response);

		JSONObject obj = new JSONObject(response.getBody());
		JSONObject people = obj.getJSONObject("people");
		Long _total = people.getLong("_total"); //total number of visible profiles for given keyword
		System.out.println("Total number of visible profiles for given keyword: "+_total);
		System.out.println("people object: " + people);
		System.out.println();

		for(int i=0;i<=_total;i=i+MAX_NUM_PROFILES){
			System.out.println("Printing from "+i + " to " + (i+MAX_NUM_PROFILES));
			//2. Paginate through total profiles
			url = "https://api.linkedin.com/v1/people-search:(people:(id,first-name,last-name,headline),num-results)?keywords="+OAuthEncoder.encode(keywords)+"&start="+i+"&count="+(MAX_NUM_PROFILES);
			request = new OAuthRequest(Verb.GET,url);
			request.addQuerystringParameter("oauth2_access_token", accessToken );
			request.addHeader("x-li-format", "json");
			response = request.send();
			checkResponse(response);

			obj = new JSONObject(response.getBody());
			people = obj.getJSONObject("people");
			JSONArray featuresArr = people.getJSONArray("values");
			for (int j=0; j<featuresArr.length(); j++)
			{
				System.out.print((i+j));
				JSONObject person = featuresArr.getJSONObject(j);
				String first_name = "", last_name="", headline = "";
				try{
					first_name = person.getString("firstName"); //first name
					last_name = person.getString("lastName"); //last name
					headline = person.getString("headline"); //last name					
				}catch(JSONException e){
					//ignore if any field is not available
				}
				System.out.println(first_name +","+last_name+","+headline);
			}
		}
	}
}
