%% Diagrama de mermaid.

C4Component
    title Diagrama de componentes

    Container_Boundary(fe, "Frontend") {
        Component(frontend, "Next.js App", "Next.js", "")
    }

    Container_Boundary(be, "Backend") {
        Component(backend, "Spring Boot App", "Spring Boot", "")
    }

    Container_Boundary(ext, "Yahoo Finance") {
        Component(query1, "query1.finance.yahoo.com", "REST API", "")
        Component(query2, "query2.finance.yahoo.com", "REST API", "")
    }

    ContainerDb(db, "MariaDB", "Base de datos", "")

    Rel(frontend, backend, "HTTP REST")
    Rel(backend, query1, "GET HTTPS")
    Rel(backend, query2, "GET HTTPS")
    Rel(backend, db, "JPA / SQL")
