package de.unigoettingen.ct.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import de.unigoettingen.ct.R;

/**
 * Displays a simple 'about/info' screen. The user can follow a displayed link.
 * @author Fabian Sudau
 *
 */
public class AboutActivity extends Activity implements OnClickListener{
	
	private TextView apacheLink;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		this.apacheLink = (TextView) findViewById(R.id.apache_link);
		this.apacheLink.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v == this.apacheLink){
			//starts a web browser to open up the web page
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(this.apacheLink.getText().toString()));
			try{
				startActivity(i);
			}
			catch(ActivityNotFoundException e){
				//in this case, there is no web browser installed.
				//we can silently ignore this and thus make the web feature unavailable.
				Log.e("WebBrowser Intent", "Activity not found - no web browser installed");
			}
		}
	}
	
}
