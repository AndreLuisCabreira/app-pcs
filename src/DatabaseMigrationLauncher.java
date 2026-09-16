public final class DatabaseMigrationLauncher {

    private DatabaseMigrationLauncher() {
    }

    public static void main(String[] args) {
        try {
            System.out.println("Aplicando atualizações estruturais do banco...");
            new DatabaseMigrationService().aplicar();
            new DatabaseSchemaValidator().verificar();
            System.out.println("Migration concluída e estrutura validada com sucesso.");
        } finally {
            ConnectionFactory.fechar();
        }
    }
}
