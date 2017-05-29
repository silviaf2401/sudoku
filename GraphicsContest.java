/*
 * File: GraphicsContest.java
 * --------------------------
 * This program implements the game of Sudoku. In it a player needs to completely fill the emtpy spots of a large grid (9x9) with numbers 1-9. 
 * The grid has some prepopulated spaces.
 * Each number can only be entered once per row and per column. The player can add numbers to the grid by clicking on a case until it highlights, then selecting
 * a number on a number pad. The player can delete numbers he/she added to the grid by clicking on them, then clickin on the trash can icon on the screen where the grid is.
 * The game continues until the player finishes populating the grid. At that point the grid is compared to the solution grid. If they math, the player won, if
 * they do not match, the player did not win. 
 * Player can select a level (beginner to expert), after selection a random started grid is loaded.
 * Player has up to 6 guesses, which can be redeemed by clicking on an empty space in the grid, then on the star button at the button of the screen.
 * The number of guesses left is indicated in a star picture with a counter on the section of the screen where the grid is. 
 * Once the player has used all his/her guesses, the star button is disabled and the star with the counter disappears. 
 * When the user only has one case to fill in the grid, the player loses any remaining guesses, the star button is disabled and the star with the counter disappears.
 * The user can click on an info "i" icon button that explains what the icons in the game signify, a tutorial icon button  "?" where the game rules are explained, 
 * a lock icon button which unlocks the solution (and user forfeits game), and a trashcan icon that resets all the entries in the game so far.
 * When a player loses or wins, screen with a message stating that is displayed.
 */

import acm.program.*;
import acm.util.MediaTools;
import acm.util.RandomGenerator;
import acm.graphics.*;
import acm.io.IODialog;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.JButton;


public class GraphicsContest extends GraphicsProgram implements GraphicsContestConstants {

	// Instance variable for GridImage, accessed by multiple methods
	private GImage gridImage = null;
	// Keeps track of the numbers present on the grid at any given time of the game (accessed by multiple methods)
	private int[][] gameStatus = new int[NROWSGRID][NCOLSGRID];
	// Keeps track of the solution grid, accessed by multiple methods
	private int[][] gameSolution = new int[NROWSGRID][NCOLSGRID];
	// Keeps track of the starting grid, accessed by multiple methods
	private int[][] originalGrid = new int[NROWSGRID][NCOLSGRID];
	// Keeps track of the row and column clicked at any given time, accessed by multiple methods
	private int[] rowColumnClicked = {-1,-1};
	
	// x and y of upper left corner of each case in large grid, and of the right hand side limit of the large grid (upper left corner of next case if it existed). Accessed by multiple methods.
	private double[][] xCoord = new double[NROWSGRID+1][NCOLSGRID+1];
	private double[][] yCoord = new double[NROWSGRID+1][NCOLSGRID+1];
	
	// keeps track of whether a cell has been selected or not, accessed by multiple methods. 
	private boolean cellSelected = false;
	// keeps track of whether we have selected a number on the pad or clicked on the trashcan image, accessed by multiple methods.
	private boolean actionSelected = false;
	// x and y of upper left corner of each case in number pad, and of the right hand side limit of the pad (upper left corner of next case if it existed). Accessed by multiple methods.
	private double[][] xCoordSmallGrid = new double[NROWSPAD +1][NCOLSPAD+1];
	private double[][] yCoordSmallGrid = new double[NROWSPAD +1][NCOLSPAD+1];
	
	// Keeps track of the value that was selected in the game pad and trash can image (=0 if user clicked on trash can image, otherwise value of number selected in game pad).
	private int valueSelected = -1;
	
	// GRect that highlights cells in the grid. Accessed by multiple methods.
	private GRect highlightRect = null;
	// Audioclips played when player looses. Accessed by multiple methods
	private AudioClip lostClip = MediaTools.loadAudioClip(LOST_SOUND);
	// Random number generator
	private RandomGenerator rg = new RandomGenerator();

	// Buttons added at bottom of screen, accessed by multiple methods.
	private JButton southButtonTutorial;
	private JButton southButtonTrashcan;
	private JButton southButtoncheckmark;
	private JButton southButtonunlockAnswer;
	private JButton southButtonInfo;
	
	// Keeps track of the number of guesses left, accessed by multiple methods.
	private int counterLifes = NGUESSES;
	
	boolean tutorialHasbeenClicked = false;
	boolean infoHasbeenClicked = false;
	
	private GRect transparencyScreenTutorial= null;
	private GImage tutorialImage =null;
	private GImage infoImage = null;
	private GRect transparencyScreenInfo = null;
	private GImage lifeLeft = null;
	private GLabel counterText = null;
	private GImage trashCan = null;
	// Keeps track of whether there are empty cells in the grid. Accessed by multiple methods.
	private boolean emptyCells = true;

	/** Method: init 
	 * --------------
	 * This method creates the clickable icon buttons at the bottom of the screen, provides the name of action when they are clicked, adds them to the screen,
	 * adds action listeners.
	 */

	public void init() {
		ImageIcon iconTrashcan = new ImageIcon(TRASH_ICON);
		ImageIcon iconTutorial = new ImageIcon(QUESTION_MARK);
		ImageIcon iconunlockAnswer = new ImageIcon(LOCK);
		ImageIcon checkmark = new ImageIcon(STAR_ICON);
		ImageIcon info = new ImageIcon(INFO_ICON);
		southButtonInfo = new JButton(info);
		add(southButtonInfo, SOUTH);
		southButtonInfo.setActionCommand("info");
		southButtonTutorial = new JButton(iconTutorial);
		add(southButtonTutorial, SOUTH);
		southButtonTutorial.setActionCommand("Tutorial");
		southButtoncheckmark = new JButton(checkmark);
		add(southButtoncheckmark, SOUTH);
		southButtoncheckmark.setActionCommand("checkmark");
		southButtonTrashcan = new JButton(iconTrashcan);
		southButtonTrashcan.setActionCommand("Deleteall");
		add(southButtonTrashcan, SOUTH);
		southButtonunlockAnswer = new JButton(iconunlockAnswer);
		southButtonunlockAnswer.setActionCommand("Unlock");
		add(southButtonunlockAnswer, SOUTH);
		addActionListeners();

	}

	/** Method: actionPerformed
	 * ------------------------
	 * This method defines what happens when we click on each of the image buttons added to the screen in the init method.
	 * When the icon with the "?" is clicked for the first time, a screen with a tutorial appears on screen. If the Tutorial icon is 
	 * clicked again, the screen with the tutorial disappears.
	 * When the icon with the lock is clicked, the solutions for the ongoing grid are displayed, the player is declared to have lost.
	 * 
	 */

	public void actionPerformed(ActionEvent e) {
		/** When player clicks on the tutorial icon button for the first time, a splashcreen with the tutorial is displayed.
		 * When player clicks on the tutorial icon button a second time, it disappears
		 */
		if (e.getActionCommand().equals("Tutorial")) {
			if (!tutorialHasbeenClicked) {
				tutorialHasbeenClicked = true;
				transparencyScreenTutorial = new GRect(getWidth(), getHeight());
				transparencyScreenTutorial.setColor(TRANSLUCID_GOLD);
				transparencyScreenTutorial.setFilled(true);
				add(transparencyScreenTutorial);
				tutorialImage = new GImage(TUTORIAL_SCREEN);
				tutorialImage.setSize(getWidth()*TUTORIAL_IMAGE_RESIZE_WIDTH, getHeight()*TUTORIAL_IMAGE_RESIZE_HEIGHT);
				add(tutorialImage, (getWidth()-tutorialImage.getWidth())*0.5, (getHeight()-tutorialImage.getHeight())*0.5);
			} else {
				remove(tutorialImage);
				remove(transparencyScreenTutorial);
				tutorialHasbeenClicked = false;
			}
		}
		/* When player clicks on unlock icon button, the solution grid is displayed on the screen, the player forfeits game and loses. 
		 * All icon buttons at the bottom of the screen are inactivated. Splash screen displaying player did not win displayed.
		 */
		if (e.getActionCommand().equals("Unlock")) {
			removeAll();
			displayWelcomeScreen();
			for (int r = 0; r< NROWSGRID; r++) {
				for (int c = 0; c < NCOLSGRID; c++) {
					if (originalGrid[r][c] == 0) {
						GImage numberImage = new GImage(String.valueOf(gameSolution[r][c])+"red.png");
						numberImage.setSize(gridImage.getWidth()/NUMBERS_RESIZE, gridImage.getHeight()/NUMBERS_RESIZE);
						add(numberImage, xCoord[r][c], yCoord[r][c]);

					} else {
						GImage numberImage = new GImage(String.valueOf(gameSolution[r][c])+".png");
						numberImage.setSize(gridImage.getWidth()/NUMBERS_RESIZE, gridImage.getHeight()/NUMBERS_RESIZE);
						add(numberImage, xCoord[r][c], yCoord[r][c]);

					}
				}
			}
			lostClip.play();
			southButtonTutorial.setEnabled(false);
			southButtonunlockAnswer.setEnabled(false);
			southButtonTrashcan.setEnabled(false);
			southButtoncheckmark.setEnabled(false);
			southButtonInfo.setEnabled(false);

			GImage splashScreenLost = new GImage(LOST_SCREEN);
			GRect transparencyScreen = new GRect(getWidth(), getHeight());
			transparencyScreen.setColor(TRANSLUCID_GOLD);
			transparencyScreen.setFilled(true);
			add(transparencyScreen);
			splashScreenLost.setSize(getWidth()*LOST_SCREEN_WIDTHRESIZE, getHeight()*LOST_SCREEN_HEIGTHRESIZE);
			add(splashScreenLost,(getWidth()- splashScreenLost.getWidth() - LOST_SCREEN_RIGHTMARGIN), (getHeight()- splashScreenLost.getHeight())*0.5 + LOST_SCREEN_LEFTMARGIN);

		}

		/* When player clicks on the trash can icon button, dialog window asks player to confirm if they want to clear all the numbers entered so far. 
		 *  if player confirms, grid is reset to original state. If not, game resumes.
		 */
		if (e.getActionCommand().equals("Deleteall")) {
			IODialog dialog = getDialog();
			int resetGame = -1;
			GRect transparencyScreen= null;
			int counter =0;
			while (resetGame != 1 && resetGame!=2 ) {
				transparencyScreen = new GRect(getWidth(), getHeight());
				transparencyScreen.setColor(TRANSLUCID_GOLD);
				transparencyScreen.setFilled(true);
				add(transparencyScreen);
				counter++;
				resetGame = dialog.readInt("Are you sure you want to reset the grid? Enter 1 for YES, 2 to continue your game.");
			}
			if (resetGame == 1) {
				for (int i = 0; i < counter; i++) {
					remove(getElementAt(getWidth()*0.5,getHeight()*0.5)); 
				}
				counter =0;
				for (int r =0; r< NROWSGRID; r++) {
					for (int c =0; c<NCOLSGRID; c++) {
						gameStatus[r][c] = originalGrid[r][c];
					}
				}
				updateDisplayUserGuesses();
				resetGame = -1;
			} else {
				for (int i = 0; i < counter; i++) {
					remove(getElementAt(getWidth()*0.5,getHeight()*0.5)); 
				}
				counter =0;
			}

		}

		/* When player clicks on the star icon button, after having selected an empty case on the grid, the number on 
		 * that case is revealed. The player guess count decreases by one and the counter on the screen reflects the change. 
		 * If the player has no guesses left or if there is only one empty case on the grid, the star icon button is disabled and the 
		 * counter disappears.
		 */
		if (e.getActionCommand().equals("checkmark")) {
			AudioClip guessGiven = MediaTools.loadAudioClip(GUESS_SOUND);
			if (cellSelected) {
				gameStatus[rowColumnClicked[0]][rowColumnClicked[1]] = gameSolution[rowColumnClicked[0]][rowColumnClicked[1]];
				cellSelected = false;
				highlightRect = null;
				updateDisplayUserGuesses(); 
				counterLifes --;
				emptyCells = hasEmptyCells();
				remove(counterText);
				counterText= new GLabel(Integer.toString(counterLifes));
				counterText.setFont(FONT_COUNTER);

				counterText.setColor(CUSTOM_BROWN);
				add(counterText, trashCan.getX()+ lifeLeft.getWidth()*COUNTERTEXT_X, lifeLeft.getY() + counterText.getAscent()*COUNTERTEXT_Y);
				guessGiven.play();
				if (counterLifes < 1 || countEmptyCells() == 1) {
					southButtoncheckmark.setEnabled(false);
					remove(lifeLeft);
					remove(counterText);
					counterLifes = 0;
				}
			}
		}


		/** When player clicks on the "i" icon button for the first time, a splashcreen with information on the games icons' meaning is displayed.
		 * When player clicks on the "i"  icon button a second time, it disappears.
		 */
		if (e.getActionCommand().equals("info")) {

			if (!infoHasbeenClicked) {
				infoHasbeenClicked = true;
				infoImage = new GImage(ICONS_EXPLAINED);
				transparencyScreenInfo = new GRect(getWidth(), getHeight());
				transparencyScreenInfo.setColor(TRANSLUCID_GOLD);
				transparencyScreenInfo.setFilled(true);
				add(transparencyScreenInfo);
				infoImage.setSize(getWidth()*LOST_SCREEN_WIDTHRESIZE, getHeight()*LOST_SCREEN_HEIGTHRESIZE);
				add(infoImage,(getWidth()- infoImage.getWidth() - LOST_SCREEN_RIGHTMARGIN), (getHeight()- infoImage.getHeight())*0.5 +LOST_SCREEN_LEFTMARGIN);
			} else {
				remove(infoImage);
				remove(transparencyScreenInfo);
				infoHasbeenClicked = false;
			}   
		}
	}
	/* Method: run
	 * ---------------
	 * This method loads the welcome screen, makes it responsive to the mouse, asks user what level they want to play in.
	 * Then the grid and pad are loaded, the game is played, then the game results are checked.
	 */


	public void run() {
		displayWelcomeScreen();
		addMouseListeners();
		IODialog dialog = getDialog();
		int difficultyLevel = -1;
		while (difficultyLevel != 1 && difficultyLevel!=2 && difficultyLevel!=3) {
			difficultyLevel = dialog.readInt("Enter player level: 1 (Beginner) to 3 (Expert)");
		}
		loadGrid(difficultyLevel);
		loadPlayerControls();
		emptyCells = hasEmptyCells();
		playGame();
		checkEndGame();

	}

	/* Method: playGame
	 * ---------------
	 * While there are empty cells, the grid and the guesses left are updated as player adds numbers
	 * to the grid and uses his/her guesses. If there are no empty cells left in the grid, we break from the game.
	 * If there are no guesses left or if there is only one empty cell in the grid, the representation of the guesses disappears and the guesses button is deactivated.
	 */

	private void playGame() {
		while (emptyCells) {
			if (actionSelected) {
				actionSelected = false;
				cellSelected = false;
				gameStatus[rowColumnClicked[0]][rowColumnClicked[1]] = valueSelected;
				emptyCells = hasEmptyCells();
				if (countEmptyCells() == 1) {
					southButtoncheckmark.setEnabled(false);
					remove(lifeLeft);
					remove(counterText);
					counterLifes = 0;
				}
				updateDisplayUserGuesses(); 

			}
			updateDisplayUserGuesses();
			if (!emptyCells) { break;}
			waitForClick();
			updateDisplayUserGuesses(); 

		}
	}

	/* Method: checkEndGame
	 * ---------------
	 * This method checks to see if the grid the player has filled matches the solution grid. If it does, the player has won, if not,
	 * the player has lost. Game results are displayed on screen accordingly. Button icons are disabled at the end of the game.
	 */
	private void checkEndGame() {
		AudioClip winClip = MediaTools.loadAudioClip(WIN_SOUND);
		disableAllSouthButtons();
		if (Arrays.deepEquals(gameStatus, gameSolution)) {
			GImage splashScreenLost = new GImage(YOU_WON);
			GRect transparencyScreen = new GRect(getWidth(), getHeight());
			transparencyScreen.setColor(TRANSLUCID_BEIGE);
			transparencyScreen.setFilled(true);
			add(transparencyScreen);
			splashScreenLost.setSize(getWidth()*COUNTERTEXT_X, getHeight()*LOST_SCREEN_HEIGTHRESIZE);
			add(splashScreenLost,(getWidth()- splashScreenLost.getWidth() - LOST_SCREEN_RIGHTMARGIN), (getHeight()- splashScreenLost.getHeight())*0.5 +LOST_SCREEN_LEFTMARGIN);
			winClip.play();



		} else {
			lostClip.play();
			GImage splashScreenLost = new GImage("YouLost.png");
			GRect transparencyScreen = new GRect(getWidth(), getHeight());
			transparencyScreen.setColor(TRANSLUCID_GOLD);
			transparencyScreen.setFilled(true);
			add(transparencyScreen);
			splashScreenLost.setSize(getWidth()*LOST_SCREEN_WIDTHRESIZE, getHeight()*LOST_SCREEN_HEIGTHRESIZE);
			add(splashScreenLost,(getWidth()- splashScreenLost.getWidth() - LOST_SCREEN_RIGHTMARGIN), (getHeight()- splashScreenLost.getHeight())*0.5 +LOST_SCREEN_LEFTMARGIN);
			lostClip.play();


		}
	}

	/* Method: disableAllSouthButtons()
	 * ---------------
	 * This method disables the 5 icon buttons at the bottom of the screen.
	 */
	private void disableAllSouthButtons() {
		southButtonTutorial.setEnabled(false);
		southButtonunlockAnswer.setEnabled(false);
		southButtonTrashcan.setEnabled(false);
		southButtoncheckmark.setEnabled(false);
		southButtonInfo.setEnabled(false);

	}
	/* Method: countEmptyCells()
	 * ---------------
	 * This method counts how many empty cells our grid still has relying on our gameStatus 2D array (keeps track of numbers on the grid during game).
	 * Returns an integer with the count of empty cells.
	 */

	private int countEmptyCells() {
		int countCells = 0;
		for (int r =0; r < NROWSGRID ; r++) {
			for (int c =0; c< NCOLSGRID; c++) {
				if (gameStatus[r][c] == 0) {
					countCells++;
				}
			}   
		}
		return countCells;
	}
	/* Method: hasEmptyCells()
	 * ---------------
	 * This method returns true if the grid has emtpy cells, false if not.
	 */
	private boolean hasEmptyCells(){
		boolean emptyCells = false;
		if (countEmptyCells() > 0) {
			emptyCells = true;
		}
		return emptyCells;
	}

	/* Method: updateDisplayUserGuesses()
	 * ---------------
	 * This method redraws the display and updates the numbers of the grid based on the game status. Numbers are drawn in brown if
	 * they were in the grid at the start of the game, in pink if they were user-entered.
	 */

	private void updateDisplayUserGuesses() {
		removeAll();
		displayWelcomeScreen();
		loadPlayerControls();
		if (highlightRect != null) {add(highlightRect);}
		for (int r = 0; r< NROWSGRID; r++) {
			for (int c = 0; c < NCOLSGRID; c++) {
				if (gameStatus[r][c] !=0 && originalGrid[r][c] == 0) {
					GImage numberImage = new GImage(String.valueOf(gameStatus[r][c])+"red.png");
					numberImage.setSize(gridImage.getWidth()/NUMBERS_RESIZE, gridImage.getHeight()/NUMBERS_RESIZE);
					add(numberImage, xCoord[r][c], yCoord[r][c]);
				}
				if (originalGrid[r][c] != 0) {
					GImage numberImage = new GImage(String.valueOf(originalGrid[r][c])+".png");
					numberImage.setSize(gridImage.getWidth()/NUMBERS_RESIZE, gridImage.getHeight()/NUMBERS_RESIZE);
					add(numberImage, xCoord[r][c], yCoord[r][c]);
				}
			}
		}

	}

	/* Method: displayWelcomeScreen
	 * ---------------
	 * This method adds the background image, logo, empty grid to the screen.
	 */

	private void displayWelcomeScreen() {
		GImage backgroundImage= new GImage(WELCOME_SCREEN);
		backgroundImage.setSize(getWidth(), getHeight());
		add(backgroundImage, 0,0);
		gridImage= new GImage(EMPTY_GRID);
		gridImage.setSize(getWidth()*0.5, getHeight()*LOST_SCREEN_HEIGTHRESIZE);
		add(gridImage, getWidth()*GRID_IMAGE_XRESIZE, GRID_IMAGE_YRESIZE*getHeight());
		GImage logoImage= new GImage(LOGO);
		double logoImageWidth = logoImage.getWidth();
		double logoImageHeight = logoImage.getHeight();
		logoImageWidth *= LOGO_RESIZE;
		logoImageHeight *= LOGO_RESIZE;
		logoImage.setSize(logoImageWidth, logoImageHeight);
		add(logoImage, (getWidth()-logoImage.getWidth())*0.5, 0);

	}

	/* Method: mouseCliked
	 * ---------------
	 * This method defines what occurs when user clicks on the screen with the mouse.
	 * If a cell has not been selected yet and the user clicks on a cell that was originally emtpy, cell gets selected and 
	 * highlighted (orange). If a cell has been selected and the user clicks on the number pad, that number is added to the cell.
	 * If a cell has been selected and the user clicks on the trash image on screen, if the cell had a user-entered number,
	 * the number disappeared. If no number was present, the highlight is removed.
	 */

	public void mouseClicked(MouseEvent e) {
		double x = e.getX();
		double y = e.getY();
		// cell has not been selected yet
		if (!cellSelected) {
			for (int r = 0; r < NROWSGRID; r++) {
				for (int c =0; c < NCOLSGRID; c++) {
					if (x >= xCoord[r][c] && x < xCoord[r][c+1] && y >= yCoord[r][c] && y < yCoord[r+1][c]) {
						rowColumnClicked[0] = r;
						rowColumnClicked[1] = c;
						if (originalGrid[r][c] == 0) {
							double hCorrection =0;
							double wCorrection =0;
							hCorrection = getHCorrection(rowColumnClicked[0],rowColumnClicked[1]);
							wCorrection = getWCorrection(rowColumnClicked[0],rowColumnClicked[1]);
							highlightRect = new GRect(HIGHLIGHT_WIDTH+wCorrection,HIGHLIGHT_HEIGHT+hCorrection);
							highlightRect.setColor(CUSTOM_ORANGE);
							highlightRect.setFilled(true);
							double xcoordCorrection =0;
							double ycoordCorrection =0;
							xcoordCorrection = getXCoordCorrection(rowColumnClicked[0],rowColumnClicked[1]);
							ycoordCorrection = getYCoordCorrection(rowColumnClicked[0],rowColumnClicked[1]);
							add(highlightRect, xCoord[rowColumnClicked[0]][rowColumnClicked[1]]-HIGHLIGHT_MARGIN+xcoordCorrection, yCoord[rowColumnClicked[0]][rowColumnClicked[1]]-HIGHLIGHT_MARGIN+ycoordCorrection);
							cellSelected = true;


						}
					}
				}
			}
			// cell has been selected
		} else {
			for (int r = 0; r < NROWSPAD; r++) {
				for (int c =0; c < NCOLSPAD; c++) {
					if (x >= xCoordSmallGrid[r][c] && x < xCoordSmallGrid[r][c+1] && y >= yCoordSmallGrid[r][c] && y < yCoordSmallGrid[r+1][c]) {
						highlightRect = null;
						valueSelected = 3*r+c+1;
						actionSelected = true;
					}
				}
			}
			// if we click inside the trash can icon coordinates
			if (x >= MINTRASHCAN_X && x < MAXTRASHCAN_X && y >= MINTRASHCAN_Y &&y <= MAXTRASHCAN_Y) {
				valueSelected = 0;
				highlightRect = null;
				actionSelected = true;

			}
		}


	}

	/* Method: getXCoordCorrection
	 * ----------------------------
	 * This method does a pixel correction along the x-axis for the rectangle highlighting the cases when user clicks on them.
	 * The correction was fine-tuned manually for each of the cells. It returns a double that is used when plotting the highlight rectangle on the grid.
	 * It takes as parameters the row of the grid that is clicked (int type) and the column on the grid that is clicked (int type).
	 */
	private double getXCoordCorrection(int rowClicked, int columnClicked) {
		double xCoordCorrection =0;
		if(rowClicked == 0&& columnClicked ==1) { xCoordCorrection = 2;}
		if(rowClicked == 0&& columnClicked ==3) { xCoordCorrection = -2;}
		if(rowClicked == 0&& columnClicked ==4) { xCoordCorrection = -1;}
		if(rowClicked == 0&& columnClicked ==5) { xCoordCorrection = 1;}
		if(rowClicked == 0&& columnClicked ==6) { xCoordCorrection = -2;}
		if(rowClicked == 1&& columnClicked ==1) { xCoordCorrection = 1;}
		if(rowClicked == 1&& columnClicked ==3) { xCoordCorrection = -1;}
		if(rowClicked == 1&& columnClicked ==6) { xCoordCorrection = -2;}
		if(rowClicked == 1&& columnClicked ==7) { xCoordCorrection = -1;}
		if(rowClicked == 2&& columnClicked ==1) { xCoordCorrection = 1;}
		if(rowClicked == 2&& columnClicked ==3) { xCoordCorrection = -2;}
		if(rowClicked == 2&& columnClicked ==6) { xCoordCorrection = -2;}
		if(rowClicked == 3&& columnClicked ==3) { xCoordCorrection = -1;}
		if(rowClicked == 3&& columnClicked ==6) { xCoordCorrection = -2;}
		if(rowClicked == 4&& columnClicked ==1) { xCoordCorrection = 1;}
		if(rowClicked == 4&& columnClicked ==2) { xCoordCorrection = -1;}
		if(rowClicked == 4&& columnClicked ==3) { xCoordCorrection = -2;}
		if(rowClicked == 4&& columnClicked ==6) { xCoordCorrection = -2;}
		if(rowClicked == 5&& columnClicked ==1) { xCoordCorrection = 1;}
		if(rowClicked == 5&& columnClicked ==2) { xCoordCorrection = -1;}
		if(rowClicked == 5&& columnClicked ==3) { xCoordCorrection = -1.5;}
		if(rowClicked == 5&& columnClicked ==6) { xCoordCorrection = -2;}
		if(rowClicked == 5&& columnClicked ==8) { xCoordCorrection = 2;}
		if(rowClicked == 6&& columnClicked ==3) { xCoordCorrection = -2;}
		if(rowClicked == 6&& columnClicked ==5) { xCoordCorrection = 0.5;}
		if(rowClicked == 6&& columnClicked ==6) { xCoordCorrection = -1;}
		if(rowClicked == 6&& columnClicked ==7) { xCoordCorrection = -1;}
		if(rowClicked == 7&& columnClicked ==1) { xCoordCorrection = 1;}
		if(rowClicked == 7&& columnClicked ==3) { xCoordCorrection = -2;}
		if(rowClicked == 7&& columnClicked ==6) { xCoordCorrection = -2;}
		if(rowClicked == 7&& columnClicked ==7) { xCoordCorrection = -1;}
		if(rowClicked == 8&& columnClicked ==1) { xCoordCorrection = 1;}
		if(rowClicked == 8&& columnClicked ==2) { xCoordCorrection = -1;}
		if(rowClicked == 8&& columnClicked ==3) { xCoordCorrection = -2;}
		if(rowClicked == 8&& columnClicked ==4) { xCoordCorrection = 1;}
		if(rowClicked == 8&& columnClicked ==6) { xCoordCorrection = -2;}
		if(rowClicked == 8&& columnClicked ==7) { xCoordCorrection = -1;}
		if(rowClicked == 8&& columnClicked ==8) { xCoordCorrection = -1;}
		return xCoordCorrection;
	}

	/* Method: getYCoordCorrection
	 * ----------------------------
	 * This method does a pixel correction along the y-axis for the rectangle highlighting the cases when user clicks on them.
	 * The correction was fine-tuned manually for each of the cells. It returns a double that is used when plotting the highlight rectangle on the grid.
	 * It takes as parameters the row of the grid that is clicked (int type) and the column on the grid that is clicked (int type).
	 */

	private double getYCoordCorrection(int rowClicked, int columnClicked) {
		double yCoordCorrection =0;
		if(rowClicked == 0&& columnClicked ==0) { yCoordCorrection = 1;}
		if(rowClicked == 0&& columnClicked ==1) { yCoordCorrection = 2;}
		if(rowClicked == 0&& columnClicked ==2) { yCoordCorrection = 2;}
		if(rowClicked == 0&& columnClicked ==3) { yCoordCorrection = 2;}
		if(rowClicked == 0&& columnClicked ==4) { yCoordCorrection = 2;}
		if(rowClicked == 0&& columnClicked ==5) { yCoordCorrection = 1;}
		if(rowClicked == 0&& columnClicked ==6) { yCoordCorrection = 2;}
		if(rowClicked == 0&& columnClicked ==7) { yCoordCorrection = 2;}
		if(rowClicked == 0&& columnClicked ==8) { yCoordCorrection = 2;}
		if(rowClicked == 1&& columnClicked ==0) { yCoordCorrection = 1;}
		if(rowClicked == 1&& columnClicked ==2) { yCoordCorrection = 1;}
		if(rowClicked == 1&& columnClicked ==3) { yCoordCorrection = 1;}
		if(rowClicked == 2&& columnClicked ==0) { yCoordCorrection = 2;}
		if(rowClicked == 2) { yCoordCorrection = 2;}
		if(rowClicked == 2&& columnClicked ==5) { yCoordCorrection = 1;}
		if(rowClicked == 3&& columnClicked ==0) { yCoordCorrection = -1;}
		if(rowClicked == 3&& columnClicked ==1) { yCoordCorrection = -1;}
		if(rowClicked == 3&& columnClicked ==2) { yCoordCorrection = -1;}
		if(rowClicked == 3&& columnClicked ==3) { yCoordCorrection = -2;}
		if(rowClicked == 3&& columnClicked ==4) { yCoordCorrection = -2;}
		if(rowClicked == 3&& columnClicked ==5) { yCoordCorrection = -1;}
		if(rowClicked == 3&& columnClicked ==6) { yCoordCorrection = -2;}
		if(rowClicked == 3&& columnClicked ==7) { yCoordCorrection = -2;}
		if(rowClicked == 3&& columnClicked ==8) { yCoordCorrection = -2;}
		if(rowClicked == 4&& columnClicked ==0) { yCoordCorrection = 1.5;}
		if(rowClicked == 4&& columnClicked ==1) { yCoordCorrection = 1;}
		if(rowClicked == 4&& columnClicked ==2) { yCoordCorrection = 1;}
		if(rowClicked == 5&& columnClicked ==0) { yCoordCorrection = -1;}
		if(rowClicked == 5&& columnClicked ==1) { yCoordCorrection = -1;}
		if(rowClicked == 5&& columnClicked ==2) { yCoordCorrection = -1;}
		if(rowClicked == 5&& columnClicked ==3) { yCoordCorrection = -1;}
		if(rowClicked == 5&& columnClicked ==4) { yCoordCorrection = -2;}
		if(rowClicked == 5&& columnClicked ==5) { yCoordCorrection = -2;}
		if(rowClicked == 5&& columnClicked ==6) { yCoordCorrection = -2;}
		if(rowClicked == 5&& columnClicked ==7) { yCoordCorrection = -1;}
		if(rowClicked == 5&& columnClicked ==8) { yCoordCorrection = -1;}
		if(rowClicked == 6&& columnClicked ==6) { yCoordCorrection = 1;}
		if(rowClicked == 7&& columnClicked ==0) { yCoordCorrection = -2;}
		if(rowClicked == 7&& columnClicked ==1) { yCoordCorrection = -2;}
		if(rowClicked == 7&& columnClicked ==2) { yCoordCorrection = -2;}
		if(rowClicked == 7&& columnClicked ==3) { yCoordCorrection = -2;}
		if(rowClicked == 7&& columnClicked ==4) { yCoordCorrection = -3;}
		if(rowClicked == 7&& columnClicked ==5) { yCoordCorrection = -2;}
		if(rowClicked == 7&& columnClicked ==6) { yCoordCorrection = -2;}
		if(rowClicked == 7&& columnClicked ==7) { yCoordCorrection = -2;}
		if(rowClicked == 7&& columnClicked ==8) { yCoordCorrection = -2;}
		if(rowClicked == 8&& columnClicked ==0) { yCoordCorrection = -2;}
		if(rowClicked == 8&& columnClicked ==1) { yCoordCorrection = -2;}
		if(rowClicked == 8&& columnClicked ==2) { yCoordCorrection = -2;}
		if(rowClicked == 8&& columnClicked ==3) { yCoordCorrection = -2;}
		if(rowClicked == 8&& columnClicked ==4) { yCoordCorrection = -2;}
		if(rowClicked == 8&& columnClicked ==5) { yCoordCorrection = -3;}
		if(rowClicked == 8&& columnClicked ==6) { yCoordCorrection = -3;}
		if(rowClicked == 8&& columnClicked ==7) { yCoordCorrection = -2;}
		if(rowClicked == 8&& columnClicked ==8) { yCoordCorrection = -3;}
		return yCoordCorrection;
	}

	/* Method: getHCoordCorrection
	 * ----------------------------
	 * This method does a pixel correction for the height of the rectangle highlighting the cases when user clicks on them.
	 * The correction was fine-tuned manually for each of the cells. It returns a double that is used when creating the highlight rectangle on the grid.
	 * It takes as parameters the row of the grid that is clicked (int type) and the column on the grid that is clicked (int type).
	 */
	private double getHCorrection(int rowClicked, int columnClicked){
		double hCorrection = 0;
		if(rowClicked == 0&& columnClicked ==0) { hCorrection = 1;}
		if(rowClicked == 0&& columnClicked ==3) { hCorrection = 1;}
		if(rowClicked == 0&& columnClicked ==5) { hCorrection = 1;}
		if(rowClicked == 1&& columnClicked ==1) { hCorrection = 1;}
		if(rowClicked == 1&& columnClicked ==4) { hCorrection = 1;}
		if(rowClicked == 1&& columnClicked ==6) { hCorrection = 1;}
		if(rowClicked == 1&& columnClicked ==7) { hCorrection = 1;}
		if(rowClicked == 1&& columnClicked ==8) { hCorrection = 1;}
		if(rowClicked == 2&& columnClicked ==5) { hCorrection = 1;}
		if(rowClicked == 3&& columnClicked ==0) { hCorrection = 3;}
		if(rowClicked == 3&& columnClicked ==1) { hCorrection = 3;}
		if(rowClicked == 3&& columnClicked ==2) { hCorrection = 3;}
		if(rowClicked == 3&& columnClicked ==3) { hCorrection = 3;}
		if(rowClicked == 3&& columnClicked ==4) { hCorrection = 3;}
		if(rowClicked == 3&& columnClicked ==5) { hCorrection = 2;}
		if(rowClicked == 3&& columnClicked ==6) { hCorrection = 3;}
		if(rowClicked == 3&& columnClicked ==7) { hCorrection = 3;}
		if(rowClicked == 3&& columnClicked ==8) { hCorrection = 3;}
		if(rowClicked == 4&& columnClicked ==3) { hCorrection = 1;}
		if(rowClicked == 4&& columnClicked ==4) { hCorrection = 1;}
		if(rowClicked == 4&& columnClicked ==5) { hCorrection = 1;}
		if(rowClicked == 4&& columnClicked ==7) { hCorrection = 1;}
		if(rowClicked == 5&& columnClicked ==0) { hCorrection = 2;}
		if(rowClicked == 5&& columnClicked ==1) { hCorrection = 2;}
		if(rowClicked == 5&& columnClicked ==2) { hCorrection = 2.5;}
		if(rowClicked == 5&& columnClicked ==3) { hCorrection = 2;}
		if(rowClicked == 5&& columnClicked ==4) { hCorrection = 2;}
		if(rowClicked == 5&& columnClicked ==5) { hCorrection = 2;}
		if(rowClicked == 5&& columnClicked ==6) { hCorrection = 2;}
		if(rowClicked == 5&& columnClicked ==7) { hCorrection = 1;}
		if(rowClicked == 5&& columnClicked ==8) { hCorrection = 1;}
		if(rowClicked == 6&& columnClicked ==0) { hCorrection = -1;}
		if(rowClicked == 6&& columnClicked ==1) { hCorrection = -1;}
		if(rowClicked == 6&& columnClicked ==4) { hCorrection = -1;}
		if(rowClicked == 6&& columnClicked ==5) { hCorrection = -1;}
		if(rowClicked == 6&& columnClicked ==6) { hCorrection = -2;}
		if(rowClicked == 6&& columnClicked ==7) { hCorrection = -1;}
		if(rowClicked == 7&& columnClicked ==0) { hCorrection = 2;}
		if(rowClicked == 7&& columnClicked ==1) { hCorrection = 2;}
		if(rowClicked == 7&& columnClicked ==2) { hCorrection = 2;}
		if(rowClicked == 7&& columnClicked ==3) { hCorrection = 2;}
		if(rowClicked == 7&& columnClicked ==4) { hCorrection = 2;}
		if(rowClicked == 7&& columnClicked ==5) { hCorrection = 2;}
		if(rowClicked == 8&& columnClicked ==0) { hCorrection = 2;}
		if(rowClicked == 8&& columnClicked ==1) { hCorrection = 2;}
		if(rowClicked == 8&& columnClicked ==2) { hCorrection = 1.5;}
		if(rowClicked == 8&& columnClicked ==3) { hCorrection = 2;}
		if(rowClicked == 8&& columnClicked ==4) { hCorrection = 2;}
		if(rowClicked == 8&& columnClicked ==5) { hCorrection = 2;}
		if(rowClicked == 8&& columnClicked ==6) { hCorrection = 3;}
		if(rowClicked == 8&& columnClicked ==7) { hCorrection = 2;}
		if(rowClicked == 8&& columnClicked ==8) { hCorrection = 3;}

		return hCorrection;
	}

	/* Method: getWCoordCorrection
	 * ----------------------------
	 * This method does a pixel correction for the width of the rectangle highlighting the cases when user clicks on them.
	 * The correction was fine-tuned manually for each of the cells. It returns a double that is used when creating the highlight rectangle on the grid.
	 * It takes as parameters the row of the grid that is clicked (int type) and the column on the grid that is clicked (int type).
	 */
	private double getWCorrection(int rowClicked, int columnClicked){
		double wCorrection = 0;
		if(rowClicked == 0&& columnClicked ==4) { wCorrection = 2;}
		if(rowClicked == 0&& columnClicked ==5) { wCorrection = 1;}
		if(rowClicked == 1&& columnClicked ==5) { wCorrection = 1;}
		if(rowClicked == 1&& columnClicked ==7) { wCorrection = 1;}
		if(rowClicked == 2&& columnClicked ==4) { wCorrection = 1;}
		if(rowClicked == 2&& columnClicked ==3) { wCorrection = 1;}
		if(rowClicked == 2&& columnClicked ==5) { wCorrection = 1;}
		if(rowClicked == 3&& columnClicked ==0) { wCorrection = -1;}
		if(rowClicked == 3&& columnClicked ==1) { wCorrection = 1;}
		if(rowClicked == 3&& columnClicked ==2) { wCorrection = +1;}
		if(rowClicked == 3&& columnClicked ==4) { wCorrection = 1.5;}
		if(rowClicked == 3&& columnClicked ==5) { wCorrection = 1;}
		if(rowClicked == 4&& columnClicked ==2) { wCorrection = 1;}
		if(rowClicked == 4&& columnClicked ==4) { wCorrection = 1;}
		if(rowClicked == 4&& columnClicked ==5) { wCorrection = 1;}
		if(rowClicked == 4&& columnClicked ==7) { wCorrection = 1;}
		if(rowClicked == 5&& columnClicked ==0) { wCorrection = -1;}
		if(rowClicked == 5&& columnClicked ==2) { wCorrection = 1;}
		if(rowClicked == 5&& columnClicked ==4) { wCorrection = 1;}
		if(rowClicked == 5&& columnClicked ==5) { wCorrection = 1;}
		if(rowClicked == 5&& columnClicked ==7) { wCorrection = 1;}
		if(rowClicked == 5&& columnClicked ==8) { wCorrection = -2;}
		if(rowClicked == 6&& columnClicked ==0) { wCorrection = -1;}
		if(rowClicked == 6&& columnClicked ==1) { wCorrection = 2;}
		if(rowClicked == 6&& columnClicked ==4) { wCorrection = 1;}
		if(rowClicked == 6&& columnClicked ==5) { wCorrection = 1;}
		if(rowClicked == 6&& columnClicked ==6) { wCorrection = -2;}
		if(rowClicked == 7&& columnClicked ==1) { wCorrection = 2;}
		if(rowClicked == 7&& columnClicked ==3) { wCorrection = 1;}
		if(rowClicked == 7&& columnClicked ==4) { wCorrection = 1;}
		if(rowClicked == 7&& columnClicked ==5) { wCorrection = 1;}
		if(rowClicked == 8&& columnClicked ==2) { wCorrection = 1;}
		if(rowClicked == 8&& columnClicked ==3) { wCorrection = 1;}
		if(rowClicked == 8&& columnClicked ==4) { wCorrection = 1;}
		if(rowClicked == 8&& columnClicked ==6) { wCorrection = -2;}
		if(rowClicked == 8&& columnClicked ==7) { wCorrection = 1;}
		if(rowClicked == 8&& columnClicked ==8) { wCorrection = 1;}
		return wCorrection;
	}

	/* Method: loadPlayerControls
	------------------------------
	 * This method loads the number pad, the trashcan allowing to delete numbers entered in the grid and the star
	 * displaying how  many guesses the player has left.
	 */

	private void loadPlayerControls(){
		GImage playerNumbers= new GImage(PAD);
		playerNumbers.setSize(getWidth()*PLAYERNUMBERS_WIDTH, getHeight()*PLAYERNUMBERS_HEIGHT);
		add(playerNumbers, gridImage.getX()+ gridImage.getWidth(), gridImage.getY()+ GRID_IMAGEY*gridImage.getHeight());
		trashCan= new GImage(GOLDTRASHCAN);
		trashCan.setSize(getWidth()*TRASHCAN_WIDTH, getHeight()*TRASHCAN_HEIGHT);
		add(trashCan, playerNumbers.getX() + playerNumbers.getWidth()*PLAYERNUMBERSX , playerNumbers.getY() + playerNumbers.getHeight());
		lifeLeft= new GImage(GOLDSTAR);
		lifeLeft.setSize(getWidth()*LIFELEFT_WIDTH, getHeight()*LIFELEFT_HEIGHT);
		if (counterLifes > 0) { add(lifeLeft, trashCan.getX(), trashCan.getY() + trashCan.getWidth());}
		counterText= new GLabel(Integer.toString(counterLifes));
		counterText.setFont(FONT_COUNTER);
		counterText.setColor(CUSTOM_BROWN);
		add(counterText, trashCan.getX()+ lifeLeft.getWidth()*COUNTERTEXT_X, lifeLeft.getY() + counterText.getAscent()*COUNTERTEXT_Y);
	}

	/* Method: initCoordLargeGridLimits
    -----------------------------
	 * This method initializes the coordinates of the limits of the grid and the cells in it.
	 *
	 */
	private void initCoordLargeGridLimits() {
		// First row of large grid
		xCoord[0][0] = 38;
		yCoord[0][0] = 87;
		xCoord[0][1] = 73;
		yCoord[0][1] = 87;
		xCoord[0][2] = 111;
		yCoord[0][2] = 87;
		xCoord[0][3] = 154;
		yCoord[0][3] = 87;
		xCoord[0][4] = 189;
		yCoord[0][4] = 87;
		xCoord[0][5] = 226;
		yCoord[0][5] = 87;
		xCoord[0][6] = 270;
		yCoord[0][6] = 87;
		xCoord[0][7] = 304;
		yCoord[0][7] = 87;
		xCoord[0][8] = 340;
		yCoord[0][8] = 87;
		xCoord[0][9] = 376;
		yCoord[0][9] = 87;

		// Second row
		xCoord[1][0] = 38;
		yCoord[1][0] = 121;
		xCoord[1][1] = 73;
		yCoord[1][1] = 121;
		xCoord[1][2] = 111;
		yCoord[1][2] = 121;
		xCoord[1][3] = 154;
		yCoord[1][3] = 121;
		xCoord[1][4] = 189;
		yCoord[1][4] = 121;
		xCoord[1][5] = 226;
		yCoord[1][5] = 121;
		xCoord[1][6] = 270;
		yCoord[1][6] = 121;
		xCoord[1][7] = 304;
		yCoord[1][7] = 121;
		xCoord[1][8] = 340;
		yCoord[1][8] = 121;
		xCoord[1][9] = 376;
		yCoord[1][9] = 121;

		// Third row
		xCoord[2][0] = 38;
		yCoord[2][0] = 152;
		xCoord[2][1] = 73;
		yCoord[2][1] = 152;
		xCoord[2][2] = 111;
		yCoord[2][2] = 152;
		xCoord[2][3] = 154;
		yCoord[2][3] = 152;
		xCoord[2][4] = 189;
		yCoord[2][4] = 152;
		xCoord[2][5] = 226;
		yCoord[2][5] = 152;
		xCoord[2][6] = 270;
		yCoord[2][6] = 152;
		xCoord[2][7] = 304;
		yCoord[2][7] = 152;
		xCoord[2][8] = 340;
		yCoord[2][8] = 152;
		xCoord[2][9] = 376;
		yCoord[2][9] = 152;

		// Fourth row
		xCoord[3][0] = 38;
		yCoord[3][0] = 192;
		xCoord[3][1] = 73;
		yCoord[3][1] = 192;
		xCoord[3][2] = 111;
		yCoord[3][2] = 192;
		xCoord[3][3] = 154;
		yCoord[3][3] = 192;
		xCoord[3][4] = 189;
		yCoord[3][4] = 192;
		xCoord[3][5] = 226;
		yCoord[3][5] = 192;
		xCoord[3][6] = 270;
		yCoord[3][6] = 192;
		xCoord[3][7] = 304;
		yCoord[3][7] = 192;
		xCoord[3][8] = 340;
		yCoord[3][8] = 192;
		xCoord[3][9] = 376;
		yCoord[3][9] = 192;

		// Fifth row
		xCoord[4][0] = 38;
		yCoord[4][0] = 225;
		xCoord[4][1] = 73;
		yCoord[4][1] = 225;
		xCoord[4][2] = 111;
		yCoord[4][2] = 225;
		xCoord[4][3] = 154;
		yCoord[4][3] = 225;
		xCoord[4][4] = 189;
		yCoord[4][4] = 225;
		xCoord[4][5] = 226;
		yCoord[4][5] = 225;
		xCoord[4][6] = 270;
		yCoord[4][6] = 225;
		xCoord[4][7] = 304;
		yCoord[4][7] = 225;
		xCoord[4][8] = 340;
		yCoord[4][8] = 225;
		xCoord[4][9] = 376;
		yCoord[4][9] = 225;

		// Sixth row
		xCoord[5][0] = 38;
		yCoord[5][0] = 259;
		xCoord[5][1] = 73;
		yCoord[5][1] = 259;
		xCoord[5][2] = 111;
		yCoord[5][2] = 259;
		xCoord[5][3] = 154;
		yCoord[5][3] = 259;
		xCoord[5][4] = 189;
		yCoord[5][4] = 259;
		xCoord[5][5] = 226;
		yCoord[5][5] = 259;
		xCoord[5][6] = 270;
		yCoord[5][6] = 259;
		xCoord[5][7] = 304;
		yCoord[5][7] = 259;
		xCoord[5][8] = 340;
		yCoord[5][8] = 259;
		xCoord[5][9] = 376;
		yCoord[5][9] = 259;

		// Seventh row
		xCoord[6][0] = 38;
		yCoord[6][0] = 295;
		xCoord[6][1] = 73;
		yCoord[6][1] = 295;
		xCoord[6][2] = 111;
		yCoord[6][2] = 295;
		xCoord[6][3] = 154;
		yCoord[6][3] = 295;
		xCoord[6][4] = 189;
		yCoord[6][4] = 295;
		xCoord[6][5] = 226;
		yCoord[6][5] = 295;
		xCoord[6][6] = 270;
		yCoord[6][6] = 295;
		xCoord[6][7] = 304;
		yCoord[6][7] = 295;
		xCoord[6][8] = 340;
		yCoord[6][8] = 295;
		xCoord[6][9] = 376;
		yCoord[6][9] = 295;

		// Eighth row
		xCoord[7][0] = 38;
		yCoord[7][0] = 328;
		xCoord[7][1] = 73;
		yCoord[7][1] = 328;
		xCoord[7][2] = 111;
		yCoord[7][2] = 328;
		xCoord[7][3] = 154;
		yCoord[7][3] = 328;
		xCoord[7][4] = 189;
		yCoord[7][4] = 328;
		xCoord[7][5] = 226;
		yCoord[7][5] = 328;
		xCoord[7][6] = 270;
		yCoord[7][6] = 328;
		xCoord[7][7] = 304;
		yCoord[7][7] = 328;
		xCoord[7][8] = 340;
		yCoord[7][8] = 328;
		xCoord[7][9] = 376;
		yCoord[7][9] = 328;

		// Ninth row
		xCoord[8][0] = 38;
		yCoord[8][0] = 361;
		xCoord[8][1] = 73;
		yCoord[8][1] = 361;
		xCoord[8][2] = 111;
		yCoord[8][2] = 361;
		xCoord[8][3] = 154;
		yCoord[8][3] = 361;
		xCoord[8][4] = 189;
		yCoord[8][4] = 361;
		xCoord[8][5] = 226;
		yCoord[8][5] = 361;
		xCoord[8][6] = 270;
		yCoord[8][6] = 361;
		xCoord[8][7] = 304;
		yCoord[8][7] = 361;
		xCoord[8][8] = 340;
		yCoord[8][8] = 361;
		xCoord[8][9] = 376;
		yCoord[8][9] = 361;

		// 10th row (only used for coordinate calculation)
		xCoord[9][0] = 38;
		yCoord[9][0] = 396;
		xCoord[9][1] = 73;
		yCoord[9][1] = 396;
		xCoord[9][2] = 111;
		yCoord[9][2] = 396;
		xCoord[9][3] = 154;
		yCoord[9][3] = 396;
		xCoord[9][4] = 189;
		yCoord[9][4] = 396;
		xCoord[9][5] = 226;
		yCoord[9][5] = 396;
		xCoord[9][6] = 270;
		yCoord[9][6] = 396;
		xCoord[9][7] = 304;
		yCoord[9][7] = 396;
		xCoord[9][8] = 340;
		yCoord[9][8] = 396;
		xCoord[9][9] = 376;
		yCoord[9][9] = 396;
	}
	/* Method: initCoordPadLimits
    -----------------------------
	 * This method initializes the coordinates of the limits of the pad and the cells in it.
	 *
	 */
	private void initCoordPadLimits() {

		// First row of small grid
		xCoordSmallGrid[0][0] = 413;
		yCoordSmallGrid[0][0] = 188;
		xCoordSmallGrid[0][1] = 451;
		yCoordSmallGrid[0][1] = 188;
		xCoordSmallGrid[0][2] = 487;
		yCoordSmallGrid[0][2] = 188;
		xCoordSmallGrid[0][3] = 526;
		yCoordSmallGrid[0][3] = 188;

		// Second row of small grid
		xCoordSmallGrid[1][0] = 413;
		yCoordSmallGrid[1][0] = 217;
		xCoordSmallGrid[1][1] = 451;
		yCoordSmallGrid[1][1] = 217;
		xCoordSmallGrid[1][2] = 487;
		yCoordSmallGrid[1][2] = 217;
		xCoordSmallGrid[1][3] = 526;
		yCoordSmallGrid[1][3] = 217;

		// Third row of small grid
		xCoordSmallGrid[2][0] = 413;
		yCoordSmallGrid[2][0] = 248;
		xCoordSmallGrid[2][1] = 451;
		yCoordSmallGrid[2][1] = 248;
		xCoordSmallGrid[2][2] = 487;
		yCoordSmallGrid[2][2] = 248;
		xCoordSmallGrid[2][3] = 526;
		yCoordSmallGrid[2][3] = 248;

		// 4th row of small grid
		xCoordSmallGrid[3][0] = 413;
		yCoordSmallGrid[3][0] = 280;
		xCoordSmallGrid[3][1] = 451;
		yCoordSmallGrid[3][1] = 280;
		xCoordSmallGrid[3][2] = 487;
		yCoordSmallGrid[3][2] = 280;
		xCoordSmallGrid[3][3] = 526;
		yCoordSmallGrid[3][3] = 280;

	}
	/* Method: loadGrid
	------------------------------
	 * This method loads a starting grid based on the specified level passed as parameter (int) to this method.
	 * One of 3 grid files is picked randomly, read, numbers stored in appropriated data structure, and the numbers drawn on the screen.
	 * This method also loads the solution associated to this grid (reads appropriate file) and stores it in a data structure.
	 */

	private void loadGrid(int difficultyLevel) {

		String[][] easyInitialString = new String[NROWSGRID][NCOLSGRID];
		String[][] mediumInitialString = new String[NROWSGRID][NCOLSGRID];
		String[][] hardInitialString = new String[NROWSGRID][NCOLSGRID];
		initCoordLargeGridLimits();
		initCoordPadLimits();
		int filetoChoose = -1;

		if (difficultyLevel == 1) {
			filetoChoose = rg.nextInt(1,3);
			String filename ="";
			if (filetoChoose == 1) {
				filename = EASY_GRID_ONE;
			} else if (filetoChoose == 2) {
				filename = EASY_GRID_TWO;
			} else {
				filename = EASY_GRID_THREE;
			}

			try {       
				BufferedReader rd = new BufferedReader(new FileReader(filename));
				String line = rd.readLine();
				int r = 0;
				int c = 0;
				while(line != null) {
					easyInitialString[r][c] = line;
					if (Integer.valueOf(line)!=0) {
						gameStatus[r][c] = Integer.valueOf(line);
						originalGrid[r][c] = gameStatus[r][c];
					}
					c++;
					if (c == NCOLSGRID) { 
						r++;
						c = 0;
					}
					if ( r == NROWSGRID) { break; }
					line = rd.readLine();
				}
				rd.close();
			} catch (IOException e) {
				System.out.println("Am error occurred reading the file: " + e);
			}

			for (int r = 0; r< NROWSGRID; r++) {
				for (int c = 0; c < NCOLSGRID; c++) {
					if (!easyInitialString[r][c].equals("0") ) {
						GImage numberImage = new GImage(easyInitialString[r][c]+".png");
						numberImage.setSize(gridImage.getWidth()/NUMBERS_RESIZE, gridImage.getHeight()/NUMBERS_RESIZE);
						add(numberImage, xCoord[r][c], yCoord[r][c]);
					}
				}
			}

		}

		if (difficultyLevel == 2) {
			filetoChoose = rg.nextInt(1,3);
			String filename ="";
			if (filetoChoose == 1) {
				filename = MEDIUM_GRID_ONE;
			} else if (filetoChoose == 2) {
				filename = MEDIUM_GRID_TWO;
			} else {
				filename = MEDIUM_GRID_THREE;
			}

			try {       
				BufferedReader rd = new BufferedReader(new FileReader(filename));
				String line = rd.readLine();
				int r = 0;
				int c = 0;
				while(line != null) {
					mediumInitialString[r][c] = line;
					if (Integer.valueOf(line)!=0) {
						gameStatus[r][c] = Integer.valueOf(line);
						originalGrid[r][c] = gameStatus[r][c];
					}
					c++;
					if (c == NCOLSGRID) { 
						r++;
						c = 0;
					}
					if ( r == NROWSGRID) { break; }
					line = rd.readLine();
				}
				rd.close();
			} catch (IOException e) {
				System.out.println("Am error occurred reading the file: " + e);
			}

			for (int r = 0; r< NROWSGRID; r++) {
				for (int c = 0; c < NCOLSGRID; c++) {
					if (!mediumInitialString[r][c].equals("0") ) {
						GImage numberImage = new GImage(mediumInitialString[r][c]+".png");
						numberImage.setSize(gridImage.getWidth()/NUMBERS_RESIZE, gridImage.getHeight()/NUMBERS_RESIZE);
						add(numberImage, xCoord[r][c], yCoord[r][c]);
					}
				}
			}

		}

		if (difficultyLevel == 3) {
			filetoChoose = rg.nextInt(1,3);
			String filename ="";
			if (filetoChoose == 1) {
				filename = HARD_GRID_ONE;
			} else if (filetoChoose == 2) {
				filename = HARD_GRID_TWO;
			} else {
				filename = HARD_GRID_THREE;
			}

			try {       
				BufferedReader rd = new BufferedReader(new FileReader(filename));
				String line = rd.readLine();
				int r = 0;
				int c = 0;
				while(line != null) {
					hardInitialString[r][c] = line;
					if (Integer.valueOf(line)!=0) {
						gameStatus[r][c] = Integer.valueOf(line);
						originalGrid[r][c] = gameStatus[r][c];
					}
					c++;
					if (c == 9) { 
						r++;
						c = 0;
					}
					if ( r == 9) { break; }
					line = rd.readLine();
				}
				rd.close();
			} catch (IOException e) {
				System.out.println("Am error occurred reading the file: " + e);
			}

			for (int r = 0; r< NROWSGRID; r++) {
				for (int c = 0; c < NCOLSGRID; c++) {
					if (!hardInitialString[r][c].equals("0") ) {
						GImage numberImage = new GImage(hardInitialString[r][c]+".png");
						numberImage.setSize(gridImage.getWidth()/NUMBERS_RESIZE, gridImage.getHeight()/NUMBERS_RESIZE);
						add(numberImage, xCoord[r][c], yCoord[r][c]);
					}
				}
			}
		}

		// load solution grid
		if (difficultyLevel == 1) {
			String filename ="";
			if (filetoChoose == 1) {
				filename = EASY_GRID_ONE_SLN;
			} else if (filetoChoose == 2) {
				filename = EASY_GRID_TWO_SLN;
			} else {
				filename =EASY_GRID_THREE_SLN;
			}
			try {       
				BufferedReader rd = new BufferedReader(new FileReader(filename));
				String line = rd.readLine();
				int r = 0;
				int c = 0;
				while(line != null) {
					gameSolution[r][c] = Integer.valueOf(line);
					c++;
					if (c == NCOLSGRID) { 
						r++;
						c = 0;
					}
					if ( r == NROWSGRID) { break; }
					line = rd.readLine();
				}
				rd.close();
			} catch (IOException e) {
				System.out.println("Am error occurred reading the file: " + e);
			}   

		}

		if (difficultyLevel == 2) {
			String filename ="";
			if (filetoChoose == 1) {
				filename = MEDIUM_GRID_ONE_SLN;
			} else if (filetoChoose == 2) {
				filename = MEDIUM_GRID_TWO_SLN;
			} else {
				filename =MEDIUM_GRID_THREE_SLN;
			}
			try {       
				BufferedReader rd = new BufferedReader(new FileReader(filename));
				String line = rd.readLine();
				int r = 0;
				int c = 0;
				while(line != null) {
					gameSolution[r][c] = Integer.valueOf(line);
					c++;
					if (c == NCOLSGRID) { 
						r++;
						c = 0;
					}
					if ( r == NROWSGRID) { break; }
					line = rd.readLine();
				}
				rd.close();
			} catch (IOException e) {
				System.out.println("Am error occurred reading the file: " + e);
			}   

		}

		if (difficultyLevel == 3) {
			String filename ="";
			if (filetoChoose == 1) {
				filename = HARD_GRID_ONE_SLN;
			} else if (filetoChoose == 2) {
				filename = HARD_GRID_TWO_SLN;
			} else {
				filename =HARD_GRID_THREE_SLN;
			}

			try {       
				BufferedReader rd = new BufferedReader(new FileReader(filename));
				String line = rd.readLine();
				int r = 0;
				int c = 0;
				while(line != null) {
					gameSolution[r][c] = Integer.valueOf(line);
					c++;
					if (c == NCOLSGRID) { 
						r++;
						c = 0;
					}
					if ( r == NROWSGRID) { break; }
					line = rd.readLine();
				}
				rd.close();
			} catch (IOException e) {
				System.out.println("Am error occurred reading the file: " + e);
			}   

		}
		filetoChoose = -1;

	}



}





