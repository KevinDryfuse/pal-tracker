package io.pivotal.pal.tracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTimeEntryRepository implements TimeEntryRepository {

    private Map<Long, TimeEntry> map = new HashMap<Long, TimeEntry>();

    public TimeEntry create(TimeEntry any) {
        any.setId(this.map.size() + 1);
        map.put(any.getId(), any);
        return any;
    }

    public TimeEntry find(long l) {
        return this.map.get(l);
    }

    public List<TimeEntry> list() {
        List<TimeEntry> list = new ArrayList<TimeEntry>();
        for (TimeEntry t : this.map.values()) {
            list.add(t);
        }
        return list;
    }

    public TimeEntry update(long eq, TimeEntry any) {
        if (map.containsKey(eq)) {
            TimeEntry timeEntry = map.get(eq);
            timeEntry.setDate(any.getDate());
            timeEntry.setHours(any.getHours());
            timeEntry.setProjectId(any.getProjectId());
            timeEntry.setUserId(any.getUserId());
            return timeEntry;
        }
        return null;
    }

    public TimeEntry delete(long l) {
        return this.map.remove(l);
    }
}
