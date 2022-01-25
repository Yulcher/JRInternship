package com.game.controller;

import com.game.model.Player;
import com.game.service.PlayerService;
import com.game.service.PlayerServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class PlayerController {
    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping(value = "/rest/players")
    public ResponseEntity<List<Player>> readAll(@RequestParam Map<String, String> requestParams) {
        final List<Player> players = playerService.readAll(requestParams);

        //Output for any pages
        Integer pageNumber = PlayerServiceImp.parseVal("pageNumber", requestParams, Integer.class);//0 номер отображаемой страницы
        Integer pageSize = PlayerServiceImp.parseVal("pageSize", requestParams, Integer.class);//3 количество игроков на странице

        pageNumber = pageNumber == null ? 0 : pageNumber;
        pageSize = pageSize == null ? 3 : pageSize;
        int beg = Math.min(players.size(), pageNumber * pageSize);
        int end = Math.min(players.size(), beg + pageSize);
        List<Player> playersOnPage = players.subList(beg, end);

        return new ResponseEntity<>(playersOnPage, HttpStatus.OK);
    }

    @PostMapping(value = "/rest/players")
    public ResponseEntity<?> create(@RequestBody Player player) {
        if (!PlayerServiceImp.checkPlayer(player))//тут проверка на корректность данных для создания Player (имя, id, ...)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        playerService.create(player);
        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @PostMapping(value = "/rest/players/{id}")
    public ResponseEntity<?> update(@PathVariable(name = "id") String id, @RequestBody Player player) {

        if (!PlayerServiceImp.checkPlayerForUpdate(player))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);//400

        long valID = PlayerServiceImp.idValide(id);
        if (valID == -1)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);//400 id not valid
        Player playerFind = playerService.update(player, valID);
        if (playerFind!=null)
            return new ResponseEntity<>(playerFind,HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);//404 player not found
    }

    @DeleteMapping(value = "/rest/players/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") String id) {
        long valID = PlayerServiceImp.idValide(id);
        if (valID == -1)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);//400
        final boolean deleted = playerService.delete(valID);

        return deleted
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);//404
    }

    @GetMapping(value = "/rest/players/{id}")
    public ResponseEntity<Player> read(@PathVariable(name = "id") String id) {
        long valID = PlayerServiceImp.idValide(id);
        if (valID == -1)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        final Player player = playerService.read(valID);

        return player != null
                ? new ResponseEntity<>(player, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/rest/players/count")
    public ResponseEntity<Integer> readAllCount(@RequestParam Map<String, String> requestParams) {
        int count = playerService.readAllCount(requestParams);

        return new ResponseEntity<>(count, HttpStatus.OK);
    }

}
