package service;

import model.*;
import java.util.ArrayList;
import java.util.List;

/** Draft of a single build; navigation never persists data or modifies the original build. */
public final class MontagemPC {
    public enum Etapa {
        PROCESSADOR("Processador"), PLACA_MAE("Placa-mãe"), MEMORIA("Memória"),
        PLACA_VIDEO("Placa de vídeo"), SSD("SSD"), FONTE("Fonte"), REVISAO("Revisão");
        private final String titulo;
        Etapa(String titulo) { this.titulo = titulo; }
        public String titulo() { return titulo; }
    }

    public record Catalogo(List<Processador> processadores, List<PlacaMae> placasMae,
                           List<Memoria> memorias, List<PlacaVideo> placasVideo,
                           List<SSD> ssds, List<Fonte> fontes) {
        public Catalogo {
            processadores = List.copyOf(processadores); placasMae = List.copyOf(placasMae);
            memorias = List.copyOf(memorias); placasVideo = List.copyOf(placasVideo);
            ssds = List.copyOf(ssds); fontes = List.copyOf(fontes);
        }
    }

    private final int usuarioId;
    private final ConsumoService consumo = new ConsumoService();
    private Build rascunho;
    private Etapa etapa = Etapa.PROCESSADOR;
    private boolean modificado;

    public MontagemPC(int usuarioId) {
        if (usuarioId <= 0) throw new IllegalArgumentException("Entre na conta para montar seu PC.");
        this.usuarioId = usuarioId;
        this.rascunho = new Build("", usuarioId);
    }

    public void editar(Build original) {
        if (original == null || original.getUsuarioId() != usuarioId) {
            throw new SecurityException("Esta build não pertence à conta atual.");
        }
        rascunho = copiar(original);
        etapa = Etapa.values()[primeiraIncompleta()];
        modificado = false;
    }

    public Etapa etapa() { return etapa; }
    public boolean modificado() { return modificado; }
    public boolean editando() { return rascunho.getId() > 0; }
    public Build parcial() { return copiar(rascunho); }
    public int fonteRecomendada() { return consumo.consumoRecomendado(rascunho); }

    public Componente selecionado(Etapa e) {
        return switch (e) {
            case PROCESSADOR -> rascunho.getProcessador();
            case PLACA_MAE -> rascunho.getPlacaMae();
            case MEMORIA -> rascunho.getMemoria();
            case PLACA_VIDEO -> rascunho.getPlacaVideo();
            case SSD -> rascunho.getSsd();
            case FONTE -> rascunho.getFonte();
            case REVISAO -> null;
        };
    }

    public List<? extends Componente> opcoes(Catalogo c) {
        return switch (etapa) {
            case PROCESSADOR -> c.processadores();
            case PLACA_MAE -> c.placasMae().stream().filter(p -> rascunho.getProcessador() != null
                    && igual(p.getSocket(), rascunho.getProcessador().getSocket())).toList();
            case MEMORIA -> c.memorias().stream().filter(m -> rascunho.getPlacaMae() != null
                    && igual(m.getTipo(), rascunho.getPlacaMae().getTipoMemoria())).toList();
            case PLACA_VIDEO -> c.placasVideo();
            case SSD -> c.ssds();
            case FONTE -> c.fontes().stream().filter(f -> f.getPotencia() >= fonteRecomendada()).toList();
            case REVISAO -> List.of();
        };
    }

    /** Returns only the names of selections invalidated by this change. */
    public List<String> selecionar(Componente item) {
        if (item == null || !aceitaTipo(item)) throw new IllegalArgumentException("Escolha uma peça desta etapa.");
        if (etapa == Etapa.PLACA_MAE && (rascunho.getProcessador() == null
                || !igual(((PlacaMae)item).getSocket(), rascunho.getProcessador().getSocket()))) {
            throw new IllegalArgumentException("A placa-mãe precisa ter o mesmo socket do processador.");
        }
        if (etapa == Etapa.MEMORIA && (rascunho.getPlacaMae() == null
                || !igual(((Memoria)item).getTipo(), rascunho.getPlacaMae().getTipoMemoria()))) {
            throw new IllegalArgumentException("Escolha uma memória compatível com a placa-mãe.");
        }
        if (etapa == Etapa.FONTE && ((Fonte)item).getPotencia() < fonteRecomendada()) {
            throw new IllegalArgumentException("Escolha uma fonte de pelo menos " + fonteRecomendada() + " W.");
        }
        if (selecionado(etapa) != item) modificado = true;
        switch (etapa) {
            case PROCESSADOR -> rascunho.setProcessador((Processador)item);
            case PLACA_MAE -> rascunho.setPlacaMae((PlacaMae)item);
            case MEMORIA -> rascunho.setMemoria((Memoria)item);
            case PLACA_VIDEO -> rascunho.setPlacaVideo((PlacaVideo)item);
            case SSD -> rascunho.setSsd((SSD)item);
            case FONTE -> rascunho.setFonte((Fonte)item);
            default -> throw new IllegalArgumentException("Selecione uma etapa de componentes.");
        }
        List<String> removidas = new ArrayList<>();
        if (rascunho.getPlacaMae() != null && rascunho.getProcessador() != null
                && !igual(rascunho.getPlacaMae().getSocket(), rascunho.getProcessador().getSocket())) {
            remover(Etapa.PLACA_MAE, removidas);
            if (rascunho.getMemoria() != null) remover(Etapa.MEMORIA, removidas);
        }
        if (rascunho.getMemoria() != null && rascunho.getPlacaMae() != null
                && !igual(rascunho.getMemoria().getTipo(), rascunho.getPlacaMae().getTipoMemoria())) {
            remover(Etapa.MEMORIA, removidas);
        }
        if (rascunho.getFonte() != null && !consumo.fonteSuporta(rascunho)) remover(Etapa.FONTE, removidas);
        return List.copyOf(removidas);
    }

    public void irPara(Etapa destino) {
        if (!podeIrPara(destino)) throw new IllegalStateException("Conclua as escolhas anteriores primeiro.");
        etapa = destino;
    }
    public boolean podeIrPara(Etapa destino) { return destino != null && destino.ordinal() <= primeiraIncompleta(); }
    public boolean podeAvancar() { return etapa != Etapa.REVISAO && etapa.ordinal() < primeiraIncompleta(); }
    public void avancar() {
        if (!podeAvancar()) throw new IllegalStateException("Selecione uma peça para continuar.");
        irPara(Etapa.values()[etapa.ordinal() + 1]);
    }
    public void voltar() { if (etapa.ordinal() > 0) etapa = Etapa.values()[etapa.ordinal() - 1]; }

    public void definirNome(String nome) {
        String valor = nome == null ? "" : nome;
        if (!valor.equals(rascunho.getNome())) modificado = true;
        rascunho.setNome(valor);
    }
    public void definirFavorita(boolean favorita) {
        if (favorita != rascunho.isFavorita()) modificado = true;
        rascunho.setFavorita(favorita);
    }
    public Build finalizar() {
        if (primeiraIncompleta() < Etapa.REVISAO.ordinal()) throw new IllegalArgumentException("Escolha os seis componentes antes de salvar.");
        if (rascunho.getNome() == null || rascunho.getNome().isBlank()) throw new IllegalArgumentException("Dê um nome para sua build.");
        if (rascunho.getNome().trim().length() > 120) throw new IllegalArgumentException("Use até 120 caracteres no nome.");
        if (!new CompatibilidadeService().verificarBuild(rascunho) || !consumo.fonteSuporta(rascunho)) {
            throw new IllegalArgumentException("Revise a compatibilidade das peças e a capacidade da fonte.");
        }
        Build resultado = copiar(rascunho); resultado.setNome(resultado.getNome().trim()); return resultado;
    }

    private int primeiraIncompleta() {
        for (Etapa e : Etapa.values()) {
            if (e == Etapa.REVISAO) break;
            if (selecionado(e) == null) return e.ordinal();
            if (e == Etapa.PLACA_MAE && !new CompatibilidadeService().verificarSocket(rascunho)) return e.ordinal();
            if (e == Etapa.MEMORIA && !new CompatibilidadeService().verificarMemoria(rascunho)) return e.ordinal();
            if (e == Etapa.FONTE && !consumo.fonteSuporta(rascunho)) return e.ordinal();
        }
        return Etapa.REVISAO.ordinal();
    }
    private boolean aceitaTipo(Componente c) {
        return switch (etapa) {
            case PROCESSADOR -> c instanceof Processador; case PLACA_MAE -> c instanceof PlacaMae;
            case MEMORIA -> c instanceof Memoria; case PLACA_VIDEO -> c instanceof PlacaVideo;
            case SSD -> c instanceof SSD; case FONTE -> c instanceof Fonte; default -> false;
        };
    }
    private void remover(Etapa e, List<String> removidas) {
        removidas.add(e.titulo());
        switch (e) {
            case PLACA_MAE -> rascunho.setPlacaMae(null); case MEMORIA -> rascunho.setMemoria(null);
            case FONTE -> rascunho.setFonte(null); default -> throw new IllegalArgumentException();
        }
    }
    private static boolean igual(String a, String b) { return a != null && b != null && a.equalsIgnoreCase(b); }
    private static Build copiar(Build b) {
        return new Build(b.getId(), b.getNome(), b.getUsuarioId(), b.getProcessador(), b.getPlacaMae(),
                b.getMemoria(), b.getPlacaVideo(), b.getSsd(), b.getFonte(), b.isFavorita());
    }
}
