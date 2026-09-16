package model;

import java.text.Normalizer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ProdutoCatalogo {

    private final String idExterno;
    private final String nome;
    private final String fabricante;
    private final Double precoUsd;
    private final Map<String, String> detalhes;

    public ProdutoCatalogo(
            String idExterno,
            String nome,
            String fabricante,
            Double precoUsd,
            Map<String, String> detalhes
    ) {
        this.idExterno = idExterno;
        this.nome = nome;
        this.fabricante = fabricante;
        this.precoUsd = precoUsd;
        this.detalhes = Collections.unmodifiableMap(new LinkedHashMap<>(detalhes));
    }

    public String getIdExterno() {
        return idExterno;
    }

    public String getNome() {
        return nome;
    }

    public String getFabricante() {
        return fabricante;
    }

    public Double getPrecoUsd() {
        return precoUsd;
    }

    public Map<String, String> getDetalhes() {
        return detalhes;
    }

    public String buscarDetalhe(String... termos) {
        for (String termo : termos) {
            String procurado = normalizar(termo);
            for (Map.Entry<String, String> detalhe : detalhes.entrySet()) {
                String nomeDetalhe = normalizar(detalhe.getKey());
                if (nomeDetalhe.equals(procurado) || nomeDetalhe.contains(procurado)) {
                    return detalhe.getValue() == null ? "" : detalhe.getValue();
                }
            }
        }
        return "";
    }

    private String normalizar(String valor) {
        return Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
