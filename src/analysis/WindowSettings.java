package analysis;

public class WindowSettings {

    public boolean tumblingEnabled;
    public double tumblingWindowSize;

    public boolean expandingEnabled;
    public double expandingWindowSize;

    public boolean hoppingEnabled;
    public double hoppingWindowSize;
    public double hoppingHopSize;
    
    public boolean eventEnabled;
    public String event;
    public double eventTimeout;

    public WindowSettings() {
        this.tumblingEnabled = false;
        this.expandingEnabled = false;
        this.hoppingEnabled = false;
        this.eventEnabled = false;
    }
    
    public WindowSettings(boolean tumbling, boolean expanding, boolean hopping, boolean event) {
        this.tumblingEnabled = tumbling;
        this.expandingEnabled = expanding;
        this.hoppingEnabled = hopping;
        this.eventEnabled = event;
    }

    @Override
    public String toString() {
        return "Tumbling: " + tumblingEnabled + "\n" +
                "Expanding: " + expandingEnabled + "\n" +
                "Hopping: " + hoppingEnabled + "\n" +
                "Event: " + eventEnabled;
    }
}
