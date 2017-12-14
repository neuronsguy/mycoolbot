import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Circle;
import hlt.*;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class MyBot {
    public static void main(final String[] args) {
        new MyBot().run();
    }

    public void run() {
        final Networking networking = new Networking(System.out, System.in);
        final GameMap gameMap = networking.initialize("Tamagocchi");

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                        "; height: " + gameMap.getHeight() +
                        "; players: " + gameMap.getAllPlayers().size() +
                        "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);
        Timer timer = new Timer();


        double diagonal = Math.sqrt(Math.pow(gameMap.getHeight(), 2) + Math.pow(gameMap.getWidth(), 2));


        final Map<Integer, Move> shipMoves = new HashMap<Integer, Move>();
        for (int turn = 0; ; turn++) {
            timer.turn(turn);

            shipMoves.clear();
            networking.updateMap(gameMap);

            timer.time("map updated");

            RTree<Entity, Circle> everything = RTree.create();
            RTree<Planet, Circle> unoccupiedPlanets = RTree.create();
            RTree<Planet, Circle> myPlanets = RTree.create();
            RTree<Planet, Circle> enemyPlanets = RTree.create();
            for (Planet planet : gameMap.getAllPlanets().values()) {
                everything = everything.add(planet, planet.getCircle());
                if (!planet.isOwned()) {
                    unoccupiedPlanets = unoccupiedPlanets.add(planet, planet.getCircle());
                } else if (planet.getOwner() == gameMap.getMyPlayerId()) {
                    myPlanets = myPlanets.add(planet, planet.getCircle());
                } else {
                    enemyPlanets = enemyPlanets.add(planet, planet.getCircle());
                }
            }

            RTree<Ship, Circle> allShips = RTree.create();
            RTree<Ship, Circle> myShips = RTree.create();
            RTree<Ship, Circle> enemyShips = RTree.create();
            for (Ship ship : gameMap.getAllShips()) {
                everything = everything.add(ship, ship.getCircle());
                allShips = allShips.add(ship, ship.getCircle());
                if (ship.getOwner() == gameMap.getMyPlayerId()) {
                    myShips = myShips.add(ship, ship.getCircle());
                } else {
                    enemyShips = enemyShips.add(ship, ship.getCircle());
                }
            }

            timer.time("trees built");

            // 1. Planetary objectives

            for (Planet planet : gameMap.getAllPlanets().values()) {

                if (planet.isOwned()) {
                    continue;
                }

                for (Entry<Ship, Circle> entry : myShips.nearest(planet.getPoint(), diagonal / 2.0, planet.getDockingSpots()).toBlocking().toIterable()) {

                    Ship ship = entry.value();
                    double distance = planet.getPoint().distance(ship.getPoint()) - planet.getRadius();

                    if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                        continue;
                    }
                    shipMoves.compute(ship.getId(), (integer, move) -> {

                        if (null != move && move instanceof ThrustMove) {

                        }

                    });
                }

            }

            for (Ship ship : gameMap.getMyPlayer().getShips().values()) {
                // Get planets in order of distance
                planets.nearest(ship.getCircle().mbr(), diagonal, gameMap.getAllPlanets().size());
            }



            // 2. Docking


            // 3. Fighting objectives


            // 4. Defence objectives


            // 5. Positioning


            timer.time("sending moves");
            networking.sendMoves(shipMoves.values());

            timer.flush();
        }
    }


    public static class Timer {

        long turnStart = 0L;
        ArrayList<Pair<Long, String>> times = new ArrayList<Pair<Long, String>>();

        public void turn(int turn) {
            times.clear();
            turnStart = System.currentTimeMillis();
            times.add(new Pair<Long, String>(0L, "starting turn " + turn));
        }

        public void time(String s) {
            times.add(new Pair<Long, String>(System.currentTimeMillis() - turnStart, s));
        }

        public void flush() {
            time("turn complete");
            long lastTime = 0L;
            for (Pair<Long, String> time : times) {
                Log.log(time.getKey() + "ms\t" + time.getValue() + " (" + (time.getKey() - lastTime) + "ms)");
                lastTime = time.getKey();
            }
            times.clear();
        }

    }

    static class ScoreFunction {

        static final double SHIPS = 0.0;
        static final double UNDOCKED = 1.0;
        static final double HEALTH = 1.0 / 255;
        static final double PRODUCTION = 1.0;
        static final double PLANETS = 1.0;

        public double calc(int remainingTurns, double ships, double undockedShips, double health, double productionRate, double planets) {
            return SHIPS * ships + UNDOCKED * undockedShips + HEALTH * health
                    + PRODUCTION * remainingTurns * productionRate * Math.exp(0.01 * remainingTurns)
                    + PLANETS * planets;
        }

    }


    static class PositionSummary {

        final int id;
        private int remainingTurns;

        double ships;
        double undockedShips;
        double health;
        double productionRate;
        double planets;

        PositionSummary(int playerId, int remaining) {
            this.id = playerId;
            this.remainingTurns = remaining;
        }

        public void onShip(Ship ship) {
            if (ship.getOwner() == id && ship.getHealth() > 0) {
                ships += 1;
                health += ship.getHealth();
                if (ship.getDockingStatus() == Ship.DockingStatus.Undocked) {
                    undockedShips += 1;
                }
            }
        }

        public void onPlanet(Planet planet) {
            if (planet.getHealth() > 0 && planet.isOwned() && planet.getOwner() == id) {
                this.planets += 1;
                this.productionRate += planet.getDockedShips().size() / 12.0;
            }
        }

        public double score(ScoreFunction scoreFunction) {
            return scoreFunction.calc(remainingTurns, ships, undockedShips, health, productionRate, planets);
        }

        public PositionSummary copy() {
            PositionSummary positionSummary = new PositionSummary(id, remainingTurns);
            positionSummary.ships = ships;
            positionSummary.undockedShips = undockedShips;
            positionSummary.health = health;
            positionSummary.productionRate = productionRate;
            positionSummary.planets = planets;
            return positionSummary;
        }

    }
}
