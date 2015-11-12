package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    A Grub is a Creature that moves slowly on the ground.
*/
public class Grub extends Creature {

	public static long firsttime = 0;
	public static long lastshot = 0;
	public static float move = 0;
    public Grub(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
    }


    public float getMaxSpeed() {
        return move;
    }

    public void setTime(long t) {
    	firsttime = t;
    }
    
    public void startMove() {
    	move = 0.05f;
    }
    
    public void setShot(long t) {
    	lastshot = t;    	
    }
}
