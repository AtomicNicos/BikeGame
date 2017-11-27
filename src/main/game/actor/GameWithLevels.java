/**
 *	Author: Clément Jeannet
 *	Date: 	27 nov. 2017
 */
package main.game.actor;

import java.util.List;

import main.game.actor.myEntities.FinishActor;
import main.game.levels.Level;
import main.io.FileSystem;
import main.window.Window;

public abstract class GameWithLevels extends ActorGame {

	ActorLevel actorLevel;
	FinishActor finishActor;
	
	@Override
	public boolean begin(Window window, FileSystem fileSystem) {
		super.begin(window, fileSystem);

		actorLevel = new ActorLevel(this, createLevelList());
		addActor(actorLevel);
		
		
		
		return true;
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
	}
	
	protected abstract List<Level> createLevelList();
	
	public void setFinishActor(FinishActor fa) {
		this.finishActor = fa;
	}
}