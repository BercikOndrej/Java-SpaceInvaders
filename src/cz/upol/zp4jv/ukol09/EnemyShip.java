package cz.upol.zp4jv.ukol09;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class EnemyShip {
    private IntegerProperty row;
    private IntegerProperty col;
    private BooleanProperty isActive;
    private Direction direction = Direction.RIGHT;

    public EnemyShip(int row, int col) {
        this.row = new SimpleIntegerProperty(row);
        this.col = new SimpleIntegerProperty(col);
        isActive = new SimpleBooleanProperty(true);
    }

    // Metody pro přístup k propertám
    // Řádek
    public IntegerProperty rowProperty() {
        return row;
    }

    public void setRow(int row) {
        this.row.set(row);
    }

    public int getRow() {
        return row.get();
    }

    // Sloupec
    public IntegerProperty colProperty() {
        return col;
    }

    public void setCol(int col) {
        this.col.set(col);
    }

    public int getCol() {
        return col.get();
    }

    // Aktivita
    public BooleanProperty isActiveProperty() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive.set(isActive);
    }

    public boolean getIsActive() {
        return this.isActive.get();
    }

    // Směr pohybu
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    // Metoda pro zničení lodě
    public void  shipDestroyed() {
        setIsActive(false);
    }

    // Metoda update pro pohyb lodě
    public void update() {
        if(direction == Direction.RIGHT) {
            setCol(getCol() + 1);
        }
        else {
            setCol(getCol() - 1);
        }
    }

    // Změna  pohybu lodě
    public void toggleDirection() {
        if(direction == Direction.RIGHT) {
            setDirection(Direction.LEFT);
        }
        else {
            setDirection(Direction.RIGHT);
        }
    }

    // Metoda výstřelu
    public Projectile enemyShooted() {
        return new Projectile(getRow() + 1, getCol(), Direction.DOWN, ProjectileStatus.ENEMY);
    }
}
