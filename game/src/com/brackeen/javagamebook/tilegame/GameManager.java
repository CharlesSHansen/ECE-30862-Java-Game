package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.sound.*;
import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.test.GameCore;
import com.brackeen.javagamebook.tilegame.sprites.*;

/**
    GameManager manages all parts of the game.
*/
public class GameManager extends GameCore {

	private static String cmdarg;
    public static void main(String[] args) {
    	if(args.length >= 1) {
        	cmdarg = args[0];
        }
    	new GameManager().run();        
    }

    // uncompressed, 44100Hz, 16-bit, mono, signed, little-endian
    private static final AudioFormat PLAYBACK_FORMAT =
        new AudioFormat(44100, 16, 1, true, false);

    private static final int DRUM_TRACK = 1;

    public static final float GRAVITY = 0.002f;

    private Point pointCache = new Point();
    private TileMap map;
    private MidiPlayer midiPlayer;
    private SoundManager soundManager;
    private ResourceManager resourceManager;
    private Sound prizeSound;
    private Sound boopSound;
    private Sound shootSound;
    private InputManager inputManager;
    private TileMapRenderer renderer;

    private GameAction moveLeft;
    private GameAction moveRight;
    private GameAction jump;
    private GameAction fall;
    private GameAction exit;
    private GameAction shoot;
    
    private int dir = 1;
    private int count = 0;
    private int star = 0;
    private int gas = 0;
    private float starWalk = 0;
    private float gasWalk = 0;
    private float duck = 0;
    //private int updateHealth = 0; //Is 1 when a creature is killed and increments health
    
    private long time;
    private long newtime;
    private long time2;
    private long wait;
    private long grubTime;
    private long grubWait;
    private long starTime;
    private long starWait;
    private long gasTime;
    private long gasWait;
    
    private Player porig;
    
    
    public void init() {
        super.init();

        // set up input manager
        initInput();

        // start resource manager
        resourceManager = new ResourceManager(
        screen.getFullScreenWindow().getGraphicsConfiguration());

        // load resources
        renderer = new TileMapRenderer();
        renderer.setBackground(
            resourceManager.loadImage("background.png"));

        // load first map
    	map = resourceManager.cmdArgMap(cmdarg);
        // load sounds
        soundManager = new SoundManager(PLAYBACK_FORMAT);
        prizeSound = soundManager.getSound("sounds/prize.wav");
        boopSound = soundManager.getSound("sounds/boop2.wav");
        shootSound = soundManager.getSound("sounds/boop2.wav");

        
        // start music
        midiPlayer = new MidiPlayer();
        Sequence sequence =
            midiPlayer.getSequence("sounds/music.midi");
        midiPlayer.play(sequence, true);
        toggleDrumPlayback();
        
    }


    /**
        Closes any resurces used by the GameManager.
    */
    public void stop() {
        super.stop();
        midiPlayer.close();
        soundManager.close();
    }


    private void initInput() {
        moveLeft = new GameAction("moveLeft");
        moveRight = new GameAction("moveRight");
        fall = new GameAction("fall", GameAction.DETECT_INITAL_PRESS_ONLY);
        jump = new GameAction("jump",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        exit = new GameAction("exit",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        shoot = new GameAction("shoot");
        //shoot = new GameAction("shoot", GameAction.DETECT_INITAL_PRESS_ONLY);

        inputManager = new InputManager(
            screen.getFullScreenWindow());
        inputManager.setCursor(InputManager.INVISIBLE_CURSOR);

        inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
        inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
        inputManager.mapToKey(fall, KeyEvent.VK_DOWN);
        inputManager.mapToKey(jump, KeyEvent.VK_UP);
        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
        inputManager.mapToKey(shoot, KeyEvent.VK_S);
    }


    private void checkInput(long elapsedTime) {

        if (exit.isPressed()) {
            stop();
        }
        
        Player player = (Player)map.getPlayer();
        porig = player;
        if (player.isAlive()) {
            float velocityX = 0;
            float velocityY = 0;
            if (moveLeft.isPressed() && duck == 0) {
                velocityX-=player.getMaxSpeed();
                dir = -1;
            }
            if (moveRight.isPressed() && duck == 0) {
                velocityX+=player.getMaxSpeed();
                dir = 1;
            }
            if (jump.isPressed()) {
            	if(duck == 1){
            		duck = 0;
            		porig.duck(0);
            	}
            	else{
            		player.jump(false);
            		if(player.onGround == true) {
            			velocityY = player.getVelocityY() - player.getMaxSpeed() / 2;
            			player.setVelocityY(player.getVelocityY() - player.getMaxSpeed() / 2);
            		}
            	}
            }
            if (fall.isPressed()) {
            	duck = 1;
            	porig.duck(1);
        	}

            if(!(shoot.isPressed())){
            	count = 0;
            }
            
            gasTime = System.currentTimeMillis();
            if(gasTime - gasWait >= 1000 || Math.abs(gasWalk - porig.getX()) >= 10 * 64){
            	gas = 0;
            	gasTime = 0;
            	gasWait = 0;
            	gasWalk = 0;
            }
            
            time2 = System.currentTimeMillis();
            if(shoot.isPressed() && gas == 0){
            	if(count == 0){
            		Image[][] images = new Image[1][];
            		soundManager.play(shootSound);
            		
            		// load left-facing images
            		images[0] = new Image[] { 
            				loadImage("images/cannon1.png"), loadImage("images/cannon2.png"),
            				loadImage("images/cannon3.png"), loadImage("images/cannon2.png")};

            		// create creature animations
            		Animation[] flyAnim = new Animation[4];
            		flyAnim[0] = resourceManager.createFlyAnim(images[0][0], images[0][1], images[0][2]);
            			
            		Bullet b = new Bullet(flyAnim[0], flyAnim[0], flyAnim[0], flyAnim[0]);		
            		b.shoot(player.getX(), player.getY(), dir);
            		resourceManager.addSprite(map, b, (int) (player.getX() / 64) + (dir * 2), (int) player.getY() / 64);
            		resourceManager.reloadMap();
            		count++;
            		wait = time2;
    
            	}
            	else if((count >= 1 && count < 10) && time2 - wait >= 500){
            		Image[][] images = new Image[1][];
            		soundManager.play(shootSound);

            		// load left-facing images
            		images[0] = new Image[] { 
            				loadImage("images/cannon1.png"), loadImage("images/cannon2.png"),
            				loadImage("images/cannon3.png"), loadImage("images/cannon2.png")};

            		// create creature animations
            		Animation[] flyAnim = new Animation[4];
            		flyAnim[0] = resourceManager.createFlyAnim(images[0][0], images[0][1], images[0][2]);
            			
            		Bullet b = new Bullet(flyAnim[0], flyAnim[0], flyAnim[0], flyAnim[0]);		
            		b.shoot(player.getX(), player.getY(), dir);
            		resourceManager.addSprite(map, b, (int) (player.getX() / 64) + (dir * 2), (int) player.getY() / 64);
            		resourceManager.reloadMap();
            		count++;
            		wait = time2;

            	}
            	else if(count >= 10 && time2 - wait >= 1000){
            		Image[][] images = new Image[1][];
            		
            		soundManager.play(shootSound);
            		// load left-facing images
            		images[0] = new Image[] { 
            				loadImage("images/cannon1.png"), loadImage("images/cannon2.png"),
            				loadImage("images/cannon3.png"), loadImage("images/cannon2.png")};

            		// create creature animations
            		Animation[] flyAnim = new Animation[4];
            		flyAnim[0] = resourceManager.createFlyAnim(images[0][0], images[0][1], images[0][2]);
            			
            		Bullet b = new Bullet(flyAnim[0], flyAnim[0], flyAnim[0], flyAnim[0]);		
            		b.shoot(player.getX(), player.getY(), dir);
            		resourceManager.addSprite(map, b, (int) (player.getX() / 64) + (dir * 2), (int) player.getY() / 64);

            		resourceManager.reloadMap();
            		count = 0;
            		wait = time2;

            	}
            }
            
            
            player.setVelocityX(velocityX);
            if(velocityX == 0 && velocityY == 0) {
            	newtime = System.currentTimeMillis();
            	if(newtime - time >= 1000 && player.lasthealth - System.currentTimeMillis() <= 1000) {
            		time = newtime;
                	player.updateHealth(5.0);
            	}
            }
            else {
            	player.updateHealth(0.01);
            	time = System.currentTimeMillis();
            }
            
            //Move player back to beginning
            if(player.getX() >= map.getWidth() * 64 - player.getWidth())
            	player.setX(0);
        }       

    }


    public void draw(Graphics2D g) {
        renderer.draw(g, map,
            screen.getWidth(), screen.getHeight());
    }


    /**
        Gets the current map.
    */
    public TileMap getMap() {
        return map;
    }


    /**
        Turns on/off drum playback in the midi music (track 1).
    */

    public void toggleDrumPlayback() {
        Sequencer sequencer = midiPlayer.getSequencer();
        if (sequencer != null) {
            sequencer.setTrackMute(DRUM_TRACK,
                !sequencer.getTrackMute(DRUM_TRACK));
        }
    }



    /**
        Gets the tile that a Sprites collides with. Only the
        Sprite's X or Y should be changed, not both. Returns null
        if no collision is detected.
    */
    public Point getTileCollision(Sprite sprite,
        float newX, float newY)
    {
        float fromX = Math.min(sprite.getX(), newX);
        float fromY = Math.min(sprite.getY(), newY);
        float toX = Math.max(sprite.getX(), newX);
        float toY = Math.max(sprite.getY(), newY);

        // get the tile locations
        int fromTileX = TileMapRenderer.pixelsToTiles(fromX);
        int fromTileY = TileMapRenderer.pixelsToTiles(fromY);
        int toTileX = TileMapRenderer.pixelsToTiles(
            toX + sprite.getWidth() - 1);
        int toTileY = TileMapRenderer.pixelsToTiles(
            toY + sprite.getHeight() - 1);

        // check each tile for a collision
        for (int x=fromTileX; x<=toTileX; x++) {
            for (int y=fromTileY; y<=toTileY; y++) {
                if (x < 0 || x >= map.getWidth() ||
                    map.getTile(x, y) != null)
                {
                    // collision found, return the tile
                    pointCache.setLocation(x, y);
                    return pointCache;
                }
            }
        }

        // no collision found
        return null;
    }


    /**
        Checks if two Sprites collide with one another. Returns
        false if the two Sprites are the same. Returns false if
        one of the Sprites is a Creature that is not alive.
    */
    public boolean isCollision(Sprite s1, Sprite s2) {
        // if the Sprites are the same, return false
        if (s1 == s2) {
            return false;
        }

        // if one of the Sprites is a dead Creature, return false
        if (s1 instanceof Creature && !((Creature)s1).isAlive()) {
            return false;
        }
        if (s2 instanceof Creature && !((Creature)s2).isAlive()) {
            return false;
        }

        // get the pixel location of the Sprites
        int s1x = Math.round(s1.getX());
        int s1y = Math.round(s1.getY());
        int s2x = Math.round(s2.getX());
        int s2y = Math.round(s2.getY());

        // check if the two sprites' boundaries intersect
        return (s1x < s2x + s2.getWidth() &&
            s2x < s1x + s1.getWidth() &&
            s1y < s2y + s2.getHeight() &&
            s2y < s1y + s1.getHeight());
    }


    /**
        Gets the Sprite that collides with the specified Sprite,
        or null if no Sprite collides with the specified Sprite.
    */
    public Sprite getSpriteCollision(Sprite sprite) {

        // run through the list of Sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite otherSprite = (Sprite)i.next();
            if (isCollision(sprite, otherSprite)) {
                // collision found, return the Sprite
                return otherSprite;
            }
            if(otherSprite instanceof Bullet) {
            	Bullet b = (Bullet) otherSprite;
            	b.checkStatus();  	
            }
		}
        
        // no collision found
        return null;
    }


    /**
        Updates Animation, position, and velocity of all Sprites
        in the current map.
    */
    public void update(long elapsedTime) {
        Creature player = (Creature)map.getPlayer();


        // player is dead! start map over
        if (player.getState() == Creature.STATE_DEAD) {            	
        	map = resourceManager.reloadMap();
            return;
        }

        // get keyboard/mouse input
        checkInput(elapsedTime);

        // update player
        updateCreature(player, elapsedTime);
        player.update(elapsedTime);

        
        // update other sprites
        Iterator i = map.getSprites();
        starTime = System.currentTimeMillis();
        if(starTime - starWait >= 1000 || Math.abs(starWalk - porig.getX()) >= 10 * 64){
        	star = 0;
        	starTime = 0;
        	starWait = 0;
        	starWalk = 0;
        }
        while (i.hasNext()) {
            Sprite sprite = (Sprite)i.next();
            if (sprite instanceof Creature) {
                Creature creature = (Creature)sprite;
                if (creature.getState() == Creature.STATE_DEAD) {
                    i.remove();
                }
                else {
                	
                	//Tries to make a grub shoot when it starts moving, but crashes when grub appears on screen
                	//Uncomment second to last line and game crashes
                	//Also this has a bug where if you shoot the grub the bullet comes back and kills you
                	
                	if(creature instanceof Grub && creature.getState() != Creature.STATE_DEAD && creature.getState() != Creature.STATE_DYING){
                		if(Math.abs(creature.getX() - porig.getX()) <= 8 * 64) {
                			Grub g = (Grub) creature;
                			if(g.firsttime == 0) {
                				g.setTime(System.currentTimeMillis());
                			System.out.println("Contact");
                		}
                			else if(System.currentTimeMillis() - g.firsttime >= 500 || Math.abs(creature.getX() - porig.getX()) <= 6 * 64) {
		                		g.startMove();
                				if(System.currentTimeMillis() - g.lastshot >= 1000 && g.getState() != Creature.STATE_DEAD) {
                					Image[][] images = new Image[1][];
                					g.setShot(System.currentTimeMillis());
			                		// load left-facing images
			                		images[0] = new Image[] { 
			                				loadImage("images/cannon1.png"), loadImage("images/cannon2.png"),
			                				loadImage("images/cannon3.png"), loadImage("images/cannon2.png")};
			
			               
			                		// create creature animations
			                		Animation[] flyAnim = new Animation[4];
			                		flyAnim[0] = resourceManager.createFlyAnim(images[0][0], images[0][1], images[0][2]);
			                			
			                		Bullet b = new Bullet(flyAnim[0], flyAnim[0], flyAnim[0], flyAnim[0]);	
			                		int dir = 0;
			                		if(creature.getX() - player.getX() > 0)
			                			dir = -1;
			                		else
			                			dir = 1;
			                		b.shoot(creature.getX(), creature.getY(), dir);
			                		try{
			                			resourceManager.addSprite(map, b, (int) (creature.getX() / 64) + (dir * 2), (int) creature.getY() / 64);
			                			break;
			                		}
			                		catch (Exception e) {
			                			e.printStackTrace();
			                		}
			                		resourceManager.reloadMap();
	                				}
                			}
                		}
                	
                	}
                	
                    updateCreature(creature, elapsedTime);
                }
            }
            // normal update
            sprite.update(elapsedTime);
        }
        
    }


    /**
        Updates the creature, applying gravity for creatures that
        aren't flying, and checks collisions.
    */
    private void updateCreature(Creature creature,
        long elapsedTime)
    {

        // apply gravity
    	if (!creature.isFlying()) {
            creature.setVelocityY(creature.getVelocityY() +
                GRAVITY * elapsedTime);
        }

        // change x
        float dx = creature.getVelocityX();
        float oldX = creature.getX();
        float newX = oldX + dx * elapsedTime;
        Point tile =
            getTileCollision(creature, newX, creature.getY());
        if (tile == null) {
            creature.setX(newX);
        }
        else {
            // line up with the tile boundary
            if (dx > 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x) -
                    creature.getWidth());
            }
            else if (dx < 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x + 1));
            }
            creature.collideHorizontal();
        }
        
        if(creature instanceof Grub){
        	checkGrubCollision((Grub)creature,true);
        }

        // change y
        float dy = creature.getVelocityY();
        float oldY = creature.getY();
        float newY = oldY + dy * elapsedTime;
        tile = getTileCollision(creature, creature.getX(), newY);
        if (tile == null) {
            creature.setY(newY);
        }
        else {
            // line up with the tile boundary
            if (dy > 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y) -
                    creature.getHeight());
            }
            else if (dy < 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y + 1));
            }
            creature.collideVertical();
        }
        if (creature instanceof Player) {
            boolean canKill = true;//(oldY < creature.getY() - 100);
            checkPlayerCollision((Player)creature, canKill);
        }
        
    }

    public void checkGrubCollision(Grub grub, boolean canKill){
    	if (!grub.isAlive()) {
            //updateHealth = 0;
            return;
        }
    	Sprite collisionSprite = getSpriteCollision(grub);
    	
    	if(collisionSprite instanceof Bullet){
    		Bullet b = (Bullet)collisionSprite;
    		if(canKill){
    			soundManager.play(prizeSound);
    			grub.setState(Creature.STATE_DYING);
    			b.setState(Creature.STATE_DEAD);
    			porig.updateHealth(10.0);
    			//updateHealth = 1;
    		}
    	}
    }
    
   
    /**
        Checks for Player collision with other Sprites. If
        canKill is true, collisions with Creatures will kill
        them.
    */
    public void checkPlayerCollision(Player player,
        boolean canKill)
    {
        if (!player.isAlive()) {
            return;
        }
        // check for player collision with other sprites
        Sprite collisionSprite = getSpriteCollision(player);
        if (collisionSprite instanceof PowerUp) {
            acquirePowerUp((PowerUp)collisionSprite,player);
        }
        else if (collisionSprite instanceof Creature) {
            Creature badguy = (Creature)collisionSprite;
            if (canKill && badguy instanceof Grub && star != 1) {
                // kill the badguy and make player bounce
                soundManager.play(prizeSound);
                player.setState(Creature.STATE_DYING);
                duck = 0;
            }
            else if(duck == 0){
            	//take damage
            	if(star != 1){
            		player.updateHealth(-5.0);
            	}
            	if(badguy instanceof Bullet){
            		badguy.setState(Creature.STATE_DEAD);
            	}
            	player.setLastHealth(System.currentTimeMillis());
            	if(player.getHealth() == 0) {
            		player.setState(Creature.STATE_DYING);
            		soundManager.play(boopSound);
            	}
              	if(badguy instanceof Bullet){
            		badguy.setState(Creature.STATE_DEAD);
            	}
              	else{
              		badguy.setState(Creature.STATE_DYING);
              	}
            }
        }
        
    }


    /**
        Gives the player the speicifed power up and removes it
        from the map.
    */
    public void acquirePowerUp(PowerUp powerUp,Player player) {
        // remove it from the map
        map.removeSprite(powerUp);

        if (powerUp instanceof PowerUp.Star) {
        	starTime = System.currentTimeMillis();
        	starWait = System.currentTimeMillis();
        	starWalk = player.getX();
            soundManager.play(prizeSound);
            star = 1;
        }
        else if (powerUp instanceof PowerUp.Music) {
            // change the music
            soundManager.play(prizeSound);
            toggleDrumPlayback();
        }
        else if (powerUp instanceof PowerUp.Goal) {
            // advance to next map
            soundManager.play(prizeSound,
                new EchoFilter(2000, .7f), false);
            map = resourceManager.loadNextMap();
        }
        else if(powerUp instanceof PowerUp.Mushroom){
        	player.updateHealth(5.0);
        	soundManager.play(prizeSound);
        }
        else if(powerUp instanceof PowerUp.Explode){
        	player.updateHealth(-10.0);
        }
        else if(powerUp instanceof PowerUp.Gas){
        	gasTime = System.currentTimeMillis();
        	gasWait = System.currentTimeMillis();
        	gasWalk = player.getX();
            gas = 1;
        }
    }

}
