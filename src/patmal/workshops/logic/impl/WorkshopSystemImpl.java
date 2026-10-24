package patmal.workshops.logic.impl;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Student;
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
    private final Map<Integer, InstructorImpl> instructors;
    private final Map<Integer, TeamImpl> teams;
    
    public WorkshopSystemImpl() {
        students = new HashMap<>();
        workshops = new HashMap<>();
        instructors = new HashMap<>();
        teams = new HashMap<>();
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
}


