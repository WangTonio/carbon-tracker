package de.unigoettingen.ct.ui;

import android.content.Intent;

public interface CallbackUI {

	public void diplayText(String text);
	
	public void indicateRunning(boolean running);
	
	public void indicateLoading(boolean loading);
	
	public void startActivityForResult(Intent intent, int requestCode);
	
	public void promptUserToChooseFrom(String title, String[] options);
}
