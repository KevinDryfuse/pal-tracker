package io.pivotal.pal.tracker;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JdbcTimeEntryRepository implements TimeEntryRepository {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public JdbcTimeEntryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        StringBuilder query = new StringBuilder();
        query.append("insert into time_entries");
        query.append(" (id, project_id, user_id, date, hours) ");
        query.append(" values (");

        List<String> values = new ArrayList<>();
        values.add(String.valueOf(timeEntry.getId()));
        values.add(String.valueOf(timeEntry.getProjectId()));
        values.add(String.valueOf(timeEntry.getUserId()));
        values.add("'" + String.valueOf(timeEntry.getDate()) + "'");
        values.add(String.valueOf(timeEntry.getHours()));

        query.append(String.join(",", values));

        query.append(");");

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps =
                                connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
                        return ps;
                    }
                },
                keyHolder);

        timeEntry.setId(Long.parseLong(keyHolder.getKey().toString()));

        return timeEntry;
    }

    @Override
    public TimeEntry find(long id) {

        TimeEntry timeEntry = null;

        StringBuilder query = new StringBuilder();
        query.append("SELECT id, project_id, user_id, date, hours FROM time_entries");
        query.append(" WHERE id = ?");

        PreparedStatementCreator psc = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps =
                        connection.prepareStatement(query.toString());
                ps.setLong(1, id);
                return ps;
            }
        };

        timeEntry = jdbcTemplate.query(psc, new ResultSetExtractor<TimeEntry>() {

            @Override
            public TimeEntry extractData(ResultSet rs) throws SQLException, DataAccessException {
                TimeEntry timeEntry = null;
                if (rs.next()) {
                    timeEntry = new TimeEntry(rs.getLong("id"), rs.getLong("project_id"), rs.getInt("user_id"),
                            LocalDate.parse(rs.getString("date")), rs.getInt("hours"));
                }
                return timeEntry;
            }
        });

        return timeEntry;
    }

    @Override
    public List<TimeEntry> list() {

        List<TimeEntry> timeEntries = null;

        StringBuilder query = new StringBuilder();
        query.append("SELECT id, project_id, user_id, date, hours FROM time_entries");

        PreparedStatementCreator psc = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps =
                        connection.prepareStatement(query.toString());
                return ps;
            }
        };

        timeEntries = jdbcTemplate.query(psc, new ResultSetExtractor<List<TimeEntry>>() {

            @Override
            public List<TimeEntry> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<TimeEntry> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new TimeEntry(rs.getLong("id"), rs.getLong("project_id"), rs.getInt("user_id"),
                            LocalDate.parse(rs.getString("date")), rs.getInt("hours")));
                }
                return list;
            }
        });

        return timeEntries;

    }

    @Override
    public TimeEntry update(long id, TimeEntry timeEntry) {

        StringBuilder query = new StringBuilder();

        List<String> fields = Arrays.asList("project_id", "user_id", "date", "hours");

        query.append("UPDATE time_entries set ");
        query.append(String.join("= ? ,", fields));
        query.append("= ?");
        query.append(" WHERE id = ?");

        jdbcTemplate.update(new UpdatePreparedStatementCreator(query.toString(), id, timeEntry));
        timeEntry = find(id);

        return timeEntry;
    }

    @Override
    public TimeEntry delete(long id) {

        TimeEntry timeEntry = find(id);

        if(timeEntry != null) {
            jdbcTemplate.execute("DELETE FROM time_entries WHERE id = " + id);
        }

        return timeEntry;
    }

    static class UpdatePreparedStatementCreator implements  PreparedStatementCreator {

        private String query;
        private Long id;
        private TimeEntry timeEntry;

        public UpdatePreparedStatementCreator(String query, Long id, TimeEntry timeEntry) {
            this.query = query;
            this.id = id;
            this.timeEntry = timeEntry;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
            PreparedStatement ps =
                    connection.prepareStatement(query);
            ps.setLong(1, timeEntry.getProjectId());
            ps.setLong(2, timeEntry.getUserId());
            ps.setDate( 3, Date.valueOf(timeEntry.getDate()));
            ps.setInt(4, timeEntry.getHours());
            ps.setLong(5, id);
            return ps;
        }
    }
}
