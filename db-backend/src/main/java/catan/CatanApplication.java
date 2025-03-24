package catan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@SpringBootApplication
@RestController
public class CatanApplication {

	// Sample hello world API
	@GetMapping("/helloIsaac")
	public String helloClass() {
		System.out.println("HELLO");
		return "Hello Isaac. Welcome to class!";
	}

	@GetMapping("/getPlayerById/{id}")
	public Player getPlayerById(@PathVariable("id") long id) {
		System.out.println(id);
		DatabaseConnectionManager dcm = new DatabaseConnectionManager("db",
				"rps", "postgres", "password");
		Player player = new Player();
		try {
			Connection connection = dcm.getConnection();
			PlayerDAO playerDAO = new PlayerDAO(connection);
			player = playerDAO.findById(id);
			System.out.println(player);
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return player;
	}

	@PostMapping("/createNewPlayer")
	public Player createNewPlayer(@RequestBody String json) throws JsonProcessingException {
		System.out.println(json);
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, String> inputMap = objectMapper.readValue(json, Map.class);
		DatabaseConnectionManager dcm = new DatabaseConnectionManager("db",
				"rps", "postgres", "password");
		Player player = new Player();
		try {
			Connection connection = dcm.getConnection();
			PlayerDAO playerDAO = new PlayerDAO(connection);
			player.setUserName(inputMap.get("userName"));
			player.setPassword(inputMap.get("password"));
			player.setTotalGames(Integer.parseInt(inputMap.get("totalGames")));
			player.setTotalWins(Integer.parseInt(inputMap.get("totalWins")));
			player.setTotalLosses(Integer.parseInt(inputMap.get("totalLosses")));
			player = playerDAO.create(player);
			System.out.println(player);
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return player;
	}

	public static void main(String[] args) {
		SpringApplication.run(CatanApplication.class, args);
	}

}