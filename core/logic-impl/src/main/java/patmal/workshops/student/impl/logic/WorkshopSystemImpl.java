package patmal.workshops.student.impl.logic;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.model.InstructorImpl;
import patmal.workshops.student.impl.model.StudentImpl;
import patmal.workshops.student.impl.model.TeamImpl;
import patmal.workshops.student.impl.model.WorkshopImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkshopSystemImpl implements WorkshopSystem {

    private static final Logger LOG = LogManager.getLogger(WorkshopSystemImpl.class);
    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();

    // LinkedHashMap keeps the creation order (needed for the JSON exports)
    private final Map<String, StudentImpl> students;
    private final Map<Integer, WorkshopImpl> workshops;
    private final Map<String, InstructorImpl> instructors;
    private final Map<Integer, TeamImpl> teams;
    private int nextWorkshopId;
    private int nextTeamId;

    private final JsonExporter jsonExporter;
    private final CsvStudentImporter csvImporter;

    public WorkshopSystemImpl() {
        students = new LinkedHashMap<>();
        workshops = new LinkedHashMap<>();
        instructors = new LinkedHashMap<>();
        teams = new LinkedHashMap<>();
        nextWorkshopId = 1;
        nextTeamId = 1;
        jsonExporter = new JsonExporter();
        csvImporter = new CsvStudentImporter(this);
    }

    /** Logs a rejected operation at warn level and returns the exception to throw. */
    private static IllegalArgumentException reject(String message) {
        LOG.warn("Rejected: {}", message);
        return new IllegalArgumentException(message);
    }

    private static void validateEmail(String email) {
        if (!EMAIL_VALIDATOR.isValid(email)) {
            throw reject("Invalid e-mail address: " + email);
        }
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        WorkshopImpl workshop = new WorkshopImpl(nextWorkshopId, name, description);
        workshops.put(workshop.getId(), workshop);
        nextWorkshopId++;
        LOG.info("Workshop created: id={}, name={}", workshop.getId(), name);
        return workshop;
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        if (students.containsKey(id)) {
            throw reject("Student id already exists: " + id);
        }
        validateEmail(email);
        StudentImpl student = new StudentImpl(id, name, email);
        students.put(id, student);
        LOG.info("Student created: id={}, name={}", id, name);
        return student;
    }

    @Override
    public Student getStudent(String id) {
        return students.get(id);
    }

    @Override
    public List<Student> getStudents() {
        return new ArrayList<>(students.values());
    }

    @Override
    public Instructor getInstructor(String id) {
        return instructors.get(id);
    }

    @Override
    public List<Instructor> getInstructors() {
        return new ArrayList<>(instructors.values());
    }

    @Override
    public Instructor createInstructor(String id, String name, String email) {
        if (instructors.containsKey(id)) {
            throw reject("Instructor id already exists: " + id);
        }
        validateEmail(email);
        InstructorImpl instructor = new InstructorImpl(id, name, email);
        instructors.put(id, instructor);
        LOG.info("Instructor created: id={}, name={}", id, name);
        return instructor;
    }

    @Override
    public Workshop getWorkshop(Integer id) {
        return workshops.get(id);
    }

    @Override
    public List<Workshop> getWorkshops() {
        return new ArrayList<>(workshops.values());
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        if (instructor == null || workshop == null) {
            throw reject("Instructor and Workshop cannot be null");
        }
        if (instructors.get(instructor.getId()) != instructor) {
            throw reject("Instructor does not exist in the system: " + instructor.getId());
        }
        if (workshops.get(workshop.getId()) != workshop) {
            throw reject("Workshop does not exist in the system: " + workshop.getId());
        }
        if (workshop.getInstructor() == instructor) {
            return;
        } else if (workshop.getInstructor() != null) {
            Instructor oldInstructor = workshop.getInstructor();
            ((InstructorImpl) oldInstructor).removeWorkshop(workshop);
        }

        ((WorkshopImpl) workshop).setInstructor(instructor);
        ((InstructorImpl) instructor).addWorkshop(workshop);
        LOG.info("Workshop {} associated with instructor {}", workshop.getId(), instructor.getId());
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> members) {
        if (workshop == null) {
            throw reject("Workshop cannot be null");
        }
        if (workshops.get(workshop.getId()) != workshop) {
            throw reject("Workshop does not exist in the system: " + workshop.getId());
        }
        if (members == null || members.isEmpty()) {
            throw reject("Members list cannot be null or empty");
        }
        List<Student> teamMembers = new ArrayList<>();
        for (String memberId : members) {
            Student student = students.get(memberId);
            if (student == null) {
                throw reject("Student with id " + memberId + " does not exist");
            }
            if (teamMembers.contains(student)) {
                throw reject("Duplicate student id " + memberId + " in members list");
            }
            if (((StudentImpl) student).getTeam() != null) {
                throw reject("Student with id " + memberId + " is already in a team");
            }

            teamMembers.add(student);
        }
        TeamImpl team = new TeamImpl(nextTeamId, name, teamMembers, workshop);
        teams.put(team.getId(), team);
        for (Student student : teamMembers) {
            ((StudentImpl) student).setTeam(team);
        }
        nextTeamId++;
        LOG.info("Team created: id={}, name={}, workshop={}, members={}", team.getId(), name, workshop.getId(), members);
        return team;
    }

    @Override
    public Team getTeam(Integer id) {
        return teams.get(id);
    }

    @Override
    public List<Team> getTeams() {
        return new ArrayList<>(teams.values());
    }

    @Override
    public List<Student> getFreeStudents() {
        List<Student> freeStudents = new ArrayList<>();
        for (StudentImpl student : students.values()) {
            if (student.getTeam() == null) {
                freeStudents.add(student);
            }
        }
        return freeStudents;
    }

    @Override
    public List<Student> getStudentsOfWorkshop(Integer workshopId) {
        Workshop workshop = workshops.get(workshopId);
        if (workshop == null) {
            throw reject("Workshop does not exist in the system: " + workshopId);
        }
        List<Student> studentsOfWorkshop = new ArrayList<>();
        for (Team team : teams.values()) {
            if (team.getWorkshop().equals(workshop)) {
                studentsOfWorkshop.addAll(team.getStudents());
            }
        }
        return studentsOfWorkshop;
    }

    @Override
    public List<Student> getStudentsOfInstructor(String instructorId) {
        Instructor instructor = instructors.get(instructorId);
        if (instructor == null) {
            throw reject("Instructor does not exist in the system: " + instructorId);
        }
        List<Student> studentsOfInstructor = new ArrayList<>();
        for (Workshop workshop : workshops.values()) {
            if (workshop.getInstructor() != null && workshop.getInstructor().equals(instructor)) {
                studentsOfInstructor.addAll(getStudentsOfWorkshop(workshop.getId()));
            }
        }
        return studentsOfInstructor;
    }

    // ---------- API 2.0: files ----------

    @Override
    public void exportInstructorsDataToJson(File targetFileName) {
        jsonExporter.exportInstructors(getInstructors(), getTeams(), targetFileName);
        LOG.info("Instructors exported to {}", targetFileName);
    }

    @Override
    public void exportStudentsDataToJson(File targetFileName) {
        jsonExporter.exportStudents(getStudents(), getTeams(), targetFileName);
        LOG.info("Students exported to {}", targetFileName);
    }

    @Override
    public void exportTeamsDataToJson(File targetFileName) {
        jsonExporter.exportTeams(getTeams(), targetFileName);
        LOG.info("Teams exported to {}", targetFileName);
    }

    @Override
    public List<Student> importStudentDataFromFile(File source) {
        List<Student> imported = csvImporter.importStudents(source);
        LOG.info("{} students imported from {}", imported.size(), source);
        return imported;
    }
}
