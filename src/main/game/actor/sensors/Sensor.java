package main.game.actor.sensors;

import main.game.actor.Actor;

public interface Sensor extends Actor {
	boolean getSensorDetectionStatus();
	boolean isOccupied();

	void runAction(Runnable action, float time);

	/**
	 * Simulates a single time step.
	 * @param deltaTime elapsed time since last update, in seconds, non-negative
	 */
	default void update(float deltaTime) {
	}

	/**
	 * Default destroy, don't do anything
	 * */
	default void destroy() {
	}
}
