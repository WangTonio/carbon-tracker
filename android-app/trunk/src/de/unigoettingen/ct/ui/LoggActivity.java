package de.unigoettingen.ct.ui;

import de.unigoettingen.ct.R;
import de.unigoettingen.ct.container.Logg;
import de.unigoettingen.ct.data.DebugMessage;
import de.unigoettingen.ct.data.GenericObserver;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Displays the custom log ({@link Logg}) in real time.
 * @author Fabian Sudau
 *
 */
public class LoggActivity extends Activity implements OnClickListener, GenericObserver<Logg>{

	private Button uploadBtn;
	private ListView logListView;
	private Handler mainThread;
	
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
		Toast.makeText(this, "Upload not yet implemented", Toast.LENGTH_SHORT).show();
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
