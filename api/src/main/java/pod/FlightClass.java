package pod.api;

import java.io.Serializable;

public enum FlightClass implements Serializable {
    NonRegular,
    Regular,
    PrivateFlightInternationalPatent,
    PrivateFlightNationalPatent;

    public static FlightClass parse(String string) {
        switch (string) {
            case "Regular":
                return Regular;
            case "No Regular":
                return NonRegular;
            case "Vuelo Privado con Matrícula Nacional":
                return PrivateFlightNationalPatent;
            case "Vuelo Privado con Matrícula Extranjera":
                return PrivateFlightInternationalPatent;
            default:
                throw new RuntimeException("Unidentified move type: " + string);
        }
    }

}
