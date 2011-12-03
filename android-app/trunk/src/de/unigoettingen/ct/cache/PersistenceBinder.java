package de.unigoettingen.ct.cache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import de.unigoettingen.ct.data.OngoingTrack;
import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.data.io.Person;
import de.unigoettingen.ct.data.io.TrackPart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Provides some high level access to the local SQLite data base. This is intended to be used for storing measurement related 
 * data. Do not use the data base from different Threads at once. Call {@link #close()} when you are done using this object.
 * @author Fabian Sudau
 *
 */
public class PersistenceBinder extends SQLiteOpenHelper{

	public static final String DB_NAME = "SavedMeasurements";
	public static final String LOG_TAG = "PersistenceBinder";
	
	public static final String SELECT_TRACK_ID_USING_PROPERTIES = "SELECT _id FROM T_Track WHERE started_at = ? AND VIN = ? AND forename = ? "+
		"AND lastname = ?";
	public static final String SELECT_FIRST_X_MEASUREMENTS = "SELECT * FROM T_Measurement WHERE id_T_Track = ? ORDER BY _id LIMIT ?";
	public static final String SELECT_ALL_TRACKS_EMPTY = "SELECT * FROM T_Track";

	/**
	 * Constructs an object; implicitly creating the physical data base if it is missing.
	 * @param context the app context needed to locate the data base
	 */
	public PersistenceBinder(Context context) {
		super(context, DB_NAME , null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// this gets executed only once when the app starts for the first time.
		// tables are created capable of storing everything that is part of a 'TrackPart'.
		// note that vin and person information also have to be stored, because the phone
		// can be taken into another vehicle and the person information can be changed.
		// in those cases, the tracks must not be confused.
		 db.execSQL("CREATE TABLE T_Track( "+
				 	"_id INTEGER PRIMARY KEY AUTOINCREMENT , "+
				    "started_at INTEGER , "+
				    "closed INTEGER , "+
				    "forename TEXT , "+
				    "lastname TEXT , "+
				    "vin TEXT  , "+
				    "description TEXT "+
				    ")");
		 db.execSQL("CREATE TABLE T_Measurement ( "+
				 	"_id INTEGER PRIMARY KEY AUTOINCREMENT , "+
				 	"id_T_Track INTEGER , "+ //foreign reference
				    "point_of_time INTEGER , "+
				    "longitude REAL , "+
				    "latitude REAL , "+
				    "altitude REAL , "+
				    "rpm INTEGER , "+
				    "maf REAL , "+
				    "speed INTEGER , "+
				    "eot INTEGER , "+
				    "ert INTEGER , "+
				    "lambda REAL "+
				    ")");
		
	}
	
	/**
	 * Returns the id of the Track with matching 'startedAt', 'vin' and 'person'. Note that the 'closed' attribute
	 * and the measurements are not looked at.
	 * @param trackPart Track representation to get the id of
	 * @return the track id or -1 if not found
	 */
	private int getPrimaryKeyOfTrack(TrackPart trackPart){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.rawQuery(SELECT_TRACK_ID_USING_PROPERTIES, new String[]{ String.valueOf(trackPart.getStartedAt().getTimeInMillis()), trackPart.getVin(), 
				trackPart.getDriver().getForename(), trackPart.getDriver().getLastname()});
		if(cur.getCount() > 1){
			cur.close();
			//the following log call kills the app as this condition is very unexpected
			Log.wtf(LOG_TAG, "Corrupted db: Multiple track entries found for track "+trackPart);
		}
		if(cur.moveToFirst()){
			int retVal = cur.getInt(cur.getColumnIndex("_id"));
			cur.close();
			return retVal;
		}
		else{
			cur.close();
			return -1;
		}
	}
	
	/**
	 * Searches for a matching track and if one is found, reads the first X {@link Measurement}s from the data base and appends
	 * them to the provided track-representing object. This method does nothing if the Track is not found (always ignoring the
	 * 'closed' attribute). This method does not check whether measurements are already present in the object; may insert
	 * duplicates. 
	 * @param track the track representation to fill with measurements
	 * @param numberOfMeasurements max number of measurements to read; chronologically
	 */
	public void loadMeasurementsIntoTrack(OngoingTrack track, int numberOfMeasurements){
		int trackPrimaryKey = this.getPrimaryKeyOfTrack(track.getEmptyTrackPart());
		SQLiteDatabase db = this.getReadableDatabase();
		if(trackPrimaryKey >= 0){
			Cursor cur = db.rawQuery(SELECT_FIRST_X_MEASUREMENTS, new String[]{String.valueOf(trackPrimaryKey), String.valueOf(numberOfMeasurements) });
			while(cur.moveToNext()){
				Measurement currMeasurement = new Measurement();
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(cur.getLong(cur.getColumnIndex("point_of_time")));
				currMeasurement.setPointOfTime(cal);
				currMeasurement.setLongitude(cur.getDouble(cur.getColumnIndex("longitude")));
				currMeasurement.setLatitude(cur.getDouble(cur.getColumnIndex("latitude")));
				currMeasurement.setAltitude(cur.getDouble(cur.getColumnIndex("altitude")));
				currMeasurement.setRpm(cur.getInt(cur.getColumnIndex("rpm")));
				currMeasurement.setMaf(cur.getDouble(cur.getColumnIndex("maf")));
				currMeasurement.setSpeed(cur.getInt(cur.getColumnIndex("speed")));
				currMeasurement.setEot(cur.getInt(cur.getColumnIndex("eot")));
				currMeasurement.setErt(cur.getInt(cur.getColumnIndex("ert")));
				currMeasurement.setLambda(cur.getDouble(cur.getColumnIndex("lambda")));
				track.addMeasurement(currMeasurement);
			}
			cur.close();
		}
		//the left-out else means the track is not present and this method simply becomes a no-op
	}
	
	/**
	 * Returns an unordered list of all found tracks containing NO measurements (yet).
	 * @return all found tracks empty
	 */
	public List<OngoingTrack> loadAllTracksEmpty(){
		SQLiteDatabase db = this.getReadableDatabase();
		List<OngoingTrack> retVal = new ArrayList<OngoingTrack>();
		Cursor cur = db.rawQuery(SELECT_ALL_TRACKS_EMPTY, null);
		while(cur.moveToNext()){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(cur.getLong(cur.getColumnIndex("started_at")));
			retVal.add(new OngoingTrack(cal, cur.getString(cur.getColumnIndex("vin")), cur.getString(cur.getColumnIndex("description")), 
					new Person(cur.getString(cur.getColumnIndex("forename")), cur.getString(cur.getColumnIndex("lastname")))));
		}
		cur.close();
		return retVal;
	}
	
	/**
	 * Returns an unordered list of all found Tracks, that are still open. The Tracks do not have 
	 * measurements associated (yet).
	 * @return all open tracks empty
	 */
	public List<OngoingTrack> loadOpenTracksEmpty(){
		List<OngoingTrack> retVal = this.loadAllTracksEmpty();
		for(Iterator<OngoingTrack> it = retVal.iterator(); it.hasNext();){
			OngoingTrack currTrack = it.next();
			if(currTrack.isClosed()){
				it.remove();
			}
		}
		return retVal;
	}
	
	/**
	 * Ignoring the 'closed' field and the measurements, this will look for a matching track in the data base.
	 * If one is found, the track and all it's associated measurement data gets deleted.
	 * @param track track to delete
	 */
	public void deleteTrackCompletely(TrackPart track){
		int primaryKeyTrack = this.getPrimaryKeyOfTrack(track);
		if(primaryKeyTrack != -1){
			SQLiteDatabase db = this.getReadableDatabase();
			db.delete("T_Track", "_id = ?", new String[]{String.valueOf(primaryKeyTrack)});
			db.delete("T_Measurement", "id_T_Track = ?", new String[]{String.valueOf(primaryKeyTrack)});
		}
	}

	/**
	 * Writes the specified track and all it's provided measurements to the data base. This does NOT check, whether the track
	 * or any measurements are already present. Call this only, if you are sure, that the track is not yet present in the data base.
	 * @param track track to store 
	 */
	public void writeFullTrack(TrackPart track){
		SQLiteDatabase db = this.getReadableDatabase();
		//the _id column is left out due to auto increment
		ContentValues row = new ContentValues();
		row.put("started_at", track.getStartedAt().getTimeInMillis());
		row.put("closed", track.isLastPart());
		row.put("forename", track.getDriver().getForename());
		row.put("lastname", track.getDriver().getLastname());
		row.put("vin", track.getVin());
		row.put("description", track.getDescription());
		db.insert("T_Track", null, row);
		row = null;
		int trackPrimaryKey = this.getPrimaryKeyOfTrack(track); //using this because i do not trust the insert(...)'s return value
		for(Measurement m: track.getMeasurements()){
			row = new ContentValues();
			row.put("id_T_Track", trackPrimaryKey);
			row.put("point_of_time", m.getPointOfTime().getTimeInMillis());
			row.put("longitude", m.getLongitude());
			row.put("latitude", m.getLatitude());
			row.put("altitude", m.getAltitude());
			row.put("rpm", m.getRpm());
			row.put("maf", m.getMaf());
			row.put("speed", m.getSpeed());
			row.put("eot", m.getEot());
			row.put("ert", m.getErt());
			row.put("lambda", m.getLambda());
			db.insert("T_Measurement", null, row);
		}
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//think about this, when a new db schema is necessary
	}

}
