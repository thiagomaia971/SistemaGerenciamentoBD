package testes;

import entidades.GerenciadorArquivo;
import entidades.blocos.BlocoContainer;
import interfaces.IFileManager;
import org.junit.Test;
import utils.GlobalVariables;

import java.io.IOException;

public class ComoGerenciadorDeArquivoPossoCriarUmArquivo {
    @Test
    public void GravarArquivo() throws IOException {
        IFileManager gerenciadorArquivo = new GerenciadorArquivo();
        BlocoContainer container = gerenciadorArquivo.criarContainerPeloArquivoEntrada(GlobalVariables.LOCAL_ARQUIVO_ENTRADA + GlobalVariables.ARQUIVO_ENTRADA_MENOR);

        gerenciadorArquivo.gravarArquivo(container, GlobalVariables.LOCAL_ARQUIVO_FINAL);
    }

    @Test
    public void GravarArquivoCompleto() throws IOException {
        IFileManager gerenciadorArquivo = new GerenciadorArquivo();
        BlocoContainer container = gerenciadorArquivo.criarContainerPeloArquivoEntrada(GlobalVariables.LOCAL_ARQUIVO_ENTRADA + GlobalVariables.ARQUIVO_ENTRADA);

        gerenciadorArquivo.gravarArquivo(container, GlobalVariables.LOCAL_ARQUIVO_FINAL);
    }
}