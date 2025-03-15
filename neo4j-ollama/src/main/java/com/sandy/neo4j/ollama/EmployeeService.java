package com.sandy.neo4j.ollama;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
public class EmployeeService {
    @Autowired
    private Neo4jClient neo4jClient;

    public Collection<Map<String, Object>> findEmployeeByCompany(String companyName) {
        String query = """
            MATCH (e:Employee)-[:WORKS_FOR]->(c:Company {name: $companyName})
            RETURN e.name AS employeeName, e.role AS role, c.name AS companyName
            """;
        return neo4jClient
                .query(query)
                .bind(companyName).to("companyName")
                .fetch()
                .all();
    }
}