import java.util.ArrayList;

import acm.graphics.*;

public class StaticGObject {
	private Vector2 imageSize;
	private PhysXObject physObj;
	private GOval[] objects;

	public StaticGObject(PhysXObject physObj) {
		this.physObj = physObj;
		this.imageSize = Vector2.One();
	}

	public StaticGObject(PhysXObject physObj, Vector2 size) {
		this.physObj = physObj;
		this.imageSize = size;
	}

	public StaticGObject(Vector2 position, Vector2 size) {
		physObj = new PhysXObject();
		physObj.setPosition(position);
		this.imageSize = size;
	}

	public PhysXObject getPhysObj() {
		return this.physObj;
	}

	public void setSize(Vector2 size) {
		this.imageSize = size;
	}

	public void setup(ArrayList<Double> ColliderSizes) {
		this.objects = new GOval[physObj.getColliders().length];
		for(int i =0; i<this.objects.length; ++i) {
			Vector2 center = Camera.backendToFrontend(physObj.getColliders()[i].getCenter().add(physObj.getPosition()));
			float halfsize = physObj.getColliders()[i].getRadius();
			this.objects[i] = new GOval(center.getX() + halfsize, center.getY() + halfsize, ColliderSizes.get(i), ColliderSizes.get(i));
		}
	}

	public void setLocationRespectSize(int coll, Vector2 pos) {
		this.objects[coll].setLocation(pos.getX(), pos.getY());
	}

	public GOval[] getObjects() {
		return this.objects;
	}

	public void setObjects(GOval[] objects) {
		this.objects = objects;
	}

	public void setSingleObject(GOval object) {
		this.objects = new GOval[1];
		this.objects[0] = object;
	}
}
