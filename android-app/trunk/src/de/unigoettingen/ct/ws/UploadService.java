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
//		soapObject.addProperty("TrackPart", "<?xml version='1.0' encoding='UTF-8'?><Track started_at='2011/09/23 13:42:01' last_part='false' vin='2SHDBV35JAS' description='paar bier geholt' >	<Person name='Kevin' forename='Horst'/>	<Measurement point_of_time='2011/09/23 13:42:01' longitude='13.408056' latitude='52.510611' altitude='11.1' rpm='1500' maf='100' speed='101'		eot='90' ert='00:16:00'/>	<Measurement point_of_time='2011/09/23 13:42:02' longitude='13.408156' latitude='52.511611' altitude='12.2' rpm='1501' maf='101' speed='102'		eot='90' ert='00:16:01'/>	<Measurement point_of_time='2011/09/23 13:42:03' longitude='13.408256' latitude='52.512611' altitude='13.3' rpm='1502' maf='102' speed='103' eot='90' ert='00:16:02'/></Track>");

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
