package it.redhat.demo.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

public class DataService {

	String DELETE_ALL = "MATCH (n) OPTIONAL MATCH (n) -[r]-> () DELETE n,r";

	private GraphDatabaseService graphDb;

	public DataService(GraphDatabaseService graphDb) {
		this.graphDb = graphDb;
	}

	public void createEntities() {
		try ( Transaction trx = graphDb.beginTx() ) {
			Node ogm = graphDb.createNode( Label.label( "Project" ) );
			ogm.setProperty( "name", "Hibernate OGM" );
			ogm.setProperty( "licence", "LGPL" );

			Node fabio = graphDb.createNode( Label.label( "Person" ) );
			fabio.setProperty( "name", "Fabio M." );
			fabio.setProperty( "nick", "fax4ever" );

			fabio.createRelationshipTo( ogm, RelationshipType.withName( "member" ) );

			trx.success();
		}
	}

	public void cleanUp() {
		try ( Transaction trx = graphDb.beginTx() ) {
			graphDb.execute( DELETE_ALL ).close();

			trx.success();
		}
	}

	public List<Map<String, Object>> queryEntities() {
		try ( Transaction trx = graphDb.beginTx() ) {
			Result result = graphDb.execute(
					"MATCH (x:Project) <-[member]- (p:Person) " +
							"WHERE x.licence = 'LGPL'" +
							"RETURN p.name, p.nick" );

			List<Map<String, Object>> output = result.stream().collect( Collectors.toList() );

			trx.success();
			return output;
		}
	}

	public void disposeConstraints() {
		graphDb.execute( "DROP CONSTRAINT ON (p:Project) ASSERT p.name IS UNIQUE" );
		graphDb.execute( "DROP CONSTRAINT ON (p:Person) ASSERT p.name IS UNIQUE" );
	}

	public void createConstraints() {
		graphDb.execute( "CREATE CONSTRAINT ON (p:Project) ASSERT p.name IS UNIQUE" );
		graphDb.execute( "CREATE CONSTRAINT ON (p:Person) ASSERT p.name IS UNIQUE" );
	}

}
