package de.othr.vs.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import de.othr.vs.data.entity.Adresse;
import de.othr.vs.data.entity.Student;
import java.util.Random;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.sql.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/*
  Testaufrufe z. B. mit Hilfe von Firefox und dem Add-On "restclient"
*/

@Path("studienangelegenheiten")
public class StudentResource {
    
    // Aufruf mit: http://localhost:8080/webresources/studienangelegenheiten/student/12345
    // HTTP-Methode GET auswählen
    // kann auch normal im Browser getestet werden
    @GET
    @Path("student/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Student getStudentById(@PathParam("id") int id) {
        ResultSet rs = null;
        Student returnStudent = null;
        if(ServerREST.mapCustomers.containsKey(id)) {
            System.out.println("Student bereits im Cache");
            return ServerREST.mapCustomers.get(id);
        }
        
        try {	
            Class.forName("com.mysql.jdbc.Driver");	
            Connection c = DriverManager.getConnection("jdbc:mysql://im-vm-011/vs-08","vs-08","vs-08-pw");
            Statement stmt = c.createStatement();	
            String query = "SELECT vorname, nachname, ects, strasse, ort FROM Student WHERE matrikelNr = "+ id ;
            rs = stmt.executeQuery(query);
            rs.first();	//	Moves the cursor to the first row	in	this ResultSet object   
            returnStudent = new Student(id, rs.getString("Vorname"), rs.getString("Nachname"), rs.getInt("Ects"), new Adresse(rs.getString("Strasse"), rs.getString("Ort")));
            c.close();
        } catch (SQLException ex) {
            Logger.getLogger(StudentResource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(StudentResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ServerREST.mapCustomers.put(id, returnStudent);
        System.out.println(ServerREST.mapCustomers.get(id));
       
        return returnStudent;
    }

    // Aufruf mit: http://localhost:8080/webresources/studienangelegenheiten/student
    // HTTP-Methode DELETE auswählen
    @DELETE
    @Path("student/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteStudentById(@PathParam("id") int id) {
        System.out.println("delete Student with id " + id);
    }

    // Aufruf mit: http://localhost:8080/webresources/studienangelegenheiten/student
    // HTTP-Methode POST und custom header "Content-Type: application/xml" oder application/json
    @POST
    @Path("student")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void createNewStudent(Student s) {
        System.out.println("create new student " + s);
    }

    // Aufruf mit: http://localhost:8080/webresources/studienangelegenheiten/students?from=1&to=19
    @GET
    @Path("students")
    @Produces(MediaType.APPLICATION_JSON)
    public Student[] getStudentsByIdRange(@QueryParam("from") int from, @QueryParam("to") int to) {
        if(to < from)
            return new Student[0];
        Student[] students = new Student[(to-from)+1];
        for(int id = from; id <= to; id++) {
            students[id - from] = new Student(id, "Vorname_"+id, "Nachname_"+id, new Random().nextInt(210), new Adresse("Strasse_"+id, "Ort_"+id));
        }
        return students;
    }
    
}
