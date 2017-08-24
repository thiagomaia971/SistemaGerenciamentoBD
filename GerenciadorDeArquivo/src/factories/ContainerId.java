package factories;

import interfaces.IBinary;
import utils.ByteArrayUtils;

public class ContainerId implements IBinary{

    private int id;

    public  ContainerId(){ }

    private ContainerId(int id) {
        this.id = id;
    }

    public static  ContainerId create() {
        return new ContainerId();
    }

    public static ContainerId create(int id) {
        return new ContainerId(id);
    }

    @Override
    public byte[] toByteArray() {
        return new byte[1];
    }

    @Override
    public ContainerId fromByteArray(byte[] byteArray) {
        int _containerId = ByteArrayUtils.byteArrayToInt(byteArray);
        return this.create(_containerId);
    }

}