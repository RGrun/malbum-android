package guru.furu.malbum_android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.net.ssl.HttpsURLConnection;

import guru.furu.malbum_android.model.AlbumPhoto;
import guru.furu.malbum_android.model.Comment;
import guru.furu.malbum_android.model.MalbumUser;
import guru.furu.malbum_android.model.UserAlbum;

/**
 * This class connects to the server specified in the login activity.
 */
public class ServerConnect {

    private static final String DEBUG = "ServerConnect";

    // These are for the POST form parameters
    private static final String USERNAME = "uname";
    private static final String PASSWORD = "pwd";
    private static final String API_KEY = "key";


    // TODO: remove default port (and make user enter it as part of the hostname)?
    public static final String DEFAULT_PORT = ":3000";


    /*
     *   LOGIN METHODS
     */

    // returns null object upon failed connection
    public static MalbumUser attemptLogin(String hostname, String username, String password)
        throws JSONException, IOException{


        URL endpoint = new URL("http://" + hostname + DEFAULT_PORT + "/api/login");

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

            return parseLogin(jsonString, hostname);

        } else {

            // TODO: make this throw an exception instead?
            return null;

        }

    }

    // JSON parsing
    private static MalbumUser parseLogin(String jsonString, String hostname)
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

    private static List<UserAlbum> parseAlbums(String hostname, String jsonString) {

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

                    String thumbPath = "http://" + hostname + DEFAULT_PORT +
                            "/img/" + uname + "/" + thumbName;

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

    public static List<UserAlbum> fetchAlbums(String hostname, String apiKey)
            throws JSONException, IOException{


        HashMap<String, String> params = new HashMap<>();

        params.put(API_KEY, apiKey);

        URL url = buildURL(hostname, "albums", params);

        Log.d(DEBUG + "plpi", url.toString());

        try {

            byte[] buffer = getUrlBytes(url.toString());

            Log.d(DEBUG + "fetchAlbum", new String(buffer));


            List<UserAlbum> photos = parseAlbums(hostname, new String(buffer));


            return photos;

        } catch (IOException ioe) {
            Log.e(DEBUG, "IO Error.");
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        // will never reach here
        return null;

    }

    /*
     *   SINGLE USER ALBUM METHODS
     *
     */

    public static List<AlbumPhoto> getPhotosForUser(String hostname, String apiKey, String username)
            throws JSONException, IOException {


        HashMap<String, String> params = new HashMap<>();

        params.put(API_KEY, apiKey);

        URL url = buildURL(hostname, "photos-for-user", params, username);

        Log.d(DEBUG + "pfu", url.toString());

        try {

            byte[] buffer = getUrlBytes(url.toString());


            List<AlbumPhoto> photos = parseAlbumPhotos(hostname, new String(buffer));


            return photos;

        } catch (IOException ioe) {
            Log.e(DEBUG, "IO Error.");
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        // will never reach here
        return null;

    }

    private static List<AlbumPhoto> parseAlbumPhotos(String hostname, String jsonString) {

        List<AlbumPhoto> albums = new ArrayList<>();

        Log.d(DEBUG, jsonString);

        try {
            JSONObject json = new JSONObject(jsonString);

            String status = json.getString("status");

            if(status.equals("ok")) {
                JSONArray photos = json.getJSONArray("photos");

                for(int i = 0; i < photos.length(); i++) {
                    JSONObject photo = photos.getJSONObject(i);


                    AlbumPhoto album = new AlbumPhoto(hostname, photo);

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


    /*
     *   PHOTO INFORMATION METHODS
     */

    public static AlbumPhoto getPhotoInformation(String hostname, String apiKey, String photoId) {

        HashMap<String, String> params = new HashMap<>();

        params.put("key", apiKey);
        params.put("photo_id", photoId);

        URL url = buildURL(hostname, "photo-information", params);

        try {
            assert url != null;
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            BufferedInputStream in = new BufferedInputStream(con.getInputStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            if(con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(DEBUG, "HTTP error.");
            }


            int bytesRead = 0;
            byte [] buffer = new byte[2048];

            while((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();
            con.disconnect();

            AlbumPhoto photo = parsePhotoInformation(hostname, new String(buffer));

            // grab the image itself
            byte[] photoBytes = getUrlBytes(photo.getFullImageURL());

            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);

            photo.setPhoto(bitmap);

            return photo;

        } catch (IOException ioe) {
            Log.e(DEBUG, "IO Error.");
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        // will never reach here
        return null;

    }

    private static AlbumPhoto parsePhotoInformation(String hostname, String json) {

        try {
            JSONObject root = new JSONObject(json);

            String status = root.getString("status");

            if(status.equals("ok")) {
                JSONObject photoData = root.getJSONObject("photo");

                JSONArray comments = root.getJSONArray("comments");

                List<Comment> commentsList = new ArrayList<>();

                for (int i = 0; i < comments.length(); i++) {
                    JSONObject comment = comments.getJSONObject(i);

                    String uname = comment.getString("uname");
                    String date = comment.getString("date");
                    String cmt = comment.getString("comment");
                    int userId = comment.getInt("user_id");
                    int photoId = comment.getInt("photo_id");
                    int commentId = comment.getInt("comment_id");

                    commentsList.add(new Comment(uname, date, cmt, userId, photoId, commentId));

                }

                return new AlbumPhoto(hostname, photoData, commentsList);

            } else {
                Log.e(DEBUG, "API returned failure result.");
            }

        } catch (JSONException joe) {
            Log.e(DEBUG, "Error parsing json.");
            joe.printStackTrace();
        }

        // will never reach here
        return null;
    }

     /*
     *   LATEST PHOTOS METHODS
     */

    public static List<AlbumPhoto> getLatestPhotos(String hostname, String apikey) {
        return getLatestPhotos(hostname, apikey, 0, 5);
    }

    public static List<AlbumPhoto> getLatestPhotos(String hostname, String apiKey,
                                                   int start, int end) {
        HashMap<String, String> params = new HashMap<>();

        params.put("key", apiKey);
        params.put("start", "" + start);
        params.put("end", "" + end);

        URL url = buildURL(hostname, "latest-images", params);

        Log.d(DEBUG + "plpi", url.toString());

        try {

            byte[] buffer = getUrlBytes(url.toString());


            List<AlbumPhoto> photos = parseLatestPhotoInformation(hostname, new String(buffer));


            return photos;

        } catch (IOException ioe) {
            Log.e(DEBUG, "IO Error.");
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        // will never reach here
        return null;
    }

    private static List<AlbumPhoto> parseLatestPhotoInformation(String hostname, String json) {

        List<AlbumPhoto> photosToReturn = new ArrayList<>();

        try {

            Log.d(DEBUG, json);

            JSONObject root = new JSONObject(json);

            String status = root.getString("status");

            if(status.equals("ok")) {
                JSONArray photos = root.getJSONArray("photos");

                for(int i = 0; i < photos.length(); i++) {
                    JSONObject photo = photos.getJSONObject(i);

                    AlbumPhoto p = new AlbumPhoto(hostname, photo);

                    // grab the image
                    //byte[] photoBytes = getUrlBytes(p.getFullImageURL());

                    //Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);

                    //p.setPhoto(bitmap);

                    photosToReturn.add(p);
                }

                return photosToReturn;

            } else {
                Log.e(DEBUG, "HTTP Failure when downloading latest images.");
            }

        } catch (JSONException joe) {
            Log.e(DEBUG, "Error parsing photo information json.");
            joe.printStackTrace();
        }

        // will never reach here
        return null;

    }

    /*
     *   MISC METHODS
     */

    // grabs raw bytes from the specified URL
    public static byte[] getUrlBytes(String urlSpec) throws IOException {
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


    private static String getPostDataString(HashMap<String, String> params)
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


    private static URL buildURL(String hostname, String endpoint, HashMap<String, String> params) {
        StringBuilder output = new StringBuilder();

        String tmp = "http://" + hostname + DEFAULT_PORT + "/api/" + endpoint + "?";

        output.append(tmp);
        try {
            output.append(getPostDataString(params));

            return new URL(output.toString());

        } catch (UnsupportedEncodingException uee) {
            Log.e(DEBUG, "Error with params hashmap.");
            uee.printStackTrace();
        } catch (MalformedURLException mue) {
            Log.e(DEBUG, "Error with URL.");
            mue.printStackTrace();
        }

        // should never reach here
        return null;
    }

    // version for "photos-for-user"
    private static URL buildURL(String hostname, String endpoint,
                                HashMap<String, String> params,
                                String username) {

        StringBuilder output = new StringBuilder();

        String tmp = "http://" + hostname + DEFAULT_PORT + "/api/" + endpoint +
                "/" + username + "?";

        output.append(tmp);
        try {
            output.append(getPostDataString(params));

            return new URL(output.toString());

        } catch (UnsupportedEncodingException uee) {
            Log.e(DEBUG, "Error with params hashmap.");
            uee.printStackTrace();
        } catch (MalformedURLException mue) {
            Log.e(DEBUG, "Error with URL.");
            mue.printStackTrace();
        }

        // should never reach here
        return null;
    }

    public static boolean postComment(String comment, MalbumUser user, String photoId) {

        try {
            URL endpoint = new URL("http://" + user.getHostname() + DEFAULT_PORT +
                    "/api/new-comment");

            HashMap<String, String> postParams = new HashMap<>();

            postParams.put("key", user.getApi_key());
            postParams.put("photo_id", photoId);
            postParams.put("comment", comment);

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

                JSONObject json = new JSONObject(jsonString);

                return json.getString("status").equals("ok");

            } else {

                // TODO: make this throw an exception instead?
                return false;

            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // will never reach here
        return false;

    }
}
