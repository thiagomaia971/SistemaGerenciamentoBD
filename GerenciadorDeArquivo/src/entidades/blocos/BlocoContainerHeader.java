package entidades.blocos;

import entidades.GerenciadorArquivo;
import entidades.GerenciadorDeIO;
import entidades.index.IndexFileManager;
import exceptions.ContainerNoExistent;
import factories.ContainerId;
import interfaces.IBinary;
import utils.ByteArrayConcater;
import utils.ByteArrayUtils;
import utils.GlobalVariables;

import java.awt.*;
import java.io.IOException;

public class BlocoContainerHeader implements IBinary{

    private ContainerId containerId;
    private int tamanhoDosBlocos = GlobalVariables.TAMANHO_BLOCO;
    private int statusContainer;
    private int proximoBlocoLivre = 0;
    private int tamanhoDescritor;

    BlocoContainerHeader(int containerId) {
        this.containerId = ContainerId.create(containerId);
    }
    BlocoContainerHeader(byte[] bytes) {
        this.fromByteArray(bytes);
    }

    public int getContainerId() {
        return this.containerId.getValue();
    }
    public int getTamanhoDosBlocos() {
        return this.tamanhoDosBlocos;
    }
    public int getProximoBlocoLivre() {
        return this.proximoBlocoLivre;
    }
    public void adicionarProximoBlocoLivre() {
        this.proximoBlocoLivre += tamanhoDosBlocos;
    }
    public int getTamanhoDescritor() {
        return  this.tamanhoDescritor;
    }
    public int getTamanhoDescritorFile(String path) throws IOException {
        return ByteArrayUtils.byteArrayToInt(GerenciadorDeIO.getBytes(path, 9, 2));
    }

    public void setProximoBlocoLivre(int proximoBlocoLivre) {
        this.proximoBlocoLivre = proximoBlocoLivre;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayConcater concater = new ByteArrayConcater(11);
        concater
                .concat(this.containerId.toByteArray())
                .concat(ByteArrayUtils.intTo3Bytes(this.tamanhoDosBlocos))
                .concat(ByteArrayUtils.intTo1Bytes(this.statusContainer))
                .concat(ByteArrayUtils.intToBytes(this.proximoBlocoLivre))
                .concat(ByteArrayUtils.intTo2Bytes(this.tamanhoDescritor));

        return concater.getFinalByteArray();
    }

    @Override
    public BlocoContainerHeader fromByteArray(byte[] byteArray) {
        this.containerId = ContainerId.create(byteArray[0]);
        this.tamanhoDosBlocos = ByteArrayUtils.byteArrayToInt(ByteArrayUtils.subArray(byteArray, 1, 3));
        this.statusContainer = ByteArrayUtils.byteArrayToInt(ByteArrayUtils.subArray(byteArray, 4, 1));
        this.proximoBlocoLivre = ByteArrayUtils.byteArrayToInt(ByteArrayUtils.subArray(byteArray, 5, 4));
        this.tamanhoDescritor = ByteArrayUtils.byteArrayToInt(ByteArrayUtils.subArray(byteArray, 9, 2));

        return this;
    }

    public void atualizarTamanhoDescritor(int length) {
        this.tamanhoDescritor += length;
    }
}
