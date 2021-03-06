package com.jack.dicewars.dice_wars;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.jack.dicewars.dice_wars.ai.AbstractAi;
import com.jack.dicewars.dice_wars.ai.SimpleAi;
import com.jack.dicewars.dice_wars.game.Configuration;
import com.jack.dicewars.dice_wars.game.Game;
import com.jack.dicewars.dice_wars.game.Results;
import com.jack.dicewars.dice_wars.game.board.filter.Selectable;
import com.jack.dicewars.dice_wars.setup.GameConfigActivity;

/**
 * This activity will handle displaying the progression of a DiceWars game.
 */
public class MainGameActivity extends Activity implements GameController {

    /**
     * The root container for DiceWars game logic.
     */
    private Game game;

    private AbstractBoardView boardView;

    /**
     * Loads the configuration to use for the game.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main_game);

        Bundle configBundle = getIntent().getExtras();

        // Restart bundle will not be null if the intent came from a user initiated restart.
        final Bundle restartBundle = configBundle.getBundle(Configuration.RESTART);
        if (restartBundle != null) {
            configBundle = restartBundle;
        }

        this.game = new Game(new Configuration(configBundle), this);
        game.start();

        // Choose the game mode to run
        if ((game.getAppMode() & Debug.gridText.f) == Debug.gridText.f) {
            boardView = new GridTextBoardView(game.getBoard(), this);
        } else {
            throw new EnumConstantNotPresentException(Debug.class, "App mode does not exist");
        }

        boardView.apply((ViewGroup) findViewById(R.id.boardContainer));
        uiUpdate();

        // Call a phase change to change into the first phase.
        onPhaseChange();
    }

    /**
     * Updates the view based on the state of {@game}. This includes text labels and the Android View objects that
     * are associated with model objects such as Territories.
     */
    private void uiUpdate() {
        boardView.updateViews();
        boardView.updatePrimaryAction();
        updateLabels();
    }

    /**
     * Updates labels on the view to match the state of the model.
     */
    private void updateLabels() {
        ((TextView) findViewById(R.id.activePlayerName)).setText(game.currentPlayerName());
        ((TextView) findViewById(R.id.activePlayerName)).setTextColor(game.currentPlayerColor().getHexColor());
        ((TextView) findViewById(R.id.activePhase)).setText(game.currentPhase().toString());
        ((TextView) findViewById(R.id.phaseEnd)).setText((game.getPrimaryActionId()));
    }

    /**
     * Advances the Game to the next Phase, Turn, or Round.
     *
     * @param view The button clicked to call this method
     */
    public void userPrimaryAction(View view) {
        // Update the Game when the button is clicked
        if (game.myTurn()) {
            game.doPrimaryAction();
            uiUpdate();
        }
    }

    @Override
    public void onPhaseChange() {
        if (!game.myTurn()) {
            // Get a new AI task (each is only valid to execute once)
            AsyncTask<AbstractAi, Selectable, Void> aiTask = generateAiTask();
            // Execute the AI's turn in the background
            aiTask.execute(new AbstractAi[]{new SimpleAi(game)});
        }
    }

    @Override
    public AsyncTask<AbstractAi, Selectable, Void> generateAiTask() {
        return new AsyncTask<AbstractAi, Selectable, Void>() {
            @Override
            protected Void doInBackground(AbstractAi... params) {
                Log.i(Debug.ai.s, "Starting AI Phase");
                final AbstractAi ai = params[0];

                // TODO Abstract this flow into the AI class itself
                while (ai.desiredSelection()) {
                    Selectable selection = ai.makeSelection();
                    publishProgress(selection);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Selectable... selectables) {
                // TODO have a more efficient selection of which views could have been affected.
                uiUpdate();
            }

            @Override
            protected void onPostExecute(Void blank) {
                while (!getString(game.getPrimaryActionId()).equals("End Phase")) {
                    game.doPrimaryAction();
                }
                game.doPrimaryAction();

                uiUpdate();
            }
        };
    }

    @Override
    public void onGameEnd() {
        Intent resultsScreen = new Intent(this, ResultsActivity.class);
        resultsScreen = gatherResults(resultsScreen);
        startActivity(resultsScreen);
        // Don't come back to the game once it's done.
        // TODO pause so the user decides when to leave the game for good.
        finish();
    }

    /**
     * Places all relevant results in the supplied ResultsActivity Intent.
     * @param intent The intent that is going to the results Screen
     * @return The updated intent will relevant results data stored as extras.
     */
    private Intent gatherResults(Intent intent) {
        intent.putParcelableArrayListExtra(Results.CLOSED_PLAYERS, game.getClosedPlayers());
        intent.putExtra(Results.ROUND_NUM, game.getRoundNum());
        intent.putExtra(Results.ORIGINAL_CONFIG, getIntent().getExtras());
        return intent;
    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.hardware_back_in_game).setTitle(R.string.common_sure);

        builder.setMessage(R.string.hardware_back_in_game).setPositiveButton(R.string.common_yes, new
                DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.i(Debug.nav.s, "Go to Game Config");
                goToModeSelect();
            }

        }).setNegativeButton(R.string.common_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.i(Debug.nav.s, "cancel");
            }
        });
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Debug. This method should block the back button. Ends the game, and sends the intent for this game back to the
     * configuration activity.
     */
    private void goToModeSelect() {
        Intent i = new Intent(this, GameConfigActivity.class);
        i.putExtras(GameConfigActivity.defaultExtras());
        finish();
    }

}