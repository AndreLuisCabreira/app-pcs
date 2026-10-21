package service;

import model.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Standalone exports: no account credentials, external assets or executable content. */
public final class BuildExportService {
    private final ConsumoService consumo = new ConsumoService();

    public void exigirProprietario(Build build, Usuario usuario) {
        if (build == null) throw new IllegalArgumentException("Selecione uma build.");
        if (usuario == null || usuario.getId() <= 0 || build.getUsuarioId() != usuario.getId()) {
            throw new SecurityException("Esta build não pertence à conta atual.");
        }
    }

    public Map<String, String> resumo(Build b) {
        Map<String, String> linhas = new LinkedHashMap<>();
        linhas.put("Processador", nome(b.getProcessador()));
        linhas.put("Placa-mãe", nome(b.getPlacaMae()));
        linhas.put("Placa de vídeo", nome(b.getPlacaVideo()));
        linhas.put("Memória", nome(b.getMemoria()));
        linhas.put("SSD", nome(b.getSsd()));
        linhas.put("Fonte", nome(b.getFonte()));
        linhas.put("Preço total", moeda(b.getPrecoTotal()));
        linhas.put("Consumo estimado", consumo.calcularConsumo(b) + " W");
        linhas.put("Fonte recomendada", consumo.consumoRecomendado(b) + " W");
        linhas.put("Socket e DDR compatíveis", new CompatibilidadeService().verificarBuild(b) ? "Sim" : "Não");
        linhas.put("Fonte suficiente", consumo.fonteSuporta(b) ? "Sim" : "Não");
        return linhas;
    }

    public String texto(Build b, Usuario usuario) {
        exigirProprietario(b, usuario);
        StringBuilder s = new StringBuilder("IntraTech — " + b.getNome() + "\nExportado em " + LocalDate.now() + "\n\n");
        resumo(b).forEach((k,v) -> s.append(k).append(": ").append(v).append('\n'));
        return s.append("\nPreços cadastrados, sem cotação em tempo real. Consumo estimado.\n").toString();
    }

    public String html(List<Build> builds, Usuario usuario) {
        if (builds == null || builds.isEmpty()) throw new IllegalArgumentException("Selecione uma build.");
        builds.forEach(b -> exigirProprietario(b, usuario));
        StringBuilder s = new StringBuilder("""
                <!doctype html><html lang="pt-BR"><meta charset="utf-8">
                <meta name="viewport" content="width=device-width"><title>IntraTech — Builds</title>
                <style>body{font:16px system-ui;margin:32px;color:#17212d}table{border-collapse:collapse;width:100%}
                td,th{padding:12px;border:1px solid #ccd2dc;text-align:left;overflow-wrap:anywhere}th{background:#eee}
                p{color:#566} @media print{body{margin:12mm}tr{break-inside:avoid}}</style>
                <h1>IntraTech — Builds</h1><p>Exportado em
                """).append(LocalDate.now()).append("</p><table><thead><tr><th>Item</th>");
        builds.forEach(b -> s.append("<th>").append(escapar(b.getNome())).append("</th>"));
        s.append("</tr></thead><tbody>");
        Map<String,String> primeira = resumo(builds.getFirst());
        for (String chave : primeira.keySet()) {
            s.append("<tr><th>").append(escapar(chave)).append("</th>");
            for (Build b : builds) s.append("<td>").append(escapar(resumo(b).get(chave))).append("</td>");
            s.append("</tr>");
        }
        return s.append("</tbody></table><p>Preços cadastrados, sem cotação em tempo real. Consumo estimado; compatibilidade limitada a socket, DDR e fonte. Abra este arquivo no navegador e use Imprimir → Salvar como PDF.</p></html>").toString();
    }

    private static String nome(Componente c) { return c == null ? "Não informado" : c.getNome() + " — " + moeda(c.getPreco()); }
    public static String moeda(double valor) { return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor); }
    private static String escapar(String s) { return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); }
}
