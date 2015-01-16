package slotmachien;

public enum Status {
    OPEN("o"), CLOSED("c"), DEADZONED("d");

    private String abbreviation;

    private Status(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}
