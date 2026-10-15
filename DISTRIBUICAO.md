# Usar o IntraTech PC Builder em outro PC

Esta distribuição é portátil para **Windows 64 bits**. O Java 21, o JavaFX e os drivers do banco já estão dentro da pasta; não é necessário instalar Java, Maven ou VS Code no computador de destino.

## Preparar o outro computador

1. Extraia todo o arquivo `IntraTech-PC-Builder-1.8.1-windows-x64.zip`.
2. Entre na pasta `IntraTech PC Builder` extraída.
3. Faça uma cópia de `.env.example` com o nome `.env`.
4. Edite o novo `.env` e preencha os dados de **Connect > Session pooler** do seu projeto Supabase.
5. Abra `IntraTech PC Builder.exe`.

O `.env` precisa ficar na mesma pasta do executável. Não coloque as API keys `sb_publishable_...` ou `sb_secret_...` nele: o aplicativo desktop usa a URL, o usuário e a senha PostgreSQL do Session pooler.

## Gerar uma nova build

Na raiz do projeto, execute:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\package-windows.ps1
```

Na primeira execução, o script baixa o Eclipse Temurin JDK 21 oficial, valida seu checksum SHA-256 e o mantém em `.tools`, pasta ignorada pelo Git. As próximas builds reutilizam essa cópia.

O resultado fica em:

```text
dist\IntraTech-PC-Builder-1.8.1-windows-x64.zip
```

## Segurança

- O script nunca inclui o `.env` real na build; somente `.env.example` é distribuído.
- Não publique nem envie uma cópia já configurada com a senha do banco.
- Esta conexão direta é aceitável para uso pessoal em computadores controlados por você. Para distribuir o aplicativo a terceiros, substitua o acesso JDBC direto por Supabase Auth e uma API/backend com permissões por usuário; uma senha PostgreSQL dentro de um aplicativo distribuído pode ser extraída.
- Se o ZIP sair do seu controle depois de você adicionar um `.env`, troque imediatamente a senha do banco no Supabase.

## Atualizações

Uma nova build não apaga dados porque as informações ficam no Supabase. Você pode substituir a pasta antiga pela nova e copiar apenas o seu `.env` para a nova pasta.

O executável não aplica migrations automaticamente. Antes de distribuir uma
versão que exija alterações no banco, o responsável pelo projeto deve executar,
na cópia de desenvolvimento atualizada:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\migrate-database.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\check-database.ps1
```

Os computadores dos usuários finais apenas verificam a estrutura ao abrir o
programa e não precisam receber ferramentas ou credenciais administrativas de
migration.
