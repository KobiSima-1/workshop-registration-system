package patmal.workshops.model.impl;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Workshop;
import java.util.List;
import java.util.ArrayList;


public class TeamImpl implements Team {
    private final Integer id;
    private final String name;
    private final List<Student> students;
    private final Workshop workshop;


    public TeamImpl(Integer id, String name, List<Student> students, Workshop workshop) {
        this.id = id;
        this.name = name;
        this.students = new ArrayList<Student>(students);
        this.workshop = workshop;
    }

    @Override 
    public Integer getId() {
        return id;
    }

    @Override 
    public String getName() {
        return name;
    }

    @Override 
    public List<Student> getStudents() {
        return new ArrayList<Student>(students);
    }

    @Override 
    public Workshop getWorkshop() {
        return workshop;
    }

   
}
