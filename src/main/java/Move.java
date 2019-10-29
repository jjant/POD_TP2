import java.io.Serializable;

public class Move implements Serializable {
    public final FlightType flightType;
    public final MoveType moveType;
    public final FlightClass flightClass;
    public final String originOaci;
    public final String destinationOaci;

    private Move(FlightType flightType, MoveType moveType, FlightClass flightClass, String originOaci, String destinationOaci) {
        this.flightType = flightType;
        this.moveType = moveType;
        this.flightClass = flightClass;
        this.originOaci = originOaci;
        this.destinationOaci = destinationOaci;
    }

    public static Move of(FlightType flightType, MoveType moveType, FlightClass flightClass, String originOaci, String destinationOaci) {
        return new Move(flightType, moveType, flightClass, originOaci, destinationOaci);
    }

    public String toString() {
        return "Move " + flightType + " " + moveType + " " + flightClass + " " + "\"" + originOaci + "\"" + " " + "\"" + destinationOaci + "\"";
    }
}
