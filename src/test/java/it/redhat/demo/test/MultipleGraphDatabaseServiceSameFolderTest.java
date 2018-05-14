package it.redhat.demo.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import it.redhat.demo.service.DataService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class MultipleGraphDatabaseServiceSameFolderTest {

	public static final int TIMES = 1;

	@Test(expected = RuntimeException.class)
	public void test() {
		DataService service = new DataService( new GraphDatabaseFactory().newEmbeddedDatabase( new File( "data/projects" ) ) );
		GraphDatabaseService[] graphDbs = new GraphDatabaseService[TIMES];

		try {
			service.createConstraints();

			try {
				for (int i=0; i< TIMES; i++) {
					graphDbs[i] = ( singleTest( i ) );
				}
			} finally {
				for (int i=0; i< TIMES; i++) {
					GraphDatabaseService graphDb = graphDbs[i];
					if (graphDb != null) {
						graphDb.shutdown();
					}
				}
			}
		} finally {
			service.cleanUp();
			service.disposeConstraints();
		}


	}

	public GraphDatabaseService singleTest(int seed) {
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( new File( "data/projects" ) );
		DataService service = new DataService( graphDb );

		service.createEntities();

		List<Map<String, Object>> result = service.queryEntities();

		assertEquals( 1, result.size() );
		assertEquals( "Fabio M.", result.get( 0 ).get( "p.name" ) );
		assertEquals( "fax4ever", result.get( 0 ).get( "p.nick" ) );

		return graphDb;
	}
}
