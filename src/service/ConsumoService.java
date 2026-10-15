package service;

import model.Build;

public class ConsumoService {

    // Soma o consumo dos componentes
    public int calcularConsumo(Build build) {

        int consumo = 0;

        if (build.getProcessador() != null) {
            consumo += build.getProcessador().getConsumo();
        }

        if (build.getPlacaMae() != null) {
            consumo += build.getPlacaMae().getConsumo();
        }

        if (build.getPlacaVideo() != null) {
            consumo += build.getPlacaVideo().getConsumo();
        }

        if (build.getMemoria() != null) {
            consumo += 5;
        }

        if (build.getSsd() != null) {
            consumo += 5;
        }

        return consumo;
    }

    // Adiciona uma margem de segurança de 20%
    public int consumoRecomendado(Build build) {

        int consumo = calcularConsumo(build);

        return (int) Math.ceil(consumo * 1.20);
    }

    // Verifica se a fonte suporta a configuração
    public boolean fonteSuporta(Build build) {

        if (build.getFonte() == null) {
            return false;
        }

        return build.getFonte().getPotencia() >= consumoRecomendado(build);
    }

}