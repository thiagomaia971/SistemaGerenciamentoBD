package entidades.index;

import entidades.GerenciadorDeIO;
import entidades.blocos.BlocoControle;
import entidades.blocos.Descritor;
import entidades.blocos.RowId;
import entidades.blocos.TipoDado;
import entidades.index.abstrations.IndexBlock;
import entidades.index.inner.InnerIndexBlock;
import entidades.index.leaf.LeafIndexBlock;
import exceptions.ContainerNoExistent;
import exceptions.innerBlock.IndexBlockNotFoundException;
import factories.BlocoId;
import factories.ContainerId;
import interfaces.IBinary;
import utils.ByteArrayConcater;
import utils.ByteArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IndexContainer implements IBinary {
    private int NEXT_BLOCK_ID = 1;

    private BlocoControle blocoControle;
    private IndexBlock block;


    IndexContainer(int containerId) {
        blocoControle = new BlocoControle(containerId);

    }

    public IndexContainer(byte[] containerBytes) {
        blocoControle = new BlocoControle(containerBytes);
        this.NEXT_BLOCK_ID = blocoControle.getHeader().getProximoBlocoLivre() / blocoControle.getHeader().getTamanhoDosBlocos() + 1;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayConcater bc = new ByteArrayConcater(this.getBlocoControle().getHeader().getTamanhoDosBlocos());
        bc.concat(blocoControle.toByteArray());

        return bc.getFinalByteArray();
    }

    @Override
    public IndexContainer fromByteArray(byte[] byteArray) {
        this.blocoControle.fromByteArray(byteArray);

        return this;
    }

    public BlocoControle getBlocoControle() {
        return blocoControle;
    }

    public static IndexContainer getJustContainer(ContainerId indexId) throws IOException, ContainerNoExistent {
        String diretorio = IndexFileManager.getDiretorio(indexId.getValue());
        int blockLenght = BlocoControle.getBlockLengthFile(diretorio);
        byte[] containerBytes = GerenciadorDeIO.getBytes(diretorio, 0, blockLenght);
        return new IndexContainer(containerBytes);
    }

    public int incrementNextFreeBlock() throws IOException, ContainerNoExistent {
        String indexContainer = IndexFileManager.getDiretorio(this.getBlocoControle().getContainerId());
        int old = this.getNextBlock();
        this.NEXT_BLOCK_ID = ((this.getBlocoControle().getHeader().getTamanhoDosBlocos() * old) / this.getBlocoControle().getHeader().getTamanhoDosBlocos()) + 1;
        this.blocoControle.getHeader().setProximoBlocoLivre(this.NEXT_BLOCK_ID * this.getBlocoControle().getHeader().getTamanhoDosBlocos());

        GerenciadorDeIO.atualizarBytes(indexContainer, 5, ByteArrayUtils.intToBytes(this.blocoControle.getHeader().getProximoBlocoLivre()));
        return this.NEXT_BLOCK_ID;
    }

    public static InnerIndexBlock loadInnerIndexBlock(RowId rowId) throws IOException, ContainerNoExistent, IndexBlockNotFoundException {
        byte[] blockBytes = loadIndexBlockBytes(rowId);
        return new InnerIndexBlock(blockBytes);
    }

    public static LeafIndexBlock loadLeafIndexBlock(RowId rowId) throws IOException, ContainerNoExistent, IndexBlockNotFoundException {
        byte[] blockBytes = loadIndexBlockBytes(rowId);
        return new LeafIndexBlock(blockBytes);
    }

    private static byte[] loadIndexBlockBytes(RowId rowId) throws IOException, ContainerNoExistent, IndexBlockNotFoundException {
        IndexContainer ic = getJustContainer(ContainerId.create(rowId.getContainerId()));

//        int nextFreeBlock = ic.getBlocoControle().getHeader().getProximoBlocoLivre();
//        int blockPosition = (rowId.getBlocoId() - 1) * ic.getBlocoControle().getHeader().getTamanhoDosBlocos();
//        if (nextFreeBlock < blockPosition)
//            throw new IndexBlockNotFoundException();

        String indexPath = IndexFileManager.getDiretorio(rowId.getContainerId());

        int blockLength = ic.getBlocoControle().getHeader().getTamanhoDosBlocos();
        int offset = blockLength + (blockLength * (rowId.getBlocoId() - 1));

        return GerenciadorDeIO.getBytes(indexPath, offset, blockLength);
    }

    public int getNextBlock() throws IOException, ContainerNoExistent {
        String indexContainer = IndexFileManager.getDiretorio(this.getBlocoControle().getContainerId());
        this.NEXT_BLOCK_ID = ByteArrayUtils.byteArrayToInt(GerenciadorDeIO.getBytes(indexContainer, 5, 4)) / this.getBlocoControle().getHeader().getTamanhoDosBlocos();
        return this.NEXT_BLOCK_ID;
    }

    public LeafIndexBlock getRoot() throws IOException, ContainerNoExistent, IndexBlockNotFoundException {
        // TODO
        String path = IndexFileManager.getDiretorio(this.getBlocoControle().getContainerId());
        Descritor rootDescritor = IndexContainer.getIndexDescritorsByType(ContainerId.create(this.getBlocoControle().getContainerId()), TipoDado.ROOT).get(0);
        BlocoId x = BlocoId.create(Integer.parseInt(rootDescritor.getNome()));

        int blockRoot = ByteArrayUtils.byteArrayToInt(GerenciadorDeIO.getBytes(path, BlocoControle.CONTROLLER_BLOCK_LENGTH, BlocoId.LENGTH));
        return IndexContainer.loadLeafIndexBlock(RowId.create(this.blocoControle.getContainerId(), blockRoot));
    }

    public static List<Descritor> getIndexDescritorsByType(ContainerId containerId, TipoDado collumn) throws IOException, ContainerNoExistent {
        String indexPath = IndexFileManager.getDiretorio(containerId.getValue());
        int blockLength = BlocoControle.getBlockLengthFile(indexPath);

        byte[] descritorsByte = GerenciadorDeIO.getBytes(indexPath, BlocoControle.CONTROLLER_BLOCK_LENGTH, blockLength - BlocoControle.CONTROLLER_BLOCK_LENGTH);
        return IndexContainer.getJustContainer(containerId).getBlocoControle().descritoresFromByteArray(descritorsByte).stream().filter(x -> x.getTipoDado() == TipoDado.COLLUMN).collect(Collectors.toList());
    }
}