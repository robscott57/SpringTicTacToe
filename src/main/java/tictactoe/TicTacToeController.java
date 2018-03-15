package tictactoe;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicTacToeController {

    @RequestMapping("/TicTacToe")
    public String greeting(@RequestParam(value="game",
            defaultValue="[\"-\",\"-\",\"-\",\"-\",\"-\",\"-\",\"-\",\"-\",\"-\"]") String game) {
        return new TicTacToe(game).processRequest();
    }
}
