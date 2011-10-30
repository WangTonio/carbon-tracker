package de.unigoettingen.ct.service;


/**
 * Describes a subsystem (a kind of Facade) which performs it's work in an own encapsulated Thread or possibly
 * in a set of Threads. All of these methods are asynchronous, which means, that they return immediately and cause work to be done
 * in another Thread. The caller should register itself as the {@link SubsystemStatusListener} by using the method
 * {@link AsynchronousSubsystem#setStatusListener(SubsystemStatusListener)}. Doing so, the caller will receive
 * asynchronous call backs (issued from another Thread!) indicating the current status of this AsynchronousSubsystem.
 * The caller must ensure to handle these callbacks in the caller's preferred Thread.
 * @author Fabian Sudau
 *
 */
public interface AsynchronousSubsystem {

	/**
	 * Sets the listener of this subsystem. The listener will receive notifications whenever the status of this
	 * subsystem changes. (Think of this as a return value of asynchronous methods).
	 * @param listener
	 */
	public abstract void setStatusListener(SubsystemStatusListener listener);

	/**
	 * Prepares the subsystem, e.g. performs some mandatory work that has to be done, before {@link #start()} can be called.
	 * The subsystem will immediately respond with {@link SubsystemStatus.States.SETTING_UP}. After the work has been
	 * done and no error has occurred, the subsystem responds with {@link SubsystemStatus.States.SET_UP}.
	 */
	public abstract void setUp();

	/**
	 * Call this method only, if this is in state  {@link SubsystemStatus.States.SET_UP}. The subsystem will start
	 * it's ongoing routine in an infinite loop. It will return  {@link SubsystemStatus.States.IN_PROGRESS} and remain in that
	 * state as long as no error occurres.
	 */
	public abstract void start();

	/**
	 * Calls this method only, if this is in state  {@link SubsystemStatus.States.IN_PROGRESS}. The subsystem will terminate
	 * it's infinite loop logic gracefully and return  {@link SubsystemStatus.States.STOPPED_BY_USER} after completion.
	 */
	public abstract void stop();

}