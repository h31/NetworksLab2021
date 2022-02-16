import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class Requests {
	final static int GET = 0;
	final static int POST = 1;
	final private String addr;
	
	final private CloseableHttpClient httpClient = HttpClients.custom()
			.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
	        .build();
	
	Requests(String addr) {
		this.addr = addr;
	}
	
	public Response make(int response_type, String route,
			List<BasicNameValuePair> requestArgs) {
		HttpResponse response;
		if (response_type == Requests.POST)
			response = makePostRequest(route, requestArgs);
		else
			response = makeGetRequest(route, requestArgs);
		
		try {
			if (response.getEntity() == null) {
				return new Response(response.getStatusLine().getStatusCode());
			} else {
				return new Response(response.getStatusLine().getStatusCode(),
						EntityUtils.toString(response.getEntity()));
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	public void close() {
		try {
			httpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private HttpResponse makeGetRequest(String route, List<BasicNameValuePair> params) {
		String url = addr + route;
		if (params != null)
			url += "?" + URLEncodedUtils.format(params, "utf-8");
		
		try {
			HttpGet httpGet = new HttpGet(url);
			return httpClient.execute(httpGet);
		} catch (Exception e) {
			return null;
		}
	}
	
	private HttpResponse makePostRequest(String route, 
			List<BasicNameValuePair> requestArgs) {
		try {
			HttpPost httpPost = new HttpPost(addr + route);
			if (requestArgs != null)
				httpPost.setEntity(new UrlEncodedFormEntity(requestArgs));
			return httpClient.execute(httpPost);
		} catch (Exception e) {
			return null;
		}
	}
}
