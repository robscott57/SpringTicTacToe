package tictactoe;
//import jdk.nashorn.internal.parser.JSONParser;
import java.util.concurrent.ThreadLocalRandom;
import org.json.simple.JSONArray;

        import java.lang.String;

/**
 * TicTacToe Web application
 * Uses recursive analysis to determine the next move
 * includes random selection among equal moves to make it more interesting
 *
 * input: HTTP Get with parameters specified as a URL search string which is the JSON Array representation of the game board example:
 * GET /AwsBasic1/TicTacToe?game=["X","-","-","-","-","-","-","-","-"]
 * The response is just the server move square# 0..8
 *
 * @author robscott57
 */

public class TicTacToe{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private char[] game; //represents the game board array of 9 p
    int moves; //number of moves taken
    char me,you; //who is X and who is O

    public enum Outcome {WIN,LOOSE,TIE};

    static final char PLAYER_X='X';
    static final char PLAYER_O='O';
    static final char PLAYER_NONE='-';

    //contents (square#) for each "ROW" (row, column, or diag)
    static final int[] ROW1 = {0,1,2};
    static final int[] ROW2 = {3,4,5};
    static final int[] ROW3 = {6,7,8};
    static final int[] COL1 = {0,3,6};
    static final int[] COL2 = {1,4,7};
    static final int[] COL3 = {2,5,8};
    static final int[] DIAG1 = {0,4,8};
    static final int[] DIAG2 = {2,4,6};

    //list of all "ROWS" each square is a part of
//so square 0 is part of ROW1, COL1, and DIAG1
    static final int[][][] ROWS = {
            {ROW1,COL1,DIAG1},
            {ROW1,COL2},
            {ROW1,COL3,DIAG2},
            {ROW2,COL1},
            {ROW2,COL2,DIAG1,DIAG2},
            {ROW2,COL3},
            {ROW3,COL1,DIAG2},
            {ROW3,COL2},
            {ROW3,COL3,DIAG1}
    };
    private static String gameStr;

    public TicTacToe(String gameParm) {
        gameStr=gameParm;
    }

    //accept parm game=... and return response using json game=
    protected String processRequest()
    {
        //for each game square groupList[square] is the list of groups square intersects with
        //for example square 0 is part of row 0, column 0, diag 0 = group indexes 0,3,7
        // -groups are in groups array order {row0, row1, row2, col0, col1, col2, diag0, diag1}
        // so the group indexes are 0,3,7
        org.json.simple.parser.JSONParser p = new org.json.simple.parser.JSONParser();
        org.json.simple.JSONArray ja;
        game=new char[9];
        int move=-1;
        try{
            ja=(JSONArray)p.parse(gameStr);
        }
        catch (Exception  e){
            return "error 400 Illegal JSON array format:"+gameStr;
        }
        if (ja.size()!=9){
            return "error 400 Illegal game= parameter format";
        }
        moves=0;
        for (int i=0;i<9;i++){
            Object o = ja.get(i);
            if (o instanceof java.lang.String)
                game[i]=((String) o).charAt(0);
            else {
                return "error 400 Illegal game= parameter format";
            }
            if (game[i]!=PLAYER_NONE)
                moves++;
        }
        if ((moves%2)==0){  //0 or even #moves means I move(d) first
            me=PLAYER_X;
            you=PLAYER_O;
        }
        else {
            me=PLAYER_O;
            you=PLAYER_X;
        }
        if (!validGame()){
            return "error 400,bad game state";
        }
        move=getMove();

        if (move==-1){
            return "error 400 game over";
        }

        return Integer.toString(move);
    }

    boolean validGame(){
        int xCount=0;
        int oCount=0;
        int emptyCount=0;
        for (char square:game){
            switch (square){
                case PLAYER_X:
                    xCount++;
                    break;
                case PLAYER_O:
                    oCount++;
                    break;
                case PLAYER_NONE:
                    emptyCount++;
                    break;
                default:
                    return false; //illegal value
            }//switch
        }//for
        if (!((xCount==oCount)||(xCount-oCount==1)))
            return false;
        if (emptyCount==0)
            return false;

        if (winner(ROW1)||winner(ROW2)||winner(ROW3)||
                winner(COL1)||winner(COL2)||winner(COL3)||
                winner(DIAG1)||winner(DIAG2))
            return false;

        return true;
    }
    /*
     * check if this is a winning row,col,diag
     */
    boolean winner(int[] row){
        char sq1=game[row[0]];
        char sq2=game[row[1]];
        char sq3=game[row[2]];
        return (sq1!=PLAYER_NONE && sq1==sq2 &&sq1==sq3);
    }

    protected int getMove(){
        Outcome mvalue;
        int[] winMoves= new int[9]; int winCount=0;
        int[] tieMoves= new int[9]; int tieCount=0;
        int[] looseMoves = new int[9]; int looseCount=0;
        for (int m=0;m<9;m++){
            if (game[m]==PLAYER_NONE) {//legal move
                mvalue=analyzeMove(m);
                switch(mvalue){
                    case WIN:
                        winCount++;
                        winMoves[winCount-1]=m;
                        break;
                    case TIE:
                        tieCount++;
                        tieMoves[tieCount-1]=m;
                        break;
                    case LOOSE:
                        looseCount++;
                        looseMoves[looseCount-1]=m;
                }//switch
            }//fi
        }//for
        //return the best available move but randomize among equal value moves
        ThreadLocalRandom r = ThreadLocalRandom.current();
        if (winCount>0)
            return winMoves[r.nextInt(winCount)];
        else if (tieCount>0)
            return tieMoves[r.nextInt(tieCount)];
        else
            return looseMoves[r.nextInt(looseCount)];
    }
    //Tic-Tac-Toe recursive analysis
    //moveAnalysis looks at myMove and assume respnse will be in opponents best interest
    //respAnalysis looks at oponents move and assumes responses will be in my best interest

    void doMove(int move){
        game[move]=getPlayer();
        moves++;
    }

    void undoMove(int move){
        game[move]=PLAYER_NONE;
        moves--;
    }

    char getPlayer(){
        return (moves%2==0)? PLAYER_X:PLAYER_O;
    }

    protected boolean winner(int move){
        int count=0;
        int curentPlayer=getPlayer();
        doMove(move);

        for (int[] row:ROWS[move]){ //for each "row" this square belongs to
            count=0;
            for (int sq: row){ //for each square in that "row"
                if (game[sq]==game[move]){  //note player has already been incremented
                    count++ ;
                }
            }//for each sq
            if (count==3){
                undoMove(move);
                return true;
            }
        }//for each grp;
        undoMove(move);
        return false;
    }

    protected boolean lastMove(int move){
        game[move]=PLAYER_O;
        for (char ch: game){
            if (ch==PLAYER_NONE) {
                game[move]=PLAYER_NONE;
                return false;
            }
        }
        game[move]=PLAYER_NONE;
        return true;
    }

    protected Outcome analyzeMove(int move){
        boolean tieFound=false;
        Outcome respValue;
        if (winner(move))
            return Outcome.WIN; //this move wins
        if (lastMove(move))
            return Outcome.TIE;
        doMove(move); //enter the move before doing recursive analysis
        for (int resp=0;resp<9;resp++) { //all responses
            if (game[resp]==PLAYER_NONE){ //valid move.. square empty
                respValue=analyzeResp(resp);
                switch(respValue){
                    case LOOSE: {
                        undoMove(move); //undo the move
                        return Outcome.LOOSE; //Assume that will be the oponents response if its available
                    }
                    case TIE:
                        tieFound=true;  //continue looking for any loose but remember there is a tie.
                    default: break;

                }//switch
            }//fi
        } //for
        undoMove(move);//undo the move
        if (tieFound)
            return Outcome.TIE;
        else
            return Outcome.WIN; //returns win if no loose or tie response found
    }//analyzeMove

    protected Outcome analyzeResp(int resp){
        if (winner(resp))
            return Outcome.LOOSE; //this move I loose
        if (lastMove(resp))
            return Outcome.TIE; //this move game over..tie
        Outcome myResponseValue;
        boolean tieFound=false;
        doMove(resp); //enter the move before doing recursive analysis
        for (int myResponse=0; myResponse<9; myResponse++){
            if (game[myResponse]==PLAYER_NONE) { //valid move.. square empty
                myResponseValue = analyzeMove(myResponse);
                if (myResponseValue==Outcome.WIN){  //I have a winning response so I will take it
                    undoMove(resp); //undo the move before returning
                    return Outcome.WIN;
                }
                else if (myResponseValue==Outcome.TIE) //set default to tie if no win move found
                    tieFound=true;
            }//fi
        }//for
        undoMove(resp); //undo the move before returning
        if (tieFound)
            return Outcome.TIE;
        else
            return Outcome.LOOSE;
    }//analyzeResp

    //... on first move I do complete analysis and should not repeat it on the next move.
    //I can save the right move for each response; then I could do the same for the whole game
    //and save in database :)



}
