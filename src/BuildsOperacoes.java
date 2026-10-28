import model.Build;
import java.util.List;
import java.util.Objects;

/** Separates a confirmed write from an optional refresh of the list afterwards. */
final class BuildsOperacoes {
    record Resultado(List<Build> builds, DataAccessException erroAtualizacao) {
        Resultado { builds = List.copyOf(builds); }
        boolean listaAtualizada() { return erroAtualizacao == null; }
    }

    private final BuildDAO dao;
    private final int usuarioId;

    BuildsOperacoes(BuildDAO dao, int usuarioId) {
        if (usuarioId <= 0) throw new SecurityException("Entre na sua conta.");
        this.dao = Objects.requireNonNull(dao);
        this.usuarioId = usuarioId;
    }

    Resultado salvar(Build build) {
        exigirProprietario(build);
        if (build.getId() > 0) dao.atualizarDoUsuario(build, usuarioId);
        else dao.inserir(build);
        return recarregar();
    }

    Resultado excluir(Build build) {
        exigirProprietario(build);
        if (build.getId() <= 0) throw new IllegalArgumentException("Selecione uma build salva para excluir.");
        dao.excluirDoUsuario(build.getId(), usuarioId);
        return recarregar();
    }

    private Resultado recarregar() {
        try {
            return new Resultado(dao.listarPorUsuario(usuarioId), null);
        } catch (DataAccessException erro) {
            // The write already completed. Do not invite a retry of that write.
            return new Resultado(List.of(), erro);
        }
    }

    private void exigirProprietario(Build build) {
        if (build == null) throw new IllegalArgumentException("Selecione uma build.");
        if (build.getUsuarioId() != usuarioId) throw new SecurityException("Esta build pertence a outro usuário.");
    }
}
