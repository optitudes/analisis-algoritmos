%% Diagrama de mermaid.

erDiagram
  active ||--o{ price_data : "tiene"
  active {
    id BIGINT(20) PK "NOT NULL, AUTO_INCREMENT"
    name VARCHAR(255) "NOT NULL"
    symbol VARCHAR(255) UK "NOT NULL"
  }
  price_data {
    id BIGINT(20) PK "NOT NULL, AUTO_INCREMENT"
    close DOUBLE "NOT NULL"
    date DATE "NOT NULL"
    high DOUBLE "NOT NULL"
    low DOUBLE "NOT NULL"
    open DOUBLE "NOT NULL"
    volume BIGINT(20) "NOT NULL"
    active_id BIGINT(20) FK "NOT NULL"
  }
