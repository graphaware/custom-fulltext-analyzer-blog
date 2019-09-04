package com.graphaware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.Assert.assertTrue;

import org.junit.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.QueryExecutionException;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.junit.Neo4jRule;

/**
 *
 */
public class CustomAnalyzerTest {

    @ClassRule
    public static Neo4jRule neo4j = new Neo4jRule();

    private static GraphDatabaseService service;

    @BeforeClass
    public static void setUpClass() throws Exception {
        service = neo4j.getGraphDatabaseService();

        Result result = service.execute("CALL db.index.fulltext.createNodeIndex('person-name-czech', ['Person'], ['name'], {analyzer:'czech-custom'})");
        result.close();
    }

    @Before
    public void setUp() throws Exception {
        service.execute("MATCH (n) DETACH DELETE n").close();
    }

    @Test
    public void test() {
        service.execute(
                "CREATE (:Person {name: 'Černý'}) " +
                "CREATE (:Person {name: 'Černá'}) "
        );

        Result result = service.execute("CALL db.index.fulltext.queryNodes('person-name-czech', 'černý') ");
        assertThat(newArrayList(result)).hasSize(2);
        result.close();

        result = service.execute("CALL db.index.fulltext.queryNodes('person-name-czech', 'cerny')");
        assertThat(newArrayList(result)).hasSize(2);
        result.close();
    }
}
