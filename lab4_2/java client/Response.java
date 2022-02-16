import org.json.JSONArray;
import org.json.JSONObject;

public class Response {
	public final int status_code;
	public final String content;
	public JSONObject json = null;
	
	Response(int code) {
		this.status_code = code;
		this.content = null;
	}
	
	Response(int code, String content) {
		this.status_code = code;
		this.content = content;
	}
	
	private void getJson() {
		if (content == null) {
			json = new JSONObject();
		} else {
			json = new JSONObject(content);
		}
	}

	public int getInt(String key) {
		if (json == null) getJson();
		return json.getInt(key);
	}
	
	public String getString(String key) {
		if (json == null) getJson();
		return json.getString(key);
	}
	
	public boolean getBoolean(String key) {
		if (json == null) getJson();
		return json.getBoolean(key);
	}
	
	public JSONArray getArray(String key) {
		if (json == null) getJson();
		return json.getJSONArray(key);
	}
}
