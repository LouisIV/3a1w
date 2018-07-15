
# HTBX by 3A1W
![HTBX Title](https://i.imgur.com/s8r4zn7.png "HTBX")

A bullet hell game created in Java with Eclipse IDE.

# Controls
![HTBX Controls](https://i.imgur.com/GXCauwy.gif "Controls")

W - Move forward.
<br /> A - Bank left.
<br /> S - Move backward.
<br /> D - Bank right.
<br /> Left Mouse Button (LMB) - Shoot at cursor.
<br /> Right Mouse Button (RMB) - Teleport at cursor.

# Features
+ [Knockback](#knockback)
+ [QUID System](#quid-system)
+ [Enemy HUD](#enemy-hud)
+ [Collision System](#collision-system)
+ [Rotations](#rotations)
+ [Layering](#layering)
+ [Boss](#boss)
+ [Camera](#camera)
+ [Pseudo Random Generation](#pseudo-random-generation)

#### Knockback
![Knockback](https://i.imgur.com/gaEil2D.gif "Knockback")

The Knockback feature helped make it easier to notice when you were hit.

#### QUID System
![Quid](https://i.imgur.com/aI7cDgE.gif "Quid")

The QUID feature was one of earliest systems implemented. By dividing the game area into rects of equal size we could optimize several systems. The enemies are only "awake" if the player is in a nearby QUID and the collision system only checks for collisions within the player's current QUID and those adjacent to it.

#### Enemy HUD
![Enemy HUD](https://i.imgur.com/X1bWIuh.gif "Enemy HUD")

The Enemy Heads Up Display helped lead players towards enemies. We found in testing that it was more enjoyable if the player knew that their current direction would lead them to something rather than explore on their own.

#### Collision System
![Collision System](https://i.imgur.com/ygjkBlM.gif "Collision System")

The Collision System was my favorite feature to build. When we were designing the game I realized that we A. Needed collisions and that B. Creating a complicated collider system would take more time than we had. So I decided to use circles. How it works:
   
Each PHYSX object has an array of `CircleCollider` objects on it. Each `CircleCollider` stores a `radius` and a `center` position. The `radius` keeps track of exactly how big this collider is. The `center` position stores the offset from the center of the object.
When two PHYSX objects get close enough the PHYSX manager starts doing distance calculation. If the two objects are far enough from each other that their colliders don't matter it simply returns false for the collision. If the two objects are close enough though it will start comparing the objects.
To determine a collision the PHYSX manager grabs the `CircleCollider` array from each PHYSX object. It then does a distance calculation:
 ```
 Point A = Transform_A.Position (Current Position of PHYSX object A) + CircleColliders_A[i].center (The offset)
 Point B = Transform_B.Position (Current Position of PHYSX object B) + CircleColliders_B[i].center (The offset)
 if (Distance(Point A, Point B) <= (CircleColliders_A[i].radius + CircleColliders_B[i].radius)) {
    return Collision;
 }
 ```
The rationale being that if the combined radii are larger than the distance between them they must be overlapping. There are optimizations and other things going on but that is the simplified version of it.
  
#### Rotations
![Rotations](https://i.imgur.com/Cey7qWk.gif "Rotations")

![Rotator](https://i.imgur.com/bzMCfdr.gif "Rotator")

Rotations were especially difficult to do. We were the only project that required them in the class and therefore the small image libraries we were allowed to use had to be extended. You can see in our code how we did it but it involves taking in the  source image as pixels, skewing the image, and then rendering the new image on screen.

#### Layering
Each image is drawn in sequence so that it will appear in the right layer to the player.

#### Boss
While we didn't get to finish the Boss battle the way we wanted it was fun to fight our teacher's face in space. 

#### Camera
In order to have the player move around the world we needed a way to move all of the static objects each frame so they would appear to be still relative to the player. We achieved this by specifying an object as the camera and drawing everything relative to it each frame.

#### Pseudo Random Generation
The map is created at the start of each game from a random seed. This way everything is different each play-through. We thought it would be exceptionally boring if the game was the same every time. It does have a few rules however.
- No ships can be placed on top of asteroids
- The boss entrance cannot be next to the player
- No ships can be placed on another ship
The generator creates the map one rect at a time and fills the map from the top to the bottom.

# Credits
https://i.imgur.com/1zMuU44.gif
