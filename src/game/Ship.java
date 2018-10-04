package game;

import java.io.Serializable;

public class Ship implements Serializable {
private int cellsCount;

public Ship(int cells) {
	cellsCount=cells;
}
public void decreaseCells() {
cellsCount--;
}

public int getCellsCount() {
	return cellsCount;
}

public boolean isDestroyed() {
	return cellsCount==0;
}

}
