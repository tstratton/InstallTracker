package net.devrand.kihon.installtracker;

/**
 * Created by tstratto on 2/2/2016.
 */
public class EventItem {
    public String name;
    public String type;
    public String timestamp;

    public EventItem(String name, String type, String timestamp) {
        this.name = name;
        this.type = type;
        this.timestamp = timestamp;
    }
}
