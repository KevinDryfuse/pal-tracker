package io.pivotal.pal.tracker;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/time-entries")
public class TimeEntryController {

    private TimeEntryRepository repo;

    public TimeEntryController(TimeEntryRepository timeEntryRepository) {
        this.repo = timeEntryRepository;
    }

    @PostMapping
    public ResponseEntity create(@RequestBody TimeEntry timeEntry) {
        return new ResponseEntity<TimeEntry>(this.repo.create(timeEntry), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity read(@PathVariable("id") long l) {
        TimeEntry body = this.repo.find(l);
        if( body == null ){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok(body);
        }
    }

    @GetMapping
    public ResponseEntity<List<TimeEntry>> list() {
        return ResponseEntity.ok(this.repo.list());
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@PathVariable("id") long l, @RequestBody TimeEntry expected) {
        TimeEntry body = this.repo.update(l, expected);
        if( body == null ){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok(body);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TimeEntry> delete(@PathVariable("id") long l) {
        return new ResponseEntity<TimeEntry>(this.repo.delete(l), HttpStatus.NO_CONTENT);
    }
}
