import java.io.Serializable;

public class Airport implements Serializable {
    public final String oaci;
    public final String name;
    public final String province;

    private Airport(final String oaci, final String name, final String province) {
        this.oaci = oaci;
        this.name = name;
        this.province = province;
    }

    public static Airport of(final String oaci, final String name, final String province) {
        return new Airport(oaci, name, province);
    }

    public String toString() {
        return "Airport " + "\"" + oaci + "\"" +  " " + "\"" +  name + "\"" +  " " + "\"" +  province +"\"" ;
    }
}
