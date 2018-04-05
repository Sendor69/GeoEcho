package model.client;

/**
 * Created by Dani on 05/04/2018.
 */

public abstract class Query extends Packet{

    private float coordX;
    private float coordY;

    /**
     * Getter coordX
     * @return Retorna les coordenades
     */
    public float getCoordX() {
        return coordX;
    }

    /**
     * Setter coordX
     * @param coordX Coordenades del ount
     */
    public void setCoordX(float coordX) {
        this.coordX = coordX;
    }
    /**
     * Getter coordY
     * @return Retorna les coordenades
     */
    public float getCoordY() {
        return coordY;
    }

    /**
     * Setter coordY
     * @param coordY Coordenades del ount
     */
    public void setCoordY(float coordY) {
        this.coordY = coordY;
    }

}
