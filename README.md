# Projeto de Sistemas Ubíquos - Vigilância de Saúde Infantil

## Sobre o Projeto

Este projeto é um sistema de software para a área da saúde, focado no monitoramento de crianças de 0 a 5 anos na rede municipal de educação. O objetivo do sistema é receber resultados de exames de hemograma completo, analisar tendências de imunidade baixa e anemias, e notificar os gestores de saúde.

## Frontend do Projeto

[Repositório do Frontend](https://github.com/Vitorialuz229/SSU-2025-02_hemograma-mobile)

O frontend do projeto foi desenvolvido com **React Native** utilizando o **Expo**, permitindo a execução em dispositivos Android e iOS de forma simples.

## Tecnologias Utilizadas

* Java / Spring Boot
* Maven
* HAPI FHIR (Structures R4) para manipulação de dados de saúde no padrão FHIR

## Como Executar o Projeto

1.  **Pré-requisitos**:
    * Java 21 ou superior
    * Apache Maven

2.  **Clone o Repositório**:
    ```bash
    git clone https://github.com/TanyRM/SSU-2025-02_hemograma.git
    cd SSU-2025-02_hemograma
    ```

3.  **Execute a Aplicação**:
    * **Pelo terminal**: Use o comando `./mvnw spring-boot:run`
    * **Pela sua IDE**: Importe o projeto como um projeto Maven e execute a classe principal `HemogramaApplication.java`.

---

## Marcos do Projeto

### Marco 1: Recepção de Dados FHIR (✅ Concluído)

* **Objetivo**: Implementar um endpoint funcional capaz de receber e processar uma mensagem FHIR `Observation`, demonstrando a integração da tecnologia de recebimento de dados assíncronos do FHIR.


* **Como Testar**:
  O endpoint para este marco é `POST http://localhost:8080/api/fhir/subscription`. Use um arquivo local ou o código raw do json como corpo em uma requisição de teste, o modelo usado para testes pode ser encontrado [aqui](https://fhir.saude.go.gov.br/r4/exame/Bundle-hemograma-completo.json.html). </br>
    * **Opção A: cURL (Terminal, usando arquivo)**
        ```bash
        curl -X POST -H "Content-Type: application/json" -d "@Bundle-hemograma-completo.json" http://localhost:8080/api/fhir/subscription
        ```

    * **Opção B: HTTP Client** </br>
        `POST http://localhost:8080/api/fhir/subscription`
        
        </br>Header:
        `Content-Type: application/json`

        </br>Body:
        `Insira o código raw do json do hemograma`


* **Verificação (Prova de Conceito)**:
  O sucesso do teste é confirmado pelos logs no console da aplicação, que devem mostrar que o objeto foi recebido e os campos lidos.

### Marco 2: Análise e Persistência de Dados (🔜 To-Do)

### Marco 3: Notificações e Dashboard (🔜 To-Do)

### Marco 4: Análise Coletiva  (🔜 To-Do)


### Funcionamento do Projeto

1. Integração com o Gerador de Hemogramas:

    O primeiro passo é clonar e executar o repositório responsável por gerar os Bundles FHIR contendo os dados de exames:

**Clone o Repositório**:
```bash
git clone https://github.com/RaquelDiasES/Gerador-de-Hemogramas.git
cd Gerador-de-Hemogramas
```

- **Gerador de hemogramas** capta o hemograma vinda do gerador → envia bundle FHIR → `FhirBundleController` → processa e salva no DB.
- **MonitoramentoController** → consulta dados do banco → fornece de notificação de ultimos casos para frontend.
- **DashboardController** → resume estatísticas e timeline → frontend consome e gera gráficos.
- **Frontend (React Native/Expo)**:
    - `DashboardScreen`: exibe gráficos e métricas.
    - `RelatoriosScreen`: lista detalhada de hemogramas, filtros e relatórios.
    - Alertas de surto mostrados em cards ou banners.

---


