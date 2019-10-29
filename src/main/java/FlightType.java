import java.io.Serializable;

public enum FlightType  implements Serializable {
    International,
    Domestic,
    NA;

    public static FlightType parse(String string) {
        switch (string) {
            case "Internacional":
                return International;
            case "Cabotaje":
                return Domestic;
            case "N/A":
                return NA;
            default:
                throw new RuntimeException("Unidentified flight type: " + string);
        }
    }

    public String toString() {
        switch (this) {
            case International:
                return "International";
            case Domestic:
                return "Domestic";
            case NA:
                return "N/A";
            default:
                return "Something went wrong";
        }
    }
}
