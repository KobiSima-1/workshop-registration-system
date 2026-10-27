package patmal.workshops.model.impl;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.api.model.Instructor;
import java.util.List;
import java.util.ArrayList;

public class InstructorImpl extends AbstractPerson implements Instructor {

    private final List<Workshop> workshops;

     public InstructorImpl(String id, String name, String email) {
         super(id, name, email);
         workshops = new ArrayList<Workshop>();
     }

    @Override 
    public List<Workshop> getWorkshops() {
       List<Workshop> copy = new ArrayList<Workshop>(workshops);
       return copy;
    }

    public void addWorkshop(Workshop workshop) {
        for (Workshop w : workshops) {
            if (w.getId().equals(workshop.getId())) {
                return;
            }
        }
        workshops.add(workshop);
    }

    public void removeWorkshop(Workshop workshop) {
        workshops.remove(workshop);
    }

    

} 
    

