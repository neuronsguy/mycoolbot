package hlt;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class Networking {

    private static final char UNDOCK_KEY = 'u';
    private static final char DOCK_KEY = 'd';
    private static final char THRUST_KEY = 't';
    private final PrintStream out;
    private InputStream in;

    private String botName;
    private int turn = 0;

    public Networking(PrintStream out, InputStream in) {
        this.out = out;
        this.in = in;
    }


    public void sendMoves(final Iterable<Move> moves) {
        final StringBuilder moveString = new StringBuilder();

        for (final Move move : moves) {
            switch (move.getType()) {
                case Noop:
                    continue;
                case Undock:
                    moveString.append(UNDOCK_KEY)
                            .append(" ")
                            .append(move.getShip().getId())
                            .append(" ");
                    break;
                case Dock:
                    moveString.append(DOCK_KEY)
                            .append(" ")
                            .append(move.getShip().getId())
                            .append(" ")
                            .append(((DockMove) move).getDestinationId())
                            .append(" ");
                    break;
                case Thrust:
                    moveString.append(THRUST_KEY)
                            .append(" ")
                            .append(move.getShip().getId())
                            .append(" ")
                            .append(((ThrustMove) move).getThrust())
                            .append(" ")
                            .append(((ThrustMove) move).getAngle())
                            .append(" ");
                    break;
            }
        }
        out.println(moveString);
    }

    private static String readLine(InputStream in) {
        try {
            StringBuilder builder = new StringBuilder();
            int buffer;

            for (; (buffer = in.read()) >= 0;) {
                if (buffer == '\n') {
                    break;
                }
                if (buffer == '\r') {
                    // Ignore carriage return if on windows for manual testing.
                    continue;
                }
                builder = builder.append((char)buffer);
            }
            return builder.toString();
        } catch(final Exception e) {
            System.exit(1);
            throw new RuntimeException(e);
        }
    }

    private static Metadata readLineIntoMetadata(InputStream in) {
        return new Metadata(readLine(in).trim().split(" "));
    }

    public GameMap initialize(final String botName) {
        this.botName = botName;

        final int myId = Integer.parseInt(readLine(in));
        try {
            Log.initialize(new FileWriter(String.format("%d_%s.log", myId, botName)));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        final Metadata inputStringMapSize = readLineIntoMetadata(in);
        final int width = Integer.parseInt(inputStringMapSize.pop());
        final int height = Integer.parseInt(inputStringMapSize.pop());

        final GameMap gameMap = new GameMap(width, height, myId);
        updateMap(gameMap);

        return gameMap;
    }

    public void updateMap(final GameMap map) {
        if (turn == 1) {
            out.println(botName);
        }

        final Metadata inputStringMetadata = readLineIntoMetadata(in);

        if (turn == 0) {
            Log.log("--- PRE-GAME ---");
        } else {
            Log.log("--- TURN " + turn + " ---");
        }
        ++turn;

        map.updateMap(inputStringMetadata);
        map.setTurn(turn);
    }
}
