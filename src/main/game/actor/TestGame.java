/**
 *	Author: Clément Jeannet
 *	Date: 	22 nov. 2017
 */
package main.game.actor;

import main.game.actor.myEntities.Bike;
import main.game.actor.myEntities.EllipseCinematicEntity;
import main.game.actor.myEntities.Ground;
import main.game.actor.myEntities.RectangleEntity;
import main.io.FileSystem;
import main.math.Entity;
import main.math.Polyline;
import main.math.Vector;
import main.window.Window;

import java.awt.*;

public class TestGame extends ActorGame {

	Bike player;

	public boolean begin(Window window, FileSystem fileSystem) {
		super.begin(window, fileSystem);

		// TODO creation objects du program
		Polyline p = new Polyline(-50f, 0.f, 0.f, -2.f, 50.f, 0.f);

		new Ground(this, null, p);

		player = new Bike(this, new Vector(-0, 5));

		Entity e = super.newEntity(new Vector(-25,6),false);
		this.addActor(new EllipseCinematicEntity(e, 1f, .5f, Color.BLUE,Color.BLUE,.1f,1,0));
		this.addActor(new RectangleEntity(super.newEntity(new Vector(-28,4),false),"res/wood.4.png",1,.5f));

		this.setViewCandidate(e);
		return true;

	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		player.update(deltaTime);
	}
}
