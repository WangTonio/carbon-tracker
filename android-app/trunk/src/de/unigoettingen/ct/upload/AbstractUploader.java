package de.unigoettingen.ct.upload;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.util.Log;

/**
 * Designed to upload 'data' (e.g. simple) objects to a web service interface in a 'fire-and-forget' manner.
 * That means, the uploading will happen in an own, encapsulated thread and after some unspecified amount of time
 * will either have succeeded or failed. Users of this class can ask any time, whether the upload has finished
 * and whether it has succeeded. <br>
 * Concrete subclasses upload concrete data objects.
 * @author Fabian Sudau
 *
 */
public abstract class AbstractUploader {

	private Thread uploadingThread;
	private Exception caughtException;

	private static final String WEB_SERVICE_URL = "http://134.76.20.156/CarbonTrackerWs/";
	private static final String LOG_TAG = "AbstractUploader";
	
	public AbstractUploader(){
		Runnable updatingLogic = new Runnable() {	
			@Override
			public void run() {
				String fullUrl = WEB_SERVICE_URL + getUrlSuffix();
				String content = getBodyContent();
				if (Log.isLoggable(LOG_TAG, Log.VERBOSE)){
					Log.v(LOG_TAG, "Sending the following via HTTP POST to "+fullUrl);
					Log.v(LOG_TAG, content);
				}
				
				HttpPost httppost = new HttpPost(fullUrl);          
				StringEntity se = null;
				try {
					se = new StringEntity(content,HTTP.UTF_8);
				}
				catch (UnsupportedEncodingException e) {
					Log.wtf(LOG_TAG, "UNSUPPORTED ENCODING UTF-8");
					Log.wtf(LOG_TAG, e);
					preserveError(e);
					return;
				}

				se.setContentType("application/json");  
				httppost.setHeader("Content-Type","application/json;charset=UTF-8");
				httppost.setEntity(se);  

				HttpClient httpclient = new DefaultHttpClient();
				HttpResponse httpResponse;
				try {
					httpResponse = httpclient.execute(httppost); 
				}
				catch (ClientProtocolException e) {
					Log.e(LOG_TAG, "ClientProtocolException.. Wrong Url? Misconfigured Server?", e);
					preserveError(e);
					return;
				}
				catch (IOException e) {
					Log.e(LOG_TAG, "IOEXCEPTION", e);
					preserveError(e);
					return;
				}
				int responseCode = +httpResponse.getStatusLine().getStatusCode();
				if(responseCode != 200){
					Exception notOkExc = new ClientProtocolException("Server Response was not '200 OK'. Server said: "+responseCode+" : "
							+httpResponse.getStatusLine().getReasonPhrase());
					Log.e(LOG_TAG, "Upload not accepted" , notOkExc);
					preserveError(notOkExc);
					return;
				}
			}
		};
		this.uploadingThread = new Thread(updatingLogic);
	}
	
	/**
	 * Starts the upload asynchronously.
	 */
	public void startUpload(){
		this.uploadingThread.start();
	}
	
	/**
	 * Returns, whether code is still running.
	 * @return true, if the upload was not started yet or has already finished.
	 */
	public boolean isDone(){
		return !this.uploadingThread.isAlive();
	}
	
	/**
	 * Returns whether an error has (yet) occurred.
	 * @return true, if an error occurred 
	 */
	public boolean hasErrorOccurred(){
		return this.caughtException != null;
	}
	
	/**
	 * When called on an object that isDone() and had an occurred error, this returns whether
	 * the upload was canceled due to the android systems unpredictable behavior (bad reception,
	 * juice defender, etc.) or because the server or app is misconfigured.
	 * @return true, if another upload might succeed
	 */
	public boolean isRetryingPossible(){
		if(this.caughtException == null){
			return true;
		}
		else{
			return (this.caughtException instanceof IOException) && !(this.caughtException instanceof ClientProtocolException);
		}
	}
	
	private void preserveError(Exception e){
		this.caughtException = e;
	}
	
	/**
	 * Template method for subclasses to override. Returns the full HTTP POST body to be uploaded. 
	 * Should contain a JSON serialization of the object to upload.
	 * @return JSON string
	 */
	protected abstract String getBodyContent();
	
	/**
	 * Template method for subclasses to override.
	 * Returns the URL suffix (last part of the URL) meant to be used for the upload, e.g. the 'method name' in a RESTFUL web service.
	 * @return web service method name
	 */
	protected abstract String getUrlSuffix();
}
