package ui;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class UiSupportMoedaTest {
    @Test void formatacaoMonetariaPermaneceCorretaEmChamadasConcorrentes() throws Exception {
        double[] valores = {1234.56, 0, 987654.32, 19.90};
        String[] esperado = {"R$\u00a01.234,56", "R$\u00a00,00", "R$\u00a0987.654,32", "R$\u00a019,90"};
        CountDownLatch iniciar = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
            var tarefas = new ArrayList<Future<?>>();
            for (int thread = 0; thread < 8; thread++) {
                final int deslocamento = thread;
                tarefas.add(pool.submit(() -> {
                    try { assertTrue(iniciar.await(5, TimeUnit.SECONDS)); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
                    for (int i = 0; i < 3000; i++) {
                        int indice = (i + deslocamento) % valores.length;
                        assertEquals(esperado[indice], UiSupport.moeda(valores[indice]));
                    }
                }));
            }
            iniciar.countDown();
            for (Future<?> tarefa : tarefas) tarefa.get(15, TimeUnit.SECONDS);
        }
    }
}
