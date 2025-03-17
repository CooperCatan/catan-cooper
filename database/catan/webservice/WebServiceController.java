// spring boot controller for catan webservice
// will contain all the API endpoints
// this must be done last after all DAOs are created

package catan.webservice;

import catan.*;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


@CrossOrigin
@RestController
@RequestMapping("/api")

public class WebServiceController {

    @GetMapping("/player/{id}")
    public Player getPlayer(@PathVariable long id){
        System.out.println("Getting player with id: " + id);
        DatabaseConnectionManager dcm = new DatabaseConnectionManager("localhost","catan","postgres","password");

        try(Connection connection = dcm.getConnection()){
            PlayerDAO playerDAO = new PlayerDAO(connection);
            return playerDAO.read(id);
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    
}
    
