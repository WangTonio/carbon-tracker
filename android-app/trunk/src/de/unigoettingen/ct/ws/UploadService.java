package de.unigoettingen.ct.ws;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import de.unigoettingen.ct.data.TrackPart;
import android.util.Log;

public class UploadService {
	private static final String SOAP_ACTION = "http://data.ct.unigoettingen.de/start2";
	private static final String METHOD_NAME = "start2";
	private static final String NAMESPACE = "http://data.ct.unigoettingen.de";
	private static final String URL = "http://134.76.20.156/Android_WS/services/WebService";
	public static final String LOG_TAG = "carbontracker";

	public void callWebservice(TrackPart track) {
		
		SoapObject soapObject = new SoapObject(NAMESPACE, METHOD_NAME);
		SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER12);
		
		soapObject.addProperty("TrackPart", track);
//		soapObject.addProperty("TrackPart", "HelloWorld");

		soapEnvelope.setOutputSoapObject(soapObject);
		
		HttpTransportSE httpTransportSE = new HttpTransportSE(URL);
		httpTransportSE.debug = true;
		
		try {
			httpTransportSE.call(SOAP_ACTION, soapEnvelope);
			SoapPrimitive resultString1 = (SoapPrimitive) soapEnvelope
					.getResponse();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String debug = "";
		debug = httpTransportSE.requestDump;
		Log.v(LOG_TAG, debug);
	}

}
