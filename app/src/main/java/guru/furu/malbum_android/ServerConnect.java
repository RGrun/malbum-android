package guru.furu.malbum_android;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * This class connects to the server specified in the login activity.
 */
public class ServerConnect {

    private static final String DEBUG = "ServerConnect";

    // These are for the POST form parameters
    private static final String USERNAME = "uname";
    private static final String PASSWORD = "pwd";
    private static final String API_KEY = "key";

    private String hostname;
    private String username;
    private String password;
    private String apiKey;

    private URL endpoint;


    // if this constructor is used, we assume this object will be used for
    // logging in to the server.
    public ServerConnect(String hostname, String username, String password) {

        this.hostname = hostname;
        this.username = username;
        this.password = password;

    }

    // this constructor is for more general queries.
    public ServerConnect(String hostname, String key) {
        this.hostname = hostname;
        this.apiKey = key;
    }

    // this constructor is for using getUrlBytes()
    public ServerConnect() {}


    /*
     *   LOGIN METHODS
     */

    // returns null object upon failed connection
    public MalbumUser attemptLogin()
        throws JSONException, IOException{


        endpoint = new URL("http://" + hostname + "/api/login");

        HashMap<String, String> postParams = new HashMap<>();

        postParams.put(USERNAME, username);
        postParams.put(PASSWORD, password);

        HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
        conn.setReadTimeout(15000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);


        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(getPostDataString(postParams));

        writer.flush();
        writer.close();
        os.close();

        int responseCode = conn.getResponseCode();

        if (responseCode == HttpsURLConnection.HTTP_OK) {

            InputStream in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int bytesRead = 0;
            byte [] buffer = new byte[1024];

            while((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();
            conn.disconnect();

            String jsonString = new String(out.toByteArray());

            return parseLogin(jsonString);

        } else {

            // TODO: make this throw an exception instead?
            return null;

        }

    }

    // JSON parsing
    private MalbumUser parseLogin(String jsonString)
            throws IOException, JSONException {

        JSONObject jsonBody = new JSONObject(jsonString);

        String status = jsonBody.getString("status");

        if(status.equals("ok")) {
            JSONObject data = jsonBody.getJSONObject("data");

            return new MalbumUser(data, hostname);

        } else {
            throw new JSONException("Error parsing JSON");
        }

    }

    /*
     *   GET LATEST ALBUMS METHODS
     */



    private List<UserAlbum> parseAlbums(String jsonString) {

        List<UserAlbum> albums = new ArrayList<>();

        Log.d(DEBUG, jsonString);

        try {
            JSONObject json = new JSONObject(jsonString);

            String status = json.getString("status");

            if(status.equals("ok")) {
                JSONArray thumbs = json.getJSONArray("thumbs");

                for(int i = 0; i < thumbs.length(); i++) {
                    JSONObject thumb = thumbs.getJSONObject(i);

                    String uname = thumb.getString("uname");
                    String thumbName = thumb.getString("thumb_name");

                    String thumbPath = "http://" + hostname + "/img/" + uname + "/" + thumbName;

                    UserAlbum album = new UserAlbum(uname, thumbPath);

                    albums.add(album);

                }


                return albums;

            } else {

                //TODO: throw exception instead?
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //TODO: throw exception instead?
        return null;
    }

    public List<UserAlbum> fetchAlbums()
            throws JSONException, IOException{


        endpoint = new URL("http://" + hostname + "/api/albums");

        HashMap<String, String> postParams = new HashMap<>();

        postParams.put(API_KEY, apiKey);

        HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
        conn.setReadTimeout(15000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);


        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(getPostDataString(postParams));

        writer.flush();
        writer.close();
        os.close();

        int responseCode = conn.getResponseCode();

        if (responseCode == HttpsURLConnection.HTTP_OK) {

            InputStream in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int bytesRead = 0;
            byte [] buffer = new byte[1024];

            while((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();
            conn.disconnect();

            String jsonString = new String(out.toByteArray());

            return parseAlbums(jsonString);

        } else {

            // TODO: make this throw an exception instead?
            return null;

        }

    }


    /*
     *   MISC METHODS
     */

    // grabs raw bytes from the specified URL
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);

        Log.d(DEBUG, "Connecting to: " + url);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }

            int bytesRead = 0;
            byte [] buffer = new byte[1024];

            while((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();
            return out.toByteArray();

        } finally {
            connection.disconnect();
        }

    }


    private String getPostDataString(HashMap<String, String> params)
            throws UnsupportedEncodingException {

        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
