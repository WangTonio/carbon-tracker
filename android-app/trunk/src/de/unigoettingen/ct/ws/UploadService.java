package de.unigoettingen.ct.ws;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import de.unigoettingen.ct.data.TrackPart;

public class UploadService {
	private static final String SOAP_ACTION = "http://ws.ct.unigoettingen.de/start2";
	private static final String METHOD_NAME = "start2";
	private static final String NAMESPACE = "http://ws.ct.unigoettingen.de";
	private static final String URL = "http://134.76.20.156/Android_WS/services/WebService";

	public void callWebservice(TrackPart track) {
		
		SoapObject soapObject = new SoapObject(NAMESPACE, METHOD_NAME);
		SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER12);
		
		soapObject.addProperty("TrackPart", track);

		soapEnvelope.setOutputSoapObject(soapObject);
		HttpTransportSE httpTransportSE = new HttpTransportSE(URL);

		try {
			httpTransportSE.call(SOAP_ACTION, soapEnvelope);
			SoapPrimitive resultString1 = (SoapPrimitive) soapEnvelope
					.getResponse();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
