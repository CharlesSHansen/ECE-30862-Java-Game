package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    A Fly is a Creature that fly slowly in the air.
*/
public class Bullet extends Creature {
	private static float maxSpeed;
	public static float originalX= 0;
	public static long time_dead;
	
    public Bullet(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
    }

    public float getMaxSpeed() {
    	return maxSpeed;
    }

    public void shoot(float x, float y, int dir) {
 		this.setX(x);
    	this.setY(y);
        setVelocityX(0);
        helper(-1 * dir);
        originalX = this.getX();
    }
    
    private void helper(float max) {
    	if(max == 0) {
    		maxSpeed = 1;
    	}
    	else
    		maxSpeed = max;    		
    }

    public boolean isFlying() {
        return isAlive();
    }
    
    public void checkStatus() {
    	if(Math.abs(originalX - this.getX()) / 64 >= 10) {
    		this.setState(STATE_DEAD);
        }
    }
}
