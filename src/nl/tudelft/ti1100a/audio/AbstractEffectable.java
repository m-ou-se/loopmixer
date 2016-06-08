package nl.tudelft.ti1100a.audio;

import java.util.ArrayList;
import java.util.List;

import ddf.minim.AudioEffect;
import ddf.minim.Effectable;

/**
 * An abstract implementation of Effectable.
 * 
 * It keeps track of the effects and will apply only the enabled ones to the signal that passes through {@code process(...)}.
 * 
 * @author Mara Bos (m-ou.se@m-ou.se)
 */
public abstract class AbstractEffectable implements Effectable {
	
	private static class Effect {
		public boolean enabled;
		public AudioEffect effect;
		public Effect(AudioEffect effect){
			this.effect = effect;
			enabled = true;
		}
		@Override
		public boolean equals(Object x){
			if (x instanceof Effect){
				if (effect == null) return ((Effect) x).effect == null;
				return effect.equals(((Effect) x).effect);
			} else if (x instanceof AudioEffect) {
				return x.equals(effect);
			} else {
				return false;
			}
		}
	}
	
	protected List<Effect> effects;
	
	public AbstractEffectable(){
		effects = new ArrayList<Effect>();
	}

	/** {@inheritDoc} */
	@Override public void addEffect(AudioEffect effect) {
		effects.add(new Effect(effect));
	}

	/** {@inheritDoc} */
	@Override public void clearEffects() {
		effects.clear();
	}

	/** {@inheritDoc} */
	@Override public void enableEffect(int i) {
		effects.get(i).enabled = true;
	}

	/** {@inheritDoc} */
	@Override public void enableEffect(AudioEffect effect) {
		int i = effects.lastIndexOf(effect);
		if (i != -1) enableEffect(i);
	}

	/** {@inheritDoc} */
	@Override public void disableEffect(int i) {
		effects.get(i).enabled = false;
	}

	/** {@inheritDoc} */
	@Override public void disableEffect(AudioEffect effect) {
		int i = effects.lastIndexOf(effect);
		if (i != -1) disableEffect(i);
	}

	/** {@inheritDoc} */
	@Override public int effectCount() {
		return effects.size();
	}

	/** {@inheritDoc} */
	@Override public void effects() {
		for(Effect e : effects) e.enabled = true;
	}

	/** {@inheritDoc} */
	@Override public void noEffects() {
		for(Effect e : effects) e.enabled = false;
	}

	/** {@inheritDoc} */
	@Override public AudioEffect getEffect(int i) {
		return effects.get(i).effect;
	}

	/** {@inheritDoc} */
	@Override public boolean hasEffect(AudioEffect effect) {
		return effects.contains(effect);
	}

	/** {@inheritDoc} */
	@Override public boolean isEffected() {
		for(Effect e : effects) if (e.enabled) return true;
		return false;
	}

	/** {@inheritDoc} */
	@Override public boolean isEnabled(AudioEffect effect) {
		int i = effects.indexOf(effect);
		if (i == -1) return false;
		return effects.get(i).enabled;
	}

	/** {@inheritDoc} */
	@Override public void removeEffect(AudioEffect effect) {
		effects.remove(effect);
	}

	/** {@inheritDoc} */
	@Override public AudioEffect removeEffect(int i) {
		return effects.remove(i).effect;
	}
	
	/**
	 * Apply all enabled effects to the supplied data.
	 * 
	 * @param data The audio data to process.
	 */
	protected void process(float[] data){
		for(Effect e : effects) if (e.enabled) e.effect.process(data);
	}
	
	/**
	 * Apply all enabled effects to the supplied data.
	 * 
	 * @param left The left channel of the audio data to process.
	 * @param right The right channel of the audio data to process.
	 */
	protected void process(float[] left, float[] right){
		for(Effect e : effects) if (e.enabled) e.effect.process(left,right);
	}
	
}
