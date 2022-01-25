package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.model.Player;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PlayerServiceImp implements PlayerService {

    DataSource dataSource ;

    @Autowired
    public PlayerServiceImp(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void playerCreatInDB(Player player) {
        try {
            Connection connection = dataSource.getConnection();

            String sql = "INSERT INTO player (name,title,race,profession,experience,level,untilNextLevel,birthday,banned) VALUES (?,?,?,?,?,?,?,?,?)";
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);   //сжал текст ниже, чтобы всё на одну страницу влезно
            //statement.setLong(1, player.getId()); //убрал первый ? из  VALUES (?,
            statement.setString(1, player.getName());               statement.setString(2, player.getTitle());
            statement.setString(3, player.getRace().toString());    statement.setString(4, player.getProfession().toString());
            statement.setInt(5, player.getExperience());            statement.setInt(6, player.getLevel());
            statement.setInt(7, player.getUntilNextLevel());        statement.setString(8, new SimpleDateFormat("YYYY-MM-dd").format(player.getBirthday()));
            statement.setBoolean(9, player.getBanned());
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0)
                throw new SQLException("Creating user failed, no rows affected.");

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next())
                    player.setId(generatedKeys.getLong(1));
                else
                    throw new SQLException("Creating player failed, no ID obtained.");
            }
            connection.close();
        } catch (Exception e) {            e.printStackTrace();        }       }

    @Override
    public void create(Player player) {
        update_Level_UntilNextLevel(player);//тут обновляем по формуле из ТЗ player.untilNextLevel и player.level
        playerCreatInDB(player);//тут сама запись в БД
    }


    //БЕЗ SQL Хранилище игроков
    private static final Map<Long, Player> PLAYER_REPOSITORY_MAP = new HashMap<>();

    // Переменная для генерации ID игрока
    private static final AtomicLong PLAYER_ID_HOLDER = new AtomicLong();

    public static <T> T parseVal(String nameVal, Map<String, String> requestParams, Class<T> type) {
        String val = requestParams.containsKey(nameVal) ? requestParams.get(nameVal) : null;
        if (val == null)
            return null;

        try {
            if (nameVal.equals("name") || nameVal.equals("title"))
                return (T) val;
            else if (nameVal.equals("race"))
                return (T) Race.valueOf(val);
            else if (nameVal.equals("profession"))
                return (T) Profession.valueOf(val);
            else if (nameVal.equals("order"))
                return (T) PlayerOrder.valueOf(val);
            else if (nameVal.equals("after") || nameVal.equals("before"))
                return (T) (Object) Long.parseLong(val, 10);
            else if (nameVal.equals("minExperience") || nameVal.equals("maxExperience") ||
                    nameVal.equals("minLevel") || nameVal.equals("maxLevel") ||
                    nameVal.equals("pageNumber") || nameVal.equals("pageSize"))
                return (T) (Object) Integer.parseInt(val);
            else if (nameVal.equals("banned"))
                return (T) (Object) val.equals("true");

        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public  List<Player> listFromDB(String sql) {
        List<Player> players = new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();


            ResultSet resultSet = statement.executeQuery(sql);

            Calendar calendar = Calendar.getInstance();

            while (resultSet.next()) {
                Player pl = new Player();
                pl.setId(resultSet.getLong("id"));
                pl.setName(resultSet.getString("name"));
                pl.setTitle(resultSet.getString("title"));
                pl.setRace(Race.valueOf(resultSet.getString("race")));
                pl.setProfession(Profession.valueOf(resultSet.getString("profession")));
                pl.setExperience(resultSet.getInt("experience"));
                pl.setLevel(resultSet.getInt("level"));
                pl.setUntilNextLevel(resultSet.getInt("untilNextLevel"));

                Date date = resultSet.getDate("birthday");
                calendar.setTime(date);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR, 0);
                pl.setBirthday(calendar.getTime());
//                pl.setBirthday(date);

                pl.setBanned(resultSet.getBoolean("banned"));
                players.add(pl);

            }
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return players;
    }

    public  boolean delInDB(Long id) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();

            PreparedStatement st = connection.prepareStatement("DELETE FROM player WHERE id = ?");
            st.setString(1, String.valueOf(id));
            boolean res = (st.executeUpdate() == 1);
            connection.close();
            return res;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }



    public  boolean playerUpdateInDB(Player player) {
        try {
            Connection connection = dataSource.getConnection();
            Statement statId = connection.createStatement();


            String sql = "UPDATE player SET name=?,title=?,race=?,profession=?,experience=?," +
                    "level=?,untilNextLevel=?,birthday=?,banned=? " +
                    " WHERE id=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, player.getName());
            statement.setString(2, player.getTitle());
            statement.setString(3, player.getRace().toString());
            statement.setString(4, player.getProfession().toString());
            statement.setInt(5, player.getExperience());
            statement.setInt(6, player.getLevel());
            statement.setInt(7, player.getUntilNextLevel());
            statement.setString(8, new SimpleDateFormat("YYYY-MM-dd").format(player.getBirthday()));
            statement.setBoolean(9, player.getBanned());

            statement.setLong(10, player.getId());
            boolean res = (statement.executeUpdate() == 1);
            connection.close();
            return res;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Player> readAll(Map<String, String> requestParams) {
        //БЕЗ SQL
        //List<Player> allPlayers = new ArrayList<>(PLAYER_REPOSITORY_MAP.values());
        List<Player> allPlayers = listFromDB("SELECT * FROM player");
//        if (requestParams.size() == 0)
//            return allPlayers;

        String name = parseVal("name", requestParams, String.class);
        String title = parseVal("title", requestParams, String.class);
        Race race = parseVal("race", requestParams, Race.class);
        Profession profession = parseVal("profession", requestParams, Profession.class);
        Long after = parseVal("after", requestParams, Long.class);
        Long before = parseVal("before", requestParams, Long.class);
        Boolean banned = parseVal("banned", requestParams, Boolean.class);
        Integer minExperience = parseVal("minExperience", requestParams, Integer.class);
        Integer maxExperience = parseVal("maxExperience", requestParams, Integer.class);
        Integer minLevel = parseVal("minLevel", requestParams, Integer.class);
        Integer maxLevel = parseVal("maxLevel", requestParams, Integer.class);
        PlayerOrder order = parseVal("order", requestParams, PlayerOrder.class);

        List<Player> players = new ArrayList<>();
        for (Player pl : allPlayers) {
            if (name != null && !pl.getName().contains(name) ||
                    title != null && !pl.getTitle().contains(title) ||
                    race != null && !pl.getRace().equals(race) ||
                    profession != null && !pl.getProfession().equals(profession) ||
                    banned != null && !pl.getBanned().equals(banned) ||
                    minExperience != null && pl.getExperience() < minExperience ||
                    maxExperience != null && pl.getExperience() > maxExperience ||
                    minLevel != null && pl.getLevel() < minLevel ||
                    maxLevel != null && pl.getLevel() > maxLevel ||
                    after != null && pl.getBirthday().before(new Date(after)) ||
                    before != null && pl.getBirthday().after(new Date(before))
            )
                continue;

            players.add(pl);
        }
        //сортировка
        if (players.size() != 0)
            if (order == null || order.equals(PlayerOrder.ID))
                players.sort((p1, p2) -> (int) (p1.getId() - p2.getId()));
            else if (order.equals(PlayerOrder.LEVEL))
                players.sort((p1, p2) -> (int) (p1.getLevel() - p2.getLevel()));
            else if (order.equals(PlayerOrder.EXPERIENCE))
                players.sort((p1, p2) -> (int) (p1.getExperience() - p2.getExperience()));
            else if (order.equals(PlayerOrder.NAME))
                players.sort((p1, p2) -> (int) (p1.getName().compareTo(p2.getName())));
            else if (order.equals(PlayerOrder.BIRTHDAY))
                players.sort((p1, p2) -> (int) (p1.getBirthday().compareTo(p2.getBirthday())));

        return players;
    }



    @Override
    public Player update(Player player, Long id) {

        Player playerFind = read(id);
        if (playerFind != null) {
            if (player.getName() != null)
                playerFind.setName(player.getName());
            if (player.getTitle() != null)
                playerFind.setTitle(player.getTitle());
            if (player.getRace() != null)
                playerFind.setRace(player.getRace());
            if (player.getProfession() != null)
                playerFind.setProfession(player.getProfession());
            if (player.getBirthday() != null) {
                playerFind.setBirthday(player.getBirthday());
            }
            if (player.getBanned() != null)
                playerFind.setBanned(player.getBanned());
            if (player.getExperience() != null)
                playerFind.setExperience(player.getExperience());
            update_Level_UntilNextLevel(playerFind);
            if (playerUpdateInDB(playerFind))
                return playerFind;
//            if (!checkPlayer(player))
//                return false;
            //PLAYER_REPOSITORY_MAP.put(id,player);
        }
        return null;
    }

    @Override
    public boolean delete(Long id) {
        //БЕЗ SQL
        //return PLAYER_REPOSITORY_MAP.remove(id) != null;

        return delInDB(id);
    }

    @Override
    public Player read(Long id) {
        //БЕЗ SQL
//        return PLAYER_REPOSITORY_MAP.get(id);

        List<Player> allPlayers = listFromDB("SELECT * FROM player where id = " + id);
        return allPlayers.size() == 0 ? null : allPlayers.get(0);

    }

    @Override
    public int readAllCount(Map<String, String> requestParams) {
        return readAll(requestParams).size();
    }

    public static void update_Level_UntilNextLevel(Player player) {
        final Integer level = (int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100);
        player.setLevel(level);

        final Integer nextLevel = 50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience();
        player.setUntilNextLevel(nextLevel);
    }

    public static boolean checkPlayer(Player player) {
        Date dataBegin = (new GregorianCalendar(2000, 0, 1)).getTime();//2000,1,1
        Date dataEnd = (new GregorianCalendar(3000, 0, 1)).getTime(); //3000,1,1
        //указаны не все параметры
        if (player.getName() == null || player.getName().isEmpty() || player.getName().length() > 12 ||
                player.getTitle() == null || player.getTitle().length() > 30 ||
                player.getRace() == null || player.getProfession() == null ||
                player.getBirthday() == null || player.getBirthday().before(dataBegin) ||
                player.getBirthday().after(dataEnd) || player.getBirthday().getTime() < 0 ||
                player.getExperience() == null || player.getExperience() < 0 || player.getExperience() > 10000000
        )
            return false;
        if (player.getBanned() == null)
            player.setBanned(false);
        return true;

    }

    public static boolean checkPlayerForUpdate(Player player) {
        Date dataBegin = (new GregorianCalendar(2000, 0, 1)).getTime();//2000,1,1
        Date dataEnd = (new GregorianCalendar(3000, 0, 1)).getTime(); //3000,1,1

        if (player.getName() != null && (player.getName().isEmpty() || player.getName().length() > 12) ||
                player.getTitle() != null && player.getTitle().length() > 30 ||

                player.getBirthday() != null && (player.getBirthday().before(dataBegin) ||
                        player.getBirthday().after(dataEnd) || player.getBirthday().getTime() < 0) ||

                player.getExperience() != null && (player.getExperience() < 0 || player.getExperience() > 10000000)
        )
            return false;

        return true;

    }

    public static long idValide(String id) {
        long res = -1;
        try {
            res = Long.parseLong(id, 10);
            if (res <= 0)
                res = -1;
        } catch (Exception e) {
            return -1;
        }

        return res;
    }
}
