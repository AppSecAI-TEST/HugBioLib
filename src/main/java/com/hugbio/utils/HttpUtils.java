package com.hugbio.utils;

import android.os.Handler;
import android.text.TextUtils;

import com.hugbio.download.DownloadParams;
import com.hugbio.download.FileDownloadUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpUtils {

    private static final String BOUNDARY = "1234567890abcd";

    private static int TIMEOUT_CONNECTION = 8000;
    private static int TIMEOUT_SO = 10000;
    public static final String USER_IDENTITY_KEY = "User-Identity";

    public static final String MOBILE_AGENT_KEY = "Mobile-Agent";

    public static final String SESSION_KEY = "Session";

    public static final String COOKIE_KEY = "Cookie";
    public static final String PHPSESSID_KEY = "PHPSESSID";
    public static String sessid = "";

    public static final String COOKIE_SET_KEY = "Set-Cookie";
    private static String mMobile_Agent_Aulae = "Android" + "/%s" + "," + "%s";

    public static String MOBILE_AGENT_VALUE = String.format(mMobile_Agent_Aulae, android.os.Build.MODEL, "2.0");
    public static String USER_IDENTITY_VALUE = String.format(mMobile_Agent_Aulae, android.os.Build.MODEL, "2.0");

    public static void setConnectionTimeOut(int time) {
        TIMEOUT_CONNECTION = time;
    }

    public static void setSoTimeOut(int time) {
        TIMEOUT_SO = time;
    }

    public static InputStream doGetInputStream(String strUrl) {
        HttpResponse response = doConnection(strUrl);
        if (isResponseAvailable(response)) {
            try {
                return response.getEntity().getContent();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String doGetString(String strUrl) {
        String strResult = null;
        HttpResponse response = doConnection(strUrl);
        if (isResponseAvailable(response)) {
            try {
                strResult = EntityUtils.toString(response.getEntity(), "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (response != null) {
                strResult = "HttpStatusCode:" + response.getStatusLine().getStatusCode();
            }
        }
        return strResult;
    }

    public static String doGetString(String strUrl, String defaultCharset) {
        String strResult = null;
        HttpResponse response = doConnection(strUrl);
        if (isResponseAvailable(response)) {
            try {
                strResult = EntityUtils.toString(response.getEntity(), defaultCharset);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (response != null) {
                strResult = "HttpStatusCode:" + response.getStatusLine().getStatusCode();
            }
        }
        return strResult;
    }

    public static String doPost(String strUrl, Map<String, String> mapToNameValue) {
        return doPost(strUrl, mapToNameValue, null);
    }

    public static String doPost(String strUrl, Map<String, String> mapToNameValue, Map<String, String> mapKeyToFilePath) {
        return doPost(strUrl, mapToNameValue, mapKeyToFilePath, null, null, null);
    }

    public static String doPost(String strUrl, String filePath) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("upfile", filePath);
        return doPost(strUrl, null, map);
    }

    public static String doPost(String strUrl, String filePath, ProgressRunnable pr, Handler handler, AtomicBoolean cancel) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("upfile", filePath);
        return doPost(strUrl, null, map, pr, handler, cancel);
    }

    public static String doPostForJson(String strUrl, Map<String, String> mapToNameValue) {
        String strResult = null;
        HttpResponse response = null;
        try {
            final URI uri = new URI(strUrl);
            HttpPost httpPost = new HttpPost(uri);
            httpPost.addHeader("charset", HTTP.UTF_8);
            // 额外增加的
            if (!TextUtils.isEmpty(sessid)) {
                httpPost.addHeader(COOKIE_KEY, PHPSESSID_KEY + "=" + sessid);
            }
            httpPost.addHeader(USER_IDENTITY_KEY, USER_IDENTITY_VALUE);
            httpPost.addHeader(MOBILE_AGENT_KEY, MOBILE_AGENT_VALUE);
            httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");

//            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            JSONObject jo = new JSONObject();
            if (mapToNameValue != null) {
                for (String str : mapToNameValue.keySet()) {
                    final String value = mapToNameValue.get(str);
                    jo.put(str, value);
                }
            }
            StringEntity entity = new StringEntity(jo.toString());
            entity.setContentType("text/json");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpPost.setEntity(entity);
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams params = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_CONNECTION);
            HttpConnectionParams.setSoTimeout(params, TIMEOUT_SO);

            response = httpClient.execute(httpPost);

            if (isResponseAvailable(response)) {
                strResult = EntityUtils.toString(response.getEntity());
            } else {
                if (response != null) {
                    strResult = "HttpStatusCode:" + response.getStatusLine().getStatusCode();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strResult;
    }

    public static String doPostForJson(String strUrl, Object jo) {
        String strResult = null;
        HttpResponse response = null;
        try {
            final URI uri = new URI(strUrl);
            HttpPost httpPost = new HttpPost(uri);
            httpPost.addHeader("charset", HTTP.UTF_8);
            // 额外增加的
            if (!TextUtils.isEmpty(sessid)) {
                httpPost.addHeader(COOKIE_KEY, PHPSESSID_KEY + "=" + sessid);
            }
            httpPost.addHeader(USER_IDENTITY_KEY, USER_IDENTITY_VALUE);
            httpPost.addHeader(MOBILE_AGENT_KEY, MOBILE_AGENT_VALUE);
            httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");

//            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            StringEntity entity = new StringEntity(jo.toString());
            entity.setContentType("text/json");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpPost.setEntity(entity);
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams params = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_CONNECTION);
            HttpConnectionParams.setSoTimeout(params, TIMEOUT_SO);

            response = httpClient.execute(httpPost);

            if (isResponseAvailable(response)) {
                strResult = EntityUtils.toString(response.getEntity());
            } else {
                if (response != null) {
                    strResult = "HttpStatusCode:" + response.getStatusLine().getStatusCode();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strResult;
    }

    public static String doPost(String strUrl, Map<String, String> mapToNameValue, Map<String, String> mapKeyToFilePath,
                                ProgressRunnable pr, Handler handler, AtomicBoolean cancel) {
        String strResult = null;
        HttpResponse response = null;
        try {
            final URI uri = new URI(strUrl);
            HttpPost httpPost = new HttpPost(uri);
            httpPost.addHeader("charset", HTTP.UTF_8);
            // 额外增加的
            if (!TextUtils.isEmpty(sessid)) {
                httpPost.addHeader(COOKIE_KEY, PHPSESSID_KEY + "=" + sessid);
            }
            httpPost.addHeader(USER_IDENTITY_KEY, USER_IDENTITY_VALUE);
            httpPost.addHeader(MOBILE_AGENT_KEY, MOBILE_AGENT_VALUE);

            if (mapKeyToFilePath != null) {
                MultipartEntityEx entity = new MultipartEntityEx(HttpMultipartMode.BROWSER_COMPATIBLE, pr, handler, cancel);

                if (mapToNameValue != null) {
                    for (String str : mapToNameValue.keySet()) {
                        final String value = mapToNameValue.get(str);
                        entity.addPart(str, new StringBody(value == null ? "" : value, Charset.forName("UTF-8")));
                    }
                }
                for (String strKey : mapKeyToFilePath.keySet()) {
                    entity.addPart(strKey, new FileBody(new File(mapKeyToFilePath.get(strKey))));
                }
                entity.mTotalSize = entity.getContentLength();
                httpPost.setEntity(entity);
            } else {
                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                if (mapToNameValue != null) {
                    for (String str : mapToNameValue.keySet()) {
                        final String value = mapToNameValue.get(str);
                        formparams.add(new BasicNameValuePair(str, value == null ? "" : value));
                    }
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, HTTP.UTF_8);
                httpPost.setEntity(entity);
            }
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams params = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_CONNECTION);
            HttpConnectionParams.setSoTimeout(params, TIMEOUT_SO);

            response = httpClient.execute(httpPost);

            if (isResponseAvailable(response)) {
                strResult = EntityUtils.toString(response.getEntity());
            } else {
                if (response != null) {
                    strResult = "HttpStatusCode:" + response.getStatusLine().getStatusCode();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strResult;
    }

    public static String doUpload(String strUrl, String strFilePath, ProgressRunnable progress, Handler handler) {
        InputStream is = null;
        OutputStream os = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();

            File file = new File(strFilePath);
            String strFilename = file.getName();

            StringBuffer sbPrefix = new StringBuffer();
            sbPrefix.append("\r\n").append("--" + BOUNDARY + "\r\n")
                    .append("Content-Disposition: form-data; name=\"pic_file\"; filename=\"" + strFilename + "\"\r\n")
                    .append("Content-Type: " + "application/octet-stream" + "\r\n").append("\r\n");

            StringBuffer sbSuffix = new StringBuffer();
            sbSuffix.append("\r\n--" + BOUNDARY + "--\r\n");

            byte bytePrefix[] = sbPrefix.toString().getBytes("UTF-8");
            byte byteSuffix[] = sbSuffix.toString().getBytes("UTF-8");

            final long lContentLength = bytePrefix.length + file.length() + byteSuffix.length;

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            conn.setRequestProperty("Content-Length", String.valueOf(lContentLength));
            // conn.setConnectTimeout(TIMEOUT_CONNECTION);
            // conn.setReadTimeout(TIMEOUT_SO);
            // conn.setRequestProperty("HOST", "192.168.1.16:8080");
            conn.setDoOutput(true);

            os = conn.getOutputStream();
            is = new FileInputStream(file);

            os.write(bytePrefix);

            byte[] buf = new byte[1024];
            int nReadBytes = 0;

            if (progress == null) {
                while ((nReadBytes = is.read(buf)) != -1) {
                    os.write(buf, 0, nReadBytes);
                }

                os.write(byteSuffix);
            } else {
                long lUploadBytes = bytePrefix.length;
                while ((nReadBytes = is.read(buf)) != -1) {
                    os.write(buf, 0, nReadBytes);
                    lUploadBytes += nReadBytes;

                    progress.mPercentage = (int) (lUploadBytes * 100 / lContentLength);
                    handler.post(progress);
                }

                os.write(byteSuffix);

                progress.mPercentage = 100;
                handler.post(progress);
            }

        } catch (Exception e) {
            e.printStackTrace();
            conn = null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    conn = null;
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    conn = null;
                }
            }
        }

        String strRet = null;

        if (conn != null) {
            try {
                InputStream isResponse = (InputStream) conn.getContent();
                if (isResponse != null) {
                    int nRead = 0;
                    byte buf[] = new byte[128];
                    CharArrayBuffer bab = new CharArrayBuffer(128);
                    while ((nRead = isResponse.read(buf)) != -1) {
                        bab.append(buf, 0, nRead);
                    }
                    strRet = bab.substring(0, bab.length());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return strRet;
    }

    public static String doUpload(String strUrl, String strFilePath) {
        return doUpload(strUrl, strFilePath, null, null);
    }

    @Deprecated
    public static boolean doDownload1(String strUrl, String strSavePath, ProgressRunnable progress, Handler handler, AtomicBoolean bCancel) {
        HttpResponse response = doConnection(strUrl);
        if (isResponseAvailable(response)) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = response.getEntity().getContent();
                fos = FileHelper.createFileOutputStream(strSavePath);
                if (fos != null) {
                    final byte buf[] = new byte[1024];
                    if (progress == null) {
                        int lReadLength = 0;
                        while ((lReadLength = is.read(buf)) != -1) {
                            fos.write(buf, 0, lReadLength);
                        }
                    } else {
                        long lDownloadLength = 0;
                        int lReadLength = 0;
                        final long lTotalLength = response.getEntity().getContentLength();
                        while (true) {
                            if (bCancel != null && bCancel.get()) {
                                File file = new File(strSavePath);
                                file.delete();
                                return false;
                            } else if ((lReadLength = is.read(buf)) != -1) {
                                fos.write(buf, 0, lReadLength);
                                lDownloadLength += lReadLength;
                                progress.mPercentage = (int) (lDownloadLength * 100 / lTotalLength);
                                handler.post(progress);
                            } else {
                                break;
                            }
                        }
                    }
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                FileHelper.deleteFile(strSavePath);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                }
            }
        }
        return false;
    }

    @Deprecated
    public static boolean doDownload1(String strUrl, String strSavePath, boolean bUseTemp, ProgressRunnable progress, Handler handler,
                                      AtomicBoolean bCancel) {
        HttpResponse response = doConnection(strUrl);
        if (isResponseAvailable(response)) {
            InputStream is = null;
            boolean bSuccess = false;
            FileOutputStream fos = null;
            final String path = bUseTemp ? strSavePath + ".temp" : strSavePath;
            try {
                is = response.getEntity().getContent();
                fos = FileHelper.createFileOutputStream(path);
                if (fos != null) {
                    final byte buf[] = new byte[1024];
                    if (progress == null) {
                        int lReadLength = 0;
                        while ((lReadLength = is.read(buf)) != -1) {
                            fos.write(buf, 0, lReadLength);
                        }
                    } else {
                        long lDownloadLength = 0;
                        int lReadLength = 0;
                        final long lTotalLength = response.getEntity().getContentLength();
                        while (true) {
                            if (bCancel != null && bCancel.get()) {
                                File file = new File(path);
                                file.delete();
                                return false;
                            } else if ((lReadLength = is.read(buf)) != -1) {
                                fos.write(buf, 0, lReadLength);
                                lDownloadLength += lReadLength;
                                progress.mPercentage = (int) (lDownloadLength * 100 / lTotalLength);
                                handler.post(progress);
                            } else {
                                break;
                            }
                        }
                    }
                    bSuccess = true;
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                FileHelper.deleteFile(path);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                    if (bSuccess) {
                        if (bUseTemp) {
                            File file = new File(path);
                            File fileDst = new File(strSavePath);
                            file.renameTo(fileDst);
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        return false;
    }

    /***
     * 下载
     *
     * @return 2000  参数错误
     * 2010  未知的错误
     * 0 下载成功
     * 1-本地文件被破坏或者与服务器文件不一致；2-本地已下载的文件太小；3-用户中断下载；4-下载过程中发送未知错误
     */
    public static int doDownload(String strUrl, String strSavePath, DownloadParams params) {
        if (TextUtils.isEmpty(strSavePath) || TextUtils.isEmpty(strUrl)) {
            return 2000;
        }
        InputStream is = null;
        FileOutputStream fos = null;
        HttpURLConnection urlConnection = null;
        try {
            final URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            is = urlConnection.getInputStream();
            fos = FileHelper.createFileOutputStream(strSavePath);
            if (fos != null) {
                final byte buf[] = new byte[1024];
                if (params.getProgressCallback() == null) {
                    int lReadLength = 0;
                    while ((lReadLength = is.read(buf)) != -1) {
                        fos.write(buf, 0, lReadLength);
                    }
                } else {
                    long lDownloadLength = 0;
                    int lReadLength = 0;
                    final long lTotalLength = urlConnection.getContentLength();
                    while (true) {
                        if (params.getDownStatus() > 0 && params.getDownStatus() < 3) {
                            File file = new File(strSavePath);
                            file.delete();
                            return 3;
                        } else if ((lReadLength = is.read(buf)) != -1) {
                            fos.write(buf, 0, lReadLength);
                            lDownloadLength += lReadLength;
                            ProgressRunnable progressCallback = params.getProgressCallback();
                            if(progressCallback != null){
                                progressCallback.mPercentage = (int) (lDownloadLength * 100 / lTotalLength);
                                params.getHandler().post(progressCallback);
                            }
                        } else {
                            break;
                        }
                    }
                }
                params.setComplete();
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            FileHelper.deleteFile(strSavePath);
        } finally {
            try {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
            }
        }
        return 2010;
    }

    /***
     * 支持断点续传的下载
     *
     * @return 2000  参数错误
     * 2001 服务器返回数据为空
     * 2002  连接错误
     * 2003 下载成功但文件保存（重命名）失败
     * 2010  未知的错误
     * 0 下载成功
     * 1-本地文件被破坏或者与服务器文件不一致；2-本地已下载的文件太小；3-用户中断下载；4-下载过程中发送未知错误
     */
    public static int doDownloadForResume(String strUrl, String strSavePath, DownloadParams params) {
        if (TextUtils.isEmpty(strSavePath) || TextUtils.isEmpty(strUrl)) {
            return 2000;   //参数错误
        }
        HttpURLConnection urlConnection = null;
        try {
            String tempFilePath = FileDownloadUtils.getTempFilePath(strSavePath);
            long range = 0;
            if (!params.isRestart()) {
                range = FileDownloadUtils.getRange(tempFilePath);
            }else{
                FileDownloadUtils.deleteDownloadFile(strSavePath);
            }
            final URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("RANGE", "bytes=" + range + "-");
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 204 || responseCode == 205) {
                return 2001;  // 服务器返回的数据为空
            } else if (responseCode >= 300) {
                return 2002;  //连接错误
            }
            boolean isAutoResume = FileDownloadUtils.isSupportRange(urlConnection);
            if (!isAutoResume) { //服务器如果不支持断点续传，则重新下载
                range = 0;
            }
            //断点续传
            int ret = FileDownloadUtils.downLoad(urlConnection, tempFilePath, false, range, params);
            if (ret == 0 && !FileHelper.rename(tempFilePath, strSavePath, true)) {
                return 2003;
            }
            if(ret == 0){
                params.setComplete();
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
            }
        }
        return 2010;
    }


    public static boolean isResponseAvailable(HttpResponse response) {
        if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return true;
        }
        return false;
    }

    public static HttpResponse doConnection(String strUrl) {
        HttpResponse response = null;
        try {
            final URI uri = new URI(strUrl);
            HttpGet httpGet = new HttpGet(uri);
            httpGet.addHeader("charset", HTTP.UTF_8);

            // 额外增加的
            if (!TextUtils.isEmpty(sessid)) {
                httpGet.addHeader(COOKIE_KEY, PHPSESSID_KEY + "=" + sessid);
            }
            httpGet.addHeader(USER_IDENTITY_KEY, USER_IDENTITY_VALUE);
            httpGet.addHeader(MOBILE_AGENT_KEY, MOBILE_AGENT_VALUE);

            HttpClient httpClient = new DefaultHttpClient();
            HttpParams params = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_CONNECTION);
            HttpConnectionParams.setSoTimeout(params, TIMEOUT_SO);
            response = httpClient.execute(httpGet);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private static class MultipartEntityEx extends MultipartEntity {

        public long mTransferredSize;

        public long mTotalSize;

        public ProgressRunnable mRunnable;
        public Handler mHandler;
        public AtomicBoolean mCancel;

        public MultipartEntityEx(HttpMultipartMode mode, ProgressRunnable run, Handler handler, AtomicBoolean cancel) {
            super(mode);
            mRunnable = run;
            mHandler = handler;
            mCancel = cancel;
        }

        @Override
        public void writeTo(OutputStream outstream) throws IOException {
            super.writeTo(new CustomOutputStream(outstream));
        }

        private class CustomOutputStream extends FilterOutputStream {

            public CustomOutputStream(OutputStream out) {
                super(out);
            }

            @Override
            public void write(byte[] buffer, int offset, int length) throws IOException {
                if (mCancel != null && mCancel.get()) {
                    throw new IOException();
                }
                out.write(buffer, offset, length);
                // super.write(buffer, offset, length);
                mTransferredSize += length;
                notifyProgress();
            }

            @Override
            public void write(int oneByte) throws IOException {
                if (mCancel != null && mCancel.get()) {
                    throw new IOException();
                }
                super.write(oneByte);
                ++mTransferredSize;
                notifyProgress();
            }

            protected void notifyProgress() {
                if (mHandler != null && mRunnable != null) {
                    final int nPer = (int) (mTransferredSize * 100 / mTotalSize);
                    if (mRunnable.mPercentage != nPer) {
                        mRunnable.mPercentage = nPer;
                        mHandler.post(mRunnable);
                    }
                }
            }
        }
    }

    public static abstract class ProgressRunnable implements Runnable {
        public int mPercentage = -1;

        public int getPercentage() {
            return mPercentage;
        }
    }
}
