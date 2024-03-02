/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asi.voronoi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * @author asi
 */
public class PointSet {
    private BufferedReader br;
    
    private final int[][] stateMachine;
    private int state;
    private char nextToken;
    private String valueX, valueY;
    private final Set<Point> ps;
    
    public PointSet() {
        ps = new HashSet<>();
        stateMachine = PointSet.configStateMachine();
        state = 0;        
    }
    
    public Set<Point> getPointSet() {
        return ps;
    }
    
    public Set<Point> buildPointSet(String filename) throws Exception {
        FileReader fr;
        fr = new FileReader(filename);
        br = new BufferedReader(fr);
        return parsePointSet();
    }

    private static int[][] configStateMachine() {
        /*                 0   1   2   3     4     5   6   7    8    9
                        | ws | ( | + | - | digit | , | ) | ; | eof | . | ws = ' ' | '\t | '\n' |'\r'
                        ------------------------------------------------ digit = '0' | '1' | ... |'9'
                      0 | 0  | 1 | - | - |   -   | - | - | - |  -  | - |
                      1 | 1  | - | 2 | 2 |   2   | - | - | - |  -  | - |
                      2 | -  | - | - | - |   2   | 4 | - | - |  -  | 3 |
                      3 | -  | - | - | - |   5   | - | - | - |  -  | - |
                      4 | 4  | - | 6 | 6 |   6   | - | - | - |  -  | - |
                      5 | -  | - | - | - |   5   | 4 | - | - |  -  | - |
                      6 | -  | - | - | - |   6   | - | 9 | - |  -  | 7 |
                      7 | -  | - | - | - |   8   | - | - | - |  -  | - |
                      8 | -  | - | - | - |   8   | - | 9 | - |  -  | - |
                      9 | -  | - | - | - |   -   | - | - |10 |  -  | - |
                     10 | 0  | 1 | - | - |   -   | - | - | - | OK  | - |
        */
        int[][] sm = {
                        { 0,   1,  -1, -1, -1,     -1, -1, -1,  -1, -1},  //  0
                        { 1,  -1,   2,  2,  2,     -1, -1, -1,  -1, -1},  //  1
                        {-1,  -1,  -1, -1,  2,      4, -1, -1,  -1,  3},  //  2
                        {-1,  -1,  -1, -1,  5,     -1, -1, -1,  -1, -1},  //  3
                        { 4,  -1,   6,  6,  6,     -1, -1, -1,  -1, -1},  //  4
                        {-1,  -1,  -1, -1,  5,      4, -1, -1,  -1, -1},  //  5
                        {-1,  -1,  -1, -1,  6,     -1,  9, -1,  -1,  7},  //  6
                        {-1,  -1,  -1, -1,  8,     -1, -1, -1,  -1, -1},  //  7
                        {-1,  -1,  -1, -1,  8,     -1,  9, -1,  -1, -1},  //  8
                        {-1,  -1,  -1, -1, -1,     -1, -1, 10,  -1, -1},  //  9
                        { 0,   1,  -1, -1, -1,     -1, -1, -1, 100, -1}   // 10
                     };
        return sm;
    }
    
    private void error() throws Exception {
        throw new Exception("Called from: " + state + " parse error: " + nextToken);
    } 
    
    private void zero() throws Exception {
        switch(nextToken) {
            case ' ', '\t', '\n', '\r' -> state = stateMachine[state][0];
            case '(' -> {valueX = ""; valueY = ""; state = stateMachine[state][1];}
            default -> error();
        }
    }
    
    private void one() throws Exception {
        switch(nextToken) {
            case ' ', '\t', '\n', '\r' -> state = stateMachine[state][0];
            case '+' -> state = stateMachine[state][2];
            case '-' -> {valueX += nextToken; state = stateMachine[state][3];}
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->  {valueX += nextToken; state = stateMachine[state][4];}
            default -> error();
        }
    }
    
    private void two() throws Exception {
        switch(nextToken) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->  {valueX += nextToken; state = stateMachine[state][4];}
            case ',' -> state = stateMachine[state][5];
            case '.' -> {valueX += nextToken; state = stateMachine[state][9];}
            default -> error();
        }
    }
    
    private void three() throws Exception {
        switch(nextToken) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->  {valueX += nextToken; state = stateMachine[state][4];}
            default -> error();
        }
    }
    
    private void four() throws Exception {
        switch(nextToken) {
            case ' ', '\t', '\n', '\r' -> state = stateMachine[state][0];
            case '+' -> state = stateMachine[state][2];
            case '-' -> {valueY += nextToken; state = stateMachine[state][3];}
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->  {valueY += nextToken; state = stateMachine[state][4];}
            default -> error();
        }
    }
    
    private void five() throws Exception {
        switch(nextToken) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->  {valueX += nextToken; state = stateMachine[state][4];}
            case ',' -> state = stateMachine[state][5];
            default -> error();
        }
    }
    
    private void six() throws Exception {
        switch(nextToken) {
            case '.' -> {valueY += nextToken; state = stateMachine[state][9];}
            case ')'-> {state = stateMachine[state][6];}
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->  {valueY += nextToken; state = stateMachine[state][4];}
            default -> error();
        }
    }
    
    private void seven() throws Exception {
        switch(nextToken) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->  {valueY += nextToken; state = stateMachine[state][4];}
            default -> error();
        }
    }
    
    private void eight() throws Exception {
        switch(nextToken) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->  {valueY += nextToken; state = stateMachine[state][4];}
            case ')' -> state = stateMachine[state][6];
            default -> error();
        }
    }
    
    private void storePoint() {
        ps.add(new Point(Double.parseDouble(valueX),Double.parseDouble(valueY)));
    }
    
    private void nine() throws Exception {
        switch(nextToken) {
            case ';' -> {
                storePoint(); state = stateMachine[state][7];
            }
            default -> error();
        }
    }
    
    private void ten() throws Exception {
        switch(nextToken) {
            case ' ', '\t', '\n', '\r' -> state = stateMachine[state][0];
            case '(' -> {valueX = ""; state = stateMachine[state][1];}
            default -> error();
        }
    }
    
    private Set<Point> parsePointSet() throws Exception {
        int ch = 0;
        while (((ch = br.read()) != -1) && (state != 100)) {
            nextToken = (char) ch;
            switch (state) {
                case 0 -> zero();
                case 1 -> one();
                case 2 -> two();
                case 3 -> three();
                case 4 -> four();
                case 5 -> five();
                case 6 -> six();
                case 7 -> seven();
                case 8 -> eight();
                case 9 -> nine();
                case 10 -> ten();
            }
        }
        return ps;
    }
    

    
}
