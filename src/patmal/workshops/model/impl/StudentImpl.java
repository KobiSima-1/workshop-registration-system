package patmal.workshops.model.impl;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Instructor;
import java.util.List;
import java.util.ArrayList;

public class StudentImpl extends AbstractPerson implements Student {

    private Team team;

    public StudentImpl(String id, String name, String email) {
        super(id, name, email);
        team = null;
        
    }

    @Override 
    public String getTeamName() {
        if (team != null) {
            return team.getName();
        }
        return null;
    }

    @Override 
    public Instructor getInstructor() {
        if (team != null) {
            return team.getWorkshop().getInstructor();
        }
        return null;
    }

    @Override 
    public List<Student> getTeamMates() {
        if (team != null) {
            List<Student> fullList = new ArrayList <Student>(team.getStudents());
            fullList.remove(this);
            return fullList;
        }
        return new ArrayList<>();
    }

     public void setTeam(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }
} 
    

