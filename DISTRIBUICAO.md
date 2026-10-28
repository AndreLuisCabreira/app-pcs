# Usar o IntraTech PC Builder em outro PC

O script gera uma distribuição portátil para **Windows 64 bits**, com Java 21, JavaFX e drivers do banco. No pacote gerado corretamente, o computador de destino não precisa instalar Java, Maven ou VS Code. O ZIP do projeto fonte não é essa distribuição executável.

## Preparar o outro computador

1. Extraia todo o arquivo `IntraTech-PC-Builder-1.8.1-windows-x64.zip`.
2. Entre na pasta `IntraTech PC Builder` extraída.
3. Faça uma cópia de `.env.example` com o nome `.env`.
4. Edite o novo `.env` e preencha os dados de **Connect > Session pooler** do seu projeto Supabase.
5. Abra `IntraTech PC Builder.exe`.

O `.env` precisa ficar na mesma pasta do executável. Não coloque as API keys `sb_publishable_...` ou `sb_secret_...` nele: o aplicativo desktop usa a URL, o usuário e a senha PostgreSQL do Session pooler.

## Gerar uma nova build

Na raiz do projeto, prepare o ambiente descrito em [DEVELOPMENT.md](docs/DEVELOPMENT.md): JDK 21 e dependências Maven. A compilação procura JDK em JAVA_HOME/PATH e usa Maven quando disponível; a extensão Java do VS Code é apenas uma alternativa. O JDK de empacotamento também precisa conter `jpackage.exe`.

Confirme que [.env.example](.env.example) está presente: ele é copiado obrigatoriamente pelo script e agora integra o projeto. Então execute:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\package-windows.ps1
```

Se não localizar um JDK com `jpackage.exe` nos caminhos examinados, o script baixa Eclipse Temurin JDK 21, verifica o checksum SHA-256 informado pela API e mantém a cópia em `.tools`, ignorada pelo Git. Instalações posteriores podem reutilizá-la. Se usar `-SkipJdkDownload` sem um JDK encontrado, o script informa erro.

O resultado fica em:

```text
dist\IntraTech-PC-Builder-1.8.1-windows-x64.zip
```

## Segurança

- O script copia `.env.example` e verifica a ausência de `.env` na pasta gerada. Não é um scanner completo: revise também configurações privadas e arquivos incluídos antes de compartilhar o pacote.
- Não publique nem envie uma cópia já configurada com a senha do banco.
- Esta conexão direta é aceitável para uso pessoal em computadores controlados por você. Para distribuir o aplicativo a terceiros, substitua o acesso JDBC direto por Supabase Auth e uma API/backend com permissões por usuário; uma senha PostgreSQL dentro de um aplicativo distribuído pode ser extraída.
- Se o ZIP sair do seu controle depois de você adicionar um `.env`, troque imediatamente a senha do banco no Supabase.

## Atualizações

Os dados ficam no banco configurado; substituir apenas os arquivos do aplicativo não os transfere nem constitui backup. Preserve sua configuração privada e siga [BACKUP_AND_RECOVERY.md](docs/BACKUP_AND_RECOVERY.md) antes de mudanças de estrutura ou operações de substituição de dados. Mantenha a pasta anterior até validar a versão nova.

O executável não aplica migrations automaticamente. Antes de distribuir uma
versão que exija alterações no banco, o responsável pelo projeto deve executar,
na cópia de desenvolvimento atualizada, após backup e revisão de permissões:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\migrate-database.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\check-database.ps1
```

Os computadores dos usuários finais apenas verificam a estrutura ao abrir o
programa e não precisam receber ferramentas ou credenciais administrativas de
migration.

## Conferência da entrega

1. Confira se o pacote contém executável, runtime, bibliotecas, exemplo de configuração e `LEIA-ME.md`.
2. Confirme que configurações privadas não foram adicionadas à cópia destinada a terceiros.
3. Configure o banco e valide abertura, autenticação, catálogo e builds no Windows de destino.
4. Confira arraste, minimizar/maximizar, restauração e encaixe com Win + setas. O roteiro histórico completo está em [ATUALIZACOES-CATALOGO.md](ATUALIZACOES-CATALOGO.md).

O script `smoke-test-package.ps1` só verifica se o processo permanece ativo por cinco segundos; não valida o fluxo acima. O registro de 21/10/2026 informa compilação/testes em Linux e renderização JavaFX headless. Na revisão documental de 28/10/2026 nenhum desses comandos foi executado novamente; o executável e os scripts de empacotamento ainda precisam de conferência no Windows. O empacotador mantém a versão `1.8.1`, também presente no POM; alterações ainda não lançadas estão no [CHANGELOG.md](CHANGELOG.md).

As limitações do atualizador e da transferência de dados continuam abertas em [TECH_DEBT.md](docs/TECH_DEBT.md). O pacote de fonte atualizado preserva os arquivos originais enviados, inclusive configuração local; prepare uma cópia sanitizada específica antes de uma eventual publicação pública.

## Exportar somente o projeto fonte

`powershell -ExecutionPolicy Bypass -File .\scripts\package-source.ps1` cria `dist/IntraTech-source.zip` sem `.git`, `.vscode`, `.tools`, `target`, `dist`, arquivos `.env*` privados, logs e nomes usuais `*backup*.json`. `.env.example` é mantido. O comando recusa sobrescrever um ZIP existente; use `-OutputPath` com outro nome. Revise arquivos privados com nomes diferentes antes de publicar. O script não apaga arquivos locais.
