import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.util.ArrayList;

import javax.swing.Timer;

import acm.graphics.*;
import rotations.GameImage;

public class GamePane extends GraphicsPane implements ActionListener, KeyListener {
	// LAYER DATA: 0 Is the front, above 0 puts things behind
	// TODO: private static final int GUI_LAYER = 0;
	private static final int CURSOR_LAYER = 1;
	private static final int PLAYER_LAYER = 3;
	// TODO: 
	//private static final int BOSS_LAYER = 4;
	// TODO: 
	//private static final int ENEMY_LAYER = 5;
	private static final int ROCK_LAYER = 6;
//	private static final int BG_LAYER1 = 7;
//	private static final int BG_LAYER2 = 8;
//	private static final int BG_LAYER3 = 9;
	
	// =============================================================================
	
	private static final int CURSOR_DIST = 50;
	private static final int CURSOR_SIZE = 10;
	private static final int TURN_POWER = 6;
	
	private MainApplication program; // You will use program to get access to all of the GraphicsProgram calls
	private GameConsole console; // Not a new one; just uses the one from MainApplication
	private GameImage player_img;
	private PlayerShip player;
	private Vector2 last_mouse_loc;
	
	private GLabel CURRENT_QUID_LABEL;
	private GLabel CURRENT_PLAYER_POS_LABEL;
	private GLabel CURRENT_MOUSE_POS_LABEL;
	private GLabel CURRENT_ASTEROIDS_LABEL;
	private GRect DEBUGGING_BOX;
	
	private ArrayList<GOval> POINT_TEST;
	
	private ArrayList<StaticRect> DEBUGGING_LINES;
	
	private ArrayList<StaticRect> DEBUGGING_ROWS;
	private ArrayList<StaticRect> DEBUGGING_COLS;
	
	private boolean CAN_MOVE = false;
	private boolean MOVEMENT_LOCK = false;
	private float MOVEMENT_CONSTANT = .0000001f;
	
	private boolean CAN_ALIGN = true;
	private boolean ALIGNMENT_LOCK = false;
	
	private ArrayList <GOval> cursor_dots;
	private ArrayList <Integer> pressed_keys;
	private ArrayList <Asteroid> drawn_rocks;
	private ArrayList <GameImage> drawn_ships;
	private float xAxis = 0;
	private float yAxis = 0;
	private int track_amount = 0;
	
	private Vector2 TRACKING_POSITION;

	//private Vector2 combat_offset = new Vector2(0,0); Unused for now; planned for centering player post combat smoothly
	
	public GamePane(MainApplication app) {
		this.program = app;
		init();
		TRACKING_POSITION = player.getPhysObj().getPosition();
		setOffset();
	}
	
	public void setupDebug() {
   		program.add(CURRENT_QUID_LABEL);
		program.add(DEBUGGING_BOX);
		program.add(CURRENT_ASTEROIDS_LABEL);
		program.add(CURRENT_PLAYER_POS_LABEL);
		program.add(CURRENT_MOUSE_POS_LABEL);
		
		DEBUGGING_BOX.setFillColor(Color.black);
		DEBUGGING_BOX.setFilled(true);
		
		CURRENT_QUID_LABEL.setColor(Color.white);
		CURRENT_QUID_LABEL.setFont("Arial");
		
		CURRENT_ASTEROIDS_LABEL.setColor(Color.white);
		CURRENT_ASTEROIDS_LABEL.setFont("Arial");
		
		CURRENT_PLAYER_POS_LABEL.setColor(Color.WHITE);
		CURRENT_PLAYER_POS_LABEL.setFont("Arial");
		
		CURRENT_MOUSE_POS_LABEL.setColor(Color.WHITE);
		CURRENT_MOUSE_POS_LABEL.setFont("Arial");
		
		DEBUGGING_ROWS = new ArrayList<StaticRect>();
		DEBUGGING_COLS = new ArrayList<StaticRect>();
		
		DEBUGGING_LINES = new ArrayList<StaticRect>();
		
		
		for(int i =0; i < PhysXLibrary.MAP_WIDTH; ++i) {
//			DEBUGGING_ROWS.add(new StaticRect(new Vector2((PhysXLibrary.QUADRANT_WIDTH * i), -PhysXLibrary.getMapHeight()), new Vector2(5, PhysXLibrary.getMapHeight())));
			DEBUGGING_ROWS.add(new StaticRect(new Vector2(-(PhysXLibrary.QUADRANT_WIDTH * i), 0), new Vector2(5, PhysXLibrary.getMapHeight())));
			program.add(DEBUGGING_ROWS.get(i).getRect());
			DEBUGGING_ROWS.get(i).getRect().setFillColor(Color.LIGHT_GRAY);
			DEBUGGING_ROWS.get(i).getRect().setFilled(true);
		}
		
		for(int i =0; i < PhysXLibrary.MAP_HEIGHT; ++i) {
			DEBUGGING_COLS.add(new StaticRect(new Vector2(0, -(PhysXLibrary.QUADRANT_HEIGHT * i)), new Vector2(PhysXLibrary.getMapWidth(), 5)));
			program.add(DEBUGGING_COLS.get(i).getRect());
			DEBUGGING_COLS.get(i).getRect().setFillColor(Color.LIGHT_GRAY);
			DEBUGGING_COLS.get(i).getRect().setFilled(true);
		}
		
		
		
		setSpriteLayer(CURRENT_QUID_LABEL, 1);
		setSpriteLayer(CURRENT_ASTEROIDS_LABEL, 1);
		
		setSpriteLayer(CURRENT_PLAYER_POS_LABEL, 1);
		setSpriteLayer(CURRENT_MOUSE_POS_LABEL, 1);
		setSpriteLayer(DEBUGGING_BOX, 6);
	}
	
	public void pointTest(Vector2 pos) {
		POINT_TEST = new ArrayList<GOval>();
		
		Vector2 player_pos = Camera.frontendToBackend(pos);
		POINT_TEST.add(new GOval(player_pos.getX(), player_pos.getY(), 25, 25));
		program.add(POINT_TEST.get(POINT_TEST.size() - 1));
		POINT_TEST.get(POINT_TEST.size() - 1).setFillColor(Color.blue);
		POINT_TEST.get(POINT_TEST.size() - 1).setFilled(true);
		
		Vector2 player_pos_to_front = Camera.backendToFrontend(player_pos);
		
		POINT_TEST.add(new GOval(player_pos_to_front.getX(), player_pos_to_front.getY(), 10, 10));
		program.add(POINT_TEST.get(POINT_TEST.size() - 1));
		POINT_TEST.get(POINT_TEST.size() - 1).setFillColor(Color.red);
		POINT_TEST.get(POINT_TEST.size() - 1).setFilled(true);
		
		Vector2 player_pos_back = Camera.frontendToBackend(player_pos_to_front);
		
		POINT_TEST.add(new GOval(player_pos_back.getX(), player_pos_back.getY(), 5, 5));
		program.add(POINT_TEST.get(POINT_TEST.size() - 1));
		POINT_TEST.get(POINT_TEST.size() - 1).setFillColor(Color.green);
		POINT_TEST.get(POINT_TEST.size() - 1).setFilled(true);
	}
	
	public void init() {
		CAN_MOVE = false;
		
		last_mouse_loc = new Vector2(0,0);
		cursor_dots = new ArrayList <GOval>();
		pressed_keys = new ArrayList <Integer>();
		drawn_rocks = new ArrayList <Asteroid>();
		drawn_ships = new ArrayList <GameImage>();
		
		CURRENT_QUID_LABEL = new GLabel("Current QUID", 15, 25);
		CURRENT_ASTEROIDS_LABEL = new GLabel("Current ASTEROIDS", 15, 50);
		CURRENT_PLAYER_POS_LABEL = new GLabel("Current P Position", 15, 75);
		CURRENT_MOUSE_POS_LABEL = new GLabel("Current M Position", 15, 100);
		DEBUGGING_BOX = new GRect(10, 10, 300, 100);
		
		
		console = program.getGameConsole();
		player = console.getPlayer();
		
		Vector2 pos = player.getPhysObj().getPosition();
		player_img = new GameImage("PlayerShip_Placeholder.png", pos.getX(), pos.getY());
		
		setSpriteLayer(player_img, PLAYER_LAYER);
		if (console.getPlayer() != null && player != null) {
			System.out.println("GamePane successfully accessed GameConsole's Player ship");
		}
		centerPlayer();
		System.out.println("Player spawning at: " + player.getPhysObj().getPosition().getX() + ", " + player.getPhysObj().getPosition().getY());
		player.getPhysObj().setQUID(console.physx().assignQuadrant(player.getPhysObj().getPosition()));
		console.physx().setActiveQuadrant(console.physx().assignQuadrant(player.getPhysObj().getPosition()));
		
		
	
		CAN_MOVE = true;
	}
	
	public void centerPlayer() {
		Vector2 frontPos = Camera.backendToFrontend(player.getPhysObj().getPosition());
		player_img.setLocationRespectSize(frontPos.getX(), frontPos.getY());
//		player_img.setLocation((MainApplication.WINDOW_WIDTH / 2) - (player_img.getWidth() / 2), (MainApplication.WINDOW_HEIGHT / 2) - (player_img.getHeight() /2));
	}
	
	public void setOffset() {
		Camera.setOffset(Vector2.Zero().minus(TRACKING_POSITION));
	}
	
	@Override
	public void showContents() {
		program.add(player_img);
	}

	@Override
	public void hideContents() {
		program.remove(player_img);
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		// Timer should start here

		//auto_fire.setInitialDelay(0);
		//auto_fire.start();
		pointTest(new Vector2(e.getX(), e.getY()));
		GObject obj = program.getElementAt(e.getX(), e.getY());
		if(obj == player_img) {
			program.switchToMenu();
		}
		else {
			System.out.println("Clicked empty space");
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
//		auto_fire.stop();
		System.out.println("Stopped shooting");
	}

	// Every tick of the global game clock calls all visual drawing necessary
	@Override
	public void actionPerformed(ActionEvent e) {
		TRACKING_POSITION = player.getPhysObj().getPosition();
		setOffset();
		// Player shoot every tick
//		System.out.println("Fired");
		if (CAN_ALIGN && !ALIGNMENT_LOCK) {
			ALIGNMENT_LOCK = true;
			alignReticle(last_mouse_loc);
		}
		
		if(CAN_MOVE) {
			movementLoop();
		}
		
		// TESTING!!! NOT FINAL
		drawAsteroids(console.getActiveAsteroids());
		
		if(console.IS_DEBUGGING) {
			CURRENT_QUID_LABEL.setLabel("Current QUID: " + player.getPhysObj().getQUID().toString());
			CURRENT_PLAYER_POS_LABEL.setLabel("Current Player V2: " + player.getPhysObj().getPosition().toString());
			CURRENT_MOUSE_POS_LABEL.setLabel("Current Mouse V2: " + Camera.frontendToBackend(last_mouse_loc).toString());
//			pointTest();
			
			drawStaticRect(DEBUGGING_ROWS);
			drawStaticRect(DEBUGGING_COLS);
		}
		/*
		ArrayList <Quadrant> quads = console.physx().getQuadrants();
		for (int i = 0; i < quads.size(); i++) {
			if (quads.get(i).getAsteroids().size() > 0) {
//				System.out.println(quads.get(i));
//				for (Asteroid rock : quads.get(i).getAsteroids()) {
//					System.out.println(rock);
//					if (rock.getSprite() == null) {
//						System.out.println("No sprite!");
//					}
//				}
				drawAsteroids(quads.get(i).getAsteroids());
			}
		}
		*/
		
		
	}
	
	// Logic is backwards because .acm is weird. Sprites must be sent to front before sending back to layer appropriately
	private void setSpriteLayer(GObject sprite, int layer) {
		sprite.sendToFront();
		for(int i = 0; i < layer; i++) {
			sprite.sendBackward();
		}
	}
	
	private void movementLoop() {
		
		if(MOVEMENT_LOCK)
			return;
		
		MOVEMENT_LOCK = true;
		int final_turn = 0;
		int final_forward = 0;
		// Set rotation
		if (pressed_keys.contains(KeyEvent.VK_A) && !pressed_keys.contains(KeyEvent.VK_D)) {
			final_turn = -1;
		}
		else if (!pressed_keys.contains(KeyEvent.VK_A) && pressed_keys.contains(KeyEvent.VK_D)) {
			final_turn = 1;
		}
		
		if (pressed_keys.contains(KeyEvent.VK_W) && !pressed_keys.contains(KeyEvent.VK_S)) {
			final_forward = 1;
		}
		else if (!pressed_keys.contains(KeyEvent.VK_W) && pressed_keys.contains(KeyEvent.VK_S)) {
			final_forward = -1;
		}
		
		double angle = -Math.toRadians(player.getAngle());
		float speed = (float) player.getStats().getSpeed() * 5 * final_forward;
		float cos = (float) Math.cos(angle) * speed;
		float sin = (float) Math.sin(angle) * speed;
		
		player_img.rotate(TURN_POWER * final_turn);
		player.adjustAngle(TURN_POWER * -final_turn);
//		player.getPhysObj().getPosition().add(new Vector2(cos, sin));
		player.moveVector2(new Vector2(cos, sin));
		player.getPhysObj().setQUID(console.physx().assignQuadrant(player.getPhysObj().getPosition()));
		Vector2 newFEPOS = Camera.backendToFrontend(player.getPhysObj().getPosition());
		player_img.setLocationRespectSize(newFEPOS.getX(), newFEPOS.getY());
		
		if (xAxis > 0 + MOVEMENT_CONSTANT) {
			player.adjustAngle(-TURN_POWER);
			player_img.rotate(TURN_POWER);
		} else if (xAxis < 0 - MOVEMENT_CONSTANT) {
			player.adjustAngle(TURN_POWER);
			player_img.rotate(-TURN_POWER);
		}
		
		player.setDx((float) player.getStats().getSpeed() * 5 * xAxis);
		player.setDy((float) player.getStats().getSpeed() * 5 * yAxis);
		
		console.physx().setActiveQuadrant(console.physx().assignQuadrant(player.getPhysObj().getPosition()));
		
		MOVEMENT_LOCK = false;
		
		//System.out.println("Player Pos: " + (int)player.getPhysObj().getPosition().getX() + ", " + (int)player.getPhysObj().getPosition().getY() + " | Angle: " + player.getAngle() + "*");
		// Someone changed the code, so I commented it out if we want to retain any information from it.
		/*
		if (yAxis > 0 + MOVEMENT_CONSTANT) {
			double angle = -Math.toRadians(player.getAngle());
			float speed = (float) player.getStats().getSpeed() * 5;
			float cos = (float) Math.cos(angle) * speed;
			float sin = (float) Math.sin(angle) * speed;
			
			player_img.move(cos, sin);
			player.getPhysObj().getPosition().add(new Vector2(cos, sin));
		}
		else if (yAxis < 0 - MOVEMENT_CONSTANT) {
    		double angle2 = -Math.toRadians(player.getAngle());
			float speed2 = (float) player.getStats().getSpeed() * -5;
			float cos2 = (float) Math.cos(angle2) * speed2;
			float sin2 = (float) Math.sin(angle2) * speed2;
		
			player_img.move(cos2, sin2);
			player.getPhysObj().getPosition().add(new Vector2(cos2, sin2));
		}
		*/
		
//        String pressed = "Pressed keys: ";
//        for (int i = 0; i < pressed_keys.size(); i++) {
//        	pressed += String.format("%X", pressed_keys.get(i)) + " ";
//        }
//		System.out.println(pressed);

//		System.out.println("\tImage Pos: " + (int)player_img.getX() + ", " + (int)player_img.getY());
		// this has to be the last call!

	}
	
	private Vector2 fromGPoint(GPoint point) {
		return Camera.frontendToBackend(new Vector2((float)point.getX(), (float)point.getY()));
	}
	
	private void drawStaticRect(ArrayList<StaticRect> lines) {
		for (int i = 0; i < lines.size(); i++) {
			// Get the offset
			StaticRect rect = lines.get(i);
//			float offset_x = rect.getPhysObj().getPosition().getX() - player.getPhysObj().getPosition().getX();
//			float offset_y = rect.getPhysObj().getPosition().getY() - player.getPhysObj().getPosition().getY();
			
			// Make a proper vector2 location according to the camera zoom scale
			Vector2 final_off = Camera.backendToFrontend(rect.getPhysObj().getPosition());

			// Are we already drawing that rock?
			if (!DEBUGGING_LINES.contains(rect)) {
				DEBUGGING_LINES.add(rect);
				setSpriteLayer(rect.getRect(), ROCK_LAYER);
			}
			
			// Set its location according to the offset
			if (DEBUGGING_LINES.contains(rect)) {
				rect.getRect().setLocation(final_off.getX(), final_off.getY());
			}
			
		}
	}
	
	private void drawAsteroids(ArrayList<Asteroid> asteroids) {
		
		if(console.IS_DEBUGGING) {
			CURRENT_ASTEROIDS_LABEL.setLabel("Current ASTER: " + asteroids.size());
		}
		for (int i = 0; i < asteroids.size(); i++) {
			// Get the offset
			Asteroid asteroid = asteroids.get(i);
//			Vector2 offset = asteroid.getPhysObj().getPosition().minus(player.getPhysObj().getPosition());
//			float offset_x = asteroid.getPhysObj().getPosition().getX() - player.getPhysObj().getPosition().getX();
//			float offset_y = asteroid.getPhysObj().getPosition().getY() - player.getPhysObj().getPosition().getY();
			
			// Make a proper vector2 location according to the camera zoom scale
			Vector2 frontEndPos = Camera.backendToFrontend(asteroid.getPhysObj().getPosition());

			// Are we already drawing that rock?
			if (!drawn_rocks.contains(asteroid)) {
				drawn_rocks.add(asteroid);
				program.add(asteroid.getSprite());
				setSpriteLayer(asteroid.getSprite(), ROCK_LAYER);
			}
			
			// Set its location according to the offset
			if (drawn_rocks.contains(asteroid)) {
				asteroid.getSprite().setLocation(frontEndPos.getX(), frontEndPos.getY());
			}
			
		}
		
		// Remove asteroids
		for (Asteroid asteroid : drawn_rocks) {
			if (!asteroids.contains(asteroid)) {
				program.remove(asteroid.getSprite());
			}
		}
		
	}
	
	// Might be a very taxing method. We can change to having a simple cursor at the mouse pointer. Luckily, won't draw more than 5 dots
	public void alignReticle(Vector2 coord) {
		//Vector2 root = player.getPhysObj().getPosition();
		Vector2 visual_root = new Vector2((float)(player_img.getX() + (player_img.getWidth()/2)), (float)(player_img.getY() + (player_img.getHeight()/2)));
		int distance = (int)Math.floor(PhysXLibrary.distance(visual_root, new Vector2(coord.getX(), coord.getY())));
		int dots = (distance / CURSOR_DIST) + 1;
		if (cursor_dots.size() < dots) {
			for (int i = 0; i < dots - cursor_dots.size(); i++) {
				GOval dot = new GOval(10, 10, CURSOR_SIZE, CURSOR_SIZE);
				dot.setColor(Color.black);
				cursor_dots.add(dot);
				program.add(dot);
				setSpriteLayer(dot, CURSOR_LAYER);
			}
		}
		if (cursor_dots.size() > dots) {
			for (int i = 0; i < cursor_dots.size() - dots; i++) {
				program.remove(cursor_dots.get(0));
				cursor_dots.remove(0);
			}
		}
		
		// Align them properly
		double off_x = (coord.getX() - visual_root.getX());
		double off_y = (coord.getY() - visual_root.getY());
		double theta_rad = Math.atan2(off_y, off_x);
		double unit_x = (Math.cos(theta_rad) * CURSOR_DIST);
		double unit_y = (Math.sin(theta_rad) * CURSOR_DIST);
		cursor_dots.get(0).setLocation(coord.getX() - (CURSOR_SIZE / 2), coord.getY() - (CURSOR_SIZE / 2));
		for (int i = 1; i < cursor_dots.size(); i++) {
			cursor_dots.get(i).setLocation(visual_root.getX() - (CURSOR_SIZE / 2) + (unit_x * i), visual_root.getY() - (CURSOR_SIZE / 2) + (unit_y * i));
		}
		//System.out.println("Distance: " + distance + ", Drawn: " + dots);
		ALIGNMENT_LOCK = false;
	}
	
	
	private boolean containsKey(int key) {
		if (!pressed_keys.contains(key)) {
			return false;
		}
		return true;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		last_mouse_loc.setXY(e.getX(), e.getY()); 
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		last_mouse_loc.setXY(e.getX(), e.getY()); 
	}
	
	// Key Presses work; the println statements were removed to prevent clutter in the console as I test
	@Override
    public void keyPressed(KeyEvent e) {
//		System.out.print("Press");
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_D || key == KeyEvent.VK_S || key == KeyEvent.VK_W) {
        	if (!containsKey(key)) {
        		pressed_keys.add(key);
        	}
        }
        
        if(key == KeyEvent.VK_L && !console.IS_DEBUGGING) {
        		console.startDebugView();
        		setupDebug();
        }
        
        if(key == KeyEvent.VK_K && console.IS_DEBUGGING) {
    			console.endDebugView();
    			
        		program.remove(CURRENT_QUID_LABEL);
        		program.remove(DEBUGGING_BOX);
        }
        
        if(console.IS_DEBUGGING) {
        		if(key == KeyEvent.VK_P && Camera.getBackwardRatio() != 2) {
        			console.changeGraphicsRatio(1, 2);
        		}
        		if(key == KeyEvent.VK_O && Camera.getForwardRatio() != 2) {
        			console.changeGraphicsRatio(2, 1);
        		}
        		if(key == KeyEvent.VK_I && Camera.getBackwardRatio() != 2 && Camera.getForwardRatio() != 2) {
        			console.changeGraphicsRatio(2, 2);
        		}
        		if(key == KeyEvent.VK_U && Camera.getBackwardRatio() != 1 && Camera.getForwardRatio() != 1) {
        			console.changeGraphicsRatio(1, 1);
        		}
        		if(key == KeyEvent.VK_M) {
        			System.out.println("SNAPSHOT --- ");
        			System.out.println("Asteroids to draw: " + console.getActiveAsteroids().size());
        			System.out.println("Player QUID: (" + player.getPhysObj().getQUID().getX() +", " + player.getPhysObj().getQUID().getY() + ", " + player.getPhysObj().getQUID().Order() + ")");
        		}
        }

        
        
        /*
        switch(key) {
        case KeyEvent.VK_A:
//    			System.out.print("ed : A");
    			xAxis = -(1 + MOVEMENT_CONSTANT);
    			break;
        case KeyEvent.VK_D:
//        		System.out.print("ed : D");
        		xAxis = (1 + MOVEMENT_CONSTANT);
        		break;
        case KeyEvent.VK_W:
//        		System.out.print("ed : W");
        		yAxis = (1 + MOVEMENT_CONSTANT);
        		break;
        case KeyEvent.VK_S:
//    			System.out.print("ed : S");
    			yAxis = -(1 + MOVEMENT_CONSTANT);
    			break;
    		default:
//    			System.out.print("ed : NONE");
    			break;
        */
//        System.out.println("");
    }

	@Override
    public void keyReleased(KeyEvent e) {
//		System.out.print("Release");
		int key = e.getKeyCode();
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_D || key == KeyEvent.VK_S || key == KeyEvent.VK_W) {
        	if (containsKey(key)) {
        		for (int i = 0; i < pressed_keys.size(); i++) {
        			if (pressed_keys.get(i) == key) {
        				pressed_keys.remove(i);
        			}
        		}
        	}
        }
		/*
		switch(key) {
			case KeyEvent.VK_A:
//				player.setDx(0);
//				System.out.print("d: A");
				
				if (xAxis + MOVEMENT_CONSTANT < 0) {
					xAxis = 0;
				}
				break;
			case KeyEvent.VK_D:
//				player.setDx(0);
//				System.out.print("d: D");
				if (xAxis + MOVEMENT_CONSTANT > 0) {
					xAxis = 0;
				}
				break;
			case KeyEvent.VK_W:
//				player.setDy(0);
//				System.out.print("d: W");
				if (yAxis + MOVEMENT_CONSTANT > 0) {
					yAxis = 0;
				}
				break;
			case KeyEvent.VK_S:
//				player.setDy(0);
//				System.out.print("d: S");
				if (yAxis + MOVEMENT_CONSTANT < 0) {
					yAxis = 0;
				}
				break;
			default:
//				System.out.print("d: NONE");
				break;
		}
		*/
//		System.out.println("");
    }
	
	
}