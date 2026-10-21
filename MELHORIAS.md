# Melhorias de 21/10/2026

## Onde encontrar

| Recurso | Acesso |
| --- | --- |
| Montar PC por etapas | **Minhas builds → + Montar novo PC**; uma categoria de cada vez, com busca, total e revisão |
| Rever ou trocar peças | **Voltar**, etapas no topo ou resumo lateral; na revisão, botão **Trocar** |
| Editar montagem salva | Selecione uma build e use **Editar selecionada**, ou dê dois cliques na linha |
| Trocar sua senha | Menu **Minha conta**; exige senha atual e confirmação da nova |
| Backup completo | **Minha conta → Backup administrativo**, disponível para ADMIN; exige senha atual |
| Restaurar backup JSON | Mesmo local; confirma substituição integral dos dados, usa transação e encerra a sessão após sucesso |
| Comparar builds | **Minhas builds**, selecione 2–3 com Ctrl/Shift e use **Comparar** |
| Exportar build | **Minhas builds → Exportar selecionada**, em TXT ou HTML |
| Exportar comparação | Botão no diálogo de comparação; abra o HTML no navegador e use Imprimir → Salvar como PDF |
| Orçamento e desempenho desejado | Filtros no topo de **Jogos**; orçamento vazio significa sem limite |
| Busca e faixa de preço de componentes | Campos no topo de cada categoria administrativa |
| Terminal protegido | Login e perfil ADMIN antes do menu; contas comuns usam JavaFX |
| Exportar projeto fonte para compartilhar | `scripts/package-source.ps1`, sem configurações privadas e saídas usuais |

## Correções técnicas

A transferência de jogos agora inclui gênero, descrição e capa. O atualizador PostgreSQL ativa RLS nas 11 tabelas e revoga acesso das roles públicas existentes, em transação. Esses controles não foram aplicados automaticamente ao seu banco: se atualizar uma instalação existente, faça backup e siga [DATA_MODEL.md](docs/DATA_MODEL.md) e [SUPABASE.md](SUPABASE.md) com uma role administrativa apropriada.

A troca de senha usa o hash anterior na condição de atualização. Backup e restauração revalidam o perfil no banco, usam tabelas/colunas permitidas e recusam arquivos inválidos antes de alterar dados. Backup contém hashes e dados privados, sem criptografia: guarde-o em local protegido e opere sem outras gravações. Veja [BACKUP_AND_RECOVERY.md](docs/BACKUP_AND_RECOVERY.md).

## Validação e uso

64 testes aprovados (54 da entrega anterior e dez novos da montagem guiada), compilação JDK 21, testes JDBC em H2 e assistente exercitado em JavaFX headless nos temas claro/escuro, inclusive na largura mínima. A proteção SQL foi verificada na entrega anterior em PostgreSQL WASM/PGlite. Limites em [TESTING.md](docs/TESTING.md). Nenhuma credencial do seu arquivo foi usada para conectar a serviços.

O ZIP continua sendo o projeto fonte. Para executar, use JDK 21 e os comandos do [guia de desenvolvimento](docs/DEVELOPMENT.md); para distribuir o executável, recompile/empacote conforme [DISTRIBUICAO.md](DISTRIBUICAO.md). O pacote Windows e os scripts PowerShell precisam ser conferidos no Windows.

## Montagem guiada

O fluxo segue processador → placa-mãe → memória → placa de vídeo → SSD → fonte → revisão. É possível voltar e trocar uma peça mantendo as escolhas ainda compatíveis. Trocar o socket remove a placa-mãe e sua memória; trocar a geração de memória da placa-mãe remove a RAM incompatível; aumentar o consumo pode exigir escolher outra fonte. Um aviso informa as escolhas removidas. O resumo mantém o preço parcial e a potência recomendada atualizados.

O rascunho sobrevive à troca entre as abas da tela, mas não é persistido ao sair dela ou fechar o aplicativo. Iniciar outra montagem ou editar outra build pede confirmação se houver alterações. A revisão permite nomear, marcar como favorita e salvar; editar atualiza a build existente. Esta mudança não requer migration.

## Ficaram para uma próxima etapa

- Recuperação de senha esquecida e recuperação de administrador sem acesso atual.
- Backend/autorização remota para distribuir a clientes não confiáveis; acesso JDBC direto permanece.
- Compatibilidade física, BIOS e conectores: faltam dados estruturados no catálogo.
- Fonte/data dos preços, histórico persistente de builds e cenários estruturados de ray tracing/upscaling.
- Recomendação por perfil de uso e orçamento com pesquisa irrestrita de combinações; o filtro atual usa as preferências do algoritmo existente.
- PDF/imagem nativos: nesta versão, o HTML é imprimível em PDF pelo navegador.
- Agendamento/criptografia de backups, recuperação de desastre validada e testes completos em servidores nativos.
