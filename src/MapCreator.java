import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import acm.graphics.*;
import acm.program.*;
import acm.util.*;
import java.awt.*;

public class MapCreator {
	private static float min_distance_between_objects=50.0f;
	private final int MAX_ENEMIES_IN_QUAD = 6;
	private final int MAX_BLINKERS_IN_QUAD = 1;
	private final int MAX_ASTEROIDS_IN_QUAD = 4;
	private final int MAX_FENCERS_IN_QUAD = 0;
	private final int MAX_BANSHES_IN_QUAD = 1;
	private final float BORDER_X = 100f;
	private final float BORDER_Y = 100f;
	private int max_quad=0;
	private Quadrant player_spawn_quad;
	private Quadrant boss_spawn_quad;
	private int player_spawn_quad_order;
	private int boss_spawn_quad_order;

	private FileInput file;

	public MapCreator() {
		LavaLamp.setup(System.currentTimeMillis());
		file = new FileInput();
		file.decodeFile("Test.txt");
	}

	public void setPlayerAndBossQuadPositions() {
		int totalQuads = (PhysXLibrary.MAP_WIDTH * PhysXLibrary.MAP_HEIGHT);
		max_quad = totalQuads;
		int player_quad = LavaLamp.randomNumber(0, totalQuads-1);
		int boss_quad = LavaLamp.randomNumber(0, totalQuads-1);

		while(player_quad == boss_quad) {
			player_quad = LavaLamp.randomNumber(0, totalQuads -1);
			boss_quad   = LavaLamp.randomNumber(0, totalQuads -1);

		}
		this.player_spawn_quad_order = player_quad;
		this.boss_spawn_quad_order = boss_quad;
	}
	
	public ArrayList<Quadrant> createBossRoom(){
		ArrayList<Quadrant> Quadrants = new ArrayList<Quadrant>();
		Quadrants.add(createQuadrant(0,0,0));
		return Quadrants;
	}
	
	public ArrayList<Quadrant> createMap(){
		// Create a new array to hold the quads
		ArrayList<Quadrant> Quadrants = new ArrayList<Quadrant>();
		setPlayerAndBossQuadPositions();

		int order = (PhysXLibrary.MAP_HEIGHT * PhysXLibrary.MAP_WIDTH) - 1;
		for(int a = PhysXLibrary.MAP_WIDTH; a > 0; --a) {
			for(int b = PhysXLibrary.MAP_HEIGHT; b > 0; --b) {

				Quadrant quad = createQuadrant(b-1,a-1,order);
				
				if (order != boss_spawn_quad_order) {
					fillQuadrant(quad);
				}
				Quadrants.add(quad);

				if (order == player_spawn_quad_order) {
					this.player_spawn_quad = quad;
				} else if (order == boss_spawn_quad_order) {
					this.boss_spawn_quad = quad;
				}
				order--;
			}
		}

		return Quadrants;	
	}
	public Quadrant createQuadrant(int x,int y, int order) {
		return new Quadrant(new QuadrantID(x,y,order));
	}

	public void fillQuadrant(Quadrant quad) {

		int numberOfEnemies = LavaLamp.randomNumber(0, MAX_ENEMIES_IN_QUAD);
		int numberOfAsteroids = LavaLamp.randomNumber(0, MAX_ASTEROIDS_IN_QUAD);
		int numberOfBlinkers = LavaLamp.randomNumber(0, MAX_BLINKERS_IN_QUAD);
		int numberOfBanshes = LavaLamp.randomNumber(0, MAX_BANSHES_IN_QUAD);

		ArrayList<EnemyShip> EnemyShips =  placeGenericEnemies(quad.getQUID(), numberOfEnemies);
		ArrayList<Asteroid> Asteroids =  placeAsteroids(quad.getQUID(), numberOfAsteroids);
		ArrayList<Blinker> Blinkers = placeBlinkers(quad.getQUID(), numberOfBlinkers);
		ArrayList<Banshe> Banshes = placeBanshes(quad.getQUID(), numberOfBanshes);

		//objects verification
		boolean check=true;
		while(check) {//return the object that has collision, remove that object.

			int asteroidIndex = checkObjects(Asteroids);
			int asteroidRuns = -1;
			while(asteroidIndex != -404) {
				asteroidRuns ++;
				Asteroids.remove(asteroidIndex);
				Asteroids.add(placeAsteroid(quad.getQUID()));
				asteroidIndex = checkObjects(Asteroids);
			}

			int shipIndex = checkObjects(EnemyShips);
			int shipRuns = -1;
			while(shipIndex != -404) {
				shipRuns++;
				EnemyShips.remove(shipIndex);
				EnemyShips.add(placeGenericEnemy(quad.getQUID()));
				shipIndex = checkObjects(EnemyShips);
			}

			int blinkerIndex = checkObjects(Blinkers);
			while(blinkerIndex != -404) {
				Blinkers.remove(blinkerIndex);
				Blinkers.add(placeBlinker(quad.getQUID()));
				blinkerIndex = checkObjects(Blinkers);
			}

			/*
			int fencerIndex = checkObjects(Fencers);
			while(fencerIndex != -404) {
				Fencers.remove(fencerIndex);
				Fencers.add(placeFencer(quad.getQUID()));
				fencerIndex = checkObjects(Fencers);
			}
			 */
			/*
			int check_asteroid_and_fencer = checkBothObjects(Asteroids, Fencers);
			int check_asteroid_and_fencer_runs = -1;
			while(check_asteroid_and_fencer != -404) {
				check_asteroid_and_fencer_runs++;

				Asteroids.remove(check_asteroid_and_fencer);
				if(check_asteroid_and_fencer_runs > 10) {
					break;
				}

				Asteroids.add(placeAsteroid(quad.getQUID()));
				check_asteroid_and_fencer = checkBothObjects(Asteroids,EnemyShips);
			}
			 */

			int bansheIndex = checkObjects(Banshes);
			while(bansheIndex != -404) {
				Banshes.remove(bansheIndex);
				Banshes.add(placeBanshe(quad.getQUID()));
				bansheIndex = checkObjects(Banshes);
			}


			int check_asteroid_and_blink = checkBothObjects(Asteroids, Blinkers);
			int check_asteroid_and_blink_runs = -1;
			while(check_asteroid_and_blink != -404) {
				check_asteroid_and_blink_runs++;

				Asteroids.remove(check_asteroid_and_blink);
				if(check_asteroid_and_blink_runs > 10) {
					break;
				}

				//these two codes take some times:-wenrui
				Asteroids.add(placeAsteroid(quad.getQUID()));
				check_asteroid_and_blink = checkBothObjects(Asteroids,EnemyShips);
			}

			int check_both = checkBothObjects(Asteroids,EnemyShips);//comparing asteroids with enemies.
			while(check_both != -404) {

				Asteroids.remove(check_both);

				//these two codes take some times:-wenrui
				Asteroids.add(placeAsteroid(quad.getQUID()));

				check_both = checkBothObjects(Asteroids,EnemyShips);
			}

			if(asteroidIndex == -404 && shipIndex == -404 && check_both == -404 && check_asteroid_and_blink == -404 && bansheIndex == -404) {
				check=false;
			}
		}
		quad.setAsteroids(Asteroids);
		quad.setShips(EnemyShips);
		quad.setBlinkers(Blinkers);
		quad.setBanshes(Banshes);
	}

	public static Asteroid checkAsteroid(ArrayList<Asteroid> Asteroids) {
		for (int j=0;j<Asteroids.size();j++) {

			if(Asteroids.get(j).getPhysObj() == null) {
				System.out.println("Phys null");
			}
			float Jx = Asteroids.get(j).getPhysObj().getPosition().getX();
			float Jy = Asteroids.get(j).getPhysObj().getPosition().getY();

			for (int k=j+1;k<Asteroids.size();k++) {
				float Kx = Asteroids.get(k).getPhysObj().getPosition().getX();
				float Ky = Asteroids.get(k).getPhysObj().getPosition().getY();
				float differentX = Math.abs(Jx - Kx);
				float differentY = Math.abs(Jy - Ky);

				if (k!=j &&(differentX<=min_distance_between_objects)) {
					if (differentY<=min_distance_between_objects) {
						return Asteroids.get(k);
					}
				}
			}
		}
		return null;
	}

	public <Item extends Entity> int checkObjects(ArrayList<Item> objects) {
		for (int j=0;j<objects.size();j++) {

			if(objects.get(j) == null) {
				System.out.println("Phys null");
			}
			float Jx = objects.get(j).getPhysObj().getPosition().getX();
			float Jy = objects.get(j).getPhysObj().getPosition().getY();

			for (int k=j+1;k<objects.size();k++) {
				float Kx = objects.get(k).getPhysObj().getPosition().getX();
				float Ky = objects.get(k).getPhysObj().getPosition().getY();
				float differentX = Math.abs(Jx - Kx);
				float differentY = Math.abs(Jy - Ky);

				if (k!=j &&(differentX<=min_distance_between_objects)) {
					if (differentY<=min_distance_between_objects) {

						return k;
					}
				}
			}
		}
		return -404;
	}

	public <Item extends Entity, Item_2 extends Entity> int checkBothObjects(ArrayList<Item> setA,ArrayList<Item_2> setB) {
		for(int i = 0; i< setA.size();i++) {
			float Ax = setA.get(i).getPhysObj().getPosition().getX();
			float Ay = setA.get(i).getPhysObj().getPosition().getY();

			for (int k = 0; k <setB.size(); k++) {
				float Ex = setB.get(k).getPhysObj().getPosition().getX();
				float Ey = setB.get(k).getPhysObj().getPosition().getY();
				float differentX = Math.abs(Ax - Ex);
				float differentY = Math.abs(Ay - Ey);
				if(differentX<=min_distance_between_objects) {//if they at the same position or in the collision range.
					if(differentY<=min_distance_between_objects) {
						return i;
					}
				}
			}
		}
		return -404;
	}

	public EnemyShip checkEnemy(ArrayList<EnemyShip> EnemyShips) {
		for (int j=0;j<EnemyShips.size();j++) {
			float Jx = EnemyShips.get(j).getPhysObj().getPosition().getX();
			float Jy = EnemyShips.get(j).getPhysObj().getPosition().getY();

			for (int k=j+1;k<EnemyShips.size();k++) {
				float Kx = EnemyShips.get(k).getPhysObj().getPosition().getX();
				float Ky = EnemyShips.get(k).getPhysObj().getPosition().getY();
				float differentX = Math.abs(Jx - Kx);
				float differentY = Math.abs(Jy - Ky);
				if (k!=j &&(differentX<=min_distance_between_objects)) {
					if (differentY<=min_distance_between_objects) {
						return EnemyShips.get(k);
					}
				}
			}
		}
		return null;
	}

	public PhysXObject createPhysXObjectInQuad (QuadrantID quad) {
		float startingX = ((quad.getX() + 1)* PhysXLibrary.QUADRANT_WIDTH) - (PhysXLibrary.QUADRANT_WIDTH / 2);
		float startingY = ((quad.getY() + 1)* PhysXLibrary.QUADRANT_HEIGHT) -  (PhysXLibrary.QUADRANT_HEIGHT / 2);

		float randomMultiplierX = LavaLamp.nextFloat() * LavaLamp.randomNumber(-1, 1); // -1.0 - 1.0
		float randomMultiplierY = LavaLamp.nextFloat() * LavaLamp.randomNumber(-1, 1); // -1.0 - 1.0
		//the possibility of a float number is 0.0f is too big-wenrui 
		while (randomMultiplierX==0.0f) {
			randomMultiplierX = LavaLamp.nextFloat() * LavaLamp.randomNumber(-1, 1); // -1.0 - 1.0
		}
		while (randomMultiplierY==0.0f) {
			randomMultiplierY = LavaLamp.nextFloat() * LavaLamp.randomNumber(-1, 1); // -1.0 - 1.0
		}

		float x_pos = randomMultiplierX * (PhysXLibrary.QUADRANT_WIDTH / 2 - BORDER_X);
		float y_pos = randomMultiplierY * (PhysXLibrary.QUADRANT_HEIGHT / 2 - BORDER_Y);

		return new PhysXObject(quad, new Vector2(startingX + x_pos, startingY + y_pos));
	}

	// ===============================
	// ======= GENERIC ENEMIES =======
	// ===============================

	public EnemyShip buildGenericEnemy(PhysXObject physObj) {
		int preset = LavaLamp.randomNumber(0,file.numberOfShipPresets()-1);
		PhysXObject presetPhysObj = new PhysXObject(file.getShipObject(preset));
		presetPhysObj.setPosition(physObj.getPosition());
		presetPhysObj.setQUID(physObj.getQUID());
		ShipStats stats = null;
		EnemyType type = null;
		switch (file.getShipLevel(preset)) {
		case 1:
			stats = ShipStats.EnemyStats_01();
			type = EnemyType.LEVEL_1;
			break;
		case 2:
			stats = ShipStats.EnemyStats_02();
			type = EnemyType.LEVEL_2;
			break;
		case 3:
			stats = ShipStats.EnemyStats_03();
			type = EnemyType.LEVEL_3;
		}

		return new EnemyShip(presetPhysObj, file.getShipSprite(preset), stats.getHealthMax(), stats, file.getShipLevel(preset), type, 10);
	}

	public ArrayList<EnemyShip> placeGenericEnemies(QuadrantID quad, int numToCreate) {
		ArrayList<EnemyShip> EnemyShips = new ArrayList<EnemyShip>();
		for(int i =0; i < numToCreate; ++i) {
			EnemyShips.add(buildGenericEnemy(createPhysXObjectInQuad(quad)));
		}
		return EnemyShips;
	}

	public EnemyShip placeGenericEnemy (QuadrantID quad) {
		return buildGenericEnemy(createPhysXObjectInQuad(quad));
	}

	// ===============================
	// ============ ASTER ============
	// ===============================

	public Asteroid buildAsteroid(PhysXObject physObj) {
		int preset = LavaLamp.randomNumber(0,file.numberOfAsteroidPresets()-1);
		PhysXObject presetPhysObj = new PhysXObject(file.getAsteroidObject(preset));
		presetPhysObj.setPosition(physObj.getPosition());
		presetPhysObj.setQUID(physObj.getQUID());

		return new Asteroid(presetPhysObj, file.getAsteroidSprite(preset));
	}

	public ArrayList<Asteroid> placeAsteroids(QuadrantID quad, int numToCreate) {
		ArrayList<Asteroid> Asteroids = new ArrayList<Asteroid>();
		for(int i =0; i < numToCreate; ++i) {
			Asteroids.add(buildAsteroid(createPhysXObjectInQuad(quad)));
		}
		return Asteroids;
	}

	public Asteroid placeAsteroid (QuadrantID quad) {
		return buildAsteroid(createPhysXObjectInQuad(quad));
	}

	// ===============================
	// =========== BLINKER ===========
	// ===============================

	public Blinker buildBlinker(PhysXObject physObj) {
		int preset = LavaLamp.randomNumber(0,file.numberOfBlinkPresets()-1);
		PhysXObject presetPhysObj = new PhysXObject(file.getBlinkObject(preset));
		presetPhysObj.setPosition(physObj.getPosition());
		presetPhysObj.setQUID(physObj.getQUID());

		return new Blinker(presetPhysObj, file.getBlinkSprite(preset), ShipStats.EnemyStats_Blinker().getHealthMax(), ShipStats.EnemyStats_01(), file.getBlinkLevel(preset), 15);
	}

	public ArrayList<Blinker> placeBlinkers(QuadrantID quad, int numToCreate) {
		ArrayList<Blinker> blinkers = new ArrayList<Blinker>();
		for(int i =0; i < numToCreate; ++i) {
			blinkers.add(buildBlinker(createPhysXObjectInQuad(quad)));
		}
		return blinkers;
	}

	public Blinker placeBlinker (QuadrantID quad) {
		return buildBlinker(createPhysXObjectInQuad(quad));
	}

	// ===============================
	// ========== FENCER =============
	// ===============================

	public Fencer buildFencer (PhysXObject physObj) {
		int preset = LavaLamp.randomNumber(0,file.numberOfFencerPresets()-1);
		PhysXObject presetPhysObj = new PhysXObject(file.getFencerObject(preset));
		presetPhysObj.setPosition(physObj.getPosition());
		presetPhysObj.setQUID(physObj.getQUID());

		return new Fencer(presetPhysObj, file.getFencerSprite(preset), ShipStats.EnemyStats_01().getHealthMax(), ShipStats.EnemyStats_01(), file.getFencerLevel(preset), 20);
	}

	public ArrayList<Fencer> placeFencers (QuadrantID quad, int numToCreate) {
		ArrayList<Fencer> fencers = new ArrayList<Fencer>();
		for(int i =0; i < numToCreate; ++i) {
			fencers.add(buildFencer(createPhysXObjectInQuad(quad)));
		}
		return fencers;
	}

	public Fencer placeFencer (QuadrantID quad) {
		return buildFencer(createPhysXObjectInQuad(quad));
	}

	// ===============================
	// ========== BANSHE =============
	// ===============================

	public Banshe buildBanshe (PhysXObject physObj) {
		int preset = LavaLamp.randomNumber(0,file.numberOfBanshePresets()-1);
		PhysXObject presetPhysObj = new PhysXObject(file.getBansheObject(preset));
		presetPhysObj.setPosition(physObj.getPosition());
		presetPhysObj.setQUID(physObj.getQUID());

		return new Banshe(presetPhysObj, file.getBansheSprite(preset), ShipStats.EnemyStats_Banshe().getHealthMax(), ShipStats.EnemyStats_Banshe(), file.getBansheLevel(preset));
	}

	public ArrayList<Banshe> placeBanshes (QuadrantID quad, int numToCreate) {
		ArrayList<Banshe> banshes = new ArrayList<Banshe>();
		for(int i =0; i < numToCreate; ++i) {
			banshes.add(buildBanshe(createPhysXObjectInQuad(quad)));
		}
		return banshes;
	}

	public Banshe placeBanshe (QuadrantID quad) {
		return buildBanshe(createPhysXObjectInQuad(quad));
	}

	public PlayerShip placePlayer (QuadrantID quad) {
		PhysXObject physObj = createPhysXObjectInQuad(quad);
		int player_base_hp = 3;
		return new PlayerShip(physObj, player_base_hp, ShipStats.PlayerStats(player_base_hp), "Player V2.png");
	}

	public void placeBoss(Quadrant quad, Boss boss) {
		System.out.println(boss);
	}

	public Quadrant getPlayerSpawn() {
		return this.player_spawn_quad;
	}
	public Quadrant getBossSpawn() {
		return this.boss_spawn_quad;
	}
	public void placePlayer(Quadrant quad, PlayerShip player) {
		System.out.println(player);
	}
}