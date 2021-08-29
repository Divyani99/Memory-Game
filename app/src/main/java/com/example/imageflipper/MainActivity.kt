package com.example.imageflipper

import android.animation.ArgbEvaluator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.snackbar.Snackbar
import models.BoardSize
import models.MemoryCard
import models.MemoryGame
import utils.DEFAULT_ICONS

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }


    private lateinit var rvBoard: RecyclerView
    private lateinit var twNumMoves: TextView
    private lateinit var twNumPairs: TextView
    private lateinit var clRoot: ConstraintLayout

    private lateinit var memoryGame: MemoryGame
    private var boardSize: BoardSize = BoardSize.EASY
    private lateinit var adapter: MemoryBoardAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvBoard = findViewById(R.id.rvBoard)
        clRoot = findViewById(R.id.clRoot)
        twNumMoves = findViewById(R.id.tvNumMoves)
        twNumPairs = findViewById(R.id.tvNumPairs)


      setupBoard()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_refresh -> {
                if (memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()) {
                    showAlertDialog("Quit Your Current Game?", null, View.OnClickListener {
                        setupBoard()
                    })
                } else {
                    setupBoard()
                }
            }
            R.id.mi_new_size -> {
                showNewSizeDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        showAlertDialog("Choose New Size",null,View.OnClickListener {
            //Set a new value for the board size
            
        })
    }

    private fun showAlertDialog(title:String,view: View?,positiveButtonClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
                .setTitle(title)
                .setView(view)
                .setNegativeButton("Cancel",null)
                .setPositiveButton("OK"){_, _ ->
                    positiveButtonClickListener.onClick(null)
                }.show()
    }

    private fun setupBoard() {
        when (boardSize){
            BoardSize.EASY -> {
                twNumMoves.text = "Easy:4 x 2"
                twNumPairs.text = "Pairs: 0/4"
            }
            BoardSize.MEDIUM ->{
                twNumMoves.text = "Medium:6 x 3"
                twNumPairs.text = "Pairs: 0/9"
            }
            BoardSize.HARD -> {
                twNumMoves.text = "Hard:6 x 4"
                twNumPairs.text = "Pairs: 0/12"
            }
        }
        twNumPairs.setTextColor(ContextCompat.getColor(this,R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize)
        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object : MemoryBoardAdapter.CardClickListener {

            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }

        })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }


    private fun updateGameWithFlip(position: Int) {
        // Error Checking
        if (memoryGame.haveWonGame()){
            //alert the user of invalid move
                Snackbar.make(clRoot, " You already Won!",Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFaceUp(position)) {
        //alert the user of invalid move
            Snackbar.make(clRoot, " Invalid Move!",Snackbar.LENGTH_SHORT).show()
            return
        }
    //actually flip over the card
        if (memoryGame.flipCard(position)){
            Log.i(TAG,"Found a match! Num pairs found: ${memoryGame.numPairFound}")
            val color = ArgbEvaluator().evaluate(
               memoryGame.numPairFound.toFloat() / boardSize.getNumPairs(),
               ContextCompat.getColor(this,R.color.color_progress_none),
               ContextCompat.getColor(this,R.color.color_progress_full)

            ) as Int
            twNumPairs.setTextColor(color)
            twNumPairs.text = "Pairs:${memoryGame.numPairFound} / ${boardSize.getNumPairs()}"
            if (memoryGame.haveWonGame()) {
                Snackbar.make(clRoot,"You won! Congratulations",Snackbar.LENGTH_LONG).show()
            }
        }
        twNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
}