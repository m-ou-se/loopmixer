package nl.tudelft.ti1100a.audio;

/**
 * An adapter to simplify RhythmListeners that do not need all methods of a RhythmListener.
 * 
 * @author Mara Bos (m-ou.se@m-ou.se)
 */
public abstract class RhythmListenerAdapter implements RhythmListener {

	/** {@inheritDoc} */
	@Override public void beat() {}

	/** {@inheritDoc} */
	@Override public void measure() {}

	/** {@inheritDoc} */
	@Override public void isPlayingChanged() {}

	/** {@inheritDoc} */
	@Override public void tempoChanged(float factor) {}

	/** {@inheritDoc} */
	@Override public void positionChanged() {}
	
}
