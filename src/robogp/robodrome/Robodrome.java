/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robogp.robodrome;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * La classe Robodrome rappresenta un tabellone per il gioco RoboGP. 
 * @author claudia
 */
public class Robodrome {

    private final BoardCell[][] board;
    private final String name;
    private final int rows;
    private final int columns;
    private final java.util.ArrayList<CellLaser> allLasers;
    private int docksCount;

    /**
     * Costruisce un robodromo a partire da un file che lo descrive.
     * @param filename il pathname del file
     */
    public Robodrome(String filename) {
        allLasers = new java.util.ArrayList<>();
        docksCount = 0;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line.trim());
            }
        } catch (IOException ex) {
            Logger.getLogger(Robodrome.class.getName()).log(Level.SEVERE, null, ex);
        }
        String[] all = stringBuilder.toString().split(";");
        name = all[0].trim();
        String[] dim = all[1].trim().split("x");
        columns = Integer.parseInt(dim[0]);
        rows = Integer.parseInt(dim[1]);
        board = new BoardCell[rows][columns];
        int countcell = 0;
        boolean endfile = false;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if (!endfile) {
                    if (countcell + 2 < all.length) {
                        board[r][c] = BoardCell.createBoardCell(all[countcell + 2].trim().split("-"));
                        countcell++;
                        if (countcell + 2 >= all.length) {
                            endfile = true;
                        }
                        java.util.ArrayList<CellLaser> cellLasers = board[r][c].getLasers();
                        for (CellLaser las : cellLasers) {
                            las.setPosition(r, c);
                            allLasers.add(las);
                        }
                    }
                } else {
                    board[r][c] = new FloorCell(new String[0]);
                }
                if (board[r][c] instanceof FloorCell && ((FloorCell)board[r][c]).isDock()) {
                    this.docksCount++;
                }
            }
        }
        for (CellLaser las : allLasers) {
            Direction dir = las.getWall();
            boolean stop = false;
            int r = las.getRow();
            int c = las.getCol();
            switch (dir) {
                case W:
                    while (!stop) {
                        board[r][c].setHorizontalLaser(true);
                        if (board[r][c].hasWall(Direction.E) || (c + 1) == columns || board[r][c + 1].hasWall(Direction.W)) {
                            stop = true;
                        } else {
                            c++;
                        }
                    }
                    break;
                case E:
                    while (!stop) {
                        board[r][c].setHorizontalLaser(true);
                        if (board[r][c].hasWall(Direction.W) || c == 0 || board[r][c - 1].hasWall(Direction.E)) {
                            stop = true;
                        } else {
                            c--;
                        }
                    }
                    break;
                case N:
                    while (!stop) {
                        board[r][c].setVerticalLaser(true);
                        if (board[r][c].hasWall(Direction.S) || (r + 1) == rows || board[r + 1][c].hasWall(Direction.N)) {
                            stop = true;
                        } else {
                            r++;
                        }
                    }
                    break;
                case S:
                    while (!stop) {
                        board[r][c].setVerticalLaser(true);
                        if (board[r][c].hasWall(Direction.N) || r == 0 || board[r - 1][c].hasWall(Direction.S)) {
                            stop = true;
                        } else {
                            r--;
                        }
                    }
                    break;
            }
        }
    }
    
    /**
     * Restituisce la casella nella posizione specificata
     * @param row la riga della casella
     * @param col la colonna della casella
     * @return la casella in posizione (riga, colonna)
     */
    public BoardCell getCell(int row, int col) {
        if (row < rows && col < columns) {
            return board[row][col];
        }
        return null;
    }

    /** 
     * @return il numero di righe nel tabellone
     */
    public int getRowCount() {
        return rows;
    }

    /**
     * 
     * @return il numero di colonne nel tabellone
     */
    public int getColumnCount() {
        return columns;
    }

    /**
     *
     * @return il nome di questo robodromo
     */
    public String getName() {
        return name;
    }

    public int getDocksCount() {
        return this.docksCount;
    }
    
    public Direction getBeltOutput(BoardCell cell) {
        if (cell.getType() == 'B' || cell.getType() == 'E') {
            return ((BeltCell)cell).getOutputDirection();
        }
        return null;
    }
    
    public boolean hasBeltInput(BoardCell cell, Direction dir) {
        if (cell.getType() == 'B' || cell.getType() == 'E') {
            return ((BeltCell)cell).hasInputDirection(dir);
        }
        return false;
    }
    
    public boolean isCellRR(BoardCell cell) {
        if (cell.getType() == 'F') {
            return ((FloorCell)cell).isRightRotator();
        }
        return false;
    }
    
    public boolean isCellLR(BoardCell cell) {
        if (cell.getType() == 'F') {
            return ((FloorCell)cell).isLeftRotator();
        }
        return false;
    }    

    public boolean hasCellEP(BoardCell cell) {
        if (cell.getType() == 'F') {
            return ((FloorCell)cell).hasEvenPusher();
        }
        return false;
    }
    
    public boolean hasCellOP(BoardCell cell) {
        if (cell.getType() == 'F') {
            return ((FloorCell)cell).hasOddPusher();
        }
        return false;
    }
    
    public Direction getCellPshWall(BoardCell cell) {
        if (cell.getType() == 'F') {
            return ((FloorCell)cell).getPusherWall();
        }
        return null;
    }

    public boolean isCellCheckpoint(BoardCell cell) {
        if (cell.getType() == 'F') {
            return ((FloorCell)cell).isCheckPoint();
        }
        return false;
    }

    public int getCellCheckpoint(BoardCell cell) {
        if (cell.getType() == 'F') {
            return ((FloorCell)cell).getCheckpoint();
        }
        return null;
    }

    public boolean isCellRepair(BoardCell cell) {
        if (cell.getType() == 'F') {
            return ((FloorCell)cell).isRepair();
        }
        return false;
    }

    public boolean isCellUpgrade(BoardCell cell) {
        if (cell.getType() == 'F') {
            return ((FloorCell)cell).isUpgrade();
        }
        return false;
    }

    public ArrayList<Pair<Pair<int,int>, Direction>> getLasersDirection() {
        ArrayList<Pair<Pair<int,int>, Direction>> ret;
        for (CellLaser cl : allLasers) {
            Direction shotingDir = Direction.values()[(cl.getWall().ordinal()+2)%4];
            int row = cl.getRow();
            int col = cl.getCol();
            ret.add(new Pair(new Pair(row, col), shotingDir));
        }
    }

    public Direction getDockDir(int row, int, col) {
        return ((FloorCell)getCell(row,col)).getDockDirection();
    }
}
