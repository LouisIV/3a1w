import java.awt.Color;
import java.util.ArrayList;

import acm.graphics.GOval;

public class FXManager {
	ArrayList<FXParticle> particles;
	GamePaneEvents pane;
	
	// - - Bullet Flash - -
	private static final float bf_transitionSpeed = 20f;
	private static final float bf_size = 25f;
	private static final float bf_endSize = 50f;
	private static final Color bf_startColor = PaintToolbox.YELLOW;
	
	// - - Death Flash - -
	private static final float df_transitionSpeed = 20f;
	private static final int		df_maxBursts = 10;
	private static final int		df_minBursts = 2;
	private static final float df_size = 25f;
	private static final float df_endSize = 50f;
	private static final Color df_startColor = PaintToolbox.RED;
	
	public FXManager() {
		this.particles = new ArrayList<FXParticle>();
		this.pane = null;
		System.out.println("[Warning]: FX Manager requires GamePane reference to be set after intialization");
	}
	
	public void moveParticles() {
		for(FXParticle fx : particles) {
			fx.move();
			
			switch(fx.getType()) {
				case SHRINK:
//					fx.shrink();
					break;
				case COLOR_CHANGE:
					fx.shrink();
					fx.colorChange();
					break;
			default:
				break;
			}
		}
		
		int size = particles.size();
//		for (int i = 0; i < size; i++) {
//			FXParticle fx = particles.get(i);
//			fx.move();
//		}
		
		// CHECK FOR DEAD ONES AFTER
		for (int i = 0; i < size; i++) {
			FXParticle fx = particles.get(i);
			if (fx.getLife() <= 0) {
				size -= 1;
				i -= 1;
				particles.remove(fx);
			}
		}
	}
	
	public void makeDeflectSparks(FXType type, FXParticle particle) {
		double theta = Math.toDegrees(Math.atan2(0 - particle.getPosition().getY(), 0 - particle.getPosition().getX())) - 90;
		
		for (int i = 0; i < 10; i++) {
			FXParticle x = new FXParticle(particle.getSprite(), particle.getPattern(), particle.getType(), particle.getPosition(), particle.getDir(), particle.getLife());
			double angle_off = theta + LavaLamp.randomRange(-45, 45);
			float spd = LavaLamp.randomRange(3, 7);
			x.setDir(new Vector2((float)Math.cos(Math.toRadians(angle_off)) * spd, (float)Math.sin(Math.toRadians(angle_off)) * spd));
			particles.add(x);
		}
	}
	
	public void makeBulletFlash(FXType type, FXParticle particle) {
		particle.setDir(Vector2.Zero());
		
		// Build our arguments
		Object[] args = new Object[4];
		args[0] = LavaLamp.randomRange(bf_size/2, bf_size);
		args[1] = LavaLamp.randomRange(bf_endSize/2, bf_endSize);
		args[2] = bf_startColor;
		args[3] = PaintToolbox.setAlpha(bf_startColor, 0);
		
		// Assign and add
		particle.setArgs(args);
		particles.add(particle);
	}
	
	public void makeDeathFlash(FXType type, FXParticle particle) {
		
		int r = LavaLamp.randomRange(df_minBursts, df_maxBursts);
		for(int i =0; i < r; ++i) {
			
			FXParticle x = new FXParticle(particle.getSprite(), particle.getPattern(), particle.getType(), particle.getPosition(), particle.getDir(), particle.getLife());
			
			// Build our arguments
			Object[] args = new Object[4];
			args[0] = LavaLamp.randomRange(df_size/2, df_size);
			args[1] = LavaLamp.randomRange(df_endSize/2, df_endSize);
			args[2] = bf_startColor;
			args[3] = PaintToolbox.setAlpha(df_startColor, 0);
			
			x.setDir(Vector2.Zero());
			x.setPosition(x.getPosition().add(new Vector2(LavaLamp.randomRange(-df_endSize, df_endSize), LavaLamp.randomRange(-df_endSize, df_endSize))));
			
			// Assign and add
			x.setArgs(args);
			particles.add(x);
		}
	}
	
	public void setReferences(GamePaneEvents pane) {
		this.pane = pane;
	}
	
	public ArrayList<FXParticle> getParticles() {
		return this.particles;
	}
	
	public static FXParticle redParticle() {
		GOval sp = new GOval(5,5,5,5);
		sp.setFillColor(Color.RED);
		sp.setFilled(true);
		sp.setColor(PaintToolbox.TRANSPARENT);
		FXParticle fx_p = new FXParticle(sp, FXPattern.SPARKS_DEFLECT, FXType.SHRINK, Vector2.Zero(), Vector2.Zero(), 10);
		return fx_p;
	}
	
	public static FXParticle colorParticle(Color color) {
		GOval sp = new GOval(5,5,5,5);
		sp.setFillColor(color);
		sp.setFilled(true);
		sp.setColor(PaintToolbox.TRANSPARENT);
		FXParticle fx_p = new FXParticle(sp, FXPattern.SPARKS_DEFLECT, FXType.SHRINK, Vector2.Zero(), Vector2.Zero(), 10);
		return fx_p;
	}
	
	public static FXParticle deathFlash() {
		GOval sp = new GOval(df_size,df_size);
		sp.setFillColor(df_startColor);
		sp.setFilled(true);
		
		// Make the outlines transparent
		sp.setColor(PaintToolbox.TRANSPARENT);
		FXParticle fx_p = new FXParticle(sp, FXPattern.GROW_STATIONARY, FXType.COLOR_CHANGE, Vector2.Zero(), Vector2.Zero(), (int)bf_transitionSpeed);
		return fx_p;
	}
	
	public static FXParticle bulletFlash() {
		GOval sp = new GOval(bf_size,bf_size);
		sp.setFillColor(bf_startColor);
		sp.setFilled(true);
		
		// Make the outlines transparent
		sp.setColor(PaintToolbox.TRANSPARENT);
		FXParticle fx_p = new FXParticle(sp, FXPattern.GROW_STATIONARY, FXType.COLOR_CHANGE, Vector2.Zero(), Vector2.Zero(), (int)bf_transitionSpeed);
		return fx_p;
	}
}
