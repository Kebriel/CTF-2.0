package kebriel.ctf.internal;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import kebriel.ctf.Constants;
import kebriel.ctf.util.CTFLogger;
import kebriel.ctf.util.JavaUtil;
import org.apache.commons.io.IOUtils;
import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Rudimentary class purposed for the querying of Mojang's database -- at present, used primarily
 * for the stats command
 *
 * Implements a basic RateLimiter so that Mojang's query limit isn't reached
 */
public class APIQuery {

    public enum QueryType {
        USERNAME_VALID, UUID_FROM_USERNAME, SKIN
    }

    private static final JSONParser parser = new JSONParser();

    private static final int MAX_ALLOWED_RETRIES = 3;
    private static final int TIME_BETWEEN_RETRIES = 1;

    private static final RateLimiter API_QUERY_LIMIT_CORE = new RateLimiter(200, 10, TimeUnit.MINUTES);
    private static final RateLimiter API_QUERY_LIMIT_EXTRA = new RateLimiter(400, 10, TimeUnit.MINUTES);

    private final boolean frivolous;
    private volatile boolean finished;
    private volatile boolean success;
    private int retries;
    private volatile boolean terminated;
    private volatile Object result;
    private String query;

    public static APIQuery queryUsernameValid(String username) {
        APIQuery query = new APIQuery(QueryType.USERNAME_VALID, true);
        query.queryName(username);
        return query;
    }

    public static APIQuery queryUUIDUsername(String username) {
        APIQuery query = new APIQuery(QueryType.UUID_FROM_USERNAME, true);
        query.queryUUID(username);
        return query;
    }

    /**
     * @param id the UUID of the Minecraft player with the desired skin
     * @param textureMap a PropertyMap to fill with the skin
     * @return boolean success, the provided PropertyMap is mutable and will be filled
     * if query is successful
     */
    public static APIQuery querySkinFromUUID(UUID id, PropertyMap textureMap) {
        APIQuery query = new APIQuery(QueryType.SKIN, false);
        query.querySkin(id, textureMap);
        return query;
    }

    private APIQuery(QueryType type, boolean frivolous) {
        this.frivolous = frivolous;
    }

    private boolean queryAllowed() {
        return frivolous ? API_QUERY_LIMIT_EXTRA.tryAcquire() : API_QUERY_LIMIT_CORE.tryAcquire();
    }

    /**
     * Checks over and over until it's legal to send an API query,
     * or until it's reached the maximum allowed number of retries.
     */
    private void tryAndWait() {
        JavaUtil.timedWait(TIME_BETWEEN_RETRIES, TimeUnit.SECONDS);
        boolean allowed = queryAllowed();
        retries++;
        terminated = retries == MAX_ALLOWED_RETRIES;
        if(!allowed && !terminated)
            tryAndWait();
    }

    private synchronized boolean attemptQuery() {
        if(!queryAllowed() && !finished) {
            if(!frivolous) {
                tryAndWait();
            }else{ // Give up immediately if this is an unimportant request
                terminated = true;
            }
            if(terminated) {
                success = false;
                return false;
            }
        }
        return true;
    }

    private synchronized void queryName(String query) {
        if(!attemptQuery())
            return;

        queryUUID(query);
        result = result != null;
    }

    private synchronized void queryUUID(String query) {
        if(!attemptQuery())
            return;
        this.query = query;

        if(query.length() > 16)
            throw new IllegalArgumentException("Username cannot be larger than 16");

        try {
            URLConnection connection = new URL("https://api.mojang.com/users/profiles/minecraft/" + query).openConnection();
            String result = IOUtils.toString(connection.getInputStream(), Charsets.UTF_8);
            finished = true;
            if(!result.isEmpty()) {
                Map<String, Object> results = new Gson().fromJson(result, new TypeToken<Map<String, Object>>(){}.getType());
                success = true;
                this.result = JavaUtil.parseUUIDFromDashless((String) results.get("id"));
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void querySkin(UUID query, PropertyMap fill) {
        if(!attemptQuery())
            return;

        boolean retry;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + query.toString().replace("-", "") + "?unsigned=false").openConnection();
            JSONArray response = (JSONArray)((JSONObject) parser.parse(new InputStreamReader(connection.getInputStream()))).get("properties");
            JSONObject a = (JSONObject) response.get(0);
            fill.put("textures", new Property("textures", (String) a.get("value"), (String) a.get("signature")));
            success = true;
            retry = false;
        } catch (IOException ex) { // Endlessly continues attempting to grab a skin
            CTFLogger.logWarning("Could not query a skin from Mojang database. Are their servers down?");
            CTFLogger.logWarning("Trying again in " + Constants.SKIN_QUERY_FAILURE_WAIT + "s");
            retry = true;
        } catch(ParseException ex) {
            throw new RuntimeException("Failed to parse JSON reply when querying a skin");
        } finally {
            if(connection != null) connection.disconnect();
        }

        if(retry) {
            JavaUtil.timedWait(Constants.SKIN_QUERY_FAILURE_WAIT, TimeUnit.SECONDS);
            querySkin(query, fill);
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean wasSuccessful() {
        if(!success)
            success = result != null; // Result being filled means success
        return success;
    }

    public boolean wasDisallowed() {
        return terminated;
    }

    public Object getResult() {
        return result;
    }

    public String getQuery() {
        return query;
    }
}
