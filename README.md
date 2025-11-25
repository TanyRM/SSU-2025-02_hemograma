# Projeto de Sistemas Ubíquos - Vigilância de Saúde Infantil

## Sobre o Projeto

Este projeto é um sistema de software para a área da saúde, focado no monitoramento de crianças de 0 a 5 anos na rede municipal de educação. O objetivo do sistema é receber resultados de exames de hemograma completo, analisar tendências de imunidade baixa e anemias, e notificar os gestores de saúde.

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

1. Integração com o Gerador de Hemogramas

    O primeiro passo é clonar e executar o repositório responsável por gerar os Bundles FHIR contendo os dados de exames:

**Clone o Repositório**:
```bash
git clone https://github.com/RaquelDiasES/Gerador-de-Hemogramas.git
cd Gerador-de-Hemogramas
```

Esse gerador envia requisições POST para o endpoint do sistema de monitoramento, transmitindo dados de exames laboratoriais no formato FHIR (application/fhir+json).

2. Recepção e Armazenamento do Bundle FHIR
   
O backend expõe o endpoint:
```bash
POST /monitoramento/hemograma
Content-Type: application/fhir+json
```

Cada requisição contém um Bundle FHIR com os recursos:

* `Patient` → dados do paciente (CPF, data de nascimento, nome);
* `Observation` → resultados laboratoriais (Hemoglobina, Hematócrito, Hemácias);
* `Organization` → informações do laboratório (CNES/CNPJ).

O JSON completo é armazenado no banco de dados no campo bundle_json, preservando a auditabilidade e rastreabilidade do dado original.

3. Extração e Processamento dos Dados

A classe `MonitoramentoController` utiliza a biblioteca HAPI-FHIR `(ca.uhn.fhir.context.FhirContext)` para:

* Converter o JSON recebido em objetos Java (Bundle, Patient, Observation, etc.); 
* Extrair informações relevantes (ex.: Hemoglobina, Hematócrito, Hemácias); 
* Calcular a idade em meses do paciente a partir da data de nascimento; 
* Registrar metadados como data de coleta, data de recebimento e identificador do laboratório.

Esses dados são encapsulados em um objeto Hemograma, que é enviado para o serviço de análise.

4. Análise Clínica e Classificação de Anemia

A classe AnaliseService aplica as regras da Organização Mundial da Saúde (OMS) para classificar os níveis de hemoglobina conforme idade e sexo da criança, gerando:

* `is_anemia` → indica se há anemia (booleano);
* `classificacao_anemia` → indica o grau (Leve, Moderada, Grave).

Os resultados são persistidos na tabela hemogramas, incluindo os valores extraídos e o JSON original.

5. Detecção de Surtos e Monitoramento Populacional

O endpoint:
```bash
GET /monitoramento/status-surto
```
realiza a contagem de hemogramas com anemia dentro de uma janela temporal configurada (ex.: últimas 24 horas).
Se o número de casos ultrapassar um limite pré-definido, o sistema retorna um alerta de surto com código HTTP 429 (Too Many Requests), permitindo a integração com sistemas de vigilância epidemiológica.

