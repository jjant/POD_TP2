import java.io.Serializable;

public enum MoveType  implements Serializable {
    Landing,
    Takeoff;

    public static MoveType parse(String string) {
        switch (string) {
            case "Aterrizaje":
                return Landing;
            case "Despegue":
                return Takeoff;
            default:
                throw new RuntimeException("Unidentified move type: " + string);
        }
    }

    public String toString() {
        switch (this) {
            case Landing:
                return "Landing";
            case Takeoff:
                return "Takeoff";
            default:
                return "Something went wrong";
        }
    }
}
