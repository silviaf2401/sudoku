/*
 * File: GraphicsContestConstants.java
 * Name: Silvia Fernandez (SUNet ID: silviaf)
 * Section Leader: Do Park
 * ------------------------------
 * This file declares several constants that are used by the GraphicsContest class.  
 */

import java.awt.*;

import acm.graphics.GImage;
import acm.graphics.GLabel;

public interface GraphicsContestConstants {
/** Grid size **/
	public static final int NROWSGRID = 9;
	public static final int NCOLSGRID = 9;

	/*** Pad size **/
	public static final int NROWSPAD = 3;
	public static final int NCOLSPAD = 3;

/** Image files **/
	public static final String WELCOME_SCREEN = "background3.jpg";
	public static final String EMPTY_GRID = "EmptyGrid.png";
	public static final String LOGO = "SudokuLogo.png";
	public static final String TRASH_ICON = "Trashcan.png";
	public static final String QUESTION_MARK ="Tutorial.png";
	public static final String LOCK = "unlock.png";
	public static final String STAR_ICON = "GuessesLeft.png";
	public static final String INFO_ICON = "info.png";
	public static final String TUTORIAL_SCREEN = "TutorialPicture.png";
	public static final String LOST_SCREEN = "YouLost.png";
	public static final String ICONS_EXPLAINED = "Iconsexplained.png";
	public static final String YOU_WON = "YouWon.png";
	public static final String PAD = "SmallGridPlayer.png";
	public static final String GOLDTRASHCAN = "deletegold.png";
	public static final String GOLDSTAR ="Gold_Star.png";

/** Sound files **/
	public static final String WIN_SOUND = "wingame.au";
	public static final String LOST_SOUND = "lostgame.au";
	public static final String GUESS_SOUND = "correctguess.au";
	
	
/** Colors **/
	public static final Color TRANSLUCID_GOLD = new Color(247,163,0,65);
	public static final Color TRANSLUCID_ORANGE = new Color(224,100,12, 80);
	public static final Color CUSTOM_BROWN = new Color(67,27,15);
	public static final Color TRANSLUCID_BEIGE =  new Color(239,215,150,70);
	public static final Color CUSTOM_ORANGE = new Color(224,100,12);
	
/** Grid files **/
	public static final String EASY_GRID_ONE = "EasyOneStringNb.txt";
	public static final String EASY_GRID_TWO = "EasyTwoStringNb.txt";
	public static final String EASY_GRID_THREE = "EasyThreeStringNb.txt";
	public static final String EASY_GRID_ONE_SLN = "EasyOneSlnStringNb.txt";
	public static final String EASY_GRID_TWO_SLN = "EasyTwoStringNbSln.txt";
	public static final String EASY_GRID_THREE_SLN = "EasyThreeStringNbSln.txt";
	public static final String MEDIUM_GRID_ONE = "MediumOneStringNb.txt";
	public static final String MEDIUM_GRID_TWO = "MediumTwoStringNb.txt";
	public static final String MEDIUM_GRID_THREE = "MediumThreeStringNb.txt";
	public static final String MEDIUM_GRID_ONE_SLN = "MediumOneSlnStringNb.txt";
	public static final String MEDIUM_GRID_TWO_SLN = "MediumTwoStringNbSln.txt";
	public static final String MEDIUM_GRID_THREE_SLN = "MediumThreeStringNbSln.txt";
	public static final String HARD_GRID_ONE = "HardOneStringNb.txt";
	public static final String HARD_GRID_TWO = "HardTwoStringNb.txt";
	public static final String HARD_GRID_THREE = "HardThreeStringNb.txt";
	public static final String HARD_GRID_ONE_SLN = "HardOneSlnStringNb.txt";
	public static final String HARD_GRID_TWO_SLN = "HardTwoStringNbSln.txt";
	public static final String HARD_GRID_THREE_SLN = "HardThreeStringNbSln.txt";
	


/** Drawing constants **/
	public static final double LIFELEFT_WIDTH = 0.07;
	public static final double LIFELEFT_HEIGHT = 0.105;
	public static final double PLAYERNUMBERSX = 0.33;
	public static final double PLAYERNUMBERS_WIDTH = 0.2;
	public static final double PLAYERNUMBERS_HEIGHT = 0.28;
	public static final double TRASHCAN_WIDTH = 0.08;
	public static final double TRASHCAN_HEIGHT = 0.12;
	public static final double GRID_IMAGEY = 0.3;
	public static final double TUTORIAL_IMAGE_RESIZE_WIDTH = 0.7;
	public static final double TUTORIAL_IMAGE_RESIZE_HEIGHT = 0.9;
	public static final double NUMBERS_RESIZE = 14;
	public static final double LOST_SCREEN_WIDTHRESIZE = 0.38;
	public static final double LOST_SCREEN_HEIGTHRESIZE = 0.8;
	public static final double LOST_SCREEN_RIGHTMARGIN = 30;
	public static final double LOST_SCREEN_LEFTMARGIN = 20;
	public static final double COUNTERTEXT_X = 0.4;
	public static final double COUNTERTEXT_Y = 2;
	public static final double GRID_IMAGE_XRESIZE = 0.02;
	public static final double GRID_IMAGE_YRESIZE =	0.158;	
	public static final double LOGO_RESIZE = 0.75;	
	public static final double HIGHLIGHT_WIDTH = 33;
	public static final double HIGHLIGHT_HEIGHT =28.5;
	public static final double HIGHLIGHT_MARGIN = 3.5;
	public static final double MINTRASHCAN_X = 447;
	public static final double MAXTRASHCAN_X = 497;
	public static final double MINTRASHCAN_Y = 291;
	public static final double MAXTRASHCAN_Y = 341;
	

	
/** Guesses available**/
	public static final int NGUESSES = 6;

/** Fonts**/
	public static final Font FONT_COUNTER = new Font("SansSerif", Font.BOLD, 15);
	
	
}
