package pod.api;

import java.io.Serializable;

public class Move implements Serializable {
    public final FlightType flightType;
    public final MoveType moveType;
    public final FlightClass flightClass;
    public final String originOaci;
    public final String destinationOaci;
    public final String airline;

    private Move(FlightType flightType, MoveType moveType, FlightClass flightClass, String originOaci, String destinationOaci, String airline) {
        this.flightType = flightType;
        this.moveType = moveType;
        this.flightClass = flightClass;
        this.originOaci = originOaci;
        this.destinationOaci = destinationOaci;
        this.airline = airline;
    }

    public static Move of(FlightType flightType, MoveType moveType, FlightClass flightClass, String originOaci, String destinationOaci, String airline) {
        return new Move(flightType, moveType, flightClass, originOaci, destinationOaci, airline);
    }

    public String toString() {
        return "Move " + flightType + " " + moveType + " " + flightClass + " " + "\"" + originOaci + "\"" + " " + "\"" + destinationOaci + "\"";
    }
}
