package cz.upol.zp4jv.ukol09;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.*;

public class GameState {
    // Konstanty

    // Počet sloupců nepřátelských lodí
    public static final int ENEMY_COLS = 10;

    // Počet řad nepřátelských lodí
    public static final int ENEMY_ROWS = 7;

    // Všechny mezery -> 1 mezera mezi nepřátelskými sloupci
    public static final int GAPS = ENEMY_COLS - 1;

    // Prostor na manevrovatelnost
    public static final int LEFT_PLACE_FOR_FLYING = 5;
    public static final int RIGHT_PLACE_FOR_FLYING = 5;

    // Počet řádků celé aplikace včetně řádků nepřátelských lodí
    public static final int ROWS = ENEMY_ROWS * 4;

    // Počet sloupců celé aplikace -> nepřátelské lodě + mezery mezi nimi + prostor na manévry lodí
    public static final int COLUMNS = ENEMY_COLS + RIGHT_PLACE_FOR_FLYING + LEFT_PLACE_FOR_FLYING + GAPS;

    // Řádek, kde se bude nacházet hráčova loď
    public static final int PLAYER_ROW = ROWS - 1;

    // Hranice, která určuje jestli nepřítel vystřelí či ne
    public static final int SHOOT_LIMIT = 99;

    // Vlastnosti
    // Seznam všech nepřátelských lodí
    private final List<EnemyShip> enemies;

    // Projektily
    private final List<Projectile> projectiles;

    // Pozice začátku hráčovi lodi
    private final IntegerProperty playerPos;

    // Score
    private final IntegerProperty score;

    // Aktivita hry
    private final BooleanProperty isActive;

    // Konstruktor
    public GameState() {
        score = new SimpleIntegerProperty(0);
        isActive = new SimpleBooleanProperty(false);  // Ze začítku je hra neaktivní
        playerPos = new SimpleIntegerProperty(COLUMNS / 2);
        projectiles = new ArrayList<>(); // Nemají počáteční hodnotu -> budou se přidávat

        // Inicializace nepřátelských lodí
        enemies = new ArrayList<>();
        for(int row = 1; row <= ENEMY_ROWS; row++) {
            for(int col = 0; col < ENEMY_COLS; col++) {
                enemies.add(new EnemyShip(row, LEFT_PLACE_FOR_FLYING + col * 2));
            }
        }
    }

    // Metody

    // Reinicializace hry
    public void reinitialize() {
        for(int i = 0; i < enemies.size(); i++) {
            enemies.get(i).setIsActive(true);
        }
        setDefaultValues();
    }

    // Nastavení hráče do výchozí polohy + odstranění projektilů
    public void setDefaultValues() {
        projectiles.forEach(p -> p.projectileGone());
        projectiles.clear();
        setScore(0);
        setPlayerPos(COLUMNS / 2);
        setIsActive(false);
    }

    // Metoda update -> řeší zmizení a posun lodí či projektilů
    public boolean update() {

        // Vytvoříme si random object pro generování nepřátelských projektilů
        Random rand = new Random();

        // Posunu střely
        moveProjectiles();

        // Zjistím, zda se mohou nepřátele posunovat v daném směru, pokud ne změním směr všech
        if(enemyIsBlocked()) {
            changeDirecition();
        }

        // Posun nepřátel + zjištění zda do nich nenarazil projektil hráče pouze tehdy, pokud je nepřítel aktivní
        for(int index = 0; index < enemies.size(); index++) {
            EnemyShip s = enemies.get(index);
            s.update();
            // Ošetření kolizí
            if (s.getIsActive()) {
                Optional<Projectile> colision = projectiles.stream()
                        .filter(p -> p.getCol() == s.getCol())
                        .filter(p -> p.getRow() == s.getRow())
                        .filter(p -> p.getStatus() == ProjectileStatus.PLAYER)
                        .findFirst();
                // Pokud se taková střela našla, došlo ke kolizi a musíme střelu i nepřítele uvést jako neaktivní
                // Střelu taktéž odeberu ze seznamu a zvýším hráči score
                if (colision.isPresent()) {
                    projectiles.remove(colision.get());
                    colision.get().projectileGone();
                    s.shipDestroyed();
                    setScore(getScore() + 1);
                }
                // Pokud zasáhlá nebyla, musím dát šanci nepříteli vystřelit
                else {
                    int chance = rand.nextInt(100);
                    if(chance >= SHOOT_LIMIT) {
                        projectiles.add(s.enemyShooted());
                    }
                }
            }
        }
        // Zbývá ošetřit zda nenastal zásah od nepřítele
        return checkPlayer();
    }

    // Změna směru všech nepřátel
    public void changeDirecition() {
        for(int i = 0; i < enemies.size(); i++) {
            enemies.get(i).toggleDirection();
        }
    }

    // Posun projektilů
    private void moveProjectiles() {
        for(int i = 0; i < projectiles.size(); i++) {
            Projectile p = projectiles.get(i);
            p.update();
            if(p.getRow() == 0) {
                projectiles.remove(p);
                p.projectileGone();
            }
        }
    }

    // Metoda pro zjištění, zda mohou neořátelé postupovat i do strany
    private boolean enemyIsBlocked() {
        EnemyShip firstInRow = enemies.get(0);
        EnemyShip lastInRow = enemies.get(ENEMY_ROWS * ENEMY_COLS - 1);
        if(firstInRow.getCol() == 0 || lastInRow.getCol() == COLUMNS - 1) {
            return true;
        }
        else {
            return false;
        }
    }

    // Kontrola hráče
    public boolean checkPlayer() {
        // Opět budeme řešit kolizi
        Optional<Projectile> colision = projectiles.stream()
                .filter(p -> p.getRow() == PLAYER_ROW)
                .filter(p -> p.getCol() == getPlayerPos())
                .filter(p -> p.getStatus() == ProjectileStatus.ENEMY)
                .findFirst();
        // Pokud takový střela existuje určím, že hra není aktivní a vrátím false projektil zmizí
        if(colision.isPresent()) {
                colision.get().projectileGone();
                projectiles.remove(colision.get());
                setIsActive(false);
                return false;
        }
        // Pokud už žádný nepřítel není aktivní, tak hra skončila
        else if(enemies.stream().filter(e -> e.getIsActive() == true).count() == 0) {
            setIsActive(false);
            return false;
        }
        // Pokud se ani jeden netrefil, tak odstraním všechny, které jsou mimo plochu
        else {
            for(int i = 0; i < projectiles.size(); i++) {
                Projectile p = projectiles.get(i);
                if((p.getRow() == ROWS) || (p.getRow() == 0)) {
                    p.projectileGone();
                    projectiles.remove(p);
                }
            }
        }
        return true;
    }

    // Metody pro ovladatelnost hráče
    // Posun doleva
    public void moveLeft() {
        if(!getIsActive()) {
            return;
        }
        int playerCol = getPlayerPos();
        if(playerCol > 0) {
            setPlayerPos(playerCol - 1 );
        }
    }

    // Posun doprava
    public void moveRight() {
        if(!getIsActive()) {
            return;
        }
        int playerCol = getPlayerPos();
        if(playerCol < COLUMNS - 1) {
            setPlayerPos(playerCol + 1 );
        }
    }

    // Střelba
    public void playerShooted() {
        Projectile newProjectile = new Projectile(PLAYER_ROW - 1, getPlayerPos(), Direction.UP, ProjectileStatus.PLAYER);
        projectiles.add(newProjectile);
    }

    // Metody pro získání vlastoností
    // Score
    public IntegerProperty scoreProperty() {
        return score;
    }

    public int getScore() {
        return score.get();
    }

    public void setScore(int value) {
        score.set(value);
    }

    // Activita
    public BooleanProperty isActiveProperty() {
        return isActive;
    }

    public boolean getIsActive() {
        return isActive.get() ;
    }

    public void setIsActive(boolean value) {
        isActive.set(value);
    }

    // Pozice hráče
    public IntegerProperty playerPosProperty() {
        return playerPos;
    }

    public int getPlayerPos() {
        return playerPos.get();
    }

    public void setPlayerPos(int value) {
        playerPos.set(value);
    }

    // Seznam nepřátel
    public List<EnemyShip> getEnemies() {
        return enemies;
    }

    // Seznam projektilů

    public List<Projectile> getProjectiles() {
        return projectiles;
    }
}
