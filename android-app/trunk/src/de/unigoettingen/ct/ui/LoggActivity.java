package de.unigoettingen.ct.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import de.unigoettingen.ct.R;
import de.unigoettingen.ct.container.Logg;
import de.unigoettingen.ct.data.GenericObserver;
import de.unigoettingen.ct.data.io.DebugMessage;
import de.unigoettingen.ct.upload.AbstractUploader;
import de.unigoettingen.ct.upload.DebugLogUploader;

/**
 * Displays the custom log ({@link Logg}) in real time.
 * @author Fabian Sudau
 *
 */
public class LoggActivity extends Activity implements OnClickListener, GenericObserver<Logg>{

	private Button uploadBtn;
	private ListView logListView;
	private Handler mainThread;
	private AbstractUploader logUploader;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.logg);
        this.uploadBtn = (Button) findViewById(R.id.logguploadbtn);
        this.uploadBtn.setOnClickListener(this);
        this.logListView = (ListView) findViewById(R.id.logglistview);
        this.mainThread = new Handler();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//the activity is becoming visible, register for live log updates
		Logg.INSTANCE.addObserver(this);
		this.update(Logg.INSTANCE); //cause an update manually to display any up-to-now messages
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//the activity is not visible anymore, unregister live log updates
		Logg.INSTANCE.removeObserver(this);
	}
	
	
	@Override
	public void onClick(View v) {
		if(logUploader == null){
			logUploader = new DebugLogUploader(Logg.INSTANCE.getLogDump());
			logUploader.startUpload();
			Toast.makeText(this, "Uploading ...", Toast.LENGTH_SHORT).show();
		}
		else{
			if(!logUploader.isDone()){
				Toast.makeText(this, "Upload still in progress ! ", Toast.LENGTH_SHORT).show();
			}
			else{
				if(logUploader.hasErrorOccurred()){
					Toast.makeText(this, "Upload failed! Click again to retry.", Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(this, "Upload successful! Clicking again will repeat this.", Toast.LENGTH_SHORT).show();
				}
				logUploader=null;
			}
		}
	}

	@Override
	public void update(final Logg observable) {
		this.mainThread.post(new Runnable() {	
			@Override
			public void run() {
				ArrayAdapter<DebugMessage> adapter = new ArrayAdapter<DebugMessage>(LoggActivity.this, android.R.layout.simple_list_item_1, 
						observable.getMessagesAsArray());
				logListView.setAdapter(adapter);
				logListView.setSelection(adapter.getCount()-1);
			}
		});
	}
}
