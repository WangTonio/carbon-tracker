package de.unigoettingen.ct.ui;

import de.unigoettingen.ct.service.TrackerService.TrackerServiceBinder;
import android.content.Intent;

/**
 * Describes a GUI offering some high level methods hiding concrete GUI code.
 * All of these methods must be called from Thread Main.
 * Some methods, that are prompting the user for some input, will return the result by using
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
	 * The GUI will call the method {@link TrackerServiceBinder#returnUserHasSelected(int,int)} with the selected
	 * index after the user has made his/her choice.
	 * @param promptCode a request code, that will be used again, when the callback mentioned above is performed. 
	 * 			Can be used to match requests to responses.
	 * @param title title of the pop up
	 * @param options the options to display
	 */
	public void promptUserToChooseFrom(int promptCode, String title, String[] options);
	
	/**
	 * Displays a pop up asking the provided question. The user will be presented the options
	 * 'yes' or 'no'. The GUI will call the method {@link TrackerServiceBinder#returnUserHasSelected(int,int)}
	 * with 1 as the second argument, if the user chose 'yes', otherwise 0.
	 * @param promptCode  a request code, that will be used again, when the callback mentioned above is performed. 
	 * 			Can be used to match requests to responses.
	 * @param question the question to display
	 */
	public void promptUserToChooseYesOrNo(int promptCode, String question);
	
	/**
	 * Displays a pop up showing the specified message and prompting the user to enter text into a free text field.
	 * The GUI will call {@link TrackerServiceBinder#returnUserHasEntered(int, String)}} returning the user input.
	 * If the user has clicked 'cancel', null is returned.
	 * @param promptCode a request code, that will be used again, when the callback mentioned above is performed. 
	 * Can be used to match requests to responses.
	 * @param message will be displayed
	 */
	public void promtUserToEnterText(int promptCode, String message);
}
