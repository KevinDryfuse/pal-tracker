package io.pivotal.pal.tracker;

import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/time-entries")
public class TimeEntryController {

    private TimeEntryRepository repo;

    private final CounterService counter;
    private final GaugeService gauge;

    public TimeEntryController(TimeEntryRepository timeEntriesRepo,
                               CounterService counter,
                               GaugeService gauge) {

        this.repo = timeEntriesRepo;
        this.counter = counter;
        this.gauge = gauge;
    }

    @PostMapping
    public ResponseEntity create(@RequestBody TimeEntry timeEntry) {
        TimeEntry createdTimeEntry = repo.create(timeEntry);
        counter.increment("TimeEntry.created");
        gauge.submit("timeEntries.count", repo.list().size());

        return new ResponseEntity<>(createdTimeEntry, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity read(@PathVariable("id") long l) {
        ResponseEntity response = null;
        TimeEntry body = this.repo.find(l);
        if( body == null ){
            counter.increment("TimeEntry.read");
            response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            response = ResponseEntity.ok(body);
        }
        return response;
    }

    @GetMapping
    public ResponseEntity<List<TimeEntry>> list() {
        counter.increment("TimeEntry.listed");
        return ResponseEntity.ok(this.repo.list());
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@PathVariable("id") long l, @RequestBody TimeEntry expected) {
        TimeEntry body = this.repo.update(l, expected);
        if( body == null ){
            counter.increment("TimeEntry.updated");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok(body);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TimeEntry> delete(@PathVariable("id") long l) {

        this.repo.delete(l);
        counter.increment("TimeEntry.deleted");
        gauge.submit("timeEntries.count", repo.list().size());

        return new ResponseEntity<TimeEntry>(HttpStatus.NO_CONTENT);
    }
}
