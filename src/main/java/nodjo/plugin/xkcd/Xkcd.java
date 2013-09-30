package nodjo.plugin.xkcd;

import java.io.IOException;
import java.util.HashMap;

import nodjo.fcr.comics.IComic;
import nodjo.fcr.utils.StreamUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class Xkcd extends Service {

    private static final String TAG = Xkcd.class.getSimpleName();

    private static final String SERVER_URL = "http://www.xkcd.com/";
    private static final String SERVER_URL_JSON = "info.0.json";
    private static final String ONLINE_STORE_URL = "http://store.xkcd.com/?source=fastcomicreader";

    private static final HttpParams HTTP_PARAMS = new BasicHttpParams();
    static {
        HttpConnectionParams.setConnectionTimeout(HTTP_PARAMS, 15000);
        HttpConnectionParams.setSoTimeout(HTTP_PARAMS, 30000);
    }
    private ThreadSafeClientConnManager mConnectionManager;

    @Override
    public IBinder onBind(Intent intent) {
        final SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        mConnectionManager = new ThreadSafeClientConnManager(HTTP_PARAMS, registry);
        return mBinder;
    }

    private final IComic.Stub mBinder = new IComic.Stub() {

        class TitleUrl {

            private final String title;
            private final String url;

            private TitleUrl(String title, String url) {
                this.title = title;
                this.url = url;
            }
        }

        private final HashMap<Long, TitleUrl> cache = new HashMap<Long, TitleUrl>();

        @Override
        public long[] getStripIdentifiers() throws RemoteException {
            Log.v(TAG, "getStripIdentifiers");
            final HttpClient httpClient = new DefaultHttpClient(mConnectionManager, HTTP_PARAMS);
            HttpResponse response;
            HttpGet httpGet = new HttpGet(SERVER_URL + SERVER_URL_JSON);
            try {
                response = httpClient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    Log.d(TAG, "html size:" + entity.getContentLength() + " bytes");
                    final String jsonContent = StreamUtils.convertStreamToString(entity.getContent(), (int) entity.getContentLength());
                    final JSONObject json = new JSONObject(jsonContent);
                    final int currentId = json.getInt("num");
                    final long[] ids = new long[currentId];
                    for (int i = 0; i < currentId; i++)
                        ids[i] = i + 1;
                    Log.i(TAG, "returning:" + ids);
                    return ids;
                } else {
                    Log.w(TAG, "Connection problem. Response status code:" + response.getStatusLine().getStatusCode());
                }
            } catch (ClientProtocolException e) {
                Log.w(TAG, "ClientProtocolException", e);
            } catch (IOException e) {
                Log.w(TAG, "IOException", e);
            } catch (JSONException e) {
                Log.w(TAG, "JSONException", e);
            }
            return null;
        }

        @Override
        public String getStripUrl(long stripId) throws RemoteException {
            Log.v(TAG, "getStripUrl:" + stripId);
            if (!cache.containsKey(stripId))
                cache.put(stripId, getTitleUrl(stripId));
            return cache.get(stripId).url;
        }

        // TODO don't call in main thread so that it can access network...
        @Override
        public String getStripTitle(long stripId) throws RemoteException {
            Log.v(TAG, "getStripTitle:" + stripId);
            if (!cache.containsKey(stripId))
                return String.valueOf(stripId);
            // cache.put(stripId, getTitleUrl(stripId));
            return cache.get(stripId).title;
        }

        private TitleUrl getTitleUrl(long stripId) {
            Log.v(TAG, "getTitleUrl:" + stripId);

            final HttpClient httpClient = new DefaultHttpClient(mConnectionManager, HTTP_PARAMS);
            HttpResponse response;
            HttpGet httpGet = new HttpGet(SERVER_URL + stripId + "/" + SERVER_URL_JSON);
            try {
                response = httpClient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    Log.d(TAG, "html size:" + entity.getContentLength() + " bytes");
                    final String jsonContent = StreamUtils.convertStreamToString(entity.getContent(), (int) entity.getContentLength());
                    final JSONObject json = new JSONObject(jsonContent);
                    return new TitleUrl(json.getString("safe_title"), json.getString("img"));
                } else {
                    Log.w(TAG, "Connection problem. Response status code:" + response.getStatusLine().getStatusCode());
                }
            } catch (ClientProtocolException e) {
                Log.w(TAG, "ClientProtocolException", e);
            } catch (IOException e) {
                Log.w(TAG, "IOException", e);
            } catch (JSONException e) {
                Log.w(TAG, "JSONException", e);
            }
            return null;
        }

        @Override
        public String getWebsiteUrl() throws RemoteException {
            Log.v(TAG, "getWebsiteUrl");
            return ONLINE_STORE_URL;
        }

    };

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "onUnbind");
        mConnectionManager.shutdown();
        mConnectionManager = null;
        return super.onUnbind(intent);
    }
}
