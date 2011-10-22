package de.unigoettingen.ct.service;

/**
 * Describes an object capable of receiving status information from asynchronous subsystems.
 * @author Fabian Sudau
 *
 */
public interface SubsystemStatusListener {
	
	/**
	 * Listener method to be called from an {@link AsynchronousSubsystem} to indicate a status change.
	 * Implementors must deal with the fact that this method is getting called from lots of different threads.
	 * @param status the current (new) status of the subsystem
	 * @param sender back-reference to the caller
	 */
	public void notify(SubsystemStatus status, AsynchronousSubsystem sender);

}
