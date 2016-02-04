package net.devrand.kihon.installtracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tstratto on 2/4/2016.
 */
public class ExportItem {
    public Info info;
    public List<EventItem> events;
    public List<EventItem> newest;

    static class Info {
        public String timestamp;
        public String model;
        public String device;
        public String androidId;
    }

    public ExportItem() {
        info = new Info();
        events = new ArrayList<>();
        newest = new ArrayList<>();
    }
}
