package patmal.workshops.logic.impl;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.api.model.Team;
import patmal.workshops.model.impl.StudentImpl;
import patmal.workshops.model.impl.WorkshopImpl;
import patmal.workshops.model.impl.InstructorImpl;
import patmal.workshops.model.impl.TeamImpl;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class WorkshopSystemImpl implements WorkshopSystem {

    private final Map<String, StudentImpl> students;
    private final Map<Integer, WorkshopImpl> workshops;
    private final Map<String, InstructorImpl> instructors;
    private final Map<Integer, TeamImpl> teams;
    private int nextWorkshopId;
    private int nextTeamId;
    
    public WorkshopSystemImpl() {
        students = new HashMap<>();
        workshops = new HashMap<>();
        instructors = new HashMap<>();  
        teams = new HashMap<>();
        nextWorkshopId = 1;
        nextTeamId = 1;
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        WorkshopImpl workshop = new WorkshopImpl(nextWorkshopId, name, description);
        workshops.put(workshop.getId(), workshop);
        nextWorkshopId++;
        return workshop;
    }

    @Override 
    public Student createStudent(String id, String name, String email) {
        if (students.containsKey(id)) {
            throw new IllegalArgumentException("Student id already exists");
        }
        StudentImpl student = new StudentImpl(id, name, email);
        students.put(id, student);
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
            throw new IllegalArgumentException("Instructor id already exists");
        }
        InstructorImpl instructor = new InstructorImpl(id, name, email);
        instructors.put(id, instructor);
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

    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        if (instructor == null || workshop == null) {
            throw new IllegalArgumentException("Instructor and Workshop cannot be null");
        }
        if (instructors.get(instructor.getId()) != instructor) {
            throw new IllegalArgumentException("Instructor does not exist in the system");
        }
            if (workshops.get(workshop.getId()) != workshop) {
            throw new IllegalArgumentException("Workshop does not exist in the system");
        }
        if (workshop.getInstructor() == instructor) {
            return;
        } else if (workshop.getInstructor() != null) {
            Instructor oldInstructor = workshop.getInstructor();
            ((InstructorImpl) oldInstructor).removeWorkshop(workshop);
        }
       
        ((WorkshopImpl) workshop).setInstructor(instructor);
        ((InstructorImpl) instructor).addWorkshop(workshop);

    }

    public Team createTeam(String name, Workshop workshop, List<String> members) {
        if (workshop == null) {
            throw new IllegalArgumentException("Workshop cannot be null");
        }
        if (workshops.get(workshop.getId()) != workshop) {
            throw new IllegalArgumentException("Workshop does not exist in the system");
        }
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("Members list cannot be null or empty");
        }
        List<Student> teamMembers = new ArrayList<>();
        for (String memberId : members) {
            Student student = students.get(memberId);
            if (student == null) {
                throw new IllegalArgumentException("Student with id " + memberId + " does not exist");
            }
            if (teamMembers.contains(student)) {
                throw new IllegalArgumentException("Duplicate student id " + memberId + " in members list");
            }
            if (((StudentImpl) student).getTeam() != null) {
                throw new IllegalArgumentException("Student with id " + memberId + " is already in a team");
            }

            teamMembers.add(student);
        }
        TeamImpl team = new TeamImpl(nextTeamId, name, teamMembers, workshop);
        teams.put(team.getId(), team);
        for (Student student : teamMembers) {
            ((StudentImpl) student).setTeam(team);
        }
        nextTeamId++;
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

    public List<Student> getFreeStudents() {
        List<Student> freeStudents = new ArrayList<>();
        for (Student student : students.values()) {
            if (((StudentImpl) student).getTeam() == null) {
                freeStudents.add(student);
            }
        }
        return freeStudents;
    }

    public List<Student> getStudentsOfWorkshop(Integer workshopId) {
        
        Workshop workshop = workshops.get(workshopId);
        if (workshop == null) {
            throw new IllegalArgumentException("Workshop does not exist in the system");
        }
        List<Student> studentsOfWorkshop = new ArrayList<>();
        for (Team team : teams.values()) {
            if (team.getWorkshop().equals(workshop)) {
                studentsOfWorkshop.addAll(team.getStudents());
            }
        }
        return studentsOfWorkshop;
    }

    public List<Student> getStudentsOfInstructor(String instructorId) {
        Instructor instructor = instructors.get(instructorId);
        if (instructor == null) {
            throw new IllegalArgumentException("Instructor does not exist in the system");
        }
        List<Student> studentsOfInstructor = new ArrayList<>();
        for (Workshop workshop : workshops.values()) {
            if (workshop.getInstructor() != null && workshop.getInstructor().equals(instructor)) {
                studentsOfInstructor.addAll(getStudentsOfWorkshop(workshop.getId()));
            }
        }
        return studentsOfInstructor;
    }

    
}


