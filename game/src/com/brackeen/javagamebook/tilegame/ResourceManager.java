package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.ArrayList;
import javax.swing.ImageIcon;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.tilegame.sprites.*;

/**
 * The ResourceManager class loads and manages tile Images and "host" Sprites
 * used in the game. Game Sprites are cloned from "host" Sprites.
 */
public class ResourceManager {

	private ArrayList tiles;
	private int currentMap;
	private GraphicsConfiguration gc;

	// host sprites used for cloning
	private Sprite playerSprite;
	private Sprite musicSprite;
	private Sprite coinSprite;
	private Sprite goalSprite;
	private Sprite grubSprite;
	private Sprite flySprite;
	private Sprite teleportSprite;
	private Sprite mushroomSprite;
	private Sprite gasSprite;
	private Sprite expSprite;

	/**
	 * Creates a new ResourceManager with the specified GraphicsConfiguration.
	 */
	public ResourceManager(GraphicsConfiguration gc) {
		this.gc = gc;
		loadTileImages();
		loadCreatureSprites();
		loadPowerUpSprites();
	}

	/**
	 * Gets an image from the images/ directory.
	 */
	public Image loadImage(String name) {
		String filename = "images/" + name;
		return new ImageIcon(filename).getImage();
	}

	public Image getMirrorImage(Image image) {
		return getScaledImage(image, -1, 1);
	}

	public Image getFlippedImage(Image image) {
		return getScaledImage(image, 1, -1);
	}

	private Image getScaledImage(Image image, float x, float y) {

		// set up the transform
		AffineTransform transform = new AffineTransform();
		transform.scale(x, y);
		transform.translate((x - 1) * image.getWidth(null) / 2,
				(y - 1) * image.getHeight(null) / 2);

		// create a transparent (not translucent) image
		Image newImage = gc.createCompatibleImage(image.getWidth(null),
				image.getHeight(null), Transparency.BITMASK);

		// draw the transformed image
		Graphics2D g = (Graphics2D) newImage.getGraphics();
		g.drawImage(image, transform, null);
		g.dispose();

		return newImage;
	}

	public TileMap loadNextMap() {
		TileMap map = null;
		while (map == null) {
			currentMap++;
			try {
				map = loadMap("maps/map" + currentMap + ".txt");
			} catch (IOException ex) {
				if (currentMap == 1) {
					// no maps to load!
					return null;
				}
				currentMap = 0;
				map = null;
			}
		}

		return map;
	}
	
	public TileMap cmdArgMap(String arg) {
		TileMap map = null;
		try {
			map = loadMap("maps/" + arg);
			
		}	catch (IOException ex) {
			map = loadNextMap();
		}
		return map;
	}

	public TileMap reloadMap() {
		try {
			return loadMap("maps/map" + currentMap + ".txt");
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private TileMap loadMap(String filename) throws IOException {
		ArrayList lines = new ArrayList();
		int width = 0;
		int height = 0;

		// read every line in the text file into the list
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		while (true) {
			String line = reader.readLine();
			// no more lines to read
			if (line == null) {
				reader.close();
				break;
			}

			// add every line except for comments
			if (!line.startsWith("#")) {
				lines.add(line);
				width = Math.max(width, line.length());
			}
		}

		// parse the lines to create a TileEngine
		height = lines.size();
		TileMap newMap = new TileMap(width, height);
		for (int y = 0; y < height; y++) {
			String line = (String) lines.get(y);
			for (int x = 0; x < line.length(); x++) {
				char ch = line.charAt(x);

				// check if the char represents tile A, B, C etc.
				int tile = ch - 'A';
				if (tile >= 0 && tile < tiles.size()) {
					newMap.setTile(x, y, (Image) tiles.get(tile));
				}

				// check if the char represents a sprite
				else if (ch == 'o') {
					addSprite(newMap, coinSprite, x, y);
				} else if (ch == '!') {
					addSprite(newMap, musicSprite, x, y);
				} else if (ch == '*') {
					addSprite(newMap, goalSprite, x, y);
				} else if (ch == '1') {
					addSprite(newMap, grubSprite, x, y);
				} else if (ch == '2') {
					addSprite(newMap, flySprite, x, y);
				} else if (ch == 'T') {
					addSprite(newMap, teleportSprite, x, y);
				} else if (ch == 'M') {
					addSprite(newMap, mushroomSprite, x, y);
				} else if (ch == 'Y') {
					addSprite(newMap, gasSprite, x, y);
				} else if (ch == 'Z') {
					addSprite(newMap, expSprite, x, y);
				}
				
				
				
			}
		}

		// add the player to the map
		Sprite player = (Sprite) playerSprite.clone();
		// player.setX(TileMapRenderer.tilesToPixels(3));
		player.setX(player.getX());
		// System.out.println(player.getX());
		player.setY(0);
		newMap.setPlayer(player);

		return newMap;
	}

	public void addSprite(TileMap map, Sprite hostSprite, int tileX, int tileY) {
		if (hostSprite != null) {
			// clone the sprite from the "host"
			Sprite sprite = (Sprite) hostSprite.clone();

			// center the sprite
			sprite.setX(TileMapRenderer.tilesToPixels(tileX)
					+ (TileMapRenderer.tilesToPixels(1) - sprite.getWidth())
					/ 2);

			// bottom-justify the sprite
			sprite.setY(TileMapRenderer.tilesToPixels(tileY + 1)
					- sprite.getHeight());

			// add it to the map
			map.addSprite(sprite);
		}
	}

	// -----------------------------------------------------------
	// code for loading sprites and images
	// -----------------------------------------------------------

	public void loadTileImages() {
		// keep looking for tile A,B,C, etc. this makes it
		// easy to drop new tiles in the images/ directory
		tiles = new ArrayList();
		char ch = 'A';
		while (true) {
			String name = "tile_" + ch + ".png";
			File file = new File("images/" + name);
			if (!file.exists()) {
				break;
			}
			tiles.add(loadImage(name));
			ch++;
		}
	}

	public void loadCreatureSprites() {

		Image[][] images = new Image[4][];

		// load left-facing images
		images[0] = new Image[] { loadImage("player1.png"),
				loadImage("player2.png"), loadImage("player3.png"),
				loadImage("player4.png"), loadImage("player5.png"),
				loadImage("player6.png"), loadImage("player7.png"),
				loadImage("player8.png"), loadImage("player9.png"),
				loadImage("player10.png"), loadImage("player11.png"),
				loadImage("player12.png"), loadImage("player13.png"),
				loadImage("player14.png"), loadImage("player15.png"),
				loadImage("player16.png"), loadImage("player17.png"),
				loadImage("player18.png"), loadImage("player19.png"),
				loadImage("player20.png"), loadImage("player21.png"),

				loadImage("fly1.png"), loadImage("fly2.png"),
				loadImage("fly3.png"), loadImage("grub1.png"),
				loadImage("grub2.png"), };

		images[1] = new Image[images[0].length];
		images[2] = new Image[images[0].length];
		images[3] = new Image[images[0].length];
		for (int i = 0; i < images[0].length; i++) {
			// right-facing images
			images[0][i] = getMirrorImage(images[0][i]);
			// Left-facing images
			images[1][i] = getMirrorImage(images[0][i]);
			// left-facing "dead" images
			images[2][i] = getFlippedImage(images[0][i]);
			// right-facing "dead" images
			images[3][i] = getFlippedImage(images[1][i]);
		}

		// create creature animations
		Animation[] playerAnim = new Animation[4];
		Animation[] flyAnim = new Animation[4];
		Animation[] grubAnim = new Animation[4];
		for (int i = 0; i < 4; i++) {
			playerAnim[i] = createPlayerAnim(images[i][0], images[i][1],
					images[i][2], images[i][3], images[i][4], images[i][5],
					images[i][6], images[i][7], images[i][8], images[i][9],
					images[i][10], images[i][11], images[i][12], images[i][13],
					images[i][14], images[i][15], images[i][16], images[i][17],
					images[i][18], images[i][19], images[i][20]);
			flyAnim[i] = createFlyAnim(images[i][21], images[i][22],
					images[i][23]);
			grubAnim[i] = createGrubAnim(images[i][24], images[i][25]);
		}

		// create creature sprites
		playerSprite = new Player(playerAnim[0], playerAnim[1], playerAnim[2],
				playerAnim[3]);
		flySprite = new Fly(flyAnim[0], flyAnim[1], flyAnim[2], flyAnim[3]);
		grubSprite = new Grub(grubAnim[0], grubAnim[1], grubAnim[2],
				grubAnim[3]);
		teleportSprite = new Teleporter(grubAnim[0]);
	}

	private Animation createPlayerAnim(Image player1, Image player2,
			Image player3, Image player4, Image player5, Image player6,
			Image player7, Image player8, Image player9, Image player10,
			Image player11, Image player12, Image player13, Image player14,
			Image player15, Image player16, Image player17, Image player18,
			Image player19, Image player20, Image player21) {
		Animation anim = new Animation();
		anim.addFrame(player1, 80);
		anim.addFrame(player2, 80);
		anim.addFrame(player3, 80);
		anim.addFrame(player4, 80);
		anim.addFrame(player5, 80);
		anim.addFrame(player6, 80);
		anim.addFrame(player7, 80);
		anim.addFrame(player8, 80);
		anim.addFrame(player9, 80);
		anim.addFrame(player10, 80);
		anim.addFrame(player11, 80);
		anim.addFrame(player12, 80);
		anim.addFrame(player13, 80);
		anim.addFrame(player14, 80);
		anim.addFrame(player15, 80);
		anim.addFrame(player16, 80);
		anim.addFrame(player17, 80);
		anim.addFrame(player18, 80);
		anim.addFrame(player19, 80);
		anim.addFrame(player20, 80);
		anim.addFrame(player21, 80);
		return anim;
	}

	public Animation createFlyAnim(Image img1, Image img2, Image img3) {
		Animation anim = new Animation();
		anim.addFrame(img1, 110);
		anim.addFrame(img2, 110);
		anim.addFrame(img3, 110);
		anim.addFrame(img2, 110);
		return anim;
	}

	private Animation createGrubAnim(Image img1, Image img2) {
		Animation anim = new Animation();
		anim.addFrame(img1, 250);
		anim.addFrame(img2, 250);
		return anim;
	}

	private void loadPowerUpSprites() {
		// create "goal" sprite
		Animation anim = new Animation();
		anim.addFrame(loadImage("heart1.png"), 150);
		anim.addFrame(loadImage("heart2.png"), 150);
		anim.addFrame(loadImage("heart3.png"), 150);
		anim.addFrame(loadImage("heart2.png"), 150);
		goalSprite = new PowerUp.Goal(anim);

		// create "star" sprite
		anim = new Animation();
		anim.addFrame(loadImage("star1.png"), 100);
		anim.addFrame(loadImage("star2.png"), 100);
		anim.addFrame(loadImage("star3.png"), 100);
		anim.addFrame(loadImage("star4.png"), 100);
		coinSprite = new PowerUp.Star(anim);

		// create "music" sprite
		anim = new Animation();
		anim.addFrame(loadImage("music1.png"), 150);
		anim.addFrame(loadImage("music2.png"), 150);
		anim.addFrame(loadImage("music3.png"), 150);
		anim.addFrame(loadImage("music2.png"), 150);
		musicSprite = new PowerUp.Music(anim);
		
		anim = new Animation();
		anim.addFrame(loadImage("powerup1.png"), 100);
		anim.addFrame(loadImage("powerup2.png"), 100);
		anim.addFrame(loadImage("powerup3.png"), 100);
		anim.addFrame(loadImage("powerup4.png"), 100);
		mushroomSprite = new PowerUp.Mushroom(anim);
		
		anim = new Animation();
		anim.addFrame(loadImage("blackBlock.png"), 100);
		gasSprite = new PowerUp.Gas(anim);
		
		anim = new Animation();
		anim.addFrame(loadImage("swords.png"), 100);
		expSprite = new PowerUp.Explode(anim);
	}
	

}
