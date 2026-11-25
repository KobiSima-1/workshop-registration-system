package patmal.workshops.student.impl.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.logic.WorkshopSystemImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * workshop-cli: fills a WorkshopSystem with demo data and exports it to
 * instructors.json, students.json and teams.json in the current folder.
 *
 * Usage:
 *   java -jar workshop-cli.jar                 (students from the students.csv bundled in the jar)
 *   java -jar workshop-cli.jar students.csv    (students from the given CSV file)
 */
public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);
    private static final String DEMO_CSV_RESOURCE = "/students.csv";

    public static void main(String[] args) {
        LOG.info("workshop-cli started");
        WorkshopSystem system = new WorkshopSystemImpl();

        // instructors and workshops
        Instructor dana = system.createInstructor("i1", "Dana Levi", "dana.levi@univ.edu");
        system.createInstructor("i2", "Yossi Mizrahi", "yossi.mizrahi@univ.edu");
        Workshop java = system.createWorkshop("Java Workshop", "Core Java and Maven");
        Workshop web = system.createWorkshop("Web Workshop", "HTTP, servlets and REST");
        system.associateWorkshopWithInstructor(dana, java);
        system.associateWorkshopWithInstructor(dana, web);

        // students: from the CSV file given as argument, or from the demo CSV in the jar
        List<Student> students = importStudents(system, args);
        if (students.size() < 4) {
            LOG.error("At least 4 students are needed for the demo, found {}", students.size());
            System.exit(1);
        }

        // teams: two teams, the remaining students stay free
        system.createTeam("Alpha", java, List.of(students.get(0).getId(), students.get(1).getId()));
        system.createTeam("Beta", web, List.of(students.get(2).getId()));
        LOG.info("{} students are free", system.getFreeStudents().size());

        // exports, in the current folder
        File instructorsFile = new File("instructors.json");
        File studentsFile = new File("students.json");
        File teamsFile = new File("teams.json");
        system.exportInstructorsDataToJson(instructorsFile);
        system.exportStudentsDataToJson(studentsFile);
        system.exportTeamsDataToJson(teamsFile);
        LOG.info("JSON files written to {}", instructorsFile.getAbsoluteFile().getParent());
        LOG.info("workshop-cli finished");
    }

    private static List<Student> importStudents(WorkshopSystem system, String[] args) {
        if (args.length > 0) {
            File csv = new File(args[0]);
            LOG.info("Reading students from {}", csv.getAbsolutePath());
            return system.importStudentDataFromFile(csv);
        }
        // a resource inside the jar is not a File, so copy it to a temporary file first
        LOG.info("No CSV file given, reading the demo students from the jar ({})", DEMO_CSV_RESOURCE);
        try (InputStream in = Main.class.getResourceAsStream(DEMO_CSV_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found: " + DEMO_CSV_RESOURCE);
            }
            Path temp = Files.createTempFile("students", ".csv");
            try {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
                return system.importStudentDataFromFile(temp.toFile());
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
