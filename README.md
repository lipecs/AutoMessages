# AutoMessages

![Java](https://img.shields.io/badge/Java-8%2B-orange?logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A36?logo=apachemaven)
![Spigot](https://img.shields.io/badge/Spigot%20%7C%20Paper-1.8.9--1.21.11-yellow)
![Proxy](https://img.shields.io/badge/BungeeCord%20%7C%20Velocity-Proxy-blue)
![Licença](https://img.shields.io/badge/licen%C3%A7a-propriet%C3%A1ria-red)

AutoMessages é um plugin leve para servidores Minecraft que envia anúncios configuráveis no chat em intervalos regulares. Ele funciona localmente em Spigot/Paper ou de forma centralizada em redes BungeeCord e Velocity utilizando a mesma JAR.

## Funcionalidades

- Envio automático de mensagens com múltiplas linhas.
- Intervalo configurável em segundos.
- Cores tradicionais do Minecraft com `&`.
- Ordem sequencial ou aleatória.
- Ativação global e ativação individual por mensagem.
- Prefixo configurável.
- Pausa automática quando o servidor está vazio.
- Recarregamento seguro, sem tarefas duplicadas.
- Envio manual de qualquer mensagem configurada.
- Persistência das alterações feitas pelos comandos.
- Tab completion para subcomandos e IDs.
- Mensagens administrativas configuráveis em `messages.yml`.
- Permissão de bypass para jogadores que não desejam receber os anúncios.
- Modo proxy opcional para sincronizar anúncios em todos os servidores da rede.
- Uma única JAR para Spigot, Paper, BungeeCord e Velocity.

## Compatibilidade

Uma única JAR é compatível com Spigot e Paper da versão 1.8.9 até a 1.21.11.

A mesma JAR contém entradas independentes para BungeeCord e Velocity. A integração com os backends usa o canal de plugin messaging `automsg:proxy`, válido tanto no limite legado do Bukkit 1.8 quanto no formato namespaced das versões modernas.

O bytecode é Java 8. Em versões recentes do Minecraft, execute o servidor com a versão de Java exigida pelo próprio servidor; a JAR do AutoMessages continua compatível com runtimes modernos.

## Requisitos

- Servidor Spigot ou Paper compatível.
- BungeeCord ou Velocity quando o modo proxy for utilizado.
- Java 8 ou superior para compilar.
- Maven 3.8 ou superior para gerar a JAR.
- A versão de Java exigida pela distribuição e versão do servidor para executar.

## Instalação

1. Baixe ou compile `AutoMessages-1.0.0.jar`.
2. Coloque a JAR na pasta `plugins` do servidor.
3. Inicie ou reinicie o servidor.
4. Edite `plugins/AutoMessages/config.yml` e `plugins/AutoMessages/messages.yml`.
5. Execute `/automessages reload` para aplicar as alterações.

## Instalação em rede proxy

1. Coloque a mesma `AutoMessages-1.0.0.jar` na pasta `plugins` do BungeeCord ou Velocity.
2. Coloque a mesma JAR na pasta `plugins` de todos os servidores Spigot/Paper que devem receber os anúncios.
3. Inicie o proxy e os servidores uma vez para criar os arquivos.
4. No `config.yml` do proxy, defina `proxy.enabled: true` e configure mensagens, intervalo, prefixo e ordem.
5. No `config.yml` de cada backend, defina `proxy.enabled: true`.
6. Reinicie o proxy e execute `/automessages reload` em cada backend, ou reinicie toda a rede.

Quando o modo proxy está ativo no backend, sua tarefa automática local não é criada. Apenas o proxy controla a sequência e o intervalo, impedindo anúncios duplicados. Somente servidores com a JAR instalada, `proxy.enabled: true`, `enabled: true` e jogadores conectados recebem a transmissão.

O transporte utiliza as conexões dos jogadores. Um backend vazio não recebe pacotes naquele momento, pois não existe público para a mensagem. Ele passa a receber normalmente nos próximos intervalos depois que algum jogador entrar.

Os comandos administrativos continuam disponíveis nos servidores Spigot/Paper. O arquivo do proxy é aplicado ao reiniciar o proxy; o comando de reload executado em um backend não recarrega o arquivo que pertence ao proxy.

## Comandos

| Comando | Descrição |
|---|---|
| `/automessages` | Mostra a ajuda. |
| `/automessages help` | Mostra os comandos disponíveis. |
| `/automessages reload` | Recarrega os arquivos e reinicia a tarefa. |
| `/automessages list` | Lista IDs e estados das mensagens. |
| `/automessages enable` | Ativa o envio automático. |
| `/automessages disable` | Desativa o envio automático. |
| `/automessages toggle <id>` | Ativa ou desativa uma mensagem e salva a alteração. |
| `/automessages send <id>` | Envia imediatamente a mensagem indicada. |

Aliases disponíveis: `/automsg` e `/am`.

## Permissões

| Permissão | Descrição | Padrão |
|---|---|---|
| `automessages.admin` | Acesso a todos os comandos, incluindo ativar e desativar o sistema. | Operadores |
| `automessages.reload` | Recarrega as configurações. | Operadores |
| `automessages.list` | Lista as mensagens. | Operadores |
| `automessages.toggle` | Alterna o estado de uma mensagem. | Operadores |
| `automessages.send` | Envia uma mensagem manualmente. | Operadores |
| `automessages.bypass` | Não recebe mensagens distribuídas pelo plugin. | Ninguém |

`automessages.admin` herda todas as permissões administrativas específicas. Os comandos `enable` e `disable` exigem diretamente `automessages.admin`.

## Configuração

```yml
enabled: true
interval-seconds: 60
random-order: false
skip-when-empty: true
prefix: "&8[&bAutoMessages&8] "

proxy:
  enabled: false

messages:
  discord:
    enabled: true
    text:
      - "&bEntre em nosso Discord!"
      - "&7Utilize &f/discord &7para receber o convite."

  store:
    enabled: true
    text:
      - "&6Visite nossa loja!"
      - "&eAdquira vantagens e ajude o servidor."

  help:
    enabled: true
    text:
      - "&aPrecisa de ajuda?"
      - "&7Utilize &f/ajuda&7."
```

Cada chave dentro de `messages` é um ID único. Todas as linhas de `text` são enviadas na ordem declarada. O comando `toggle` atualiza o campo `enabled` correspondente e salva o `config.yml`.

Deixe `proxy.enabled` como `false` para funcionamento local. Para uma rede, use `true` no proxy e nos backends. As mensagens automáticas da rede são lidas do `config.yml` localizado na pasta do AutoMessages no proxy.

As respostas administrativas ficam em `messages.yml`. Os placeholders disponíveis incluem `{id}`, `{status}`, `{interval}` e `{count}`, de acordo com cada mensagem padrão.

## Compilação

Clone o repositório, abra um terminal na raiz e execute:

```bash
mvn clean package
```

Após uma compilação bem-sucedida, a JAR estará em:

```text
target/AutoMessages-1.0.0.jar
```

## Estrutura do projeto

```text
AutoMessages/
├── pom.xml
├── README.md
├── LICENSE
└── src/
    └── main/
        ├── java/
        │   └── br/com/lipe/automessages/
        │       ├── AutoMessagesPlugin.java
        │       ├── command/AutoMessagesCommand.java
        │       ├── config/ConfigManager.java
        │       ├── message/MessageManager.java
        │       ├── proxy/
        │       │   ├── ProxyLogger.java
        │       │   ├── ProxyPlatform.java
        │       │   ├── ProxyRuntime.java
        │       │   ├── ProxyTask.java
        │       │   ├── bukkit/BukkitProxyBridge.java
        │       │   ├── bungee/
        │       │   │   ├── AutoMessagesBungeePlugin.java
        │       │   │   └── BungeeProxyPlatform.java
        │       │   ├── config/ProxyConfigManager.java
        │       │   ├── message/
        │       │   │   ├── ProxyMessageManager.java
        │       │   │   └── ProxyMessagePacket.java
        │       │   └── velocity/
        │       │       ├── AutoMessagesVelocityPlugin.java
        │       │       └── VelocityProxyPlatform.java
        │       └── task/AutoMessageTask.java
        └── resources/
            ├── bungee.yml
            ├── plugin.yml
            ├── config.yml
            └── messages.yml
```

## Imagens e GIFs

Espaço reservado para capturas do chat, demonstrações dos comandos e GIFs do plugin em funcionamento.

## Licença

Copyright (c) 2026 Lipe. O código pode ser visualizado para portfólio e estudo, mas não pode ser redistribuído, vendido, modificado ou utilizado em projetos sem autorização expressa do autor. Consulte [LICENSE](LICENSE).
