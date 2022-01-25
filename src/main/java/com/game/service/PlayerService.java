package com.game.service;

import com.game.model.Player;

import java.util.List;
import java.util.Map;

public interface PlayerService {

    /**
     * 1.Возвращает список всех имеющихся игроков
     * @return список игроков
     */
    List<Player> readAll(Map<String,String> requestParams);


    /**
     * 2.Создает нового игрока
     * @param player - игрок для создания
     */
    void create(Player player);

    /**
     * 3. Обновляет игрока с заданным ID,
     * в соответствии с переданным игроком
     * @param player - игрок в соответсвии с которым нужно обновить данные
     * @param id - id игрока которого нужно обновить
     * @return - true если данные были обновлены, иначе false
     */
    Player update(Player player, Long id);

    /**
     * 4. Удаляет игрока с заданным ID
     * @param id - id игрока, которого нужно удалить
     * @return - true если игрок был удален, иначе false
     */
    boolean delete(Long id);


    /**
     * 5. Возвращает игрока по его ID
     * @param id - ID игрока
     * @return - объект игрока с заданным ID
     */
    Player read(Long id);

    /**
     * 7.ДОРАБОТАТЬ Возвращает количество игроков отфильтрованных в соотв. с переданным фильтром
     * @return список игроков
     */
    int readAllCount(Map<String,String> requestParams);


}
