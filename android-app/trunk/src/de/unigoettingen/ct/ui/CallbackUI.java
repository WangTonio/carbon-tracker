package de.unigoettingen.ct.ui;

import de.unigoettingen.ct.service.TrackerService.TrackerServiceBinder;
import android.content.Intent;

/**
 * Describes a GUI offering some high level methods hiding concrete GUI code.
 * All of these methods must be called from Thread Main.
 * Some methods that are prompting the user for some input will return the result by using
 * a call back method of the {@link TrackerServiceBinder} object the GUI has a reference to.
 * @author Fabian Sudau
 *
 */
public interface CallbackUI {

	/**
	 * Displays the text in a simple, shortly visible pop up.
	 * @param text text to display
	 */
	public void diplayText(String text);
	
	/**
	 * If set to true, will somehow indicate, that the app performs work.
	 * Setting this to false will indicate an 'idle' state.
	 * @param running true to indicate a running app, false for idle
	 */
	public void indicateRunning(boolean running);
	
	/**
	 * If set to true, will show a loading animation blocking any user input.
	 * Using false will cancel any loading animation, if one was present.
	 * If no animation was present, will do nothing.
	 * @param running true for loading animation, false to cancel
	 */
	public void indicateLoading(boolean loading);
	
	/**
	 * 'Ugly' method that starts an external Activity and retrieves it's result which only Activities can do.
	 * The GUI will call the method {@link TrackerServiceBinder#onActivityResult(int, int, Intent)} after some time
	 * when the external Activity has returned.
	 * @param intent Intent to start
	 * @param requestCode request code to pass 
	 */
	public void startActivityForResult(Intent intent, int requestCode);
	
	/**
	 * Displays a pop up forcing the user to choose one of the presented options.
	 * The GUI will call the method {@link TrackerServiceBinder#returnUserHasSelected(int)} with the selected
	 * index after the user has made his/her choice.
	 * @param title title of the pop up
	 * @param options the options to display
	 */
	public void promptUserToChooseFrom(String title, String[] options);
}
