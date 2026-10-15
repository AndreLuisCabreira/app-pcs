package service;

import model.Build;

public class CompatibilidadeService {

    public boolean verificarSocket(Build build) {

        if (build.getProcessador() == null || build.getPlacaMae() == null) {
            return false;
        }

        return build.getProcessador()
                .getSocket()
                .equalsIgnoreCase(build.getPlacaMae().getSocket());
    }

    public boolean verificarMemoria(Build build) {

        if (build.getMemoria() == null || build.getPlacaMae() == null) {
            return false;
        }

        return build.getMemoria()
                .getTipo()
                .equalsIgnoreCase(build.getPlacaMae().getTipoMemoria());
    }

    public boolean verificarBuild(Build build) {

        return verificarSocket(build)
                && verificarMemoria(build);
    }

}