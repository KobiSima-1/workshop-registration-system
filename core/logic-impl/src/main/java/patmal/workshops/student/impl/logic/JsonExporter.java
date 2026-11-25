package patmal.workshops.student.impl.logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Person;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Builds the three JSON exports with Jackson's tree model (ObjectNode / ArrayNode).
 * Every export is a tree that only goes "down" from its starting point, so there are no cycles:
 * inside a team the students are written with their own details only, never with their team again.
 */
class JsonExporter {

    private static final Logger LOG = LogManager.getLogger(JsonExporter.class);

    private final ObjectMapper mapper = new ObjectMapper();

    // ---------- the three exports ----------

    /** instructor -> workshops -> teams -> student details */
    void exportInstructors(List<Instructor> instructors, List<Team> allTeams, File target) {
        ArrayNode root = mapper.createArrayNode();
        for (Instructor instructor : instructors) {
            ObjectNode instructorNode = personDetails(instructor);
            ArrayNode workshopsNode = instructorNode.putArray("workshops");
            for (Workshop workshop : instructor.getWorkshops()) {
                ObjectNode workshopNode = workshopDetails(workshop);
                ArrayNode teamsNode = workshopNode.putArray("teams");
                for (Team team : allTeams) {
                    if (team.getWorkshop() == workshop) {
                        ObjectNode teamNode = teamsNode.addObject();
                        teamNode.put("id", team.getId());
                        teamNode.put("name", team.getName());
                        teamNode.set("students", studentsDetails(team));
                    }
                }
                workshopsNode.add(workshopNode);
            }
            root.add(instructorNode);
        }
        write(root, target);
    }

    /** student -> team (with student ids) -> workshop -> instructor */
    void exportStudents(List<Student> students, List<Team> allTeams, File target) {
        ArrayNode root = mapper.createArrayNode();
        for (Student student : students) {
            ObjectNode studentNode = personDetails(student);
            Team team = findTeamOf(student, allTeams);
            if (team == null) {
                studentNode.putNull("team");
            } else {
                ObjectNode teamNode = studentNode.putObject("team");
                teamNode.put("id", team.getId());
                teamNode.put("name", team.getName());
                ArrayNode idsNode = teamNode.putArray("studentIds");
                for (Student member : team.getStudents()) {
                    idsNode.add(member.getId());
                }
                teamNode.set("workshop", workshopWithInstructor(team.getWorkshop()));
            }
            root.add(studentNode);
        }
        write(root, target);
    }

    /** team -> student details, workshop -> instructor */
    void exportTeams(List<Team> teams, File target) {
        ArrayNode root = mapper.createArrayNode();
        for (Team team : teams) {
            ObjectNode teamNode = root.addObject();
            teamNode.put("id", team.getId());
            teamNode.put("name", team.getName());
            teamNode.set("students", studentsDetails(team));
            teamNode.set("workshop", workshopWithInstructor(team.getWorkshop()));
        }
        write(root, target);
    }

    // ---------- building blocks ----------

    /** id, name, email - exactly the details of a person */
    private ObjectNode personDetails(Person person) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", person.getId());
        node.put("name", person.getName());
        node.put("email", person.getEmail());
        return node;
    }

    /** id (a number), name, description */
    private ObjectNode workshopDetails(Workshop workshop) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", workshop.getId());
        node.put("name", workshop.getName());
        node.put("description", workshop.getDescription());
        return node;
    }

    private ObjectNode workshopWithInstructor(Workshop workshop) {
        ObjectNode node = workshopDetails(workshop);
        Instructor instructor = workshop.getInstructor();
        if (instructor == null) {
            node.putNull("instructor");
        } else {
            node.set("instructor", personDetails(instructor));
        }
        return node;
    }

    private ArrayNode studentsDetails(Team team) {
        ArrayNode node = mapper.createArrayNode();
        for (Student student : team.getStudents()) {
            node.add(personDetails(student));
        }
        return node;
    }

    private static Team findTeamOf(Student student, List<Team> allTeams) {
        for (Team team : allTeams) {
            if (team.getStudents().contains(student)) {
                return team;
            }
        }
        return null;
    }

    /** Writes the array in UTF-8 (Jackson's default for files); overwrites an existing file. */
    private void write(ArrayNode root, File target) {
        if (target == null) {
            throw new IllegalArgumentException("Target file cannot be null");
        }
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(target, root);
        } catch (IOException e) {
            LOG.warn("Rejected: cannot write JSON file {}: {}", target, e.getMessage());
            throw new UncheckedIOException("Cannot write JSON file " + target, e);
        }
    }
}
