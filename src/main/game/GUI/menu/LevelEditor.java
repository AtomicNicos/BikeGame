/**
 *	Author: Clément Jeannet
 *	Date: 	4 déc. 2017
 */
package main.game.GUI.menu;

import main.game.ActorGame;
import main.game.GUI.Comment;
import main.game.GUI.GraphicalButton;
import main.game.GUI.actorBuilder.ActorBuilder;
import main.game.GUI.actorBuilder.BikeBuilder;
import main.game.GUI.actorBuilder.GroundBuilder;
import main.game.GameWithLevelAndMenu;
import main.game.actor.Actor;
import main.game.actor.entities.Bike;
import main.game.actor.entities.Terrain;
import main.game.graphics.BetterTextGraphics;
import main.game.graphics.Graphics;
import main.game.graphics.ShapeGraphics;
import main.io.Save;
import main.math.*;
import main.math.Polygon;
import main.math.Shape;
import main.window.Canvas;
import main.window.Window;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

/**
 * {@linkplain LevelEditor} used to create, edit and add {@linkplain Actor}s to
 * the game
 */
public class LevelEditor implements Graphics {

	// actorBuilder stuff
	private ArrayList<ActorBuilder> actorBuilders = new ArrayList<>();
	private GroundBuilder gb;
	private BikeBuilder bb;

	// stuffs
	private ActorGame game;
	private ActorMenu actorMenu;
	private Window window;
	private boolean open = false;

	// Camera stuff
	private Vector cameraPosition = Vector.ZERO;
	private static final float windowZoom = 30f;
	private static final float cameraSpeed = 20f;
	/** Curent zoom, between {@value #minZoom} and {@value #maxZoom} */
	private float zoom = 1f;
	/** Maximum zoom value : {@value #maxZoom} */
	private static final float maxZoom = 2f;
	/** Maximum zoom value : {@value #minZoom} */
	private static final float minZoom = 0.4f;
	private float maxPosX = 120;
	private float maxPosY = 30;
	private float cameraAcceleration = .4f;
	private float xPP = 1;// camera acceleration
	private final float maxCameraXPP = 3;

	// Grid parameters
	private ArrayList<ShapeGraphics> gridLine = new ArrayList<>();
	private ShapeGraphics axeX, axeY;
	private int lineNumberX = 120, lineNumberY = 66;
	private float lineThickness = .01f;

	// button font size
	private float fontSize = .63f;
	private float butonDepth = 51;

	// position showing
	private Vector redSquarePosition = Vector.ZERO;
	private ShapeGraphics redSquareGraphics;
	private boolean showRedSquare = false; // true if the red square is show
	private boolean hasClicked = false; // true if we clicked once to get
										// position on screen
	private BetterTextGraphics redSquarePosText;

	// activate/desactivate position pointer
	private GraphicalButton getPositionButton;
	private Vector getPositionButtonPosition = new Vector(-29, 14);
	private final String getPosButtonText = "Positionneur";

	// reset camera button + position (absolue sur l'ecran)
	private GraphicalButton cameraResetPosition;
	private Vector cameraResetButtonPosition = new Vector(-21, 14);
	private final String resetCameraButtonText = "Reset camera";

	// test play button
	private GraphicalButton playButton;
	private Vector playButtonPosition = new Vector(0, 14);
	private final String playButtonText = "Play";
	private final String playButtonEditText = "Edit";

	// save button
	private GraphicalButton saveButon;
	private final String saveButonText = "Save";
	private Vector saveButonPos = new Vector(4, 14);
	private final String currentSaveName;
	private Comment error;
	private String errorText;
	private float errorTimer = 0;
	private final float maxErrorTimer = 2f;
	private boolean displayErrorText = false;

	// back to main menu button
	private GraphicalButton backToMainMenu;
	private String backText = "Back to menu";
	private Vector backPos = new Vector(-8, 14);

	/**
	 * Create a new {@linkplain LevelEditor}
	 * @param game {@linkplain ActorGame} where this {@linkplain LevelEditor}
	 * belong
	 * @param window {@linkplain Window} graphical context
	 * @param mainMenu {@linkplain MainMenu} where this {@linkplain LevelEditor}
	 * is created
	 */
	public LevelEditor(GameWithLevelAndMenu game, Window window, MainMenu mainMenu) {
		this.game = game;
		this.window = window;
		this.actorMenu = new ActorMenu(game, this, window, Color.LIGHT_GRAY);

		// red square
		Polygon p = new Polygon(0, 0, 0, 1, 1, 1, 1, 0);
		redSquareGraphics = new ShapeGraphics(p, Color.RED, null, 0, .5f, 100);
		redSquarePosText = new BetterTextGraphics(game, "0.0", 1f, new Vector(1, 1));
		redSquarePosText.setDepth(101);
		redSquarePosText.setParent(redSquareGraphics);

		game.setGameFreezeStatus(true);
		gridLine = grid();

		// create axis
		float times = 3f;
		float t2 = 12f;
		Shape line1 = new Polygon(-maxPosX * t2, -lineThickness * times, maxPosX * t2, -lineThickness * times,
				maxPosX * t2, lineThickness * times, -maxPosX * t2, lineThickness * times);
		axeX = new ShapeGraphics(line1, Color.BLACK, null, 0, 1, 50);

		Shape line2 = new Polygon(-lineThickness * times, -maxPosY * t2, -lineThickness * times, maxPosY * t2,
				lineThickness * times, maxPosY * t2, lineThickness * times, -maxPosY * t2);
		axeY = new ShapeGraphics(line2, Color.BLACK, null, 0, 1, 50);

		// get or not the position on screen when click
		getPositionButton = new GraphicalButton(game, getPositionButtonPosition, getPosButtonText, fontSize);
		getPositionButton.setDepth(butonDepth);
		getPositionButton.addOnClickAction(() -> {
			hasClicked = false;
			showRedSquare = !showRedSquare;
		});

		// reset the camera when clicked
		cameraResetPosition = new GraphicalButton(game, cameraResetButtonPosition, resetCameraButtonText, fontSize);
		cameraResetPosition.setDepth(butonDepth);
		cameraResetPosition.addOnClickAction(() -> {
			cameraPosition = Vector.ZERO;
			zoom = 1;
		});

		// playButton, centered
		playButton = new GraphicalButton(game, playButtonPosition, playButtonText, fontSize);
		playButton.setDepth(butonDepth);
		playButtonPosition = new Vector(-playButton.getWidth() / 2, playButtonPosition.y);
		playButton.setAnchor(playButtonPosition);
		playButton.addOnClickAction(() -> {

			error.setParent(playButton);
			if (isBusy() && game.isGameFrozen()) {
				errorText = "Please finish editing the actor";
				displayErrorText = true;
				errorTimer = 0;
				return;
			}
			game.setGameFreezeStatus(!game.isGameFrozen());
			if (!game.isGameFrozen()) {// play
				playButton.setText(playButtonEditText, fontSize);
				game.addActor(getActors());
				game.setPayload(bb.getActor());
				game.setViewCandidate(this.bb.getActor());
			} else { // edit
				playButton.setText(playButtonText, fontSize);
				
				for (ActorBuilder ab : actorBuilders) {
					ab.reCreate();
				}
				game.setViewCandidate(null);
				
			}
		});

		backToMainMenu = new GraphicalButton(game, backPos, backText, fontSize);
		backToMainMenu.addOnClickAction(() -> {
			game.destroyAllActors();
			this.close();
		});

		// get available name for the save
		File[] saves = Save.availableSaves(game);
		ArrayList<String> savesNames = new ArrayList<>();
		for (File f : saves) {
			savesNames.add(f.getName());
		}
		String temp = "";
		for (int i = 1; i < saves.length + 2; i++) {
			if (!savesNames.contains("save" + ((i < 10) ? "0" : "") + i)) {
				temp = "save" + ((i < 10) ? "0" : "") + i;
				break;
			}
		}
		currentSaveName = (temp);
		System.out.println(currentSaveName);

		// create save button
		saveButon = new GraphicalButton(game, saveButonPos, saveButonText, fontSize);
		saveButon.setDepth(butonDepth);
		saveButon.addOnClickAction(() -> {
			errorText = null;
			displayErrorText = false;
			error.setParent(saveButon);
			if (isBusy()) {
				errorText = "Please finish editing the actor";
				displayErrorText = true;
				errorTimer = 0;
				return;
			}
			if (this.gb == null)
				errorText = "Please create a ground";
			if (this.bb == null)
				errorText = "Please create a bike";
			if (this.gb == null && this.bb == null)
				errorText = "Please create a bike and a ground";
			if (errorText == null) {
				System.out.println("start saving");
				game.save(getActors(), bb.getBike(), bb.getActor(), currentSaveName);
				errorText = "Actors saved sucessfully";
			}
			displayErrorText = true;

		});
		error = new Comment(game, "");
		error.setParent(saveButon);
		error.setAnchor(new Vector(saveButon.getWidth() / 2, -4));
	}

	/**
	 * Simulates a single time step.
	 * @param deltaTime elapsed time since last update, in seconds, non-negative
	 */
	public void update(float deltaTime) {

		if (!game.isGameFrozen()) {

			float z = game.getViewScale() / windowZoom;
			playButton.update(deltaTime, z);

			return;
		}
		// camera accelaration
		if (game.getKeyboard().get(KeyEvent.VK_CONTROL).isDown()) {
			xPP += cameraAcceleration * deltaTime;
			xPP = (xPP >= maxCameraXPP) ? maxCameraXPP : xPP;
		}
		if (game.getKeyboard().get(KeyEvent.VK_CONTROL).isReleased())
			xPP = 1;

		// camera controls
		float posX = cameraPosition.x;
		float posY = cameraPosition.y;
		if (game.getKeyboard().get(KeyEvent.VK_W).isDown()) {
			posY += deltaTime * cameraSpeed * xPP;
		}
		if (game.getKeyboard().get(KeyEvent.VK_S).isDown()) {
			posY += -deltaTime * cameraSpeed * xPP;
		}
		if (game.getKeyboard().get(KeyEvent.VK_A).isDown()) {
			posX += -deltaTime * cameraSpeed * xPP;
		}
		if (game.getKeyboard().get(KeyEvent.VK_D).isDown()) {
			posX += deltaTime * cameraSpeed * xPP;
		}

		posX = (posX >= maxPosX) ? maxPosX : posX;
		posY = (posY >= maxPosY) ? maxPosY : posY;
		posX = (posX <= -maxPosX) ? -maxPosX : posX;
		posY = (posY <= -maxPosY) ? -maxPosY : posY;
		cameraPosition = new Vector(posX, posY);

		// zoom control
		if (game.getMouse().getMouseScrolledUp()) {
			zoom -= .1f;
			zoom = (zoom < minZoom) ? minZoom : zoom;
		} else if (game.getMouse().getMouseScrolledDown()) {
			zoom += .1f;
			zoom = (zoom > maxZoom) ? maxZoom : zoom;
		}

		// finalement placement de la camera
		window.setRelativeTransform(Transform.I.scaled(windowZoom * zoom).translated(cameraPosition));
		// game.setCameraPosition(cameraPosition);

		// ligne de placement
		gridLine = grid();

		// right click menu
		if (!isBusy())
			actorMenu.update(deltaTime, zoom);
		else
			actorMenu.setStatus(false);

		// positionneur stuff
		if (showRedSquare && game.getMouse().getLeftButton().isPressed()) {
			hasClicked = true;
			redSquarePosition = game.getMouse().getPosition();
			redSquareGraphics.setRelativeTransform(Transform.I.translated(ExtendedMath.floor(redSquarePosition)));
			redSquarePosText
					.setText((int) Math.floor(redSquarePosition.x) + ", " + (int) Math.floor(redSquarePosition.y));
		}

		// buttons update
		cameraResetPosition.update(deltaTime, zoom);
		getPositionButton.update(deltaTime, zoom);
		playButton.update(deltaTime, zoom);
		backToMainMenu.update(deltaTime, zoom);

		// current actors update
		ActorBuilder current = null;
		for (ActorBuilder actor : actorBuilders) {
			actor.update(deltaTime, zoom);
			if (!actor.isDone() && !actor.equals(gb))
				current = actor;
			if (actor.isHovered() && game.getMouse().getRightButton().isPressed())
				actor.edit();
		}

		// destroy selected actor if delete is pressed
		if (current != null && game.getKeyboard().get(KeyEvent.VK_DELETE).isPressed()) {
			current.destroy();
			actorBuilders.remove(current);
		}

		// save button and stuff update
		saveButon.update(deltaTime, zoom);
		error.update(deltaTime, zoom);

		if (displayErrorText && errorTimer > maxErrorTimer) {
			displayErrorText = false;
			errorTimer = 0;
		} else if (displayErrorText) {
			error.setText(errorText);
			errorTimer += deltaTime;
		}

	}

	@Override
	public void draw(Canvas canvas) {

		// draw button play
		playButton.draw(canvas);

		// if we are playing, return
		if (!game.isGameFrozen()) {
			return;
		}

		// draw right click menu
		actorMenu.draw(canvas);

		// draw current actors
		for (ActorBuilder actor : actorBuilders) {
			actor.draw(canvas);
		}

		// draw grid

		for (ShapeGraphics sg : gridLine) {
			sg.draw(canvas);
		}
		// draw axis
		axeX.draw(canvas);
		axeY.draw(canvas);

		if (showRedSquare && hasClicked) {
			redSquareGraphics.draw(canvas);
			redSquarePosText.draw(canvas);
		}
		getPositionButton.draw(canvas);
		cameraResetPosition.draw(canvas);
		backToMainMenu.draw(canvas);

		// draw button save
		saveButon.draw(canvas);
		if (displayErrorText)
			error.draw(canvas);

	}

	/** @return the list of all {@linkplain Actor}s created */
	public ArrayList<Actor> getActors() {
		ArrayList<Actor> a = new ArrayList<>();
		for (ActorBuilder ab : actorBuilders) {
			a.add(ab.getActor());
		}
		return a;
	}

	/** @param actor {@linkplain ActorBuilder} to add */
	public void addActorBuilder(ActorBuilder actor) {
		actorBuilders.add(actor);
	}

	/**
	 * Make sure we have a unique {@linkplain Terrain}
	 * @param {@linkplain Terrain} to add to the game
	 */
	public void addGround() {
		if (this.gb != null) {
			gb.continueBuilding();
		} else {
			gb = new GroundBuilder(game);
			actorBuilders.add(gb);
		}
	}

	/**
	 * Make sure we have a unique {@linkplain Bike}
	 * @param bike {@linkplain Bike} to add to the game
	 */
	public void addBike(BikeBuilder bike) {
		if (this.bb != null) {
			this.bb.getActor().destroy();
			actorBuilders.remove(this.bb);
		}

		this.bb = bike;
		actorBuilders.add(bike);
	}

	/** @return an ArrayList containing an updated grid */
	private ArrayList<ShapeGraphics> grid() {
		ArrayList<ShapeGraphics> lines = new ArrayList<>();

		// lignes en |
		Shape t = new Polygon(-lineThickness, -lineNumberY / 2, -lineThickness, lineNumberY / 2, lineThickness,
				lineNumberY / 2, lineThickness, -lineNumberY / 2);

		Vector c2 = ExtendedMath.floor(cameraPosition);
		for (int i = 0; i < lineNumberX; i++) {
			lines.add(new ShapeGraphics(t, Color.BLACK, null, 0, .8f, -3));
			lines.get(i).setRelativeTransform(
					Transform.I.translated(new Vector(1f * i - lineNumberX / 2, 0)).translated(c2));
		}

		// lignes en --
		Shape t2 = new Polygon(-lineNumberX / 2, -lineThickness, lineNumberX / 2, -lineThickness, lineNumberX / 2,
				lineThickness, -lineNumberX / 2, lineThickness);
		for (int i = 0; i < lineNumberX; i++) {
			lines.add(new ShapeGraphics(t2, Color.BLACK, null, 0, .8f, -3));
			lines.get(i + lineNumberX).setRelativeTransform(
					Transform.I.translated(new Vector(0, 1f * i - lineNumberX / 2)).translated(c2));
		}
		return lines;

	}

	public float getZoom() {
		return this.zoom;
	}

	/**
	 * @return the current {@link #zoom}, between {@value #minZoom} and
	 * {@value #maxZoom}
	 */
	public float getWindowScale() {
		return windowZoom;
	}

	/**
	 * @return the current camera position
	 */
	public Vector getCameraPosition() {
		return this.cameraPosition;
	}

	/**
	 * Destroy this
	 */
	public void destroy() {
		this.actorMenu.destroy();
		for (ActorBuilder a : actorBuilders)
			a.destroy();
		this.cameraResetPosition.destroy();
		this.playButton.destroy();
		this.getPositionButton.destroy();
		this.cameraResetPosition.destroy();
		this.backToMainMenu.destroy();
	}

	/** @return whether an {@linkplain ActorBuilder} is being created/edited */
	public boolean isBusy() {
		// boolean temp = true;
		for (ActorBuilder actor : actorBuilders) {
			// make sure no ActorBuilder is being created
			if (!actor.isDone() || actor.isHovered())
				return true;
			// temp = actor.isDone() & !actor.isHovered() & temp;
		}
		return false;
	}

	/** @return whether we are in this {@linkplain LevelEditor} */
	public boolean isOpen() {
		return open;
	}

	/** Set the status of this {@linkplain LevelEditor} to true */
	public void open() {
		this.open = true;
	}

	/** Set the status of this {@linkplain LevelEditor} to false */
	public void close() {
		this.open = false;
		this.destroy();
	}

}
