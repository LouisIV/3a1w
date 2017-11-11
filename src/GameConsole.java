import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.*;

import acm.program.GraphicsProgram;

public class GameConsole extends GraphicsProgram{
	
	public static boolean IS_DEBUGGING;
	
	private ArrayList<Ship> ships = new ArrayList<Ship>();
	//private PlayerShip player;
	private PlayerShip player;
	private MapCreator mapCreator;
	private PhysX physx; // The controller for all things
	private int skillPoints;
//	private GameTimer clock = new GameTimer();
	
	public GameConsole() {
		endDebugView();
		
		// Create the universe. For now, only a single quadrant
		System.out.println("Made a new game console");
		physx = new PhysX(PhysXLibrary.QUADRANT_HEIGHT, PhysXLibrary.QUADRANT_WIDTH, PhysXLibrary.MAP_WIDTH, PhysXLibrary.MAP_HEIGHT);
		mapCreator = new MapCreator();
		physx.addQuadrants(mapCreator.createMap());
		
		Quadrant playerSpawn = mapCreator.getPlayerSpawn();
		float pos_x = (playerSpawn.getQUID().getX() * PhysXLibrary.QUADRANT_WIDTH) - (PhysXLibrary.QUADRANT_WIDTH / 2);
		float pos_y = (playerSpawn.getQUID().getY() * PhysXLibrary.QUADRANT_HEIGHT) - (PhysXLibrary.QUADRANT_HEIGHT / 2);
		Vector2 pos = new Vector2(pos_x, pos_y);
		System.out.println("pos = " + pos_x + ", " + pos_y);
		
		CircleCollider playerCollider = new CircleCollider(Vector2.Zero(), 1);
		PhysXObject playerPhysXobj = new PhysXObject(playerSpawn.getQUID(), pos, playerCollider);
		player = new PlayerShip(playerPhysXobj, 1, new ShipStats(1,1,1,1));
		player.setDxDy(Vector2.Zero());
		System.out.println("Player Pos before GamePane: " + player.getPhysObj().getPosition().getX() + ", " + player.getPhysObj().getPosition().getY());
	}
	
	
	public void startDebugView() {
		IS_DEBUGGING = true;
	}
	
	public void endDebugView() {
		IS_DEBUGGING = false;
	}
	
	public PhysX physx() {
		return physx;
	}
	
	public ArrayList<Asteroid> getActiveAsteroids() {
		ArrayList<Asteroid> Asteroids = new ArrayList<Asteroid>();
		ArrayList<Quadrant> quads = physx.getActiveQuadrants();
		for (Quadrant quad : quads) {
			Asteroids.addAll(quad.getAsteroids());
		}
		return Asteroids;
	}
	
	public ArrayList<EnemyShip> getActiveShips() {
		ArrayList<EnemyShip> EnemyShips = new ArrayList<EnemyShip>();
		ArrayList<Quadrant> quads = physx.getActiveQuadrants();
		for (Quadrant quad : quads) {
			EnemyShips.addAll(quad.getShips());
		}
		return EnemyShips;
	}
	
	public PlayerShip getPlayer() {
		return this.player;
	}
}


