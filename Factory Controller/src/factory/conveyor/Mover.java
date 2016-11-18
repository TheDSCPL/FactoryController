package factory.conveyor;

public class Mover extends Conveyor {

    /**
     * @param id name/id of the mover
     * @param length Length of the mover
     */
    public Mover(String id, int length) {
        super(id, length, 2);
    }

    @Override
    protected boolean transferMotorDirection(Conveyor partner, boolean sending) {
        if (sending) {
            return partner == connections[0];
        }
        else {
            return partner == connections[1];
        }
    }

    @Override
    protected void blockTransferFinished() {
        
    }

    @Override
    protected boolean isBlockTransferPossible() {
        return true;
    }

    @Override
    protected void blockTransferPrepare() {
        
    }

    @Override
    protected boolean isBlockTransferReady() {
        return true;
    }

}
