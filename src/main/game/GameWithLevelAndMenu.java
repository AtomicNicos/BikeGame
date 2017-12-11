package main.game;

import java.util.List;

import main.game.GUI.menu.InGameMenu;
import main.game.GUI.menu.MainMenu;
import main.game.actor.Actor;
import main.game.actor.entities.Bike;
import main.game.actor.sensors.StartCheckpoint;
import main.game.levels.Level;
import main.io.FileSystem;
import main.window.Window;

/**
 * Represent a game with different level, and some menus
 * 
 * @see ActorGame
 */
public abstract class GameWithLevelAndMenu extends ActorGame {

	private List<Level> levels;
	private int currentLevel = 0;
	private boolean wasPlayed = false;

	private InGameMenu ingameMenu;
	private MainMenu mainMenu;

	@Override
	public boolean begin(Window window, FileSystem fileSystem) {
		super.begin(window, fileSystem);

		this.levels = createLevelList();
		this.ingameMenu = new InGameMenu(this, window);

		this.mainMenu = new MainMenu(this, window);

		return true;
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		mainMenu.update(deltaTime, 1);
		if (mainMenu.isOpen()) {
			mainMenu.draw(getCanvas());
			return;
		}
		ingameMenu.update(deltaTime, 1);
		if (ingameMenu.isOpen())
			ingameMenu.draw(getCanvas());

	}

	/** Go to the MainMenu */
	public void goToMainMenu() {
		this.destroyAllActors();
		mainMenu.setStatus(true);
		ingameMenu.setStatus(false);
	}

	/** Go to the next {@linkplain Level} */
	public void nextLevel() {
		wasPlayed = false;
		this.getGameManager().resetCheckpoint();
		beginLevel(currentLevel + 1);
	}

	/**
	 * Reset the current {@linkplain Level}
	 * @param wasPlayed whether its a respawn
	 */
	public void resetLevel() {
		this.wasPlayed = true;
		clearCurrentLevel();
		beginLevel(currentLevel);

		if (wasPlayed)
			levels.get(currentLevel).getSpawnCheckpoint().setTriggerStatus(false);
	}

	/** Clear all {@linkplain Actor} in the current {@linkplain Level} */
	public void clearCurrentLevel() {
		super.destroyAllActors();
	}

	/**
	 * Begin a {@linkplain Level}
	 * @param i : Number in the {@linkplain List} of the {@linkplain Level} to
	 * start
	 */
	public void beginLevel(int i) {
		this.getGameManager().inLevel(this);
		clearCurrentLevel();
		currentLevel = i;
		if (currentLevel > levels.size() - 1)
			currentLevel = 0;

		this.levels.get(currentLevel).createAllActors();
		super.addActor(levels.get(currentLevel).getActors());

		if (!wasPlayed) {
			// if first time level is loaded, 
			StartCheckpoint sc = levels.get(currentLevel).getSpawnCheckpoint();
			if (sc == null && (levels.get(currentLevel).getViewCandidate() == null)
					&& (levels.get(currentLevel).getPayload() == null))
				throw new NullPointerException("No PlayableEntity detected");
			getGameManager().setLastCheckpoint(sc);
		}
		super.setViewCandidate(levels.get(currentLevel).getViewCandidate());
		super.setPayload(levels.get(currentLevel).getPayload());

	}

	/**
	 * Create all the {@linkplain Level} for this {@linkplain GameWithLevels}
	 */
	protected abstract List<Level> createLevelList();

	/**
	 * @return the number of {@linkplain Level} in this
	 * {@linkplain GameWithLevels}
	 */
	public int numberOfLevel() {
		return levels.size();
	}
}
