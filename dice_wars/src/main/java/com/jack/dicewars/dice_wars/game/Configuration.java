package com.jack.dicewars.dice_wars.game;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import com.jack.dicewars.dice_wars.TerritoryColor;
import com.jack.dicewars.dice_wars.Debug;
import com.jack.dicewars.dice_wars.game.board.AbstractBoard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * This class encapsulates all the different settings and variations a different game of Dice Wars can have.
 */
public class Configuration {

    private static final int MAX_PLAYERS = 6;

    private static final String PLAYERS_KEY = "players";
    private static final String COLORLESS_TERRITORY_KEY = "colorlessTerritory";
    private static final String RANDOM_REINFORCE_KEY = "randomReinforce";
    private static final String BOARD_SIZE_KEY = "boardSize";
    private static final java.lang.String APP_MODE_KEY = "appMode";
    public static final String RESTART = "restart";

    public static final int DEFAULT_MODE = Debug.gridText.f;

    private final Player[] players = new Player[MAX_PLAYERS];
    private boolean colorlessTerritory;
    private boolean randomReinforce;
    private int boardSize;
    private int appMode;

    /**
     * Full constructor for a game Configuration. Forces all of the Configuration properties to be set to their
     * game values. Useful for single player games.
     *
     * @param names A list of the players' screen names.
     * @param statuses A list of the players' statuses defined by {@link Player#STATUS_YOU} STATUS variables.
     * @param territoryColors A list of the players' colors defined by
     *                        {@link com.jack.dicewars.dice_wars.TerritoryColor}.
     * @param cT Whether this configuration will have colorless territories enabled.
     * @param rR Whether this configuration will have user defined or random reinforcements.
     * @param size The size of the board defined by
     *             {@link com.jack.dicewars.dice_wars.game.board.AbstractBoard#BOARD_SIZE_SMALL}.
     */
    public Configuration(String[] names, String[] statuses, TerritoryColor[] territoryColors, boolean cT, boolean rR,
                         int size) {
        for (int i = 0; i < MAX_PLAYERS; i++) {
            getPlayers()[i] = new Player(names[i], statuses[i], territoryColors[i]);
        }

        colorlessTerritory = cT;
        randomReinforce = rR;
        boardSize = size;
        appMode = DEFAULT_MODE;
    }

    /**
     * Defines a Configuration by the extras of the passed intent. Extra keys are defined at {@link #PLAYERS_KEY}.
     *
     * @param bundle The intent with extras fully defining this Configuration. TODO update javadoc
     */
    public Configuration(Bundle bundle) {
        Parcelable[] parceledPlayers = bundle.getParcelableArray(PLAYERS_KEY);
        for (int i = 0; i < MAX_PLAYERS; i++) {
            getPlayers()[i] = ((Player) parceledPlayers[i]);
        }
        colorlessTerritory = bundle.getBoolean(COLORLESS_TERRITORY_KEY, false);
        randomReinforce = bundle.getBoolean(RANDOM_REINFORCE_KEY, false);
        boardSize = bundle.getInt(BOARD_SIZE_KEY, AbstractBoard.BOARD_SIZE_SMALL);
        appMode = bundle.getInt(APP_MODE_KEY, DEFAULT_MODE);
    }

    /**
     * Returns the passed intent with the configurations separated into primitive types and matched with string keys
     * that are defined as this class' members.
     *
     *
     * @param intent The link to the activity that needs configuration settings
     * @return An intent with extras that fully define a Configuration
     */
    public Intent upload(Intent intent) {
        intent.putExtra(PLAYERS_KEY, getPlayers());
        intent.putExtra(COLORLESS_TERRITORY_KEY, colorlessTerritory);
        intent.putExtra(RANDOM_REINFORCE_KEY, randomReinforce);
        intent.putExtra(BOARD_SIZE_KEY, getBoardSize());
        intent.putExtra(APP_MODE_KEY, appMode);
        return intent;
    }

    /**
     *
     * @return Whether the board will start with some colorless territories.
     */
    public boolean isColorlessTerritory() {
        return colorlessTerritory;
    }

    /**
     *
     * @param colorlessTerritory Setting to false will ensure all territories are assigned on game start.
     */
    public void setColorlessTerritory(boolean colorlessTerritory) {
        this.colorlessTerritory = colorlessTerritory;
    }

    /**
     *
     * @return Whether defense phase reinforcements will be randomly allocated
     */
    public boolean isRandomReinforce() {
        return randomReinforce;
    }

    /**
     *
     * @param randomReinforce Setting to true will allow players to choose how to allocate their reinforcements.
     */
    public void setRandomReinforce(boolean randomReinforce) {
        this.randomReinforce = randomReinforce;
    }

    /**
     *
     * @return The maximum number of players one game of DiceWars can handle.
     */
    public static int getMaxPlayers() {
        return MAX_PLAYERS;

    }

    /**
     *
     * @return Returns the app mode that the game is running.
     */
    public int getAppMode() {
        return appMode;
    }

    /**
     *
     * @param appMode sets the app mode for upcoming games.
     */
    public void setAppMode(int appMode) {
        this.appMode = appMode;
    }

    /**
     *
     * @return The board size code
     */
    public int getBoardSize() {
        return boardSize;
    }

    /**
     *
     * @return the array of Players for this game
     */
    public Player[] getPlayers() {
        return players;
    }

    /**
     *
     * @return An array of the players that actually take turns in a Round. This array can change throughout the game
     * as Players are defeated.
     */
    public List<Player> activePlayers() {
        List<Player> activePlayers = new ArrayList<>();
        for (Player p : players) {
            final String status = p.getStatus();
            if (status.equals(Player.STATUS_YOU) || status.equals(Player.STATUS_AI) || status.equals(Player
                    .STATUS_HUMAN)) {
                activePlayers.add(p);
            }
        }
        return activePlayers;
    }

    /**
     * This method will randomize the player order so that the order is different at the start of each game.
     */
    public void randomizePlayerOrder() {
        // TODO implement my own randomization function... if I feel like it
        final List<Player> playersList = Arrays.asList(players);
        Collections.shuffle(playersList);
        for (int i = 0; i < players.length; i++) {
            players[i] = playersList.get(i);
        }
    }
}