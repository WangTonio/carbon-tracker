package de.unigoettingen.ct.ws;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.util.Log;
import de.unigoettingen.ct.data.TrackPart;
import de.unigoettingen.ct.json.CalendarTransformer;
import flexjson.JSONSerializer;

public class UploadService {
	private static final String SOAP_ACTION = "http://data.ct.unigoettingen.de/start2";
	private static final String METHOD_NAME = "start2";
	private static final String NAMESPACE = "http://data.ct.unigoettingen.de";
	private static final String URL = "http://134.76.20.156/Android_WS/services/WebService";
	public static final String LOG_TAG = "carbontracker";

//	public void callWebservice(TrackPart track) {
//		
//		SoapObject soapObject = new SoapObject(NAMESPACE, METHOD_NAME);
//		SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(
//				SoapEnvelope.VER12);
//		
//		soapObject.addProperty("TrackPart", track);
////		soapObject.addProperty("TrackPart", "HelloWorld");
//
//		soapEnvelope.setOutputSoapObject(soapObject);
//		
//		HttpTransportSE httpTransportSE = new HttpTransportSE(URL);
//		httpTransportSE.debug = true;
//		
//		try {
//			httpTransportSE.call(SOAP_ACTION, soapEnvelope);
//			SoapPrimitive resultString1 = (SoapPrimitive) soapEnvelope
//					.getResponse();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		String debug = "";
//		debug = httpTransportSE.requestDump;
//		Log.v(LOG_TAG, debug);
//	}
	
	public void callWebservice(TrackPart track) {
		Log.d(LOG_TAG, " -------------------------------------------------------- ");
		JSONSerializer serializer = new JSONSerializer().prettyPrint(true).transform(new CalendarTransformer(), Calendar.class);
//		JSONDeserializer<TrackPart> deserializer = new JSONDeserializer<TrackPart>().use(Calendar.class, (ObjectFactory) new CalendarTransformer());
		String json = serializer.deepSerialize(track);
		System.out.println("Will send this to the web service:");
		System.out.println(json);
		
		HttpPost httppost = new HttpPost("http://134.76.20.156/CarbonTrackerWS/TrackPart");          
		StringEntity se = null;
		try {
			se = new StringEntity(json,HTTP.UTF_8);
		}
		catch (UnsupportedEncodingException e) {
			Log.e(LOG_TAG, "UNSUPPORTED ENCODING UTF-8");
			e.printStackTrace();
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
			Log.e(LOG_TAG, "ClientProtocolException.. WAT");
			e.printStackTrace();
			return;
		}
		catch (IOException e) {
			Log.e(LOG_TAG, "IOEXCEPTION");
			e.printStackTrace();
			return;
		}
		System.out.println("Status Code: "+httpResponse.getStatusLine().getStatusCode());
		System.out.println("Phrase: "+httpResponse.getStatusLine().getReasonPhrase());
		System.out.println("Whole line: "+httpResponse.getStatusLine().toString());

		
	}

}
