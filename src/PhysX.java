// PhysX Class
import java.util.*;
public class PhysX {
	// good to be back
	
	private float	QUADRANT_HEIGHT;
	private float	QUADRANT_WIDTH;
	private int 		MAP_WIDTH;
	private int 		MAP_HEIGHT;
	
	private boolean COLLISION_LOCK;
	
	private ArrayList<Quadrant> Quadrants;
	private QuadrantID ActiveQuadrant;
	
	public PhysX() {
		Quadrants = new ArrayList<Quadrant>();
		ActiveQuadrant = new QuadrantID(0,0,0);
	}
	
	public void verifyOrder() {
		int order = 0;
		for(Quadrant quad : Quadrants) {
			System.out.println("ORDER: " +quad.getQUID().Order() + " , INDEX: " + order);
			order++;
		}
	}
	
	public PhysX(float QUADRANT_HEIGHT, float QUADRANT_WIDTH, int MAP_WIDTH, int MAP_HEIGHT) {
		this.QUADRANT_HEIGHT = QUADRANT_HEIGHT;
		this.QUADRANT_WIDTH = QUADRANT_WIDTH;
		this.MAP_WIDTH = MAP_WIDTH;
		this.MAP_HEIGHT = MAP_HEIGHT;
		
		Quadrants = new ArrayList<Quadrant>();
		ActiveQuadrant = new QuadrantID(0,0,0);
	}
	
	public QuadrantID assignQuadrant(Vector2 assign) {
		
		int order = 0;
		for (int a = 0; a < MAP_HEIGHT; ++a) {
			for( int b =0; b < MAP_WIDTH; ++b) {
				if (assign.getY() > a * PhysXLibrary.QUADRANT_HEIGHT
						&& assign.getY() < (a + 1) * PhysXLibrary.QUADRANT_HEIGHT
						&& assign.getX() > b * PhysXLibrary.QUADRANT_WIDTH
						&& assign.getX() < (b + 1) * PhysXLibrary.QUADRANT_WIDTH) {
					return new QuadrantID(b,a,order);
				}
				order ++;
			}
		}
		/*
		int y_pos = (int)(assign.getY() / PhysXLibrary.QUADRANT_HEIGHT);
		int x_pos = (int)(assign.getX() / PhysXLibrary.QUADRANT_WIDTH);
		for (Quadrant quad : Quadrants) {
			if (quad.getQUID().getX() == x_pos && quad.getQUID().getY() == y_pos) {
				return quad.getQUID();
			}
		}
		*/
		
		// -45 is an error code
		return new QuadrantID (0,0,-45);
	}
	
	public ArrayList<Quadrant> getActiveQuadrants(){
		return getNearbyQuadrants(ActiveQuadrant);
	}
	
	public ArrayList<Quadrant> getNearbyQuadrants(QuadrantID QUID){
		
		// Create our returning object
		ArrayList<Quadrant> quads = new ArrayList<Quadrant>();
		
		if(QUID.Order() != -45) {
		quads.add(Quadrants.get(QUID.Order()));
		} else {
			if(GameConsole.IS_DEBUGGING) {
				System.out.println("- - OUT OF BOUNDS - -");
			}
			return new ArrayList<Quadrant>();
		}
		
		
		for(Quadrant quad : Quadrants) {
			QuadrantID testQUID = quad.getQUID();
			
			if(testQUID.getX() == QUID.getX() -1
					|| testQUID.getX() == QUID.getX() + 1) {
				if(testQUID.getY() == QUID.getY() - 1
						|| testQUID.getY() == QUID.getY() + 1) {
					
					if (Quadrants.size() > testQUID.Order() - 1 &&
							0 < testQUID.Order() - 1) {
						quads.add(Quadrants.get(testQUID.Order() - 1));
					}
					if (Quadrants.size() > testQUID.Order() + 1 &&
							0 < testQUID.Order() + 1) {
						quads.add(Quadrants.get(testQUID.Order() + 1));
					}
					
					if (Quadrants.size() > testQUID.Order()) {
						quads.add(Quadrants.get(testQUID.Order()));
					}
				}
			}
			/*
			// Test if the quad is Above or Below
			if(Math.abs(testQUID.getY() - QUID.getY()) < 2) {
				if (Quadrants.size() > testQUID.Order()) {
					quads.add(Quadrants.get(testQUID.Order()));
				}
				
				// Doesn't work for corner cases!
//				quads.add(Quadrants.get(testQUID.Order() - 1));
//				quads.add(Quadrants.get(testQUID.Order() + 1));
			}
			
			// Test if the quad is Left or Right
			if(Math.abs(testQUID.getX() - QUID.getX()) < 2) {
				if (Quadrants.size() > testQUID.Order()) {
					quads.add(Quadrants.get(testQUID.Order()));
				}
				
				// Doesn't work for corner cases!
//				quads.add(Quadrants.get(testQUID.Order() - 1));
//				quads.add(Quadrants.get(testQUID.Order() + 1));
			}
			*/
		}
		
		return quads;
	}

	private void updateQuadrantStates() {
		
		// since PhysX controls when the next phys step
		// will happen we don't need to worry about 
		// any weird issues deriving from deactivating
		// a quadrant and then reactivating it.
		for (Quadrant quad : Quadrants) {
			quad.Deactivate();
		}
		for (Quadrant quad : getNearbyQuadrants(this.ActiveQuadrant)) {
			quad.Activate();
		}
	}
	
	public void setActiveQuadrant(QuadrantID newQUID) {
		this.ActiveQuadrant = newQUID;
		/*
		if (newQUID.getX() > 0 && newQUID.getX() <= MAP_WIDTH
				&& newQUID.getY() > 0 && newQUID.getY() <= MAP_HEIGHT) {
			this.ActiveQuadrant = newQUID;
		}
		*/
		updateQuadrantStates();
	}
	
	private ArrayList<PhysXObject> getNearbyPhysXObjects(PhysXObject obj, float range){
		// Get Thingy's QUID
		// Look at the elements in the surrounding QUIDs
		ArrayList<Quadrant> quads = getNearbyQuadrants(obj.getQUID());
		ArrayList<PhysXObject> objects = new ArrayList<PhysXObject>();
		
		for(Quadrant quad : quads) {
			for(Ship ship : quad.getShips()) {
				if(PhysXLibrary.areObjectsInXRange(obj, ship.getPhysObj(), range)) {
					objects.add(ship.getPhysObj());
				}
			}
			for (Asteroid asteroid : quad.getStatics()) {
				if(PhysXLibrary.areObjectsInXRange(obj, asteroid.getPhysObj(), range)) {
					objects.add(asteroid.getPhysObj());
				}
			}
		}
		
		// Do a distance calc to see if in camera range
		// Draw everything that returns
		// Update what is and what is not to be displayed
		// Create an offset so that the camera will be centered
		return objects;
		
	}
	
	public void addQuadrants(ArrayList<Quadrant> quads) {
		
		for(int i=quads.size()-1; i > 0; --i) {
			addQuadrant(quads.get(i));
		}
		verifyOrder();
	}
	// The quadrants will always be read in order
	// so there is no need to verify placement
	// or order.
	public void addQuadrant(Quadrant quad) {
		Quadrants.add(quad);
	}
	
	/* These variables are meant to be private
	 * get their values from PhsXLibrary instead
	public float getQUADRANT_HEIGHT() {
		return QUADRANT_HEIGHT;
	}

	public float getQUADRANT_WIDTH() {
		return QUADRANT_WIDTH;
	}

	public int getMAP_WIDTH() {
		return MAP_WIDTH;
	}

	public int getMAP_HEIGHT() {
		return MAP_HEIGHT;
	}
	*/
	
	
	
	public void checkForCollisions(PhysXObject player) {
		
		if(!COLLISION_LOCK) {
			COLLISION_LOCK = true;
			
			checkForCollisionsOnObject(player);
			for(Quadrant quad : this.Quadrants) {
				quad.checkForCollisions();
			}
			COLLISION_LOCK = false;
		} else {
			COLLISION_LOCK = false;
			if (GameConsole.IS_DEBUGGING) {
				System.out.println("Dropped PhysX frame");
			}
		}
	}
	
	
	private void checkForCollisionsOnObject(PhysXObject obj) {
		ArrayList<PhysXObject> objects = getNearbyPhysXObjects(obj, 1000);
		
		for(PhysXObject coll : objects) {
			if(PhysXLibrary.areObjectsInCollisionRange(obj, coll)) {
				if (PhysXLibrary.isCollision(obj, coll)) {
					
					obj.sendCollisionData(coll.getCollisionData());
					coll.sendCollisionData(obj.getCollisionData());
					
					/*
					// Bulets hitting things
					if (obj.getHost() instanceof Bullet) {
						if (coll.getHost() instanceof Ship) {
							Bullet item = (Bullet)obj.getHost();
							Ship target = (Ship)coll.getHost();
							// Don't collide with the source!
							if ((item.getBulletType() == BulletType.PLAYER_BULLET && coll.getHost() instanceof PlayerShip) ||
									(item.getBulletType() == BulletType.ENEMY_BULLET && coll.getHost() instanceof EnemyShip)) {
								return;
							}
							item.destroy();
							target.takeDamage(item.getBulletDamage());
							System.out.println("Target HP: " + target.getCurrentHealth());
							
						}				
					}
					*/
					if (GameConsole.IS_DEBUGGING) {
						System.out.println(" - - ");
						System.out.println("Player Pos : " + obj.getPosition().toString());
						System.out.println("Coll Pos   : " + coll.getPosition().toString());
						System.out.println("Distance   : " + (int)PhysXLibrary.distance(coll.getPosition(), obj.getPosition()));
						
					}
				}
			}
		}
}
	
	public ArrayList<Quadrant> getQuadrants() {
		return Quadrants;
	}

	public void setQuadrants(ArrayList<Quadrant> quadrants) {
		Quadrants = quadrants;
	}
}