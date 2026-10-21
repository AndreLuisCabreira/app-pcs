package service;

import model.*;
import java.text.Normalizer;
import java.util.Locale;

public final class CatalogoFiltro {
    private CatalogoFiltro() { }

    public static boolean corresponde(Componente item, String pesquisa, double minimo, double maximo) {
        if (item == null || !Double.isFinite(item.getPreco())
                || item.getPreco() < minimo || item.getPreco() > maximo) return false;
        String detalhes = item.getNome() + " " + item;
        if (item instanceof Processador cpu) detalhes += " " + cpu.getFabricante() + " " + cpu.getNucleos() + " núcleos " + cpu.getConsumo() + "W";
        if (item instanceof PlacaMae pm) detalhes += " " + pm.getFabricante() + " " + pm.getConsumo() + "W";
        if (item instanceof PlacaVideo gpu) detalhes += " " + gpu.getFabricante() + " " + gpu.getMemoria() + "GB " + gpu.getConsumo() + "W";
        String alvo = normalizar(detalhes);
        for (String termo : normalizar(pesquisa).split("\\s+")) {
            if (!alvo.contains(termo)) return false;
        }
        return true;
    }

    private static String normalizar(String valor) {
        return Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }
}
