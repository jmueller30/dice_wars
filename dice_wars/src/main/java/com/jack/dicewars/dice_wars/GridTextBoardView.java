package com.jack.dicewars.dice_wars;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import com.jack.dicewars.dice_wars.game.board.AbstractBoard;
import com.jack.dicewars.dice_wars.game.board.GridTextBoard;
import com.jack.dicewars.dice_wars.game.board.TerritoryBorder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * This implementation of AbstractBoardView is to be used in Text Mode. Territories are represented by a one
 * character color code and a 1 digit dice value. The Territories are arranged in a grid whose size can be edited in
 * bespoke options.
 */
public class GridTextBoardView extends AbstractBoardView {

    /**
     * The amount of rows this grid has. Corresponds to the number of LinearLayouts in the Board's viewport in the
     * activity.
     */
    private int rows;
    /**
     * The amount of columns this grid has. Equals the number of spaces in each LinearLayout plus one.
     */
    private int cols;

    /**
     * Creates {@link com.jack.dicewars.dice_wars.GridTextTerritoryView} and maps them to Android Button to fill the
     * {@link #territoryNativeViewMap}.
     *
     * @param board Model data of the board and its grid indexed Territories.
     * @param context The MainGameActivity in which this board is being used.
     */
    public GridTextBoardView(AbstractBoard board, Context context) {
        this.board = board;
        final GridTextBoard gridTextBoard = (GridTextBoard) board;

        this.context = context;
        rows = gridTextBoard.getRows();
        cols = gridTextBoard.getCols();

        // A row major ordered list of TerritoryBorder model objects.
        final List<TerritoryBorder> grid = gridTextBoard.getBoard();

        territoryNativeViewMap = new HashMap<>();
        modelTerritoryMap = new HashMap<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // A GridTextTerritoryView can preserve the grid coordinate model information.
                final AbstractTerritoryView territoryView = new GridTextTerritoryView(i, j);
                // TODO move as much of this junk to the TerritoryView that makes sense
                Button gridTextButton = (Button) territoryView.defaultView(context);

                final TerritoryBorder territory = grid.get(gridTextBoard.coordinatesToIndex(i, j, cols));
                gridTextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Request selection. Let the model handle potential selection.
                        gridTextBoard.requestSelection(territory);
                        // Handle changes in the Views
                        updateViews();
                        // Update the Primary Action button
                        updatePrimaryAction();
                    }
                });

                // Guarantee that the TerritoryView and Button coordinates are linked
                territoryNativeViewMap.put(territoryView, gridTextButton);
                modelTerritoryMap.put(territory, territoryView);

                // First update acts as an initialization
                updateView(territory);
            }
        }
    }

    @Override
    public void updateView(TerritoryBorder modelKey) {
        Button nativeView = (Button) territoryNativeViewMap.get(modelTerritoryMap.get(modelKey));
        final TerritoryColor territoryColor = modelKey.getInternal().getColor();
        // TODO figure out why this depends on exactly two characters being here
        nativeView.setText(modelKey.getInternal().getColor().getCode() + modelKey.getInternal().getValue());
        if (modelKey.isSelected()) {
            nativeView.setTypeface(null, Typeface.BOLD);
        } else if (modelKey.isSelectable()) {
            nativeView.setText(Html.fromHtml("<u>" + nativeView.getText() + "</u>"));
            nativeView.setTypeface(null, Typeface.NORMAL);
        } else {
            nativeView.setTypeface(null, Typeface.NORMAL);
        }
        nativeView.setBackground(context.getResources().getDrawable(territoryColor.getDrawableId()));
    }

    /**
     * TerritoryViews are represented by Buttons and other defined Views include a one column GridLayout as the
     * ViewPort implementation, LinearLayouts for the rows of the grid, and Android Space objects to
     * simulate columns.
     *
     * @param viewPort A view group that can be modified and populated with TerritoryViews and other Views.
     * @return A parent View that can display a game board.
     */
    public View apply(ViewGroup viewPort) {

        // set up rows and spaces to put the buttons between
        ViewGroup container = createSkeleton(viewPort);
        ((GridLayout) container).setRowCount(rows);
        // Only one LinearLayout per row, but the same number of rows as the model. Columns are defined by the number
        // of Space views.
        ((GridLayout) container).setColumnCount(1);

        // loop through all territories and places the corresponding View in the skeleton
        for (Map.Entry<AbstractTerritoryView, View> territoryMapping : territoryNativeViewMap.entrySet()) {
            // get a reference to the model's row and column
            final GridTextTerritoryView territory = (GridTextTerritoryView) territoryMapping.getKey();
            final int modelRow = territory.getRow();
            final int modelCol = territory.getCol();
            // The correct LinearLayout of the viewport for this territory
            final ViewGroup row = (ViewGroup) container.getChildAt(modelRow);
            // Place the button at the correct index, accounting for spacers and already placed buttons.
            // Keeps track of all elements
            int adjustedCol = 0;
            // Keeps track of spaces, break after the nth spacer where n is the model column of the territory.
            int spaces = 0;
            while (spaces <= modelCol) {
                if (row.getChildAt(adjustedCol).getId() == R.id.gridTextTerritorySpace) {
                    spaces++;
                }
                adjustedCol++;
            }

            // Add to row with the adjusted column
            row.addView(territoryMapping.getValue(), adjustedCol);

        }
        return container;
    }

    /**
     *
     * @param container The parent node which to populate with Views other than the Territories.
     * @return A structured board missing its Territories.
     */
    private ViewGroup createSkeleton(ViewGroup container) {

        for (int i = 0; i < rows; i++) {
            final ViewGroup row = createRow();
            // Add 1 more space than columns because there should be a space on the beginning, end, and between all
            // buttons
            for (int j = 0; j < cols + 1; j++) {
                row.addView(createSpace());
            }
            container.addView(row);
        }
        return container;
    }

    /**
     *
     * @return an empty LinearLayout that has properties to become a GridTextBoardView row.
     */
    private ViewGroup createRow() {
        final LinearLayout row = new LinearLayout(context);
        //add properties
        final int width = LinearLayout.LayoutParams.MATCH_PARENT;
        final int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        row.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        row.setOrientation(LinearLayout.HORIZONTAL);

        return row;
    }

    /**
     *
     * @return A space which has properties that help in simulating GridTextBoardView columns.
     */
    private Space createSpace() {
        final Space space = new Space(context);
        //add properties
        space.setId(R.id.gridTextTerritorySpace);
        final int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        final int height = LinearLayout.LayoutParams.MATCH_PARENT;
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.weight = 1;
        space.setLayoutParams(params);

        return space;
    }
}
