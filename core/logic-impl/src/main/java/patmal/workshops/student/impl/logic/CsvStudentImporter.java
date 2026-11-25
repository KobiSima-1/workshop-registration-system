package patmal.workshops.student.impl.logic;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Student;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Bonus: reads students (id,name,email) from a UTF-8 CSV file with commons-csv.
 * Every line goes through createStudent, so the same rules apply; the import stops at the first rejected line.
 */
class CsvStudentImporter {

    private static final Logger LOG = LogManager.getLogger(CsvStudentImporter.class);
    private static final List<String> HEADER = List.of("id", "name", "email");

    // first line = header; empty lines are ignored; quoted values are supported
    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .get();

    private final WorkshopSystem system;

    CsvStudentImporter(WorkshopSystem system) {
        this.system = system;
    }

    List<Student> importStudents(File source) {
        if (source == null) {
            throw reject("Source file cannot be null");
        }
        List<Student> imported = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(source.toPath(), StandardCharsets.UTF_8)) {
            CSVParser parser;
            try {
                parser = FORMAT.parse(reader);
            } catch (IllegalArgumentException e) {
                // for example: a duplicate or an empty column name in the header
                throw reject("Wrong header in " + source + ": " + e.getMessage());
            }
            if (!HEADER.equals(parser.getHeaderNames())) {
                throw reject("Missing or wrong header in " + source + ", expected " + HEADER
                        + " but found " + parser.getHeaderNames());
            }
            Iterator<CSVRecord> records = parser.iterator();
            while (hasNext(records, source)) {
                CSVRecord record = records.next();
                if (record.size() != HEADER.size()) {
                    throw reject("Line " + record.getRecordNumber() + " in " + source + " has "
                            + record.size() + " values instead of " + HEADER.size());
                }
                // the same rules as createStudent (it throws IllegalArgumentException and logs the rejection)
                imported.add(system.createStudent(record.get(0), record.get(1), record.get(2)));
            }
        } catch (IOException e) {
            LOG.warn("Rejected: cannot read CSV file {}: {}", source, e.getMessage());
            throw new UncheckedIOException("Cannot read CSV file " + source, e);
        }
        return imported;
    }

    /** A line that commons-csv cannot parse (for example a broken quote) is a rejected line. */
    private static boolean hasNext(Iterator<CSVRecord> records, File source) {
        try {
            return records.hasNext();
        } catch (UncheckedIOException | IllegalStateException e) {
            throw reject("Malformed line in " + source + ": " + e.getMessage());
        }
    }

    private static IllegalArgumentException reject(String message) {
        LOG.warn("Rejected: {}", message);
        return new IllegalArgumentException(message);
    }
}
