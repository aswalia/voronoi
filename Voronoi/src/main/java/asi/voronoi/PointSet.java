/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asi.voronoi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author asi
 */
public class PointSet {

    // the states defined within the state transition matrix
    private enum State {
        beginPoint,
        beginXCoor,
        xCoor,
        xCoorBeginDecimal,
        beginYCoor,
        xCoorEndDecimal,
        yCoor,
        yCoorBeginDecimal,
        yCoorEndDecimal,
        endPoint,
        e_unexp,
        e_unkno
    }

    // the character types expected in parsing PointSet files
    private enum CharType {
        ws,
        lPerentes,
        plus,
        minus,
        digit,
        komma,
        rParentes,
        semicolon,
        unknown,
        decimal
    }
    /*       0    1   2   3     4     5   6   7    8    9
       | ws | ( | + | - | digit | , | ) | ; | uk  | . | ws = ' ' | '\t | '\n' |'\r'
       ------------------------------------------------ digit = '0' | '1' | ... |'9'
                                                        uk = unknown (any but the defined)
     0 | 0  | 1 | - | - |   -   | - | - | - |  -  | - |0 = beginPoint     - = <error state>
     1 | 1  | - | 2 | 2 |   2   | - | - | - |  -  | - |1 = beginXCoor
     2 | -  | - | - | - |   2   | 4 | - | - |  -  | 3 |2 = xCoor
     3 | -  | - | - | - |   5   | - | - | - |  -  | - |3 = xCoorBeginDecimal
     4 | 4  | - | 6 | 6 |   6   | - | - | - |  -  | - |4 = beginYCoor
     5 | -  | - | - | - |   5   | 4 | - | - |  -  | - |5 = xCoorEndDecimal
     6 | -  | - | - | - |   6   | - | 9 | - |  -  | 7 |6 = yCoor
     7 | -  | - | - | - |   8   | - | - | - |  -  | - |7 = yCoorBeginDecimal
     8 | -  | - | - | - |   8   | - | 9 | - |  -  | - |8 = yCoorEndDecimal
     9 | -  | - | - | - |   -   | - | - | 0 |  -  | - |9 = endPoint
     */
    private static final State[][] STATE_MACHINE = {
        {State.beginPoint, State.beginXCoor, State.e_unexp, State.e_unexp, State.e_unexp, State.e_unexp, State.e_unexp, State.e_unexp, State.e_unkno, State.e_unexp}, //  beginPoint
        {State.beginXCoor, State.e_unexp, State.xCoor, State.xCoor, State.xCoor, State.e_unexp, State.e_unexp, State.e_unexp, State.e_unkno, State.e_unexp}, //  beginXCoor
        {State.e_unexp, State.e_unexp, State.e_unexp, State.e_unexp, State.xCoor, State.beginYCoor, State.e_unexp, State.e_unexp, State.e_unkno, State.xCoorBeginDecimal}, //  xCoor
        {State.e_unexp, State.e_unexp, State.e_unexp, State.e_unexp, State.xCoorEndDecimal, State.e_unexp, State.e_unexp, State.e_unexp, State.e_unkno, State.e_unexp}, //  xCoorBeginDecimal
        {State.beginYCoor, State.e_unexp, State.yCoor, State.yCoor, State.yCoor, State.e_unexp, State.e_unexp, State.e_unexp, State.e_unkno, State.e_unexp}, //  beginYCoor
        {State.e_unexp, State.e_unexp, State.e_unexp, State.e_unexp, State.xCoorEndDecimal, State.beginYCoor, State.e_unexp, State.e_unexp, State.e_unkno, State.e_unexp}, //  xCoorEndDecimal
        {State.e_unexp, State.e_unexp, State.e_unexp, State.e_unexp, State.yCoor, State.e_unexp, State.endPoint, State.e_unexp, State.e_unkno, State.yCoorBeginDecimal}, //  yCoor
        {State.e_unexp, State.e_unexp, State.e_unexp, State.e_unexp, State.yCoorEndDecimal, State.e_unexp, State.e_unexp, State.e_unexp, State.e_unkno, State.e_unexp}, //  yCoorNeginDecimal
        {State.e_unexp, State.e_unexp, State.e_unexp, State.e_unexp, State.yCoorEndDecimal, State.e_unexp, State.endPoint, State.e_unexp, State.e_unkno, State.e_unexp}, //  yCoorEndDecimal
        {State.e_unexp, State.e_unexp, State.e_unexp, State.e_unexp, State.e_unexp, State.e_unexp, State.e_unexp, State.beginPoint, State.e_unkno, State.e_unexp} //  endPoint
    };

    private BufferedReader br;
    private State state;
    private CharType tokenType;
    private char nextToken;
    private String valueX, valueY;
    private final Map<Integer, Point> pm;
    private int id;

    public PointSet() {
        pm = new HashMap<>();
        state = State.beginPoint;
        id = 0;
    }

    public Map<Integer, Point> getPointMap() {
        return pm;
    }

    public Map<Integer, Point> buildPointMap(File filename) throws Exception {
        FileReader fr;
        fr = new FileReader(filename);
        br = new BufferedReader(fr);
        return parsePointSet();
    }

    public static void store(int group, Map<Integer, Point> pm) throws SQLException {
        List<String> l = new LinkedList<>();
        Set<Integer> sp = pm.keySet();
        for (Integer i : sp) {
            String r = i + " , " + group + " , " + pm.get(i).x() + " , " + pm.get(i).y();
            l.add(r);
        }
        DatabaseHandler.insertContent("points", l);
    }

    private void error(State os, char oc) throws Exception {
        throw new Exception("Parse error - in state \'" + os + "\' Got: " + oc);
    }

    private void nextTokenType() {
        switch (nextToken) {
            case ' ', '\t', '\n', '\r' ->
                tokenType = CharType.ws;
            case '+' ->
                tokenType = CharType.plus;
            case '-' ->
                tokenType = CharType.minus;
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->
                tokenType = CharType.digit;
            case '.' ->
                tokenType = CharType.decimal;
            case ',' ->
                tokenType = CharType.komma;
            case ';' ->
                tokenType = CharType.semicolon;
            case '(' ->
                tokenType = CharType.lPerentes;
            case ')' ->
                tokenType = CharType.rParentes;
            default ->
                tokenType = CharType.unknown;
        }
    }

    private void beginPoint() throws Exception {
        switch (tokenType) {
            case lPerentes -> {
                valueX = "";
                valueY = "";
                id++;
            }
        }
    }

    private String beginCoordinat(String val) throws Exception {
        switch (tokenType) {
            case minus, digit ->
                val += nextToken;
        }
        return val;
    }

    private String inCoordinat(String val) throws Exception {
        switch (tokenType) {
            case digit, decimal ->
                val += nextToken;
        }
        return val;
    }

    private String endCoordinat(String val) throws Exception {
        switch (tokenType) {
            case digit ->
                val += nextToken;
        }
        return val;
    }

    private void storePoint() {
        if (!pm.containsKey(id)) {
            pm.put(id, new Point(Double.parseDouble(valueX), Double.parseDouble(valueY)));
        }
    }

    private void endPoint() throws Exception {
        switch (tokenType) {
            case semicolon ->
                storePoint();
        }
    }

    private Map<Integer, Point> parsePointSet() throws Exception {
        int ch;
        State oldState = null;
        while ((ch = br.read()) != -1) {
            char oldChar = nextToken;
            nextToken = (char) ch;
            nextTokenType();
            switch (state) {
                case beginPoint ->
                    beginPoint();
                case beginXCoor ->
                    valueX = beginCoordinat(valueX);
                case xCoor ->
                    valueX = inCoordinat(valueX);
                case xCoorBeginDecimal, xCoorEndDecimal ->
                    valueX = endCoordinat(valueX);
                case beginYCoor ->
                    valueY = beginCoordinat(valueY);
                case yCoor ->
                    valueY = inCoordinat(valueY);
                case yCoorBeginDecimal, yCoorEndDecimal ->
                    valueY = endCoordinat(valueY);
                case endPoint ->
                    endPoint();
                default ->
                    error(oldState, oldChar);
            }
            oldState = state;
            state = STATE_MACHINE[state.ordinal()][tokenType.ordinal()];
        }
        return pm;
    }
}
