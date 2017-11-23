/**
 *	Author: Clément Jeannet
 *	Date: 	20 nov. 2017
 */
package main.game.actor.myEntities;

import main.game.actor.ActorGame;
import main.math.Vector;

public class SquareEntity extends RectangleEntity {

	public SquareEntity(ActorGame game, Vector position, float size, boolean fixed) {
		super(game, position, size, size, fixed);
	}

	public SquareEntity(ActorGame game, Vector position, String imagePath, float size, boolean fixed) {
		super(game, position, imagePath, size, size, fixed);
	}

}
