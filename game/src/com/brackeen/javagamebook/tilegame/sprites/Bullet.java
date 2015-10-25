package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    A Fly is a Creature that fly slowly in the air.
*/
public class Bullet extends Creature {
	private static float maxSpeed;
	
    public Bullet(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
    }

    public float getMaxSpeed() {
    	helper(maxSpeed);
    	return maxSpeed;
    }

    public void shoot(float x, float y, int dir) {
 		this.setX(x);
    	this.setY(y);
        setVelocityX(0);
        System.out.println(dir);
        helper(-1 * dir);
        System.out.println(maxSpeed);
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

}
