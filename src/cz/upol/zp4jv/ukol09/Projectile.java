package cz.upol.zp4jv.ukol09;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;


public class Projectile {
    private IntegerProperty row;
    private IntegerProperty col;
    private BooleanProperty isActive;
    private Direction direction;
    private ProjectileStatus status;

    public Projectile(int row, int col, Direction direction, ProjectileStatus stat) {
        this.row = new SimpleIntegerProperty(row);
        this.col = new SimpleIntegerProperty(col);
        isActive = new SimpleBooleanProperty(true);
        this.direction = direction;
        status = stat;
    }

    // Metody pro přístup k řádku a sloupci
    // Řádek

    public IntegerProperty rowProperty() {
        return row;
    }

    public int getRow() {
        return row.get();
    }

    public void setRow(int row) {
        this.row.set(row);
    }

    // Sloupec

    public IntegerProperty colProperty() {
        return col;
    }

    public int getCol() {
        return col.get();
    }

    public void setCol(int col) {
        this.col.set(col);
    }

    // Přístup k propertě isActive

    public BooleanProperty isActiveProperty() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive.set(isActive);
    }

    public boolean getIsActive() {
        return this.isActive.get();
    }


    // Metoda update pro posun projektilu
    public void update() {
        if(direction == Direction.UP) {
            setRow(getRow() -1);
        }
        else {
            setRow(getRow() + 1);
        }
    }

    // Pokud projektili nikoho netrefi musí zmizet
    public void projectileGone() {
        setIsActive(false);
    }

    // Status
    public ProjectileStatus getStatus() {
        return status;
    }
}
